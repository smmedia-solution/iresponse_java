package tech.iresponse.webservices;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.json.JSONArray;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.HetznerProcess;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.HetznerAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.hetzner.update.UpdateData;
import tech.iresponse.hetzner.instance.InstancesActions;
import tech.iresponse.hetzner.instance.InstancesManager;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class Hetzner implements Controller {

    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;

    public Response createInstances() {
        HetznerProcess htznerProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            htznerProcss = new HetznerProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (htznerProcss.getEmpty()) {
                throw new DatabaseException("No process found !");
            }
            htznerProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            htznerProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            htznerProcss.progress = "0%";
            htznerProcss.instancesCreated = 0;
            htznerProcss.instancesInstalled = 0;
            htznerProcss.update();
            ServerProvider serProvider = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Hetzner" });
            if (serProvider == null || serProvider.getEmpty()) {
                serProvider = new ServerProvider();
                serProvider.name = "Hetzner";
                serProvider.status = "Activated";
                serProvider.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.createdDate = new Date(System.currentTimeMillis());
                serProvider.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.lastUpdatedDate = new Date(System.currentTimeMillis());
                serProvider.id = serProvider.insert();
            }
            List<String> domains = Arrays.asList(htznerProcss.domains.split(Pattern.quote(",")));
            if (domains.size() != htznerProcss.nbInstances) {
                throw new DatabaseException("The number of domains should equal to the number of instances !");
            }
            HetznerAccount htznerAcc = new HetznerAccount(Integer.valueOf(htznerProcss.accountId));
            if (htznerAcc.getEmpty()) {
                throw new DatabaseException("No account found !");
            }
            int nbthread = (htznerProcss.nbInstances > 20) ? 20 : htznerProcss.nbInstances;
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            for (String str : domains) {
                execService.submit((Runnable)new InstancesManager(htznerAcc, htznerProcss, serProvider, str));
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)) {
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            IS_ERROR_OCCURED = true;
            Loggers.error(ex);
        }
        if (htznerProcss != null) {
            UpdateData.finishUpdate(htznerProcss);
        }
        return new Response("Hetzner Instance created and installed successfully !", 200);
    }

    public Response stopProcesses() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0) {
            throw new DatabaseException("No processes found !");
        }
        HetznerProcess htznerProcss = null;
        for (int i = 0; i < processesIds.length(); i++) {
            htznerProcss = new HetznerProcess(Integer.valueOf(processesIds.getInt(i)));
            if (!htznerProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(htznerProcss.status)) {
                    throw new DatabaseException("This process with id : " + htznerProcss.id + " is not in progress !");
                }
                Terminal.killProcess(htznerProcss.processId);
                htznerProcss.status = "Interrupted";
                htznerProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                htznerProcss.update();
            }
        }
        return new Response("Processes stoped successfully !", 200);
    }

    public Response executeInstancesActions() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null) {
            throw new DatabaseException("Application not found !");
        }
        JSONArray instancesIds = (app.getParameters().has("instances-ids") && app.getParameters().get("instances-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("instances-ids") : new JSONArray();
        if (instancesIds.length() == 0) {
            throw new DatabaseException("No instances found !");
        }
        String action = String.valueOf(app.getParameters().get("action"));
        if ("".equals(action) || "null".equals(action)) {
            throw new DatabaseException("Instance action not found !");
        }
        int nbthread = (instancesIds.length() > 20) ? 20 : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int i = 0; i < instancesIds.length(); i++) {
            execService.submit((Runnable)new InstancesActions(instancesIds.getInt(i), action));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)) {
            execService.shutdownNow();
        }
        return new Response("Action executed successfully !", 200);
    }

    public static synchronized void updateCountInstancesCreated(int count) {
        INSTANCES_CREATED += count;
    }

    public static synchronized void updateCountInstancsInstalled() {
        INSTANCES_INSTALLED++;
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "Y3JlYXRlSW5zdGFuY2Vz": {
                return createInstances();
            }
            case "c3RvcFByb2Nlc3Nlcw==": {
                return stopProcesses() ;
            }
            case "ZXhlY3V0ZUluc3RhbmNlc0FjdGlvbnM=": {
                return executeInstancesActions() ;
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
