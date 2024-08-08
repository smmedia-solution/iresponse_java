package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.production.component.Rotator;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class SMTPVmtaCreator extends Thread {

    private int serverId;
    private JSONArray smtpsList;
    private String vmtaConfigTemplate;
    private String encryption;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
            List vmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Default" });
            if (vmta == null || vmta.isEmpty()){
                throw new DatabaseException("No vmtas found !");
            }

            String usrId = Application.checkUser();
            Rotator rotate = new Rotator(vmta, 1);
            Date date = new Date(System.currentTimeMillis());

            for (int b = 0; b < this.smtpsList.length(); b++) {
                JSONObject jSONObject = this.smtpsList.getJSONObject(b);
                ServerVmta servVmta1 = (ServerVmta)rotate.getCurrentThenRotate();
                ServerVmta servVmta2 = new ServerVmta();

                if (jSONObject != null && jSONObject.length() >= 4 && jSONObject.has("host")) {
                    String str3 = jSONObject.getString("username").replaceAll(Pattern.quote("."), "_");
                    str3 = str3.contains("@") ? str3.split(Pattern.quote("@"))[0] : str3;
                    servVmta2.mtaServerId = mtaServ.id;
                    servVmta2.mtaServerName = mtaServ.name;
                    servVmta2.type = "SMTP";
                    servVmta2.status = "Activated";
                    servVmta2.pingStatus = servVmta1.pingStatus;
                    servVmta2.name = servVmta1.name + "_smtp_" + str3 + "_" + jSONObject.getString("host").replaceAll(Pattern.quote("."), "_");
                    servVmta2.ip = servVmta1.ip;
                    servVmta2.domain = servVmta1.domain;
                    servVmta2.customDomain = servVmta2.name;
                    servVmta2.createdBy = usrId;
                    servVmta2.createdDate = date;
                    servVmta2.lastUpdatedBy = usrId;
                    servVmta2.lastUpdatedDate = date;
                    servVmta2.smtp_1 = jSONObject.getString("username");


                    String str4 = StringUtils.replace(this.vmtaConfigTemplate, "$p_vmta", servVmta2.name);
                    str4 = StringUtils.replace(str4, "$p_ip", servVmta2.ip);
                    str4 = StringUtils.replace(str4, "$p_host", jSONObject.getString("host"));
                    str4 = StringUtils.replace(str4, "$p_port", jSONObject.getString("port"));
                    if(jSONObject.length() == 5) {
                        servVmta2.smtp_2 = jSONObject.getString("username_2");
                        str4 = StringUtils.replace(str4, "$p_username_2", jSONObject.getString("username_2"));
                    }
                    str4 = StringUtils.replace(str4, "$p_username", jSONObject.getString("username"));
                    str4 = StringUtils.replace(str4, "$p_password", jSONObject.getString("password"));

                    if ("tls".equalsIgnoreCase(this.encryption)) {
                        str4 = StringUtils.replace(str4, "$p_enc", "use-starttls yes\n\trequire-starttls yes\n\tuse-unencrypted-plain-auth yes");
                    } else if ("ssl".equalsIgnoreCase(this.encryption)) {
                        str4 = StringUtils.replace(str4, "$p_enc", "use-starttls yes\nrequire-starttls yes\nuse-unencrypted-plain-auth no");
                    } else {
                        str4 = StringUtils.replace(str4, "$p_enc", "");
                    }

                    if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                        ssh.uploadContent(str4, "/home/" + mtaServ.sshUsername + "/" + servVmta2.name + ".conf");
                        ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/" + servVmta2.name + ".conf /etc/pmta/vmtas/" + servVmta2.name + ".conf");
                    } else {
                        ssh.uploadContent(str4, "/etc/pmta/vmtas/" + servVmta2.name + ".conf");
                    }

                    servVmta2.insert();
                }
            }

            ssh.cmd(prefix + " /etc/init.d/pmta reload");
            ssh.cmd(prefix + " /etc/init.d/pmta restart");

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"serverId", "smtpsList", "vmtaConfigTemplate", "encryption"})
    public SMTPVmtaCreator(int serverId, JSONArray smtpsList, String vmtaConfigTemplate, String encryption) {
        this.serverId = serverId;
        this.smtpsList = smtpsList;
        this.vmtaConfigTemplate = vmtaConfigTemplate;
        this.encryption = encryption;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SMTPVmtaCreator))
            return false;
        SMTPVmtaCreator case1 = (SMTPVmtaCreator)paramObject;
        if (!case1.exists(this))
            return false;
        if (getServerId() != case1.getServerId())
            return false;
        JSONArray jSONArray1 = getSmtpsList();
        JSONArray jSONArray2 = case1.getSmtpsList();
        if ((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2))
            return false;
        String str1 = getVmtaConfigTemplate();
        String str2 = case1.getVmtaConfigTemplate();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getEncryption();
        String str4 = case1.getEncryption();
            return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SMTPVmtaCreator;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        JSONArray jSONArray = getSmtpsList();
        n = n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
        String str1 = getVmtaConfigTemplate();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getEncryption();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public JSONArray getSmtpsList() {
        return smtpsList;
    }

    public void setSmtpsList(JSONArray smtpsList) {
        this.smtpsList = smtpsList;
    }

    public String getVmtaConfigTemplate() {
        return vmtaConfigTemplate;
    }

    public void setVmtaConfigTemplate(String vmtaConfigTemplate) {
        this.vmtaConfigTemplate = vmtaConfigTemplate;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    @Override
    public String toString() {
        return "SMTPVmtaCreator(serverId=" + getServerId() + ", smtpsList=" + getSmtpsList() + ", vmtaConfigTemplate=" + getVmtaConfigTemplate() + ", encryption=" + getEncryption() + ")";
    }
}
