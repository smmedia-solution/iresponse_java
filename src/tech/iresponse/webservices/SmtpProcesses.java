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
import tech.iresponse.models.production.SmtpProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.production.component.Rotator;
import tech.iresponse.production.component.SmtpComponent;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.drops.SmtpProductionHelper;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.production.workers.SmtpDropServer;
import tech.iresponse.production.workers.SmtpTestServer;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.LocalRandom;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class SmtpProcesses implements Controller {

    public static volatile boolean IS_ERROR_OCCURED = false;
    public static volatile Rotator SMTUSRIDS_ROTATOR;
    public static volatile Rotator TESTEMAILS_ROTATOR;
    public static volatile Rotator AUTOREPLY_ROTATOR;
    public static volatile Rotator HEADERS_ROTATOR;
    public static volatile List PLACEHOLDERS_LIST;
    public static volatile int EMAILS_COUNTER = 0;
    public static volatile int TOTAL_PROGRESS = 0;

    public Response proceedDrop() {
        SmtpProcess smtpProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            smtpProcss = new SmtpProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (smtpProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }
            if (!"in progress".equalsIgnoreCase(smtpProcss.status)){
                throw new DatabaseException("This process is not in progress !");
            }
            SmtpComponent smtpCompnt = SmtpProductionHelper.parse(smtpProcss);
            if (smtpCompnt == null){
                throw new DatabaseException("Error while trying to parse this drop !");
            }
            if ((smtpCompnt.getSmtpUsersIds()).length == 0){
                throw new DatabaseException("This drop has no smtp users !");
            }
            smtpProcss.status = "In Progress";
            smtpProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            smtpProcss.progress = 0;
            smtpProcss.update();
            if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
                SmtpProcesses.PLACEHOLDERS_LIST = new ArrayList();
                for (int i = 0; i < smtpCompnt.getPlaceholders().size(); i++){
                    SmtpProcesses.PLACEHOLDERS_LIST.add(new Rotator(Arrays.asList(smtpCompnt.getPlaceholders().get(i)), ((Integer)smtpCompnt.getPlaceholdersRotations().get(i)).intValue()));
                }
            }
            HEADERS_ROTATOR = (smtpCompnt.getHeaders() != null && (smtpCompnt.getHeaders()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getHeaders()), smtpCompnt.getHeadersRotation()) : null;
            TESTEMAILS_ROTATOR = (smtpCompnt.getTestEmails() != null && (smtpCompnt.getTestEmails()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getTestEmails()), smtpCompnt.getTestRotation()) : null;
            AUTOREPLY_ROTATOR = (smtpCompnt.isAutoReplyActivated() && smtpCompnt.getAutoReplyMailboxes() != null && (smtpCompnt.getAutoReplyMailboxes()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getAutoReplyMailboxes()), smtpCompnt.getAutoReplyRotation()) : null;
            List<Integer> smtpServersIds = new ArrayList<>();
            for (String sui : smtpCompnt.getSmtpUsersIds()) {
                int serverId = sui.contains("|") ? TypesParser.safeParseInt(sui.split(Pattern.quote("|"))[0]) : 0;
                if (serverId > 0 && !smtpServersIds.contains(serverId)){
                    smtpServersIds.add(serverId);
                }
            }
            int dtStar = smtpCompnt.getDataStart();
            int smtpUsersCount = 0;
            int serverCount = 0;
            int countRest = 0;
            int serversSize = smtpServersIds.size();
            int smtpUsersSize = (smtpCompnt.getSmtpUsersIds()).length;
            if (serversSize == 0){
                throw new DatabaseException("This drop has no servers !");
            }
            if ("servers".equalsIgnoreCase(smtpCompnt.getSplitEmailsType())) {
                serverCount = (int) Math.ceil((smtpCompnt.getDataCount() / serversSize));
                countRest = smtpCompnt.getDataCount() - (serverCount * serversSize);
            } else {
                smtpUsersCount = (int)Math.ceil((smtpCompnt.getDataCount() / smtpUsersSize));
                countRest = smtpCompnt.getDataCount() - (smtpUsersCount * smtpUsersSize);
            }
            ExecutorService execService = Executors.newFixedThreadPool(serversSize);
            ArrayList<Integer> smtpUsersIds = null;
            for (int i = 0; i < serversSize; i++) {
                if ("vmtas".equalsIgnoreCase(smtpCompnt.getSplitEmailsType())){
                    serverCount = 0;
                }
                smtpUsersIds = new ArrayList<>();
                for (String str : smtpCompnt.getSmtpUsersIds()) {
                    int smtpUsrId = TypesParser.safeParseInt(str.split(Pattern.quote("|"))[0]);
                    int uid = TypesParser.safeParseInt(str.split(Pattern.quote("|"))[1]);
                    if (str.contains("|") && TypesParser.safeParseInt(smtpServersIds.get(i)) == smtpUsrId) {
                        smtpUsersIds.add(uid);
                        if ("vmtas".equalsIgnoreCase(smtpCompnt.getSplitEmailsType())){
                            serverCount += smtpUsersCount;
                        }
                    }
                }
                if (i == serversSize - 1){
                    serverCount += countRest;
                }
                execService.submit((Runnable)new SmtpDropServer((SmtpComponent)SerializationUtils.clone((Serializable)smtpCompnt), TypesParser.safeParseInt(smtpServersIds.get(i)), smtpUsersIds, dtStar, serverCount));
                dtStar += serverCount;
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            IS_ERROR_OCCURED = true;
            Loggers.error(ex);
        }
        if (smtpProcss != null){
            MtaDropHelper.finishProccess((ActiveRecord)smtpProcss, IS_ERROR_OCCURED, 1);
        }
        return new Response("Process completed successfully !", 200);
    }

    public Response proceedTest() {
        SmtpProcess smtpProcss = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            smtpProcss = new SmtpProcess(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (smtpProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }
            if (!"in progress".equalsIgnoreCase(smtpProcss.status)){
                throw new DatabaseException("This process is not in progress !");
            }
            SmtpComponent smtpCompnt = SmtpProductionHelper.parse(smtpProcss);
            if (smtpCompnt == null){
                throw new DatabaseException("Error while trying to parse this test !");
            }
            if ((smtpCompnt.getSmtpUsersIds()).length == 0){
                throw new DatabaseException("This test has no smtp users !");
            }
            smtpProcss.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            smtpProcss.progress = 0;
            smtpProcss.update();
            if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
                SmtpProcesses.PLACEHOLDERS_LIST = new ArrayList();
                for (int b = 0; b < smtpCompnt.getPlaceholders().size(); b++){
                    SmtpProcesses.PLACEHOLDERS_LIST.add(new Rotator(Arrays.asList(smtpCompnt.getPlaceholders().get(b)), ((Integer)smtpCompnt.getPlaceholdersRotations().get(b)).intValue()));
                }
            }
            HEADERS_ROTATOR = (smtpCompnt.getHeaders() != null && (smtpCompnt.getHeaders()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getHeaders()), smtpCompnt.getHeadersRotation()) : null;
            TESTEMAILS_ROTATOR = (smtpCompnt.getTestEmails() != null && (smtpCompnt.getTestEmails()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getTestEmails()), smtpCompnt.getTestRotation()) : null;
            AUTOREPLY_ROTATOR = (smtpCompnt.isAutoReplyActivated() && smtpCompnt.getAutoReplyMailboxes() != null && (smtpCompnt.getAutoReplyMailboxes()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getAutoReplyMailboxes()), smtpCompnt.getAutoReplyRotation()) : null;
            String[] smtpUsersIds = new String[]{};
            switch (smtpCompnt.getProcessType()) {
                case "test-ip":
                    smtpUsersIds = (String[])ArrayUtils.add(smtpUsersIds, smtpCompnt.getSmtpUsersIds()[(new Random()).nextInt((smtpCompnt.getSmtpUsersIds()).length)]);
                    break;
                case "test-servers":
                    List<String> tmpServers = new ArrayList<>();
                    String[] tmpVmtas = Arrays.copyOf(smtpCompnt.getSmtpUsersIds(), (smtpCompnt.getSmtpUsersIds()).length);
                    LocalRandom.shuffleArray(tmpVmtas);
                    for (String vmtasId : tmpVmtas) {
                        String serverId = vmtasId.contains("|") ? vmtasId.split(Pattern.quote("|"))[0] : "";
                        if (!"".equals(serverId) && !tmpServers.contains(serverId)) {
                            smtpUsersIds = (String[])ArrayUtils.add(smtpUsersIds, vmtasId);
                            tmpServers.add(serverId);
                        }
                    }
                    tmpVmtas = null;
                    tmpServers = null;
                    break;
            }
            if (smtpUsersIds.length > 0){
                smtpCompnt.setSmtpUsersIds(smtpUsersIds);
            }
            SMTUSRIDS_ROTATOR = (smtpCompnt.getSmtpUsersIds() != null && (smtpCompnt.getSmtpUsersIds()).length > 0) ? new Rotator(Arrays.asList(smtpCompnt.getSmtpUsersIds()), smtpCompnt.getSmtpUsersRotation()) : null;
            List<Integer> mtaServersIds = new ArrayList<>();
            HashMap<Integer,List<TestEmail>> serversTotalEmails = new HashMap<>();
            HashMap<Integer,List<Integer>> serversVmtas = new HashMap<>();
            int totalEmails = 0;
            for (String smtpUserId : smtpCompnt.getSmtpUsersIds()) {
                int serverId = smtpUserId.contains("|") ? TypesParser.safeParseInt(smtpUserId.split(Pattern.quote("|"))[0]) : 0;
                int uid = smtpUserId.contains("|") ? TypesParser.safeParseInt(smtpUserId.split(Pattern.quote("|"))[1]) : 0;
                if (serverId > 0 && uid > 0) {
                    if (!mtaServersIds.contains(serverId)){
                        mtaServersIds.add(serverId);
                    }
                    if (!serversVmtas.containsKey(serverId)){
                        serversVmtas.put(serverId, new ArrayList<>());
                    }
                    if (!serversTotalEmails.containsKey(serverId)){
                        serversTotalEmails.put(serverId,new ArrayList<>());
                    }
                    serversVmtas.get(serverId).add(uid);
                }
            }
            if ("test-emails".equalsIgnoreCase(smtpCompnt.getProcessType())) {
                totalEmails = getTotalEmailsTest(smtpCompnt, serversTotalEmails);
            } else {
                totalEmails = getTotalEmailsDrop(smtpCompnt, serversTotalEmails);
            }
            if (serversTotalEmails.isEmpty()){
                throw new DatabaseException("No emails generated !");
            }
            smtpProcss.totalEmails = totalEmails;
            smtpCompnt.setTotalEmails(totalEmails);
            smtpProcss.update();
            ExecutorService execService = Executors.newFixedThreadPool("multithread-servers".equals(smtpCompnt.getTestThreads()) ? mtaServersIds.size() : 1);
            mtaServersIds.parallelStream().filter(sui -> serversTotalEmails.containsKey(sui)).forEachOrdered(servrId -> execService.submit((Runnable)new SmtpTestServer((SmtpComponent)SerializationUtils.clone((Serializable)smtpCompnt), servrId.intValue(), (List)serversTotalEmails.get(servrId), (List)serversVmtas.get(servrId))));
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            IS_ERROR_OCCURED = true;
            Loggers.error(ex);
        }
        if (smtpProcss != null){
            MtaDropHelper.finishProccess((ActiveRecord)smtpProcss, IS_ERROR_OCCURED, 1);
        }
        return new Response("Process completed successfully !", 200);
    }

    private int getTotalEmailsDrop(SmtpComponent smtpCompnt, HashMap<Integer,List<TestEmail>> serversTotalEmails) {
        int totalEmail = 0;
        int smtpUserId = 0;
        int serverId = 0;
        int smtpUsersIdsCounter = (smtpCompnt.getSmtpUsersIds()).length;
        int rcptCounter = (smtpCompnt.getTestEmails()).length;
        int currentSmtpUsrIds = smtpUsersIdsCounter;
        if (smtpCompnt.isTestEmailsCombination()){
            currentSmtpUsrIds *= rcptCounter;
        }
        if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
            int sizePlaceHolder = smtpCompnt.getPlaceholders().size();
            for (int b = 0; b < sizePlaceHolder; b++) {
                if (((Boolean)smtpCompnt.getPlaceholdersCombinations().get(b)).booleanValue()){
                    currentSmtpUsrIds *= ((String[])smtpCompnt.getPlaceholders().get(b)).length;
                }
            }
        }
        int b2 = 0;
        int b3 = 0;
        int b4 = 0;
        for (int currnt = 0; currnt < currentSmtpUsrIds; currnt++) {
            String str2;
            b2 = (b2 >= smtpUsersIdsCounter) ? 0 : b2;
            String str1 = smtpCompnt.getSmtpUsersIds()[b2];
            b2++;
            if (smtpCompnt.isTestEmailsCombination()) {
                b3 = (b3 >= rcptCounter) ? 0 : b3;
                str2 = smtpCompnt.getTestEmails()[b3];
                b3++;
            } else {
                str2 = getCurrentTestEmail();
            }
            TestEmail tstEmail = new TestEmail();
            serverId = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[0]);
            smtpUserId = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[1]);
            if (serverId > 0 && smtpUserId > 0) {
                tstEmail.setComponentId(smtpUserId);
                tstEmail.setEmail(str2);
                if (smtpCompnt.isAutoReplyActivated()) {
                    tstEmail.setAutoReplyMailbox(getCurrentAutoReply());
                } else {
                    tstEmail.setAutoReplyMailbox("");
                }
                tstEmail.setPlaceholders(new ArrayList());
                if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()){
                    for (int b = 0; b < smtpCompnt.getPlaceholders().size(); b++) {
                        String[] arrayOfString = (String[])smtpCompnt.getPlaceholders().get(b);
                        if (arrayOfString != null && arrayOfString.length > 0){
                            if (((Boolean)smtpCompnt.getPlaceholdersCombinations().get(b)).booleanValue()) {
                                if (b4 >= arrayOfString.length){
                                    b4 = 0;
                                }
                                tstEmail.getPlaceholders().add(arrayOfString[b4]);
                                b4++;
                            } else {
                                tstEmail.getPlaceholders().add(getCurrentPlaceHolder(b));
                            }
                        }
                    }
                }
                totalEmail++;
                serversTotalEmails.get(serverId).add(tstEmail);
            }
        }
        return totalEmail;
    }

    private int getTotalEmailsTest(SmtpComponent smtpCompnt, HashMap<Integer,List<TestEmail>> serversTotalEmails) {
        int totalEmail = 0;
        int smtpUserId = 0;
        int serverId = 0;
        //int rcptCounter = test.testEmailsCombination ? test.testEmails.length : 1;
        int rcptCounter22 = smtpCompnt.isTestEmailsCombination() ? (smtpCompnt.getSmtpUsersIds()).length : 1;
        //int smtpUsersIdsCounter = (smtpCompnt.getSmtpUsersIds()).length;
        int rcptCounter = (smtpCompnt.getTestEmails()).length;
        int currentSmtpUsrIds = rcptCounter22;
        if (smtpCompnt.isTestEmailsCombination()){
            currentSmtpUsrIds *= (smtpCompnt.getTestEmails()).length;
        }
        if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
            int sizePlaceHolder = smtpCompnt.getPlaceholders().size();
            for (int b = 0; b < sizePlaceHolder; b++) {
                if (((Boolean)smtpCompnt.getPlaceholdersCombinations().get(b)).booleanValue()){
                    currentSmtpUsrIds *= ((String[])smtpCompnt.getPlaceholders().get(b)).length;
                }
            }
        }
        int b2 = 0;
        int b3 = 0;
        int b4 = 0;
        for (int currnt = 0; currnt < currentSmtpUsrIds; currnt++) {
            String str1;
            b3 = (b3 >= rcptCounter) ? 0 : b3;
            String str2 = smtpCompnt.getTestEmails()[b3];
            b3++;
            if (smtpCompnt.isTestEmailsCombination()) {
                b2 = (b2 >= rcptCounter22) ? 0 : b2;
                str1 = smtpCompnt.getSmtpUsersIds()[b2];
                b2++;
            } else {
                str1 = getCurrentSmtpUsrIds();
            }
            TestEmail tstEmail = new TestEmail();
            serverId = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[0]);
            smtpUserId = TypesParser.safeParseInt(str1.split(Pattern.quote("|"))[1]);
            if (serverId > 0 && smtpUserId > 0) {
                tstEmail.setComponentId(smtpUserId);
                tstEmail.setEmail(str2);
                if (smtpCompnt.isAutoReplyActivated()) {
                    tstEmail.setAutoReplyMailbox(getCurrentAutoReply());
                } else {
                    tstEmail.setAutoReplyMailbox("");
                }
                tstEmail.setPlaceholders(new ArrayList());
                if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()){
                    for (int b = 0; b < smtpCompnt.getPlaceholders().size(); b++) {
                        String[] arrayOfString = (String[])smtpCompnt.getPlaceholders().get(b);
                        if (arrayOfString != null && arrayOfString.length > 0){
                            if (((Boolean)smtpCompnt.getPlaceholdersCombinations().get(b)).booleanValue()) {
                                if (b4 >= arrayOfString.length){
                                    b4 = 0;
                                }
                                tstEmail.getPlaceholders().add(arrayOfString[b4]);
                                b4++;
                            } else {
                                tstEmail.getPlaceholders().add(getCurrentPlaceHolder(b));
                            }
                        }
                    }
                }
                totalEmail++;
                serversTotalEmails.get(Integer.valueOf(serverId)).add(tstEmail);
            }
        }
        return totalEmail;
    }

    public static synchronized int updateCounter() {
        return EMAILS_COUNTER++;
    }

    public static synchronized String getCurrentPlaceHolder(int current) {
        if (SmtpProcesses.PLACEHOLDERS_LIST != null && !SmtpProcesses.PLACEHOLDERS_LIST.isEmpty() && current < SmtpProcesses.PLACEHOLDERS_LIST.size() && SmtpProcesses.PLACEHOLDERS_LIST.get(current) != null) {
            String pl = (String) ((Rotator)SmtpProcesses.PLACEHOLDERS_LIST.get(current)).getCurrentValue();
            ((Rotator)SmtpProcesses.PLACEHOLDERS_LIST.get(current)).rotate();
            return pl;
        }
        return "";
    }

    public static synchronized String getCurrentHeader() {
        if (SmtpProcesses.HEADERS_ROTATOR != null) {
            String hd = (String)SmtpProcesses.HEADERS_ROTATOR.getCurrentValue();
            SmtpProcesses.HEADERS_ROTATOR.rotate();
            return hd;
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

    public static synchronized String getCurrentSmtpUsrIds() {
        if (SMTUSRIDS_ROTATOR != null) {
            String str = (String)SMTUSRIDS_ROTATOR.getCurrentValue();
            SMTUSRIDS_ROTATOR.rotate();
            return str;
        }
        return "";
    }

    public static synchronized void updateProgress(int progress) {
        MtaProcesses.TOTAL_PROGRESS += progress;
    }

    public static synchronized String getCurrentAutoReply() {
        if (AUTOREPLY_ROTATOR != null) {
            String str = (String)AUTOREPLY_ROTATOR.getCurrentValue();
            AUTOREPLY_ROTATOR.rotate();
            return str;
        }
        return "";
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
