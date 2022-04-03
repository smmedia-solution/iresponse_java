package tech.iresponse.webservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.production.MtaProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.production.component.Rotator;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.drops.MtaDropManager;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.production.workers.MtaTestServer;
import tech.iresponse.production.workers.MtaDropServer;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.LocalRandom;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class MtaProcesses implements Controller {

    public static volatile boolean IS_ERROR_OCCURED = false;
    public static volatile Rotator VMTASIDS_ROTATOR;
    public static volatile Rotator TESTEMAILS_ROTATOR;
    public static volatile Rotator AUTOREPLY_ROTATOR;
    public static volatile Rotator HEADERS_ROTATOR;
    public static volatile List PLACEHOLDERS_LIST;
    public static volatile int EMAILS_COUNTER = 0;
    public static volatile int TOTAL_PROGRESS = 0;

    public Response proceedDrop() {
        MtaProcess mtaProcess = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            mtaProcess = new MtaProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (mtaProcess.getEmpty()){
                throw new DatabaseException("No process found !");
            }
            if (!"in progress".equalsIgnoreCase(mtaProcess.status)){
                throw new DatabaseException("This process is not in progress !");
            }
            MtaComponent mtaComponent = MtaDropManager.parse(mtaProcess);
            if (mtaComponent == null){
                throw new DatabaseException("Error while trying to parse this drop !");
            }
            if ((mtaComponent.getVmtasIds()).length == 0){
                throw new DatabaseException("This drop has no vmtas !");
            }
            mtaProcess.status = "In Progress";
            mtaProcess.processId = app.checkAndgetInstance().getProcesssId() + "_" + app.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + app.checkAndgetInstance().getClientsDatabaseProcessId();
            mtaProcess.progress = 0;
            mtaProcess.update();
            if (mtaComponent.getPlaceholders() != null && !mtaComponent.getPlaceholders().isEmpty()) {
                MtaProcesses.PLACEHOLDERS_LIST = new ArrayList();
                for (int i = 0; i < mtaComponent.getPlaceholders().size(); i++){
                    MtaProcesses.PLACEHOLDERS_LIST.add(new Rotator(Arrays.asList(mtaComponent.getPlaceholders().get(i)), ((Integer)mtaComponent.getPlaceholdersRotations().get(i)).intValue()));
                }
            }
            HEADERS_ROTATOR = (mtaComponent.getHeaders() != null && (mtaComponent.getHeaders()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getHeaders()), mtaComponent.getHeadersRotation()) : null;
            TESTEMAILS_ROTATOR = (mtaComponent.getTestEmails() != null && (mtaComponent.getTestEmails()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getTestEmails()), mtaComponent.getTestRotation()) : null;
            AUTOREPLY_ROTATOR = (mtaComponent.getAutoReplyActivated() && mtaComponent.getAutoReplyMailboxes() != null && (mtaComponent.getAutoReplyMailboxes()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getAutoReplyMailboxes()), mtaComponent.getAutoReplyRotation()) : null;
            List<Integer> mtaServersIds = new ArrayList<>();
            for (String vmtasId : mtaComponent.getVmtasIds()) {
                int serverId = vmtasId.contains("|") ? TypesParser.safeParseInt(vmtasId.split(Pattern.quote("|"))[0]) : 0;
                if (serverId > 0 && !mtaServersIds.contains(serverId)){
                    mtaServersIds.add(serverId);
                }
            }
            int start = mtaComponent.getDataStart();
            int vmtasCount = 0;
            int serverCount = 0;
            int CountRest = 0;
            int serversSize = mtaServersIds.size();
            int vmtasSize = (mtaComponent.getVmtasIds()).length;
            if (serversSize == 0){
                throw new DatabaseException("This drop has no servers !");
            }
            if ("servers".equalsIgnoreCase(mtaComponent.getSplitEmailsType())) {
                serverCount = (int)Math.ceil((mtaComponent.getDataCount() / serversSize));
                CountRest = mtaComponent.getDataCount() - (serverCount * serversSize);
            } else {
                vmtasCount = (int)Math.ceil((mtaComponent.getDataCount() / vmtasSize));
                CountRest = mtaComponent.getDataCount() - (vmtasCount * vmtasSize);
            }
            ExecutorService serversExecutor = Executors.newFixedThreadPool(serversSize);
            List<Integer> vmtasIds = null;
            for (int i = 0; i < serversSize; i++) {
                if ("vmtas".equalsIgnoreCase(mtaComponent.getSplitEmailsType())){
                    serverCount = 0;
                }
                vmtasIds = new ArrayList<>();
                for (String vmtasId : mtaComponent.getVmtasIds()) {
                    int serverId = TypesParser.safeParseInt(vmtasId.split(Pattern.quote("|"))[0]);
                    int id = TypesParser.safeParseInt(vmtasId.split(Pattern.quote("|"))[1]);

                    if (vmtasId.contains("|") && TypesParser.safeParseInt(mtaServersIds.get(i)) == serverId) {
                        vmtasIds.add(id);
                        if ("vmtas".equalsIgnoreCase(mtaComponent.getSplitEmailsType())){
                            serverCount = serverCount + vmtasCount;
                        }
                    }
                }
                if (i == (serversSize - 1)) {
                    serverCount = serverCount + CountRest;
                }
                serversExecutor.submit((Runnable)new MtaDropServer((MtaComponent)SerializationUtils.clone((Serializable)mtaComponent), TypesParser.safeParseInt(mtaServersIds.get(i)), vmtasIds, start, serverCount));
                start += serverCount;
            }
            serversExecutor.shutdown();
            if (!serversExecutor.awaitTermination(1L, TimeUnit.DAYS)){
                serversExecutor.shutdownNow();
            }
        } catch (Exception e) {
            IS_ERROR_OCCURED = true;
            Loggers.error(e);
        }
        if (mtaProcess != null){
            MtaDropHelper.finishProccess((ActiveRecord)mtaProcess, IS_ERROR_OCCURED, 0);
        }
        return new Response("Process completed successfully !", 200);
    }

    public Response proceedTest() {
        MtaProcess mtaProcess = null;
        try {
            ArrayList<String> arrayList1;
            String[] arrayOfString2;
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            mtaProcess = new MtaProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (mtaProcess.getEmpty()){
                throw new DatabaseException("No process found !");
            }
            if (!"in progress".equalsIgnoreCase(mtaProcess.status)){
                throw new DatabaseException("This process is not in progress !");
            }
            MtaComponent mtaComponent = MtaDropManager.parse(mtaProcess);
            if (mtaComponent == null){
                throw new DatabaseException("Error while trying to parse this test !");
            }
            if ((mtaComponent.getVmtasIds()).length == 0){
                throw new DatabaseException("This test has no vmtas !");
            }
            mtaProcess.processId = app.checkAndgetInstance().getProcesssId() + "_" + app.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + app.checkAndgetInstance().getClientsDatabaseProcessId();
            mtaProcess.progress = 0;
            mtaProcess.update();
            if (mtaComponent.getPlaceholders() != null && !mtaComponent.getPlaceholders().isEmpty()) {
                PLACEHOLDERS_LIST = new ArrayList();
                for (int b = 0; b < mtaComponent.getPlaceholders().size(); b++) {
                    PLACEHOLDERS_LIST.add(new Rotator(Arrays.asList(mtaComponent.getPlaceholders().get(b)), ((Integer) mtaComponent.getPlaceholdersRotations().get(b)).intValue()));
                }
            }
            HEADERS_ROTATOR = (mtaComponent.getHeaders() != null && (mtaComponent.getHeaders()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getHeaders()), mtaComponent.getHeadersRotation()) : null;
            TESTEMAILS_ROTATOR = (mtaComponent.getTestEmails() != null && (mtaComponent.getTestEmails()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getTestEmails()), mtaComponent.getTestRotation()) : null;
            AUTOREPLY_ROTATOR = (mtaComponent.getAutoReplyActivated() && mtaComponent.getAutoReplyMailboxes() != null && (mtaComponent.getAutoReplyMailboxes()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getAutoReplyMailboxes()), mtaComponent.getAutoReplyRotation()) : null;
            String[] vmtaIds = new String[]{};
            switch (mtaComponent.getProcessType()) {
                case "test-ip":
                    vmtaIds = (String[])ArrayUtils.add((Object[])vmtaIds, mtaComponent.getVmtasIds()[(new Random()).nextInt((mtaComponent.getVmtasIds()).length)]);
                    break;
                case "test-servers":
                    arrayList1 = new ArrayList();
                    arrayOfString2 = Arrays.<String>copyOf(mtaComponent.getVmtasIds(), (mtaComponent.getVmtasIds()).length);
                    arrayOfString2 = LocalRandom.random(arrayOfString2);
                    for (String str1 : arrayOfString2) {
                        String str2 = str1.contains("|") ? str1.split(Pattern.quote("|"))[0] : "";
                        if (!"".equals(str2) && !arrayList1.contains(str2)) {
                            vmtaIds = (String[])ArrayUtils.add((Object[])vmtaIds, str1);
                            arrayList1.add(str2);
                        }
                    }
                    arrayOfString2 = null;
                    arrayList1 = null;
                    break;
            }
            if (vmtaIds.length > 0) {
                mtaComponent.setVmtasIds(vmtaIds) ;
            }
            VMTASIDS_ROTATOR = (mtaComponent.getVmtasIds() != null && (mtaComponent.getVmtasIds()).length > 0) ? new Rotator(Arrays.asList(mtaComponent.getVmtasIds()), mtaComponent.getVmtasRotation()) : null;
            ArrayList<Integer> arrayList = new ArrayList();
            HashMap<Object, Object> hashMap1 = new HashMap<>();
            HashMap<Object, Object> hashMap2 = new HashMap<>();
            int totalEmails = 0;
            for (String vmtasId : mtaComponent.getVmtasIds()) {
                int serverId = vmtasId.contains("|") ? TypesParser.safeParseInt(vmtasId.split(Pattern.quote("|"))[0]) : 0;
                int id = vmtasId.contains("|") ? TypesParser.safeParseInt(vmtasId.split(Pattern.quote("|"))[1]) : 0;
                if (serverId > 0 && id > 0) {
                    if (!arrayList.contains(Integer.valueOf(serverId))){
                        arrayList.add(Integer.valueOf(serverId));
                    }
                    if (!hashMap2.containsKey(Integer.valueOf(serverId))){
                        hashMap2.put(Integer.valueOf(serverId), new ArrayList());
                    }
                    if (!hashMap1.containsKey(Integer.valueOf(serverId))){
                        hashMap1.put(Integer.valueOf(serverId), new ArrayList());
                    }
                    ((List<Integer>)hashMap2.get(Integer.valueOf(serverId))).add(Integer.valueOf(id));
                }
            }
            if ("test-emails".equalsIgnoreCase(mtaComponent.getProcessType())) {
                totalEmails = getTotalEmailsTest(mtaComponent, hashMap1);
            } else {
                totalEmails = getTotalEmailsDrop(mtaComponent, hashMap1);
            }
            if (hashMap1.isEmpty()){
                throw new DatabaseException("No emails generated !");
            }
            mtaProcess.totalEmails = totalEmails;
            mtaComponent.setTotalEmails(totalEmails);
            mtaProcess.update();
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool("multithread-servers".equals(mtaComponent.getTestThreads()) ? arrayList.size() : 1);
            arrayList.parallelStream().filter(nn1 -> hashMap1.containsKey(nn1)).forEachOrdered(n2 -> fixedThreadPool.submit((Runnable)new MtaTestServer((MtaComponent)SerializationUtils.clone((Serializable)mtaComponent), (int)n2, (List)hashMap1.get(n2), (List)hashMap2.get(n2))));
            fixedThreadPool.shutdown();
            if (!fixedThreadPool.awaitTermination(1L, TimeUnit.DAYS)){
                fixedThreadPool.shutdownNow();
            }
        } catch (Exception e) {
            IS_ERROR_OCCURED = true;
            Loggers.error(e);
        }
        if (mtaProcess != null){
            MtaDropHelper.finishProccess((ActiveRecord)mtaProcess, IS_ERROR_OCCURED, 0);
        }
        return new Response("Process completed successfully !", 200);
    }

    private int getTotalEmailsDrop(MtaComponent mtaComp, HashMap paramHashMap) {
        int totalEmail = 0;
        int i = 0;
        int j = 0;
        int k = (mtaComp.getVmtasIds()).length;
        int m = (mtaComp.getTestEmails()).length;
        int n = 0;
        int i1 = k;
        if (mtaComp.getTestEmailsCombination()){
            i1 *= m;
        }
        if (mtaComp.getPlaceholders() != null && !mtaComp.getPlaceholders().isEmpty()) {
            int i2 = mtaComp.getPlaceholders().size();
            for (int b = 0; b < i2; b++) {
                if (((Boolean)mtaComp.getPlaceholdersCombinations().get(b)).booleanValue()) {
                    i1 *= ((String[])mtaComp.getPlaceholders().get(b)).length;
                    n += ((String[])mtaComp.getPlaceholders().get(b)).length;
                }
            }
        }
        int b2 = 0;
        n = (n == 0) ? 1 : n;
        ArrayList<TestEmail> arrayList = new ArrayList();
        int b3;
        for (b3 = 0; b3 < i1; b3++) {
            TestEmail testEmal = new TestEmail();
            testEmal.setPlaceholders(new ArrayList());
            if (mtaComp.getPlaceholders() != null && !mtaComp.getPlaceholders().isEmpty()){
                for (int b = 0; b < mtaComp.getPlaceholders().size(); b++) {
                    String[] arrayOfString = (String[])mtaComp.getPlaceholders().get(b);
                    if (arrayOfString != null && arrayOfString.length > 0){
                        if (((Boolean)mtaComp.getPlaceholdersCombinations().get(b)).booleanValue()) {
                            if (b2 >= arrayOfString.length) {
                                b2 = 0;
                            }
                            testEmal.getPlaceholders().add(arrayOfString[b2]);
                            b2++;
                        } else {
                            testEmal.getPlaceholders().add(getCurrentPlaceHolder(b));
                        }
                    }
                }
            }
            if (mtaComp.getAutoReplyActivated()) {
                testEmal.setAutoReplyMailbox(getCurrentAutoReply());
            } else {
                testEmal.setAutoReplyMailbox("");
            }
            arrayList.add(testEmal);
            totalEmail++;
        }
        if (!arrayList.isEmpty()) {
            b3 = 0;
            int b = mtaComp.getTestEmailsCombination() ? (mtaComp.getTestEmails()).length : 1;
            for (String str : mtaComp.getVmtasIds()) {
                for (int b4 = 0; b4 < b; b4++) {
                    for (int b5 = 0; b5 < n; b5++) {
                        j = TypesParser.safeParseInt(str.split(Pattern.quote("|"))[0]);
                        i = TypesParser.safeParseInt(str.split(Pattern.quote("|"))[1]);
                        if (j > 0 && i > 0) {
                            TestEmail tstEmail = arrayList.get(b3);
                            tstEmail.setComponentId(i);
                            tstEmail.setEmail(mtaComp.getTestEmailsCombination() ? mtaComp.getTestEmails()[b4] : getCurrentTestEmail());
                            b3++;
                            ((List<TestEmail>)paramHashMap.get(Integer.valueOf(j))).add(tstEmail);
                        }
                    }
                }
            }
        }
        return totalEmail;
    }

    private int getTotalEmailsTest(MtaComponent mtaComponent, HashMap paramHashMap) {
        int totalEmail = 0;
        int k = (mtaComponent.getVmtasIds()).length;
        int m = (mtaComponent.getTestEmails()).length;
        int n = 0;
        int n1 = k;
        if (mtaComponent.getTestEmailsCombination()){
            n1 *= m;
        }
        if (mtaComponent.getPlaceholders() != null && !mtaComponent.getPlaceholders().isEmpty()) {
            int i2 = mtaComponent.getPlaceholders().size();
            for (int b = 0; b < i2; b++) {
                if (((Boolean)mtaComponent.getPlaceholdersCombinations().get(b)).booleanValue()) {
                    n1 *= ((String[])mtaComponent.getPlaceholders().get(b)).length;
                    n += ((String[])mtaComponent.getPlaceholders().get(b)).length;
                }
            }
        }
        int b2 = 0;
        n = (n == 0) ? 1 : n;
        ArrayList<TestEmail> arrayTestEMail = new ArrayList();
        int b3;
        for (b3 = 0; b3 < n1; b3++) {
            TestEmail testEmail = new TestEmail();
            testEmail.setPlaceholders(new ArrayList());
            if (mtaComponent.getPlaceholders() != null && !mtaComponent.getPlaceholders().isEmpty()){
                for (int b = 0; b < mtaComponent.getPlaceholders().size(); b++){
                    String[] arrayOfString = (String[])mtaComponent.getPlaceholders().get(b);
                    if (arrayOfString != null && arrayOfString.length > 0){
                        if (((Boolean) mtaComponent.getPlaceholdersCombinations().get(b)).booleanValue()){
                            if (b2 >= arrayOfString.length){
                                b2 = 0;
                            }
                            testEmail.getPlaceholders().add(arrayOfString[b2]);
                            b2++;
                        } else{
                            testEmail.getPlaceholders().add(getCurrentPlaceHolder(b));
                        }
                    }
                }
            }
            if (mtaComponent.getAutoReplyActivated()) {
                testEmail.setAutoReplyMailbox(getCurrentAutoReply());
            } else {
                testEmail.setAutoReplyMailbox("");
            }
            arrayTestEMail.add(testEmail);
            totalEmail++;
        }
        if (!arrayTestEMail.isEmpty()) {
            b3 = 0;
            int b = mtaComponent.getTestEmailsCombination() ? (mtaComponent.getVmtasIds()).length : 1;
            for (String str : mtaComponent.getTestEmails()) {
                for (int b4 = 0; b4 < b; b4++) {
                    for (int b5 = 0; b5 < n; b5++) {
                        String str1 = mtaComponent.getTestEmailsCombination() ? mtaComponent.getVmtasIds()[b4] : getCurrentVmtaIds();
                        int num1 = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[0]);
                        int num2 = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[1]);
                        if (num1 > 0 && num2 > 0) {
                            TestEmail testEmail = arrayTestEMail.get(b3);
                            testEmail.setComponentId(num2);
                            testEmail.setEmail(str);
                            b3++;
                            ((List<TestEmail>)paramHashMap.get(Integer.valueOf(num1))).add(testEmail);
                        }
                    }
                }
            }
        }
        return totalEmail;
    }

    public static synchronized int updateCounter() {
        return EMAILS_COUNTER++;
    }

    public static synchronized String getCurrentPlaceHolder(int current) {
        if (MtaProcesses.PLACEHOLDERS_LIST != null && !MtaProcesses.PLACEHOLDERS_LIST.isEmpty() && current < MtaProcesses.PLACEHOLDERS_LIST.size() && MtaProcesses.PLACEHOLDERS_LIST.get(current) != null) {
            String pl = (String) ((Rotator)MtaProcesses.PLACEHOLDERS_LIST.get(current)).getCurrentValue();
            ((Rotator)MtaProcesses.PLACEHOLDERS_LIST.get(current)).rotate();
            return pl;
        }
        return "";
    }

    public static synchronized String getCurrentHeader() {
        if (HEADERS_ROTATOR != null) {
            String str = (String)HEADERS_ROTATOR.getCurrentValue();
            HEADERS_ROTATOR.rotate();
            return str;
        }
        return "";
    }

    public static synchronized String getCurrentTestEmail() {
        if (TESTEMAILS_ROTATOR != null) {
            String str = (String)TESTEMAILS_ROTATOR.getCurrentValue();
            TESTEMAILS_ROTATOR.rotate();
            return str;
        }
        return "";
    }

    public static synchronized String getCurrentAutoReply() {
        if (AUTOREPLY_ROTATOR != null) {
            String str = (String)AUTOREPLY_ROTATOR.getCurrentValue();
            AUTOREPLY_ROTATOR.rotate();
            return str;
        }
        return "";
    }

    public static synchronized String getCurrentVmtaIds() {
        if (VMTASIDS_ROTATOR != null) {
            String str = (String)VMTASIDS_ROTATOR.getCurrentValue();
            VMTASIDS_ROTATOR.rotate();
            return str;
        }
        return "";
    }

    public static synchronized void updateProgress(int progress) {
        MtaProcesses.TOTAL_PROGRESS += progress;
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "cHJvY2VlZERyb3A=": {
                return proceedDrop();
            }
            case "cHJvY2VlZFRlc3Q=": {
                return proceedTest();
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
