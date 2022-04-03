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
import tech.iresponse.models.admin.AtlanticProcess;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse. models.admin.AtlanticAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.atlantic.update.UpdateData;
import tech.iresponse.atlantic.instance.InstancesActions;
import tech.iresponse.atlantic.instance.InstancesManager;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class Atlantic implements Controller {

    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;

    public Response createInstances() {
        AtlanticProcess atlnticProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            atlnticProcss = new AtlanticProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (atlnticProcss.getEmpty()) {
                throw new DatabaseException("No process found !");
            }
            atlnticProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            atlnticProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            atlnticProcss.progress = "0%";
            atlnticProcss.instancesCreated = 0;
            atlnticProcss.instancesInstalled = 0;
            atlnticProcss.update();
            ServerProvider serProvider = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Atlantic" });
            if (serProvider == null || serProvider.getEmpty()) {
                serProvider = new ServerProvider();
                serProvider.name = "Atlantic";
                serProvider.status = "Activated";
                serProvider.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.createdDate = new Date(System.currentTimeMillis());
                serProvider.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.lastUpdatedDate = new Date(System.currentTimeMillis());
                serProvider.id = serProvider.insert();
            }
            List<String> domains = Arrays.asList(atlnticProcss.domains.split(Pattern.quote(",")));
            if (domains.size() != atlnticProcss.nbInstances) {
                throw new DatabaseException("The number of domains should equal to the number of instances !");
            }
            AtlanticAccount atlnticAcc = new AtlanticAccount(Integer.valueOf(atlnticProcss.accountId));
            if (atlnticAcc.getEmpty()) {
                throw new DatabaseException("No account found !");
            }
            int nbthread = (atlnticProcss.nbInstances > 20) ? 20 : atlnticProcss.nbInstances;
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            for (String domainId : domains) {
                execService.submit((Runnable)new InstancesManager(atlnticAcc, atlnticProcss, serProvider, domainId));
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)) {
                execService.shutdownNow();
            }
        } catch (Exception e) {
            IS_ERROR_OCCURED = true;
            Loggers.error(e);
        }
        if (atlnticProcss != null) {
            UpdateData.finishUpdate(atlnticProcss);
        }
        return new Response("Atlantic Instance created and installed successfully !", 200);
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
        AtlanticProcess atlnticProcss = null;
        for (int i = 0; i < processesIds.length(); i++) {
            atlnticProcss = new AtlanticProcess(Integer.valueOf(processesIds.getInt(i)));
            if (!atlnticProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(atlnticProcss.status)) {
                    throw new DatabaseException("This process with id : " + atlnticProcss.id + " is not in progress !");
                }
                Terminal.killProcess(atlnticProcss.processId);
                atlnticProcss.status = "Interrupted";
                atlnticProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                atlnticProcss.update();
            }
        }
        return new Response("Processes stoped successfully !", 200);
    }

    public Response executeInstancesActions() throws Exception  {
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
