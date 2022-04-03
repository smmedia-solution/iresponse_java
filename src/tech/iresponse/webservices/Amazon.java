package tech.iresponse.webservices;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import com.amazonaws.services.ec2.model.InstancePrivateIpAddress;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.AwsProcess;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.AwsAccount;
import tech.iresponse.models.admin.AwsAccountProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.amazon.Ec2Manager;
import tech.iresponse.amazon.update.UpdateData;
import tech.iresponse.amazon.instance.InstancesActions;
import tech.iresponse.amazon.instance.InstancesLogs;
import tech.iresponse.amazon.instance.InstancesManager;
import tech.iresponse.amazon.instance.InstancesSearchEips;
import tech.iresponse.amazon.instance.InstancesUpdater;
import tech.iresponse.amazon.instance.InstancesUpdaterProxy;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class Amazon implements Controller {

    public static volatile LinkedHashMap INSTNCES = new LinkedHashMap<>();
    public static volatile int INSTANCES_CREATED = 0;
    public static volatile int INSTANCES_INSTALLED = 0;
    public static volatile boolean IS_ERROR_OCCURED = false;
    private static final int MAX_THREAD = 5;
    private static final int FIRSTS = 5;
    private static final int NEXTS = 8;
    public static final int MAX_ASSOCIATION_ADDRESS = 50;

    public Response createInstances() {
        AwsProcess awsProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            awsProcss = new AwsProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (awsProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            awsProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            awsProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            awsProcss.status = "In Progress";
            awsProcss.progress = "0%";
            awsProcss.instancesCreated = 0;
            awsProcss.instancesInstalled = 0;
            awsProcss.update();

            ServerProvider serverPrvder = (ServerProvider)ServerProvider.first(ServerProvider.class, "name = ?", new Object[] { "AWS" });
            if (serverPrvder == null || serverPrvder.getEmpty()) {
                serverPrvder = new ServerProvider();
                serverPrvder.name = "AWS";
                serverPrvder.status = "Activated";
                serverPrvder.createdBy = (Application.checkAndgetInstance().getUser()).email;
                serverPrvder.createdDate = new Date(System.currentTimeMillis());
                serverPrvder.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                serverPrvder.lastUpdatedDate = new Date(System.currentTimeMillis());
                serverPrvder.id = serverPrvder.insert();
            }

            List awsDomains = (awsProcss.domains == null || "rdns".equalsIgnoreCase(awsProcss.domains)) ? Arrays.asList(new String[] { "rdns" }) : Arrays.asList(awsProcss.domains.split(Pattern.quote(",")));
            if (awsDomains.isEmpty()){
                throw new DatabaseException("No domains passed to this process !");
            }

            List<String> regions = Arrays.asList(awsProcss.regions.split(Pattern.quote(",")));
            if (regions.isEmpty()){
                throw new DatabaseException("No regions passed to this process !");
            }

            boolean next = (awsDomains.size() != regions.size() * awsProcss.nbInstances) ? true : false;
            int nbthread = (regions.size() > MAX_THREAD) ? MAX_THREAD : regions.size();
            ExecutorService execService = Executors.newFixedThreadPool(nbthread);

            int count = 0;
            JSONArray domains = null;

            for (String region : regions) {
                domains = new JSONArray();
                for (int b1 = 0; b1 < awsProcss.nbInstances; b1++) {
                    if (next) {
                        domains.put(awsDomains.get((new Random()).nextInt(awsDomains.size())));
                    } else {
                        domains.put(awsDomains.get(count));
                        count++;
                    }
                }
                execService.submit((Runnable)new InstancesManager(awsProcss, serverPrvder, region, domains));
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

        if (awsProcss != null){
            UpdateData.finishUpdate(awsProcss);
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

        AwsProcess awsProcss = null;

        for (int b = 0; b < processesIds.length(); b++) {
            awsProcss = new AwsProcess(Integer.valueOf(processesIds.getInt(b)));
            if (!awsProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(awsProcss.status)){
                    throw new DatabaseException("This process with id : " + awsProcss.id + " is not in progress !");
                }

                Terminal.killProcess(awsProcss.processId);
                awsProcss.status = "Interrupted";
                awsProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                awsProcss.update();
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

        String instIds = "id = ANY (ARRAY[";
        for (int b = 0; b < instancesIds.length(); b++){
            instIds = instIds + instancesIds.getInt(b) + ",";
        }
        instIds = instIds.substring(0, instIds.length() - 1) + "])";

        List<AwsInstance> awsInstnces = (List)AwsInstance.all(AwsInstance.class, instIds, null);
        if (awsInstnces.isEmpty()){
            throw new DatabaseException("No instances found !");
        }

        LinkedHashMap<String,LinkedHashMap> instances = new LinkedHashMap<>();

        for (AwsInstance awsInst : awsInstnces) {
            String id = awsInst.accountId + "|" + awsInst.region;
            if (!instances.containsKey(id)){
                instances.put(id, new LinkedHashMap<>());
            }
            ((LinkedHashMap<String, AwsInstance>)instances.get(id)).put(awsInst.getName(), awsInst);
        }

        int nbthread = (instances.size() > MAX_THREAD) ? MAX_THREAD : instances.size();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);

        instances.entrySet().stream().map(inst -> {
            execService.submit((Runnable)new InstancesActions((String)inst.getKey(), (LinkedHashMap)inst.getValue(), action));
            return inst;
        }).forEachOrdered(inst -> ThreadSleep.sleep(FIRSTS, NEXTS));

        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        return new Response("Action executed successfully !", 200);
    }

    public Response executeRestarts() throws Exception {
        AwsAccountProcess awsAccProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            awsAccProcss = new AwsAccountProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (awsAccProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            awsAccProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            awsAccProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            awsAccProcss.status = "In Progress";
            awsAccProcss.update();

            AwsAccount awsAcc = new AwsAccount(Integer.valueOf(awsAccProcss.accountId));
            if (awsAcc.getEmpty()){
                throw new DatabaseException("No account found !");
            }

            if ("".equals(awsAccProcss.region) || "null".equals(awsAccProcss.region)){
                throw new DatabaseException("Region not found !");
            }

            Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
            if (!ec2Mng.authenticate(awsAccProcss.region)){
                throw new DatabaseException("could not connect to : " + awsAcc.name + " with region : " + awsAccProcss.region);
            }

            ArrayList<String> instancesIds = null;
            List<Instance> instances = null;

            int i = 0;
            while (true) {
                instances = ec2Mng.getInstances("running");
                if (instances != null && !instances.isEmpty()) {
                    instancesIds = new ArrayList(instances.size());
                    for (Instance instance : instances){
                        instancesIds.add(instance.getInstanceId());
                    }
                    ec2Mng.actionStop(instancesIds);
                }

                instances = ec2Mng.getInstances("stopped");
                if (instances != null && !instances.isEmpty()) {
                    instancesIds = new ArrayList<>(instances.size());
                    for (Instance instance : instances){
                        instancesIds.add(instance.getInstanceId());
                    }
                    ec2Mng.actionStart(instancesIds);
                }

                i++;
                ThreadSleep.sleep(2000L);

                if(i == 1){
                    awsAccProcss.status = "Completed";
                    awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                    awsAccProcss.update();
                    break;
                }

            }
        } catch (Exception ex) {
            if (awsAccProcss != null) {
                awsAccProcss.status = "Error";
                awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                awsAccProcss.update();
            }
            throw ex;
        }

        return new Response("Processes started successfully !", 200);
    }

    public Response executeRotatesRestarts() throws Exception {
        AwsAccountProcess awsAccProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            awsAccProcss = new AwsAccountProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (awsAccProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            awsAccProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            awsAccProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            awsAccProcss.status = "In Progress";
            awsAccProcss.update();

            AwsAccount awsAcc = new AwsAccount(Integer.valueOf(awsAccProcss.accountId));
            if (awsAcc.getEmpty()){
                throw new DatabaseException("No account found !");
            }

            if ("".equals(awsAccProcss.region) || "null".equals(awsAccProcss.region)){
                throw new DatabaseException("Region not found !");
            }

            Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
            if (!ec2Mng.authenticate(awsAccProcss.region)){
                throw new DatabaseException("could not connect to : " + awsAcc.name + " with region : " + awsAccProcss.region);
            }

            ArrayList<String> instancesIds = null;
            List<Instance> instances = null;
            List<Address> adresses = null;
            AllocateAddressResult allocResult = null;
            Address addresse = null;
            String allocationId = null;
            String str2 = null;
            String str3 = System.getProperty("assets.path") + File.separator + "scripts" + File.separator + "dbl.sh";

            while (true) {
                instances = ec2Mng.getInstances("running");
                if (instances != null && !instances.isEmpty()) {
                    instancesIds = new ArrayList(instances.size());
                    for (Instance inst : instances){
                        instancesIds.add(inst.getInstanceId());
                    }
                    ec2Mng.actionStop(instancesIds);
                }

                instances = ec2Mng.getInstances("stopped");
                if (instances != null && !instances.isEmpty()) {
                    instancesIds = new ArrayList<>(instances.size());
                    adresses = ec2Mng.getAddresses();
                    if (adresses != null && !adresses.isEmpty()){
                        for (Instance inst : instances) {
                            instancesIds.add(inst.getInstanceId());
                            for (int b = 0; b < adresses.size(); b++) {
                                addresse = adresses.get(b);
                                if (addresse != null && addresse.getInstanceId() != null && inst.getInstanceId().equals(addresse.getInstanceId())) {
                                    ec2Mng.disassociateAddresses(addresse.getAssociationId());
                                    ec2Mng.releaseAddresses(addresse.getAllocationId());
                                }
                            }
                        }
                    }
                    ec2Mng.actionStart(instancesIds);
                }

                instances = ec2Mng.getInstances("running");
                if (instances != null && !instances.isEmpty()) {
                    for (Instance instance : instances) {
                        for (InstancePrivateIpAddress instncePrivateIp : ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getPrivateIpAddresses()) {
                            boolean associatedRun = false;
                            int i = 0;
                            while (!associatedRun && i != MAX_ASSOCIATION_ADDRESS) {
                                allocResult = ec2Mng.allocateAddresses();
                                if (allocResult != null) {
                                    str2 = Terminal.executeCommand("sh " + str3 + " " + allocResult.getPublicIp() + " | grep Blacklisted").replaceAll("\\s+", "");
                                    int mtchGroup = 0;
                                    Matcher mtch = Pattern.compile("\\d+").matcher(str2);
                                    while (mtch.find()){
                                        mtchGroup = TypesParser.safeParseInt(mtch.group());
                                    }
                                    if (mtchGroup == 0) {
                                        ec2Mng.associateAddresses(instance.getInstanceId(), instncePrivateIp.getPrivateIpAddress(), allocResult.getAllocationId(), ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getNetworkInterfaceId());
                                        associatedRun = true;
                                        continue;
                                    }
                                    ec2Mng.releaseAddresses(allocResult.getAllocationId());
                                    i++;
                                }
                            }
                            ec2Mng.associateAddresses(instance.getInstanceId(), instncePrivateIp.getPrivateIpAddress(), allocationId, ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getNetworkInterfaceId());
                        }
                    }

                    ThreadSleep.sleep(120000L);

                    adresses = ec2Mng.getAddresses();
                    if (adresses != null && !adresses.isEmpty()){
                        for (Instance instance : instances) {
                            for (int b1 = 0; b1 < adresses.size(); b1++) {
                                addresse = adresses.get(b1);
                                if (addresse != null && addresse.getInstanceId() != null && instance.getInstanceId().equals(addresse.getInstanceId())) {
                                    ec2Mng.disassociateAddresses(addresse.getAssociationId());
                                    ec2Mng.releaseAddresses(addresse.getAllocationId());
                                }
                            }
                        }
                    }

                    ThreadSleep.sleep(120000L);

                    for (Instance instance : instances) {
                        for (InstancePrivateIpAddress instncePrivateIp : ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getPrivateIpAddresses()) {
                            boolean associated = false;
                            int k = 0;
                            while (!associated && k != MAX_ASSOCIATION_ADDRESS) {
                                allocResult = ec2Mng.allocateAddresses();
                                if (allocResult != null) {
                                    str2 = Terminal.executeCommand("sh " + str3 + " " + allocResult.getPublicIp() + " | grep Blacklisted").replaceAll("\\s+", "");
                                    int mtcherGroup = 0;
                                    Matcher mtcher = Pattern.compile("\\d+").matcher(str2);
                                    while (mtcher.find()){
                                        mtcherGroup = TypesParser.safeParseInt(mtcher.group());
                                    }
                                    if (mtcherGroup == 0) {
                                        ec2Mng.associateAddresses(instance.getInstanceId(), instncePrivateIp.getPrivateIpAddress(), allocResult.getAllocationId(), ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getNetworkInterfaceId());
                                        associated = true;
                                        continue;
                                    }
                                    ec2Mng.releaseAddresses(allocResult.getAllocationId());
                                    k++;
                                }
                            }
                            ec2Mng.associateAddresses(instance.getInstanceId(), instncePrivateIp.getPrivateIpAddress(), allocationId, ((InstanceNetworkInterface)instance.getNetworkInterfaces().get(0)).getNetworkInterfaceId());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (awsAccProcss != null) {
                awsAccProcss.status = "Error";
                awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                awsAccProcss.update();
            }
            throw ex;
        }
    }

    public Response searchForElasticIps() throws Exception {
        AwsAccountProcess awsAccProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            awsAccProcss = new AwsAccountProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (awsAccProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            awsAccProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            awsAccProcss.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            awsAccProcss.status = "In Progress";
            awsAccProcss.update();

            AwsAccount awsAcc = new AwsAccount(Integer.valueOf(awsAccProcss.accountId));
            if (awsAcc.getEmpty()){
                throw new DatabaseException("No account found !");
            }

            if ("".equals(awsAccProcss.region) || "null".equals(awsAccProcss.region)){
                throw new DatabaseException("Region not found !");
            }

            Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
            if (!ec2Mng.authenticate(awsAccProcss.region)){
                throw new DatabaseException("could not connect to : " + awsAcc.name + " with region : " + awsAccProcss.region);
            }

            List<Instance> instances = ec2Mng.getInstances(null);
            ExecutorService execService = Executors.newFixedThreadPool(1);

            instances.forEach(instce -> execService.submit((Runnable)new InstancesSearchEips(ec2Mng, instce)));
            execService.shutdown();

            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }

            awsAccProcss.status = "Completed";
            awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            awsAccProcss.update();

        } catch (Exception ex) {
            if (awsAccProcss != null) {
                awsAccProcss.status = "Error";
                awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                awsAccProcss.update();
            }
            throw ex;
        }

        return new Response("Process completed successfully !", 200);
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

        AwsAccountProcess awsAccProcss = null;
        for (int b = 0; b < processesIds.length(); b++) {
            awsAccProcss = new AwsAccountProcess(Integer.valueOf(processesIds.getInt(b)));
            if (!awsAccProcss.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(awsAccProcss.status)){
                    throw new DatabaseException("This process with id : " + awsAccProcss.id + " is not in progress !");
                }
                Terminal.killProcess(awsAccProcss.processId);
                awsAccProcss.status = "Interrupted";
                awsAccProcss.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                awsAccProcss.update();
            }
        }

        return new Response("Processes stoped successfully !", 200);
    }

    public Response refreshInstances() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray bundles = (app.getParameters().has("bundle") && app.getParameters().get("bundle") instanceof JSONArray) ? app.getParameters().getJSONArray("bundle") : new JSONArray();
        if (bundles.length() == 0){
            throw new DatabaseException("No bundle found !");
        }

        ExecutorService execService = Executors.newFixedThreadPool(MAX_THREAD);

        for (int b = 0; b < bundles.length(); b++) {
            JSONObject bundle = bundles.getJSONObject(b);
            if (bundle.has("account-id") && bundle.has("region")) {
                execService.submit((Runnable)new InstancesUpdater(bundle.getInt("account-id"), bundle.getString("region")));
                ThreadSleep.sleep(FIRSTS, NEXTS);
            }
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        return new Response("Instances refreshed successfully !", 200);
    }

    public Response refreshInstancesProxy() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray bundles = (app.getParameters().has("bundle") && app.getParameters().get("bundle") instanceof JSONArray) ? app.getParameters().getJSONArray("bundle") : new JSONArray();
        if (bundles.length() == 0){
            throw new DatabaseException("No bundle found !");
        }

        ExecutorService execService = Executors.newFixedThreadPool(MAX_THREAD);

        for (int b = 0; b < bundles.length(); b++) {
            JSONObject bundle = bundles.getJSONObject(b);
            if (bundle.has("account-id") && bundle.has("region")) {
                execService.submit((Runnable)new InstancesUpdaterProxy(bundle.getInt("account-id"), bundle.getString("region")));
                ThreadSleep.sleep(FIRSTS, NEXTS);
            }
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        return new Response("Instances refreshed successfully !", 200);
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

        int nbthread = (instancesIds.length() > 100) ? 100 : instancesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);

        for (int b = 0; b < instancesIds.length(); b++){
            execService.submit((Runnable)new InstancesLogs(instancesIds.getInt(b)));
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        return new Response("Logs calculated successfully !", 200);
    }

    public static synchronized void updateCountInstancesCreated(int count) {
        INSTANCES_CREATED += count;
    }

    public static synchronized void updateCountInstancsInstalled() {
        INSTANCES_INSTALLED++;
    }

    public static synchronized void addInstance(String id, List lst) {
        INSTNCES.put(id, lst);
    }

    public static synchronized void addInstance(String str, Instance inst) {
        ((List<Instance>)INSTNCES.get(str)).add(inst);
    }

    public static synchronized void removeInstance(String id, Instance inst) {
        ((List)INSTNCES.get(id)).remove(inst);
    }

    public static synchronized List getInstnces(String id) {
        return (List)INSTNCES.get(id);
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
            case "ZXhlY3V0ZVJlc3RhcnRz":{
                return executeRestarts();
            }
            case "ZXhlY3V0ZVJvdGF0ZXNSZXN0YXJ0cw==":{
                return executeRotatesRestarts();
            }
            case "c2VhcmNoRm9yRWxhc3RpY0lwcw==":{
                return searchForElasticIps();
            }
            case "c3RvcEFjY291bnRzUHJvY2Vzc2Vz":{
                return stopAccountsProcesses();
            }
            case "cmVmcmVzaEluc3RhbmNlcw==":{
                return refreshInstances();
            }
            case "cmVmcmVzaEluc3RhbmNlc1Byb3h5":{
                return refreshInstancesProxy();
            }
            case "Y2FsY3VsYXRlSW5zdGFuY2VzTG9ncw==":{
                return calculateInstancesLogs();
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
