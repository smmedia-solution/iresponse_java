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
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.VultrProcess;
import tech.iresponse.models.admin.VultrAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;
import tech.iresponse.vultr.update.UpdateData;
import tech.iresponse.vultr.instance.InstancesActions;
import tech.iresponse.vultr.instance.InstancesManager;

public class Vultr implements Controller {

    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;

    public Response createInstances() {
        VultrProcess vltrProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            vltrProcss = new VultrProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (vltrProcss.getEmpty()) {
                throw new DatabaseException("No process found !");
            }
            vltrProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            vltrProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            vltrProcss.progress = "0%";
            vltrProcss.instancesCreated = 0;
            vltrProcss.instancesInstalled = 0;
            vltrProcss.update();
            ServerProvider serProvider = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Vultr" });
            if (serProvider == null || serProvider.getEmpty()) {
                serProvider = new ServerProvider();
                serProvider.name = "Vultr";
                serProvider.status = "Activated";
                serProvider.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.createdDate = new Date(System.currentTimeMillis());
                serProvider.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.lastUpdatedDate = new Date(System.currentTimeMillis());
                serProvider.id = serProvider.insert();
            }
            List<String> domains = Arrays.asList(vltrProcss.domains.split(Pattern.quote(",")));
            if (domains.size() != vltrProcss.nbInstances) {
                throw new DatabaseException("The number of domains should equal to the number of instances !");
            }
            VultrAccount vltrAcc = new VultrAccount(Integer.valueOf(vltrProcss.accountId));
            if (vltrAcc.getEmpty()) {
                throw new DatabaseException("No account found !");
            }
            int nbthread = (vltrProcss.nbInstances > 20) ? 20 : vltrProcss.nbInstances;
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            for (String domainId : domains) {
                execService.submit((Runnable)new InstancesManager(vltrAcc, vltrProcss, serProvider, domainId));
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)) {
                execService.shutdownNow();
            }
        } catch (Exception e) {
            IS_ERROR_OCCURED = true;
            Loggers.error(e);
        }
        if (vltrProcss != null) {
            UpdateData.finishUpdate(vltrProcss);
        }
        return new Response("Linode Instance created and installed successfully !", 200);
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
        VultrProcess vltrProcss = null;
        for (int i = 0; i < processesIds.length(); i++) {
            vltrProcss = new VultrProcess(Integer.valueOf(processesIds.getInt(i)));
            if (!vltrProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(vltrProcss.status)){
                    throw new DatabaseException("This process with id : " + vltrProcss.id + " is not in progress !");
                }
                Terminal.executeCommand("kill -9 " + vltrProcss.processId);
                vltrProcss.status = "Interrupted";
                vltrProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                vltrProcss.update();
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
        for (int i = 0; i < instancesIds.length(); i++){
            execService.submit((Runnable)new InstancesActions(instancesIds.getInt(i), action));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Action executed successfully !", 200);
    }

    public static synchronized void updateCountInstanceCreated(int count) {
        INSTANCES_CREATED += count;
    }

    public static synchronized void updateCountInstanceInstalled() {
        INSTANCES_INSTALLED++;
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "Y3JlYXRlSW5zdGFuY2Vz":{
                return createInstances();
            }
            case "c3RvcFByb2Nlc3Nlcw==":{
                return stopProcesses();
            }
            case "ZXhlY3V0ZUluc3RhbmNlc0FjdGlvbnM=":{
                return executeInstancesActions();
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
