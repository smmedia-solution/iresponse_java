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
import tech.iresponse.models.admin.LinodeAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.LinodeProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;
import tech.iresponse.linode.update.UpdateData;
import tech.iresponse.linode.instance.InstancesActions;
import tech.iresponse.linode.instance.InstancesManager;

public class Linode implements Controller {

    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;

    public Response createInstances() {
        LinodeProcess linodePross = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            linodePross = new LinodeProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (linodePross.getEmpty()) {
                throw new DatabaseException("No process found !");
            }

            linodePross.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            linodePross.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            linodePross.progress = "0%";
            linodePross.instancesCreated = 0;
            linodePross.instancesInstalled = 0;
            linodePross.update();

            ServerProvider serProvider = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Linode" });
            if (serProvider == null || serProvider.getEmpty()) {
                serProvider = new ServerProvider();
                serProvider.name = "Linode";
                serProvider.status = "Activated";
                serProvider.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.createdDate = new Date(System.currentTimeMillis());
                serProvider.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.lastUpdatedDate = new Date(System.currentTimeMillis());
                serProvider.id = serProvider.insert();
            }

            List<String> domains = Arrays.asList(linodePross.domains.split(Pattern.quote(",")));
            if (domains.size() != linodePross.nbInstances) {
                throw new DatabaseException("The number of domains should equal to the number of droplets !");
            }

            LinodeAccount linodAcc = new LinodeAccount(Integer.valueOf(linodePross.accountId));
            if (linodAcc.getEmpty()) {
                throw new DatabaseException("No account found !");
            }

            int nbthread = (linodePross.nbInstances > 20) ? 20 : linodePross.nbInstances;
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);

            for (String domainId : domains){
                execService.submit((Runnable)new InstancesManager(linodAcc, linodePross, serProvider, domainId));
            }
            execService.shutdown();

            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }

        } catch (Exception ex) {
            IS_ERROR_OCCURED = true;
            Loggers.error(ex);
        }

        if (linodePross != null){
            UpdateData.finishUpdate(linodePross);
        }

        return new Response("Linode Instance created and installed successfully !", 200);
    }

    public Response stopProcesses() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes found !");
        }

        LinodeProcess linodePross = null;
        for (int b = 0; b < processesIds.length(); b++) {
            linodePross = new LinodeProcess(Integer.valueOf(processesIds.getInt(b)));
            if (!linodePross.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(linodePross.status)) {
                    throw new DatabaseException("This process with id : " + linodePross.id + " is not in progress !");
                }
                Terminal.executeCommand("kill -9 " + linodePross.processId);
                linodePross.status = "Interrupted";
                linodePross.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                linodePross.update();
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
        if (instancesIds.length() == 0){
            throw new DatabaseException("No droplets found !");
        }
        String action = String.valueOf(app.getParameters().get("action"));
        if ("".equals(action) || "null".equals(action)){
            throw new DatabaseException("Droplet action not found !");
        }
        int nbthread = (instancesIds.length() > 20) ? 20 : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int b = 0; b < instancesIds.length(); b++){
            execService.submit((Runnable)new InstancesActions(instancesIds.getInt(b), action));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Action executed successfully !", 200);
    }

    public static synchronized void updateCountDropletCreated(int count) {
        INSTANCES_CREATED += count;
    }

    public static synchronized void updateCountDropletInstalled() {
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
