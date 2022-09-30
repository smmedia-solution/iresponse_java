package tech.iresponse.webservices;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.AzureAccountProcess;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.AzureProcess;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.azure.instance.InstancesInstaller;
import tech.iresponse.http.Response;
import tech.iresponse.utils.Crypto;
import tech.iresponse.azure.instance.InstancesLogs;
import tech.iresponse.azure.instance.InstancesActions;
import tech.iresponse.azure.instance.InstancesGetPublicIps;
import tech.iresponse.azure.instance.InstancesAutoChangeIps;
import tech.iresponse.azure.instance.InstancesAutoRestarts;
import tech.iresponse.azure.instance.StopAutoChangeIps;
import tech.iresponse.azure.instance.InstancesCheck;
import tech.iresponse.azure.instance.InstancesChangeIps;
import tech.iresponse.azure.update.UpdateData;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;

public class Azure implements Controller {

    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;
    public static volatile LinkedHashMap INSTNCES = new LinkedHashMap<>();
    public static final int MAX_THREAD = 100;
    private static final int FIRSTS = 3;
    private static final int NEXTS = 8;

    public Response createInstances() {
        AzureProcess azureProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            azureProcss = new AzureProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (azureProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }
            int prefixesLength = app.getParameters().getInt("prefixes-length");
            boolean usePrefixes = "enabled".equalsIgnoreCase(app.getParameters().getString("use-prefixes"));
            boolean enableCrons = "enabled".equalsIgnoreCase(app.getParameters().getString("enable-crons"));
            azureProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            azureProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            azureProcss.status = "In Progress";
            azureProcss.progress = "0%";
            azureProcss.instancesCreated = 0;
            azureProcss.instancesInstalled = 0;
            azureProcss.update();
            AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureProcss.accountId));
            if (azureAcc.getEmpty()){
                throw new DatabaseException("Azure account not found !");
            }
            ServerProvider serverPrvdr = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "Azure" });
            if (serverPrvdr == null || serverPrvdr.getEmpty()) {
                serverPrvdr = new ServerProvider();
                serverPrvdr.name = "Azure";
                serverPrvdr.status = "Activated";
                serverPrvdr.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serverPrvdr.createdDate = new Date(System.currentTimeMillis());
                serverPrvdr.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serverPrvdr.lastUpdatedDate = new Date(System.currentTimeMillis());
                serverPrvdr.id = serverPrvdr.insert();
            }
            List<String> domains = Arrays.asList(azureProcss.domains.split(Pattern.quote(",")));
            if (domains.isEmpty()){
                throw new DatabaseException("No domains passed to this process !");
            }
            JSONObject azureRegionsMap = new JSONObject(FileUtils.readFileToString(new File(System.getProperty("configs.path") + File.separator + "azure_regions.map.json"), "UTF-8"));
            boolean newRndDomains = (domains.size() != azureProcss.nbInstances) ? true : false;
            int nbthread = (azureProcss.nbInstances > MAX_THREAD) ? MAX_THREAD : azureProcss.nbInstances;
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            int j = 0;
            for (int i = 0; i < azureProcss.nbInstances; i++) {
                j = newRndDomains ? TypesParser.safeParseInt(domains.get((new Random()).nextInt(domains.size()))) : TypesParser.safeParseInt(domains.get(i));
                Domain domin = new Domain(Integer.valueOf(j));
                if (domin.getEmpty()){
                    throw new DatabaseException("Domain not found !");
                }
                execService.submit((Runnable)new InstancesInstaller(azureProcss, azureAcc, serverPrvdr, domin, azureRegionsMap, enableCrons, usePrefixes, prefixesLength));
                ThreadSleep.sleep(FIRSTS, NEXTS);
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            IS_ERROR_OCCURED = true;
            Loggers.error(ex);
        }
        if (azureProcss != null){
            UpdateData.finishUpdate(azureProcss);
        }
        return new Response("Instances created and installed successfully !", 200);
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
        AzureProcess azureProcss = null;
        for (int i = 0; i < processesIds.length(); i++) {
            azureProcss = new AzureProcess(Integer.valueOf(processesIds.getInt(i)));
            if (!azureProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(azureProcss.status)){
                    throw new DatabaseException("This process with id : " + azureProcss.id + " is not in progress !");
                }
                Terminal.killProcess(azureProcss.processId);
                azureProcss.status = "Interrupted";
                azureProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                azureProcss.update();
            }
        }
        return new Response("Processes stoped successfully !", 200);
    }

    public Response executeInstancesActions() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray instancesIds = (app.getParameters().has("instances-ids") && app.getParameters().get("instances-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("instances-ids") : new JSONArray();
        if (instancesIds.length() == 0){
            throw new DatabaseException("No instances found !");
        }
        String action = String.valueOf(app.getParameters().get("action"));
        if ("".equals(action) || "null".equals(action)){
            throw new DatabaseException("Instance action not found !");
        }
        boolean changeIps = "yes".equalsIgnoreCase(String.valueOf(app.getParameters().get("change-ips")));
        int nbthread = (instancesIds.length() > MAX_THREAD) ? MAX_THREAD : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int i = 0; i < instancesIds.length(); i++){
            execService.submit((Runnable)new InstancesActions(instancesIds.getInt(i), action, changeIps));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Action executed successfully !", 200);
    }

    public Response getPublicIps() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray instancesIds = (app.getParameters().has("instances-ids") && app.getParameters().get("instances-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("instances-ids") : new JSONArray();
        if (instancesIds.length() == 0){
            throw new DatabaseException("No instances found !");
        }
        int nbthread = (instancesIds.length() > MAX_THREAD) ? MAX_THREAD : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int i = 0; i < instancesIds.length(); i++){
            execService.submit((Runnable)new InstancesGetPublicIps(instancesIds.getInt(i)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Processes started successfully !", 200);
    }

    public Response executeAutoRestarts() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        int processId = TypesParser.safeParseInt(app.getParameters().get("process-id"));
        if (processId == 0){
            throw new DatabaseException("Account id not found !");
        }
        AzureAccountProcess azureAccProcss = new AzureAccountProcess(Integer.valueOf(processId));
        if (azureAccProcss.getEmpty()){
            throw new DatabaseException("Process not found !");
        }
        AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureAccProcss.accountId));
        if (azureAcc.getEmpty()){
            throw new DatabaseException("Account not found !");
        }
        List<AzureInstance> instances;
        if ("all".equals(azureAccProcss.regions)) {
            instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ?", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
        } else {
            String str = "'" + StringUtils.replace(azureAccProcss.regions, ",", "','") + "'";
            instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ? AND region = ANY(ARRAY[" + str + "])", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
        }
        if (instances == null || instances.isEmpty()){
            throw new DatabaseException("Account instances not found !");
        }
        long timeUnits = TypesParser.safeParseLong(azureAccProcss.processtimeValue);
        switch (azureAccProcss.processtimeUnit) {
            case "seconds":{
                timeUnits *= 1000L;
                break;
            }
            case "minutes":{
                timeUnits = timeUnits * 60L * 1000L;
                break;
            }
            case "hours":{
                timeUnits = timeUnits * 60L * 60L * 1000L;
                break;
            }
        }
        azureAccProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
        azureAccProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
        azureAccProcss.status = "In Progress";
        azureAccProcss.update();
        azureAcc.backgroundProcessStatus = "Start auto restarts";
        azureAcc.update();

        int nbthread = (instances.size() > MAX_THREAD) ? MAX_THREAD : instances.size();
        while (true) {
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            instances.forEach(instnce -> execService.submit((Runnable)new InstancesAutoRestarts(azureAcc, instnce, true)));
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
            ThreadSleep.sleep(timeUnits);
        }
    }

    public Response executeAutoChangeIps() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        int processId = TypesParser.safeParseInt(app.getParameters().get("process-id"));
        if (processId == 0){
            throw new DatabaseException("Account id not found !");
        }
        AzureAccountProcess azureAccProcss = new AzureAccountProcess(Integer.valueOf(processId));
        if (azureAccProcss.getEmpty()){
            throw new DatabaseException("Process not found !");
        }
        AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureAccProcss.accountId));
        if (azureAcc.getEmpty()){
            throw new DatabaseException("Account not found !");
        }
        List<AzureInstance> instances;
        if ("all".equals(azureAccProcss.regions)) {
            instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ?", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
        } else {
            String str = "'" + StringUtils.replace(azureAccProcss.regions, ",", "','") + "'";
            instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ? AND region = ANY(ARRAY[" + str + "])", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
        }
        if (instances == null || instances.isEmpty()){
            throw new DatabaseException("Account instances not found !");
        }
        int procssTimeVal = TypesParser.safeParseInt(azureAccProcss.processtimeValue);
        switch (azureAccProcss.processtimeUnit) {
            case "minutes": {
                procssTimeVal *= 60;
                break;
            }
            case "hours":{
                procssTimeVal = procssTimeVal * 60 * 60;
                break;
            }
        }
        azureAccProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
        azureAccProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
        azureAccProcss.status = "In Progress";
        azureAccProcss.update();

        azureAcc.backgroundProcessStatus = "Start change ips";
        azureAcc.update();

        int nbthread = (instances.size() > MAX_THREAD) ? MAX_THREAD : instances.size();

        while (true) {
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);
            instances.forEach(instnce -> execService.submit((Runnable)new InstancesAutoChangeIps(azureAcc, instnce, 0)));
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
            ThreadSleep.sleep(procssTimeVal);
        }
    }

    public Response executeChangeIps() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray instancesIds = (app.getParameters().has("instances-ids") && app.getParameters().get("instances-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("instances-ids") : new JSONArray();
        if (instancesIds.length() == 0){
            throw new DatabaseException("No instances found !");
        }
        int nbthread = (instancesIds.length() > MAX_THREAD) ? MAX_THREAD : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int i = 0; i < instancesIds.length(); i++){
            execService.submit((Runnable)new InstancesChangeIps(instancesIds.getInt(i)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Processes started successfully !", 200);
    }

    public Response stopAccountsProcesses() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes found !");
        }
        AzureAccountProcess azureAccProcss = null;
        for (int i = 0; i < processesIds.length(); i++) {
            azureAccProcss = new AzureAccountProcess(Integer.valueOf(processesIds.getInt(i)));
            if (!azureAccProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(azureAccProcss.status)){
                    throw new DatabaseException("This process with id : " + azureAccProcss.id + " is not in progress !");
                }
                if ("start-change-ips".equals(azureAccProcss.processType) || "start-change-ips-rdns".equals(azureAccProcss.processType)) {
                    /*List<AzureInstance> instances;
                    if ("all".equals(azureAccProcss.regions)) {
                        instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ?", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
                    } else {
                        String str = "'" + StringUtils.replace(azureAccProcss.regions, ",", "','") + "'";
                        instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ? AND region = ANY(ARRAY[" + str + "])", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
                    }
                    if (instances == null || instances.isEmpty()){
                        throw new DatabaseException("Account instances not found !");
                    }
                    int nbthread = (instances.size() > MAX_THREAD) ? MAX_THREAD : instances.size();
                    ExecutorService execService = Executors.newFixedThreadPool(nbthread);
                    instances.forEach(instce -> execService.submit((Runnable)new StopAutoChangeIps(instce)));
                    execService.shutdown();
                    if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                        execService.shutdownNow();
                    }*/
                    AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureAccProcss.accountId));
                    if (azureAcc.getEmpty()){
                        throw new DatabaseException("Account not found !");
                    }
                    azureAcc.backgroundProcessStatus = "Nothing";
                    azureAcc.update();
                    Terminal.killProcess(azureAccProcss.processId);
                    azureAccProcss.status = "Stopped";
                    azureAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                    azureAccProcss.update();
                } else {
                    Terminal.killProcess(azureAccProcss.processId);
                    azureAccProcss.status = "Stopped";
                    azureAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                    azureAccProcss.update();
                    boolean autoRestarts = "auto-restarts".equals(azureAccProcss.processType);
                    if (autoRestarts) {
                        AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureAccProcss.accountId));
                        if (azureAcc.getEmpty()){
                            throw new DatabaseException("Account not found !");
                        }
                        List<AzureInstance> instances = (List)AzureInstance.all(AzureInstance.class, "account_id = ?", new Object[] { Integer.valueOf(azureAccProcss.accountId) });
                        if (instances == null || instances.isEmpty()){
                            throw new DatabaseException("Account instances not found !");
                        }
                        azureAcc.backgroundProcessStatus = "Nothing";
                        azureAcc.update();
                        int nbthread = (instances.size() > MAX_THREAD) ? MAX_THREAD : instances.size();
                        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
                        instances.forEach(instnce -> execService.submit((Runnable)new InstancesCheck(azureAcc, instnce)));
                        execService.shutdown();
                        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                            execService.shutdownNow();
                        }
                    }
                }
            }
        }
        return new Response("Processes stoped successfully !", 200);
    }

    public Response calculateInstancesLogs() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }
        JSONArray instancesIds = (app.getParameters().has("instances-ids") && app.getParameters().get("instances-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("instances-ids") : new JSONArray();
        if (instancesIds.length() == 0){
            throw new DatabaseException("No instances found !");
        }
        int nbthread = (instancesIds.length() > MAX_THREAD) ? MAX_THREAD : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);
        for (int i = 0; i < instancesIds.length(); i++){
            execService.submit((Runnable)new InstancesLogs(instancesIds.getInt(i)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Logs calculated successfully !", 200);
    }

    public static synchronized void updateCountInstnceCreated(int count) {
        INSTANCES_CREATED += count;
    }

    public static synchronized void updateCountInstnceInstalled() {
        INSTANCES_INSTALLED++;
    }

    public static synchronized void getInstance(String id, JSONObject instnce) {
        if (!INSTNCES.containsKey(id)){
            INSTNCES.put(id, new ArrayList());
        }
        ((List<JSONObject>)INSTNCES.get(id)).add(instnce);
    }

    public static synchronized String getInstanceNames(AzureAccount azureAcc, String region) throws Exception {
        String names = StringUtils.replace(azureAcc.name.trim(), " ", "_").toLowerCase() + "_" + region;
        String name = names + "_1";
        List azureInstances = Database.get("system").availableTables("SELECT name FROM admin.azure_instances WHERE name LIKE '" + names + "%' ORDER BY id DESC LIMIT 1", null, 0, "name");
        if (azureInstances != null && !azureInstances.isEmpty()) {
            String str = String.valueOf(azureInstances.get(0));
            if (str.contains("_")) {
                String[] nmeAzureInstnces = str.split(Pattern.quote("_"));
                if (nmeAzureInstnces.length > 0){
                    name = names + "_" + (TypesParser.safeParseInt(nmeAzureInstnces[nmeAzureInstnces.length - 1]) + 1);
                }
            }
        }
        return name;
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
                case "ZXhlY3V0ZUF1dG9SZXN0YXJ0cw==":{
                    return executeAutoRestarts();
                }
                case "ZXhlY3V0ZUF1dG9DaGFuZ2VJcHM=":{
                    return executeAutoChangeIps();
                }
                case "c3RvcEFjY291bnRzUHJvY2Vzc2Vz":{
                    return stopAccountsProcesses();
                }
                case "Y2FsY3VsYXRlSW5zdGFuY2VzTG9ncw==":{
                    return calculateInstancesLogs();
                }
                case "Z2V0UHVibGljSXBz":{
                    return getPublicIps();
                }
                case "ZXhlY3V0ZUNoYW5nZUlwcw==":{
                    return executeChangeIps();
                }
            }
        throw new DatabaseException("Action not found !");
    }

}
