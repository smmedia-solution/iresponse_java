package tech.iresponse.webservices;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.models.production.MtaProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.pmta.manager.LogsGrabber;
import tech.iresponse.pmta.manager.SMTPVmtaCreator;
import tech.iresponse.pmta.manager.SMTPVmtaReseter;
import tech.iresponse.pmta.manager.AutoProcessManager;
import tech.iresponse.pmta.manager.CommandsManager;
import tech.iresponse.pmta.manager.AutoProcessStopper;
import tech.iresponse.pmta.manager.ConfigApplier;
import tech.iresponse.pmta.manager.GlobalVmtaUpdater;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.http.ResponseData;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class Pmta implements Controller {

    public static volatile LinkedHashMap<String,String> SERVERS_LOGS = new LinkedHashMap<>();
    public static volatile List PMTA_LOGS = new ArrayList();

    public Response getConfig() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        ResponseData respData = null;
        SSHConnector ssh = null;

        int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
        String type = String.valueOf(app.getParameters().get("type"));
        String name = String.valueOf(app.getParameters().get("name"));

        if (serverId == 0){
            throw new DatabaseException("Server id is incorrect !");
        }

        if ("".equals(type) || "null".equals(type)){
            throw new DatabaseException("Config type is incorrect !");
        }

        if ("".equals(name) || "null".equals(name)){
            throw new DatabaseException("Config name is incorrect !");
        }

        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
            ssh = Authentification.connectToServer(mtaServ);

            if (ssh != null && ssh.isConnected()) {
                String result = ssh.read("/etc/pmta/" + type + "/" + name + ".conf");
                respData = new ResponseData("Config file fetched successfully !", result, 200);
            } else {
                throw new DatabaseException("Could not connect to this server !");
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return (Response)respData;
    }

    public Response saveConfig() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        SSHConnector ssh = null;
        int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
        String type = String.valueOf(app.getParameters().get("type"));
        String name = String.valueOf(app.getParameters().get("name"));
        String content = String.valueOf(app.getParameters().get("content"));

        if (serverId == 0){
            throw new DatabaseException("Server id is incorrect !");
        }

        if ("".equals(type) || "null".equals(type)){
            throw new DatabaseException("Config type is incorrect !");
        }

        if ("".equals(name) || "null".equals(name)){
            throw new DatabaseException("Config name is incorrect !");
        }

        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

            ssh.uploadContent(content, "/etc/pmta/" + type + "/" + name + ".conf");
            ssh.cmd(prefix + " /etc/init.d/pmta reload");
            ssh.cmd(prefix + " /etc/init.d/pmta restart");

        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return new Response("Config updated successfully !", 200);
    }

    public Response applyTemplateConfigs() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray serversIds = (app.getParameters().has("servers-ids") && app.getParameters().get("servers-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("servers-ids") : new JSONArray();
        if (serversIds.length() == 0){
            throw new DatabaseException("No servers passed to this process !");
        }

        String path = String.valueOf(app.getParameters().get("path"));
        String content = String.valueOf(app.getParameters().get("content"));
        if ("".equals(path) || "null".equals(path)){
            throw new DatabaseException("Config path is incorrect !");
        }
        if ("".equals(content) || "null".equals(content)){
            throw new DatabaseException("No content passed !");
        }

        int threadCount = (serversIds.length() > 500) ? 100 : serversIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < serversIds.length(); b++){
            execService.submit((Runnable)new ConfigApplier(serversIds.getInt(b), path, content));
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Config updated successfully !", 200);
    }

    public Response executeCommand() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray bundle = (app.getParameters().has("bundle") && app.getParameters().get("bundle") instanceof JSONArray) ? app.getParameters().getJSONArray("bundle") : new JSONArray();
        if (bundle.length() == 0){
            throw new DatabaseException("No bundle passed to this process !");
        }

        int threadCount = (bundle.length() > 500) ? 100 : bundle.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < bundle.length(); b++){
            execService.submit((Runnable)new CommandsManager(bundle.getJSONObject(b)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        if (SERVERS_LOGS.isEmpty()){
            throw new DatabaseException("No response found !");
        }

        String output = "";
        for (Map.Entry entry : SERVERS_LOGS.entrySet()) {
            String serverName = (String)entry.getKey();
            String log = (String)entry.getValue();
            output = output + "Logs for " + serverName + ": \n";
            output = output + log;
        }
        return (Response)new ResponseData("Pmta command executed successfully !", output, 200);
    }

    public Response executeAutoPauseResumeProcess() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes ids passed to this process !");
        }

        int threadCount = (processesIds.length() > 100) ? 100 : processesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (byte b = 0; b < processesIds.length(); b++){
            execService.submit((Runnable)new AutoProcessManager(processesIds.getInt(b)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Processes executed successfully !", 200);
    }

    public Response stopAutoPauseResumeProcess() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes ids passed to this process !");
        }

        int threadCount = (processesIds.length() > 100) ? 100 : processesIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < processesIds.length(); b++){
            execService.submit((Runnable)new AutoProcessStopper(processesIds.getInt(b)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Processes stopped successfully !", 200);
    }

    public Response updateGlobalVmtas() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray serversIds = (app.getParameters().has("servers-ids") && app.getParameters().get("servers-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("servers-ids") : new JSONArray();
        int ispId = app.getParameters().getInt("isp-id");
        String action = String.valueOf(app.getParameters().get("action"));
        String domain = "create".equalsIgnoreCase(action) ? String.valueOf(app.getParameters().get("domain")) : "";
        String msg = "create".equalsIgnoreCase(action) ? "Vmtas created successfully !" : "Vmtas reseted successfully !";
        String pathPmta = System.getProperty("assets.path") + "/pmta";
        String pmtaConfigType = String.valueOf(Application.getSettingsParam("pmta_config_type")).toLowerCase();
        pmtaConfigType = "".equals(pmtaConfigType) ? (pathPmta + "/configs/default") : (pathPmta + "/configs/" + pmtaConfigType);
        String vmtaTemplate = FileUtils.readFileToString(new File(pmtaConfigType + "/vmta.tpl"), "UTF-8");

        if (serversIds.length() == 0){
            throw new DatabaseException("No server passed to this process !");
        }

        int threadCount = (serversIds.length() > 100) ? 100 : serversIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < serversIds.length(); b++){
            execService.submit((Runnable)new GlobalVmtaUpdater(serversIds.getInt(b), ispId, domain, vmtaTemplate));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response(msg, 200);
    }

    public Response updateIndividualVmtas() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
        JSONArray mapping = (app.getParameters().has("mapping") && app.getParameters().get("mapping") instanceof JSONArray) ? app.getParameters().getJSONArray("mapping") : new JSONArray();
        String action = String.valueOf(app.getParameters().get("action"));
        String msg = "create".equalsIgnoreCase(action) ? "Vmtas created successfully !" : "Vmtas reseted successfully !";
        String pathPmta = System.getProperty("assets.path") + "/pmta";
        String pmtaConfigType = String.valueOf(Application.getSettingsParam("pmta_config_type")).toLowerCase();
        pmtaConfigType = "".equals(pmtaConfigType) ? (pathPmta + "/configs/default") : (pathPmta + "/configs/" + pmtaConfigType);
        String vmtaTemplate = FileUtils.readFileToString(new File(pmtaConfigType + "/vmta.tpl"), "UTF-8");

        if (serverId == 0){
            throw new DatabaseException("No server passed to this process !");
        }

        SSHConnector ssh = null;
        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
            boolean resetAction = "reset".equals(action);

            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
            JSONObject jSONObject = null;
            String usr = Application.checkUser();
            Date date = new Date(System.currentTimeMillis());

            if (resetAction) {
                List<ServerVmta> list = (List)ServerVmta.all(ServerVmta.class, "type = ? AND mta_server_id = ?", new Object[] { "Custom", Integer.valueOf(serverId) });
                if (list != null && !list.isEmpty()){
                    for (ServerVmta svmta : list) {
                        ssh.cmd("rm -rf /etc/pmta/vmtas/" + svmta.name + ".conf");
                        svmta.delete();
                    }
                }
            } else {
                if (mapping.length() == 0){
                    throw new DatabaseException("No mapping passed to this process !");
                }

                for (int b = 0; b < mapping.length(); b++) {
                    jSONObject = mapping.getJSONObject(b);
                    if (jSONObject != null) {
                        ServerVmta serverVmta = (ServerVmta)ServerVmta.first(ServerVmta.class, "ip = ? AND type = ? AND mta_server_id = ?", new Object[] { jSONObject.getString("vmta-ip"), "Default", Integer.valueOf(serverId) });
                        if (serverVmta != null) {
                            String domain = jSONObject.getString("domain").trim().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");
                            ServerVmta newVmta = new ServerVmta(Integer.valueOf(serverVmta.id));
                            newVmta.id = 0;
                            newVmta.type = "Custom";
                            newVmta.name += "_" + StringUtils.replace(domain, ".", "_");
                            newVmta.customDomain = domain;

                            String vmtaConfig = StringUtils.replace(vmtaTemplate, "$p_vmta", newVmta.name);
                            vmtaConfig = StringUtils.replace(vmtaConfig, "$p_ip", newVmta.ip);
                            vmtaConfig = StringUtils.replace(vmtaConfig, "$p_domain", domain);
                            vmtaConfig = StringUtils.replace(vmtaConfig, "$p_dkim", "");

                            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                                ssh.uploadContent(vmtaConfig, "/home/" + mtaServ.sshUsername + "/" + newVmta.name + ".conf");
                                ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/" + newVmta.name + ".conf /etc/pmta/vmtas/" + newVmta.name + ".conf");
                            } else {
                                ssh.uploadContent(vmtaConfig, "/etc/pmta/vmtas/" + newVmta.name + ".conf");
                            }

                            newVmta.createdBy = usr;
                            newVmta.createdDate = date;
                            newVmta.lastUpdatedBy = usr;
                            newVmta.lastUpdatedDate = date;
                            newVmta.insert();
                        }
                    }
                }
            }

            ssh.cmd(prefix + " /etc/init.d/pmta reload");
            ssh.cmd(prefix + " /etc/init.d/pmta restart");

        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return new Response(msg, 200);
    }

    public Response createSMTPVmtas() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray serversIds = (app.getParameters().has("servers-ids") && app.getParameters().get("servers-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("servers-ids") : new JSONArray();
        JSONArray smtpsList = (app.getParameters().has("smtps-list") && app.getParameters().get("smtps-list") instanceof JSONArray) ? app.getParameters().getJSONArray("smtps-list") : new JSONArray();
        if (serversIds.length() == 0){
            throw new DatabaseException("No server passed to this process !");
        }
        if (smtpsList.length() == 0){
            throw new DatabaseException("No smtp list to this process !");
        }

        String vmtaTemplate = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/pmta/configs/default/smtp-vmta.tpl"), "UTF-8");

        int threadCount = (serversIds.length() > 100) ? 100 : serversIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < serversIds.length(); b++){
            execService.submit((Runnable)new SMTPVmtaCreator(serversIds.getInt(b), smtpsList, vmtaTemplate, "tls"));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("SMTP Vmtas created successfully !", 200);
    }

    public Response resetSMTPVmtas() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray serversIds = (app.getParameters().has("servers-ids") && app.getParameters().get("servers-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("servers-ids") : new JSONArray();
        if (serversIds.length() == 0){
            throw new DatabaseException("No server passed to this process !");
        }

        int threadCount = (serversIds.length() > 100) ? 100 : serversIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);

        for (int b = 0; b < serversIds.length(); b++){
            execService.submit((Runnable)new SMTPVmtaReseter(serversIds.getInt(b)));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("SMTP Vmtas deleted successfully !", 200);
    }

    public Response getBounceLogs() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes passed to this process !");
        }

        String processesType = String.valueOf(app.getParameters().get("processes-type"));
        String idProcess = "";

        for (int b = 0; b < processesIds.length(); b++){
            idProcess = idProcess + String.valueOf(processesIds.get(b)) + ",";
        }

        idProcess = (idProcess.length() > 0) ? idProcess.substring(0, idProcess.length() - 1) : idProcess;
        HashSet serverId = new HashSet();

        List<MtaProcess> mtaProcss = (List)MtaProcess.all(MtaProcess.class, "id IN (" + idProcess + ")", null);
        if (mtaProcss.isEmpty()){
            throw new DatabaseException("No processes found !");
        }

        mtaProcss.forEach(paramif -> {
            String[] serverIds = paramif.serversIds.split(Pattern.quote(","));
            for (String sid : serverIds){
                serverId.add(Integer.valueOf(TypesParser.safeParseInt(sid)));
            }
        });

        ExecutorService execService = Executors.newFixedThreadPool((serverId.size() > 100) ? 100 : serverId.size());
        serverId.forEach(sid -> execService.submit((Runnable)new LogsGrabber((int)sid, processesIds, processesType)));
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        if (Pmta.PMTA_LOGS.isEmpty()){
            throw new DatabaseException("No logs found !");
        }
        return (Response)new ResponseData("Pmta logs fetched successfully !", Pmta.PMTA_LOGS, 200);
    }

    public static synchronized void updateLogs(String serverName, String log) {
        Pmta.SERVERS_LOGS.put(serverName, log);
    }

    public static synchronized void updatePmtaLogs(HashMap logs) {
        Pmta.PMTA_LOGS.add(logs);
    }

    public Response controller(String action) throws Exception {
        try {
            switch (Crypto.Base64Encode(action)){
                case "Z2V0Q29uZmln":  //getConfig
                    return getConfig();
                case "c2F2ZUNvbmZpZw==":  //saveConfig
                    return saveConfig();
                case "YXBwbHlUZW1wbGF0ZUNvbmZpZ3M=":  //applyTemplateConfigs
                    return applyTemplateConfigs();
                case "ZXhlY3V0ZUNvbW1hbmQ=":  //executeCommand
                    return executeCommand();
                case "ZXhlY3V0ZUF1dG9QYXVzZVJlc3VtZVByb2Nlc3M=":  //executeAutoPauseResumeProcess
                    return executeAutoPauseResumeProcess();
                case "c3RvcEF1dG9QYXVzZVJlc3VtZVByb2Nlc3M=":  //stopAutoPauseResumeProcess
                    return stopAutoPauseResumeProcess();
                case "dXBkYXRlR2xvYmFsVm10YXM=":  //updateGlobalVmtas
                    return updateGlobalVmtas();
                case "dXBkYXRlSW5kaXZpZHVhbFZtdGFz":  //updateIndividualVmtas
                    return updateIndividualVmtas();
                case "Y3JlYXRlU01UUFZtdGFz":  //createSMTPVmtas
                    return createSMTPVmtas();
                case "cmVzZXRTTVRQVm10YXM=":  //resetSMTPVmtas
                    return resetSMTPVmtas();
                case "Z2V0Qm91bmNlTG9ncw==":  //getBounceLogs
                    return getBounceLogs();

            }
        }catch (Exception ex){
            throw new DatabaseException("Action not found !" + ex.getMessage());
        }

        return null;
    }
}
