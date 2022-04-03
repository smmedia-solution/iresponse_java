package tech.iresponse.webservices;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.DigitalOceanAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.DigitalOceanProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.digitalocean.DigitalOceanManager;
import tech.iresponse.digitalocean.update.UpdateData;
import tech.iresponse.digitalocean.droplet.DropletsActions;
import tech.iresponse.digitalocean.droplet.DropletsManager;
import tech.iresponse.core.Application;

public class DigitalOcean implements Controller {

    public static volatile int DROPLETS_CREATED = 0;
    public static volatile int DROPLETS_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;

    public Response createDroplets() throws Exception {
        DigitalOceanProcess doProcess = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
               throw new DatabaseException("Application not found !");
            }

            doProcess = new DigitalOceanProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (doProcess.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            doProcess.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            doProcess.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            doProcess.progress = "0%";
            doProcess.dropletsCreated = 0;
            doProcess.dropletsInstalled = 0;
            doProcess.update();

            ServerProvider serProvider = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Digital Ocean" });
            if (serProvider == null || serProvider.getEmpty()) {
                serProvider = new ServerProvider();
                serProvider.name = "Digital Ocean";
                serProvider.status = "Activated";
                serProvider.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.createdDate = new Date(System.currentTimeMillis());
                serProvider.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serProvider.lastUpdatedDate = new Date(System.currentTimeMillis());
                serProvider.id = serProvider.insert();
            }

            List<String> domains = Arrays.asList(doProcess.domains.split(Pattern.quote(",")));
            if (domains.size() != doProcess.nbDroplets){
                throw new DatabaseException("The number of domains should equal to the number of droplets !");
            }

            DigitalOceanAccount doAccount = new DigitalOceanAccount(Integer.valueOf(doProcess.accountId));
            if (doAccount.getEmpty()){
                throw new DatabaseException("No account found !");
            }

            DigitalOceanManager doManager = new DigitalOceanManager();
            doManager.setAccount(doAccount);

            String idKey = "";
            boolean foundSshKey = false;
            List<HashMap> sshKeys = doManager.getSshKeys();
            if (sshKeys != null){
               for (HashMap keys : sshKeys) {
                    if ("iresponse".equals(keys.get("name"))) {
                        foundSshKey = true;
                        idKey = (String)keys.get("id");
                    }
               }
            }

            if (!foundSshKey){
                idKey = doManager.SshKeys("iresponse", FileUtils.readFileToString(new File("/root/.ssh/id_rsa.pub"), "utf-8"));
            }

            if (idKey == null || "".equals(idKey)){
               throw new DatabaseException("No ssh key found in your account !");
            }

            int nbthread = (doProcess.nbDroplets > 20) ? 20 : doProcess.nbDroplets;
            ExecutorService executorService = Executors.newFixedThreadPool(nbthread);

            for (String dominId : domains){
                executorService.submit((Runnable)new DropletsManager(doAccount, doProcess, serProvider, dominId, idKey));
            }
            executorService.shutdown();

            if (!executorService.awaitTermination(1L, TimeUnit.DAYS)){
                executorService.shutdownNow();
            }
        } catch (Exception ex) {
             IS_ERROR_OCCURED = true;
             Loggers.error(ex);
        }
        if (doProcess != null){
            UpdateData.finishUpdate(doProcess);
        }
        return new Response("Droplets created and installed successfully !", 200);
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

        DigitalOceanProcess doProcess = null;
        for (int b = 0; b < processesIds.length(); b++) {
            doProcess = new DigitalOceanProcess(Integer.valueOf(processesIds.getInt(b)));
            if (!doProcess.getEmpty()) {
                 if (!"In Progress".equalsIgnoreCase(doProcess.status)){
                     throw new DatabaseException("This process with id : " + doProcess.id + " is not in progress !");
                 }
                 Terminal.killProcess(doProcess.processId);
                 doProcess.status = "Interrupted";
                 doProcess.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                 doProcess.update();
            }
        }
        return new Response("Processes stoped successfully !", 200);
    }

    public Response executeDropletsActions() throws Exception {
         Application app = Application.checkAndgetInstance();
         if (app == null){
             throw new DatabaseException("Application not found !");
         }

        JSONArray dropletsIds = (app.getParameters().has("droplets-ids") && app.getParameters().get("droplets-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("droplets-ids") : new JSONArray();
        if (dropletsIds.length() == 0){
            throw new DatabaseException("No droplets found !");
        }

        String action = String.valueOf(app.getParameters().get("action"));
        if ("".equals(action) || "null".equals(action)){
            throw new DatabaseException("Droplet action not found !");
        }

        int nbthread = (dropletsIds.length() > 20) ? 20 : dropletsIds.length();
        ExecutorService executorService = Executors.newFixedThreadPool(nbthread);

        for (int b = 0; b < dropletsIds.length(); b++){
             executorService.submit((Runnable)new DropletsActions(dropletsIds.getInt(b), action));
        }
        executorService.shutdown();

        if (!executorService.awaitTermination(1L, TimeUnit.DAYS)){
            executorService.shutdownNow();
        }
        return new Response("Action executed successfully !", 200);
    }

    public static synchronized void updateCountDropletCreated(int count) {
        DROPLETS_CREATED += count;
    }

    public static synchronized void updateCountDropletInstalled() {
       DROPLETS_INSTALLED++;
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "Y3JlYXRlRHJvcGxldHM=":{
                return createDroplets();
            }
            case "c3RvcFByb2Nlc3Nlcw==":{
                return stopProcesses();
            }
            case "ZXhlY3V0ZURyb3BsZXRzQWN0aW9ucw==":{
                return executeDropletsActions();
            }
        }
        throw new DatabaseException("Action not found !");
    }

}
