package tech.iresponse.webservices;

import inet.ipaddr.IPAddressString;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.mail.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Matcher;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.http.ResponseData;
import tech.iresponse.core.Application;
import tech.iresponse.helpers.smtp.SmtpAccount;
import tech.iresponse.helpers.services.ProxiesInstaller;
import tech.iresponse.helpers.services.ServerChecker;
import tech.iresponse.helpers.services.ProxiesUninstaller;
import tech.iresponse.helpers.services.ServersCommands;
import tech.iresponse.helpers.services.SmtpChecker;
import tech.iresponse.helpers.scripts.InstallationServices;
import tech.iresponse.helpers.scripts.SMTPauth;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.logging.Loggers;

public class Servers implements Controller {

    public static volatile LinkedHashMap<String,String> SERVERS_LOGS = new LinkedHashMap<>();
    public static volatile List<SmtpAccount> WORKING_SMTP_ACCOUNTS = new ArrayList<>();

    public Response checkServer() throws Exception {

        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        Response rsponse = null;
        SSHConnector ssh = null;
        int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
        String serverType = String.valueOf(app.getParameters().get("server-type"));

        if (serverId == 0) {
            throw new DatabaseException("Server id is incorrect !");
        }

        if ("".equals(serverType) || "null".equals(serverType)) {
            throw new DatabaseException("Server type is incorrect !");
        }

        try {
            Session session;
            switch (serverType) {
                case "mta": {
                    MtaServer mtaserver = new MtaServer(Integer.valueOf(serverId));
                    ssh = Authentification.connectToServer(mtaserver);
                    if (ssh != null && ssh.isConnected()) {
                        rsponse = new Response(mtaserver.name + " is connected !", 200);
                        ssh.disconnect();
                        break;
                    }
                    throw new DatabaseException("Could not connect to this server !");
                }
                case "management": {
                    ManagementServer manageserver = new ManagementServer(Integer.valueOf(serverId));
                    ssh = Authentification.connectToServer(manageserver);
                    if (ssh != null && ssh.isConnected()) {
                        rsponse = new Response(manageserver.name + " is connected !", 200);
                        ssh.disconnect();
                        break;
                    }
                    throw new DatabaseException("Could not connect to this server !");
                }
                case "smtp": {
                    int smtpUserId = TypesParser.safeParseInt(app.getParameters().get("smtp-user-id"));
                    SmtpServer smtpServ = new SmtpServer(Integer.valueOf(serverId));
                    SmtpUser smtpUsr = new SmtpUser(Integer.valueOf(smtpUserId));
                    session = SMTPauth.connect(smtpServ, smtpUsr);
                    if (session != null) {
                        rsponse = new Response("Authentication succeed !", 200);
                        break;
                    }
                    throw new DatabaseException("Could not authenticate to this smtp !");
                }
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return rsponse;
    }

    public Response smtpBulkCheck() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray accounts = (app.getParameters().has("accounts") && app.getParameters().get("accounts") instanceof JSONArray) ? app.getParameters().getJSONArray("accounts") : new JSONArray();
        if (accounts == null || accounts.length() == 0){
            throw new DatabaseException("No accounts passed !");
        }

        ExecutorService execService = Executors.newFixedThreadPool(100);
        SmtpAccount smtpAcc = null;
        JSONObject parameters = null;

        for (int b = 0; b < accounts.length(); b++) {
            parameters = accounts.getJSONObject(b);
            smtpAcc = new SmtpAccount();
            smtpAcc.setHost(parameters.getString("host"));
            smtpAcc.setPort(parameters.getString("port"));
            smtpAcc.setEncryption(parameters.getString("encryption"));
            smtpAcc.setUsername(parameters.getString("username"));
            smtpAcc.setPassword(parameters.getString("password"));
            if (parameters.has("proxy-host")) {
                smtpAcc.setProxyHost(parameters.getString("proxy-host"));
                smtpAcc.setProxyPort(parameters.getString("proxy-port"));
            }
            execService.submit((Runnable)new SmtpChecker(smtpAcc));
        }

        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        if (WORKING_SMTP_ACCOUNTS.isEmpty()) {
            return new Response("All smtp accounts are not working !", 500);
        }

        ArrayList listAccount = new ArrayList(WORKING_SMTP_ACCOUNTS.size());

        WORKING_SMTP_ACCOUNTS.stream().map(paramdo -> {
            HashMap<String, String> mapValue = new HashMap<>(7);
            mapValue.put("host", paramdo.getHost());
            mapValue.put("port", paramdo.getPort());
            mapValue.put("encryption", paramdo.getEncryption());
            mapValue.put("username", paramdo.getUsername());
            mapValue.put("password", paramdo.getPassword());
            mapValue.put("proxyHost", paramdo.getProxyHost());
            mapValue.put("proxyPort", paramdo.getProxyPort());
            return mapValue;
        }).forEachOrdered(paramHashMap -> listAccount.add(paramHashMap));

        return (Response)new ResponseData("Process completed successfully !", listAccount, 200);
    }

    public Response getServerInfo() throws Exception {

        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
        if (serverId == 0) {
            throw new DatabaseException("Server id is incorrect !");
        }

        MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
        if (mtaServ.getEmpty()){
            throw new DatabaseException("Server not found !");
        }

        SSHConnector ssh = null;
        LinkedHashMap<Object, Object> serverInfo = new LinkedHashMap<>();
        try {
            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to " + mtaServ.name + " !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo ";
            serverInfo.put("bits", "64bits");

            String release = String.valueOf(ssh.cmd("awk -F= '/^PRETTY_NAME=/{print $2}' /etc/os-release")).replaceAll("\n", "");
            release = release.toLowerCase().replaceAll("\"", "");

            serverInfo.put("version", release);

            serverInfo.put("ips-v4", ssh.cmd(prefix + "ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/'"));
            String[] ipV6 = ssh.cmd(prefix + "ip addr show | grep 'inet6' | grep -i 'global' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/' | awk '{ print $1}'").split("\n");

            for (int b2 = 0; b2 < ipV6.length; b2++){
                ipV6[b2] = ipV6[b2].replaceAll("\n", "").replaceAll("\r", "");
            }

            if (ipV6.length == 1 && "".equals(ipV6[0].trim())) {
                serverInfo.put("ips-v6", "");
            } else {
                if (ipV6.length > 0){
                    for (int i = 0; i < ipV6.length; i++) {
                        if (ipV6[i] != null && !"".equals(ipV6[i].trim()) && ipV6[i].contains(":")){
                            ipV6[i] = (new IPAddressString((new IPAddressString(ipV6[i])).toAddress().toFullString())).toAddress().toCompressedString();
                        }
                    }
                }
                serverInfo.put("ips-v6", String.join("\n", (CharSequence[])ipV6));
            }
            serverInfo.put("server-ram", ssh.cmd(prefix + "free -m | grep Mem: | cut -f2 | awk '{ print $2}'"));
            serverInfo.put("server-storage", ssh.cmd(prefix + "df -h | grep '/dev/' | cut -f2 | awk '{ print $2}'"));

        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return (Response)new ResponseData("Server info fetched successfully !", serverInfo, 200);
    }

    public Response executeCommand() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray serversIds = app.getParameters().getJSONArray("servers-ids");
        if (serversIds.length() == 0){
            throw new DatabaseException("No servers passed !");
        }

        String cmd = app.getParameters().getString("command");
        ExecutorService execServ = Executors.newFixedThreadPool((serversIds.length() > 100) ? 100 : serversIds.length());
        for (int b = 0; b < serversIds.length(); b++){
            execServ.submit((Runnable)new ServersCommands(TypesParser.safeParseInt(serversIds.getString(b)), cmd));
        }
        execServ.shutdown();

        if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
            execServ.shutdownNow();
        }
        return (Response)(SERVERS_LOGS.isEmpty() ? new Response("Error while trying to execute commands !", 500) : new ResponseData("Process completed successfully !", SERVERS_LOGS, 200));
    }

    public Response installProxy() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONObject parameters = app.getParameters();
        JSONArray serversIds = (app.getParameters().has("servers-ids") && app.getParameters().get("servers-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("servers-ids") : new JSONArray();
        if (serversIds == null || serversIds.length() == 0){
            throw new DatabaseException("No servers passed !");
        }

        ExecutorService execServ = Executors.newFixedThreadPool((serversIds.length() > 100) ? 100 : serversIds.length());
        for (int b = 0; b < serversIds.length(); b++){
            execServ.submit((Runnable)new ProxiesInstaller(serversIds.getInt(b), String.valueOf(parameters.get("username")), String.valueOf(parameters.get("password")), String.valueOf(parameters.get("proxy-port")), String.valueOf(parameters.get("socks-port"))));
        }
        execServ.shutdown();
        if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
            execServ.shutdownNow();
        }
        return new Response("Proxies have been installed successfully !", 200);
    }

    public Response uninstallProxies() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray proxiesIds = (app.getParameters().has("proxies-ids") && app.getParameters().get("proxies-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("proxies-ids") : new JSONArray();
        if (proxiesIds == null || proxiesIds.length() == 0){
            throw new DatabaseException("No mailboxes passed !");
        }

        ExecutorService execServ = Executors.newFixedThreadPool((proxiesIds.length() > 100) ? 100 : proxiesIds.length());
        for (int i = 0; i < proxiesIds.length(); i++){
            execServ.submit((Runnable)new ProxiesUninstaller(TypesParser.safeParseInt(proxiesIds.get(i))));
        }
        execServ.shutdown();

        if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
            execServ.shutdownNow();
        }
        return new Response("Proxy servers uninstalled successfully !", 200);
    }

    public Response installServer() {
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }
            JSONObject parameters = app.getParameters();
            int serverId = TypesParser.safeParseInt(parameters.get("server-id"));
            if (serverId == 0) {
                throw new DatabaseException("Server id is incorrect !");
            }
            MtaServer mtaserver = new MtaServer(Integer.valueOf(serverId));
            if (mtaserver.getEmpty()) {
                throw new DatabaseException("Server not found !");
            }
            if (!parameters.has("mapping") || !(parameters.get("mapping") instanceof JSONArray)) {
                throw new DatabaseException("Server mapping not found !");
            }
            JSONArray mapping = parameters.getJSONArray("mapping");
            SSHConnector ssh = null;

            try {
                FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Connecting to : " + mtaserver.name + " ......", "utf-8");
                ssh = Authentification.connectToServer(mtaserver);

                if (ssh != null && ssh.isConnected()) {
                    boolean installServices = (parameters.has("install-services") && "enabled".equalsIgnoreCase(parameters.getString("install-services").trim())) ? true : false;
                    boolean updatePort = (parameters.has("update-port") && "enabled".equalsIgnoreCase(parameters.getString("update-port").trim())) ? true : false;
                    boolean updateFirewall = (parameters.has("install-firewall") && "enabled".equalsIgnoreCase(parameters.getString("install-firewall").trim())) ? true : false;
                    boolean updatePassword = (parameters.has("update-password") && "enabled".equalsIgnoreCase(parameters.getString("update-password").trim())) ? true : false;
                    boolean updateIps = (parameters.has("update-ips") && "enabled".equalsIgnoreCase(parameters.getString("update-ips").trim())) ? true : false;
                    boolean activateDmarc = (parameters.has("activate-dmarc") && "enabled".equalsIgnoreCase(parameters.getString("activate-dmarc").trim())) ? true : false;
                    boolean activateDkim = (parameters.has("activate-dkim") && "enabled".equalsIgnoreCase(parameters.getString("activate-dkim").trim())) ? true : false;
                    boolean keepOldSubs = (parameters.has("keep-old-subs") && "enabled".equalsIgnoreCase(parameters.getString("keep-old-subs").trim())) ? true : false;
                    boolean usePredefinedSubs = (parameters.has("use-predefined-subs") && "enabled".equalsIgnoreCase(parameters.getString("use-predefined-subs").trim())) ? true : false;
                    boolean installTracking = (parameters.has("install-tracking") && "enabled".equalsIgnoreCase(parameters.getString("install-tracking").trim())) ? true : false;
                    boolean useBrands = (parameters.has("use-brands") && "enabled".equalsIgnoreCase(parameters.getString("use-brands").trim())) ? true : false;

                    boolean useSsl = (parameters.has("use-ssl") && "enabled".equalsIgnoreCase(parameters.getString("use-ssl").trim())) ? true : false;
                    boolean installPmta = (parameters.has("install-pmta") && "enabled".equalsIgnoreCase(parameters.getString("install-pmta").trim())) ? true : false;
                    int pmtaVersion = (parameters.has("pmta-version") && "45".equalsIgnoreCase(parameters.getString("pmta-version").trim())) ? 45 : 40;

                    String prefix = "root".equals(mtaserver.sshUsername) ? "" : "sudo ";
                    String release = String.valueOf(ssh.cmd("awk -F= '/^PRETTY_NAME=/{print $2}' /etc/os-release")).replaceAll("\n", "");
                    boolean isUbuntu = release.toLowerCase().contains("ubuntu");

                    if (installServices) {
                        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Installing / re-installing Fondamentals ......", "utf-8");
                        if (isUbuntu) {
                            InstallationServices.installServicesUbuntu(ssh, mtaserver, prefix, false);
                        } else {
                            InstallationServices.installServices(ssh, mtaserver, prefix, 7, false);
                        }
                    }

                    if (updateFirewall) {
                        String str = Authentification.getInfo(Crypto.Base64Decode("VU53aDhnMU1ZdEI0SWhuOXFlQVgvVFRaaGJVR2lOUUM4UjRWU0RLTFBhTVlmYnZnQUJONEQ5cmszR2hpUk1xRw=="), null);
                        if(isUbuntu) {
                            ssh.cmd(prefix + "ufw disable");
                            ssh.cmd(prefix + "systemctl stop ufw");
                            ssh.cmd(prefix + "systemctl disable ufw");
                            ssh.shellCommand(prefix + "apt-get install -y iptables");
                        } else {
                            ssh.cmd(prefix + "systemctl stop firewalld && " + prefix + "systemctl disable firewalld");
                            ssh.shellCommand(prefix + "yum install -y iptables-services");
                        }

                        ssh.cmd("iptables -F");
                        ssh.cmd("iptables -A INPUT -p tcp -s " + str + " --dport " + mtaserver.sshPort + " -j ACCEPT");
                        String[] firewallIpsDomain = String.valueOf(Application.getSettingsParam("pmta_firewall_ips_domains")).split("\n");
                        if (firewallIpsDomain != null && firewallIpsDomain.length > 0){
                            for (String str3 : firewallIpsDomain) {
                                str3 = str3.replaceAll("\r", "").replaceAll("\n", "").trim();
                                if (!"".equals(str3)){
                                    System.out.println("iptables -A INPUT -m state --state NEW -m tcp -p tcp -s " + str3 + " --dport " + String.valueOf(Application.getSettingsParam("pmta_http_port")) + " -j ACCEPT");
                                }
                            }
                        }
                        ssh.cmd("iptables -A INPUT -p tcp -m tcp --dport " + String.valueOf(Application.getSettingsParam("pmta_http_port")) + " -j ACCEPT");
                        ssh.cmd("iptables -A INPUT -p tcp -m tcp --dport 80 -j ACCEPT");
                        ssh.cmd("iptables -A INPUT -p tcp -m tcp --dport 443 -j ACCEPT");
                        ssh.cmd("iptables -P INPUT DROP");
                        ssh.cmd("iptables -P FORWARD DROP");
                        ssh.cmd("iptables -P OUTPUT ACCEPT");
                        ssh.cmd("iptables -A INPUT -i lo -j ACCEPT");
                        ssh.cmd("iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT");
                        ssh.cmd("iptables-save");
                        ssh.cmd("iptables -L -v -n");
                        ssh.cmd(prefix + "systemctl enable iptables");
                        ssh.cmd(prefix + "systemctl start iptables");

                    } else {
                        if(isUbuntu) {
                            ssh.cmd(prefix + "ufw disable");
                            ssh.cmd(prefix + "systemctl stop ufw");
                            ssh.cmd(prefix + "systemctl disable ufw");
                        } else {
                            ssh.cmd(prefix + "systemctl stop firewalld && " + prefix + "systemctl disable firewalld");
                        }
                    }

                    if (updateIps) {
                        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Updating ips ( database update & DNS records update ) ......", "utf-8");
                        if (isUbuntu) {
                            InstallationServices.installDkimDmarcUbuntu(ssh, mtaserver, prefix, mapping, keepOldSubs, usePredefinedSubs, activateDmarc, activateDkim);
                        } else {
                            InstallationServices.installDkimDmarc(ssh, mtaserver, prefix, mapping, keepOldSubs, usePredefinedSubs, activateDmarc, activateDkim);
                        }
                        mtaserver.setDkimInstalled(activateDkim);
                        mtaserver.setDmarcInstalled(activateDmarc);
                        mtaserver.update();
                    }

                    if (installTracking) {
                        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Installing / re-installing Tracking System ......", "utf-8");
                        if (isUbuntu) {
                            InstallationServices.installTrackingUbuntu(ssh, mtaserver, prefix, useBrands, useSsl);
                        } else {
                            InstallationServices.installTracking(ssh, mtaserver, prefix, useBrands, useSsl);
                        }
                    }

                    if (installPmta) {
                        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Installing / re-installing PowerMTA ......", "utf-8");
                        if (isUbuntu) {
                            InstallationServices.installPmta45Ubuntu(ssh, mtaserver, prefix);
                        } else {
                            if (pmtaVersion == 40) {
                                InstallationServices.installPmta(ssh, mtaserver, prefix);
                            }
                            if (pmtaVersion == 45) {
                                InstallationServices.installPmta45(ssh, mtaserver, prefix);
                            }
                        }
                     }

                    mtaserver.setInstalled(true);
                    mtaserver.update();
                    FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Installation completed !", "utf-8");
                }
            } catch (Exception exception) {
                FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + serverId + "_proc.log"), "Installation interrupted !", "utf-8");
                System.out.print("<version>");
                System.out.print(String.valueOf(exception.getMessage()).replace("\n", "").replace("\r", ""));
                System.out.print("</version>");
                throw exception;
            } finally {
                if (ssh != null && ssh.isConnected()){
                    ssh.disconnect();
                }
            }
        }catch (Exception ex){
            Loggers.error(ex);
        }
        return null;
    }

    public Response checkServersConnectivity() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        List<MtaServer> listServ = (List)MtaServer.all(MtaServer.class, "status = ?", new Object[] { "Activated" });
        if (listServ != null && !listServ.isEmpty()) {
            ExecutorService execServ = Executors.newFixedThreadPool(100);
            listServ.forEach(server -> execServ.submit((Runnable)new ServerChecker(server)));
            execServ.shutdown();

            if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
                execServ.shutdownNow();
            }
        }
        return new Response("All server have been checked successfully ! ", 200);
    }

    public Response configureAdditionalIps() throws Exception {
        SSHConnector ssh = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
            if (serverId == 0){
                throw new DatabaseException("Server id is incorrect !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            JSONArray ipsLines = (app.getParameters().has("lines") && app.getParameters().get("lines") instanceof JSONArray) ? app.getParameters().getJSONArray("lines") : new JSONArray();
            if (ipsLines == null || ipsLines.length() == 0){
                throw new DatabaseException("Ips not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to " + mtaServ.name + " !");
            }

            String pathIps = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/ifcfg.tpl"), "utf-8");
            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo ";
            ssh.cmd(prefix + "yum install -y net-tools");

            List<String> listIps = Arrays.asList(String.valueOf(ssh.cmd(prefix + "ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/'")).split("\n"));
            String[] interfaceConf = String.valueOf(ssh.cmd(prefix + "ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $NF}'")).split("\n");
            if (interfaceConf == null || interfaceConf.length == 0){
                throw new DatabaseException("No interfaces configured in this server !");
            }

            int b1 = 0;
            String eth = "eth0";
            boolean bool1 = false;
            for (String intface : interfaceConf) {
                if (intface != null) {
                    intface = intface.replaceAll("\r", "").replaceAll("\n", "");
                    if (intface != null && intface.contains(":")) {
                        eth = intface.split(Pattern.quote(":"))[0];
                        int j = TypesParser.safeParseInt(intface.split(Pattern.quote(":"))[1]);
                        b1 = (j > b1) ? j : b1;
                        bool1 = true;
                    } else {
                        eth = intface;
                        b1 = 0;
                    }
                }
            }

            String str4 = StringUtils.replace(StringUtils.replace(String.valueOf(ssh.cmd("cat /etc/sysconfig/network-scripts/ifcfg-" + eth + " | grep 'GATEWAY' | grep 'GATEWAY' | cut -f2 -d '=' | awk '{ print $1}'")), "\"", ""), "\n", "");
            b1 = (b1 > 0 || bool1) ? (b1 + 1) : b1;
            boolean bool2 = false;
            String str5 = (!bool1 && b1 > 1) ? "" : ":";
            ArrayList<String> arrayList = new ArrayList(ipsLines.length());
            for (int b2 = 0; b2 < ipsLines.length(); b2++) {
                String[] arrayOfString1 = ipsLines.getString(b2).split(Pattern.quote("|"));
                if (arrayOfString1.length >= 2) {
                    String str6 = arrayOfString1[0].replaceAll("\r", "").replaceAll("\n", "");
                    String str7 = arrayOfString1[1].replaceAll("\r", "").replaceAll("\n", "");
                    if (Matcher.pat2(str6) && !listIps.contains(str6) && Matcher.pat2(str7)) {
                        String str9 = StringUtils.replace(pathIps, "$p_device", eth + str5 + b1);
                        str9 = StringUtils.replace(str9, "$p_ip", str6);
                        str9 = StringUtils.replace(str9, "$p_netmask", str7);
                        String str8 = (arrayOfString1.length > 2 && Matcher.pat2(arrayOfString1[2].replaceAll("\r", "").replaceAll("\n", ""))) ? arrayOfString1[2].replaceAll("\r", "").replaceAll("\n", "").trim() : str4;
                        str9 = StringUtils.replace(str9, "$p_gateway", str8);
                        if (ssh.uploadContent(str9, "/etc/sysconfig/network-scripts/ifcfg-" + eth + str5 + b1)) {
                            arrayList.add(prefix + "ifconfig " + eth + str5 + b1 + " " + str6 + " up");
                            bool2 = true;
                            b1++;
                        }
                    }
                }
            }

            if (bool2) {
                ssh.cmd(prefix + "/etc/init.d/network restart");
                if (!arrayList.isEmpty()){
                    for (String str : arrayList){
                        ssh.cmd(str);
                    }
                }
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return new Response("Add Additional Ips have been successfully !", 200);
    }

    public Response configureAdditionalRanges() throws Exception {
        SSHConnector ssh = null;
        try {

            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            int serverId = TypesParser.safeParseInt(app.getParameters().get("server-id"));
            if (serverId == 0){
                throw new DatabaseException("Server id is incorrect !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            JSONArray lines = (app.getParameters().has("lines") && app.getParameters().get("lines") instanceof JSONArray) ? app.getParameters().getJSONArray("lines") : new JSONArray();
            if (lines == null || lines.length() == 0){
                throw new DatabaseException("Ranges list not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to " + mtaServ.name + " !");
            }

            String tplRange = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/ifcfg_range.tpl"), "utf-8");
            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo ";
            String[] interfaceConf = String.valueOf(ssh.cmd(prefix + "ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $NF}'")).split("\n");
            if (interfaceConf == null || interfaceConf.length == 0){
                throw new DatabaseException("No interfaces configured in this server !");
            }

            int b1 = 0;
            int b2 = 0;
            String device = interfaceConf[interfaceConf.length - 1].contains(":") ? interfaceConf[interfaceConf.length - 1].split(Pattern.quote(":"))[0] : interfaceConf[interfaceConf.length - 1];
            String[] arrayOfString2 = null;
            for (int b3 = 0; b3 < lines.length(); b3++) {
                arrayOfString2 = lines.getString(b3).contains("|") ? lines.getString(b3).split(Pattern.quote("|")) : null;
                if (arrayOfString2 != null && arrayOfString2.length == 2) {
                    String template = StringUtils.replace(tplRange, "$p_device", device);
                    template = StringUtils.replace(template, "$p_start_ip", arrayOfString2[0]);
                    template = StringUtils.replace(template, "$p_end_ip", arrayOfString2[1]);
                    template = StringUtils.replace(template, "$p_col_start", String.valueOf(b1));
                    if (ssh.uploadContent(template, "/etc/sysconfig/network-scripts/ifcfg-" + device + "-range" + b2)) {
                        ssh.cmd(prefix + "/etc/init.d/network restart");
                        b2++;
                    }

                    interfaceConf = String.valueOf(ssh.cmd(prefix + "ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $NF}'")).split("\n");
                    if (interfaceConf == null || interfaceConf.length == 0){
                        throw new DatabaseException("No interfaces configured in this server !");
                    }
                    for (String intface : interfaceConf) {
                        if (intface != null && intface.contains(":")) {
                            device = intface.split(Pattern.quote(":"))[0];
                            int j = TypesParser.safeParseInt(intface.split(Pattern.quote(":"))[1]);
                            b1 = (j > b1) ? j : b1;
                        }
                    }
                    b1++;
                }
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return new Response("Add Additional Ips have been successfully !", 200);
    }

    public static synchronized void updateLogs(String serverName, String log) {
        Servers.SERVERS_LOGS.put(serverName, log);
    }

    public static synchronized void updateSmtps(SmtpAccount smtp) {
        Servers.WORKING_SMTP_ACCOUNTS.add(smtp);
    }

    public Response controller(String action) {
        try {
            switch (Crypto.Base64Encode(action)){
                case "Y2hlY2tTZXJ2ZXI=": //checkServer
                    return checkServer();
                case "c210cEJ1bGtDaGVjaw==": //smtpBulkCheck
                    return smtpBulkCheck() ;
                case "Z2V0U2VydmVySW5mbw==": //getServerInfo
                    return getServerInfo() ;
                case "ZXhlY3V0ZUNvbW1hbmQ=": //executeCommand
                    return executeCommand();
                case "aW5zdGFsbFByb3h5": //installProxy
                    return installProxy();
                case "dW5pbnN0YWxsUHJveGllcw==": //uninstallProxies
                    return uninstallProxies() ;
                case "aW5zdGFsbFNlcnZlcg==": //installServer
                    return installServer();
                case "Y2hlY2tTZXJ2ZXJzQ29ubmVjdGl2aXR5": //checkServersConnectivity
                    return checkServersConnectivity();
                case "Y29uZmlndXJlQWRkaXRpb25hbElwcw==": //configureAdditionalIps
                    return configureAdditionalIps();
                case "Y29uZmlndXJlQWRkaXRpb25hbFJhbmdlcw==": //configureAdditionalRanges
                    return configureAdditionalRanges();
            }
        }catch (Exception ex){
            new DatabaseException("Action not found !");
        }
        return null;
    }
}