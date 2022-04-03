package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Isp;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class GlobalVmtaUpdater extends Thread {

    private int serverId;
    private int ispId;
    private String domain;
    private String vmtaConfigTemplate;

    @Override
    public void run() {
        SSHConnector ssh = null;

        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            boolean foundDomain = "".equals(this.domain);
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
            String usr = Application.checkUser();

            if (foundDomain) {
                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND isp_id = ? AND type = ? AND created_by = ?", new Object[] { Integer.valueOf(mtaServ.id), Integer.valueOf(this.ispId), "Custom", usr });
                if (vmtas != null && !vmtas.isEmpty()){
                    for (ServerVmta vmta : vmtas) {
                        ssh.cmd(prefix + " rm -rf /etc/pmta/vmtas/" + vmta.name + ".conf");
                        vmta.delete();
                    }
                }
            } else {
                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Default" });
                if (vmtas == null || vmtas.isEmpty()){
                    throw new DatabaseException("No vmtas found !");
                }

                Isp isp = new Isp(Integer.valueOf(this.ispId));
                if (isp.getEmpty()){
                    throw new DatabaseException("ISP not found !");
                }

                Date date = new Date(System.currentTimeMillis());
                for (ServerVmta oldVmtas : vmtas) {
                    boolean found;
                    ServerVmta newVmtas = (ServerVmta)ServerVmta.first(ServerVmta.class, "name = ?", new Object[] { oldVmtas.name + "_" + this.ispId + "_" + this.domain.replaceAll(Pattern.quote("."), "_") });
                    if (newVmtas != null && !newVmtas.getEmpty()) {
                        found = true;
                    } else {
                        newVmtas = new ServerVmta();
                        found = false;
                    }

                    newVmtas.mtaServerId = mtaServ.id;
                    newVmtas.mtaServerName = mtaServ.name;
                    newVmtas.type = "Custom";
                    newVmtas.status = "Activated";
                    newVmtas.pingStatus = oldVmtas.pingStatus;
                    oldVmtas.name += "_" + this.ispId + "_" + this.domain.replaceAll(Pattern.quote("."), "_");
                    newVmtas.ip = oldVmtas.ip;
                    newVmtas.ispId = isp.id;
                    newVmtas.ispName = isp.name;
                    newVmtas.domain = oldVmtas.domain;
                    newVmtas.ispName = isp.name;
                    newVmtas.customDomain = this.domain;
                    newVmtas.createdBy = usr;
                    newVmtas.createdDate = date;
                    newVmtas.lastUpdatedBy = usr;
                    newVmtas.lastUpdatedDate = date;

                    String str = StringUtils.replace(this.vmtaConfigTemplate, "$p_vmta", newVmtas.name);
                    str = StringUtils.replace(str, "$p_ip", newVmtas.ip);
                    str = StringUtils.replace(str, "$p_domain", this.domain);
                    str = StringUtils.replace(str, "$p_dkim", "");

                    if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                        ssh.uploadContent(str, "/home/" + mtaServ.sshUsername + "/" + newVmtas.name + ".conf");
                        ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/" + newVmtas.name + ".conf /etc/pmta/vmtas/" + newVmtas.name + ".conf");
                    } else {
                        ssh.uploadContent(str, "/etc/pmta/vmtas/" + newVmtas.name + ".conf");
                    }

                    if (found) {
                        newVmtas.update();
                        continue;
                    }

                    newVmtas.insert();
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

    @ConstructorProperties({"serverId", "ispId", "domain", "vmtaConfigTemplate"})
    public GlobalVmtaUpdater(int serverId, int ispId, String domain, String vmtaConfigTemplate) {
        this.serverId = serverId;
        this.ispId = ispId;
        this.domain = domain;
        this.vmtaConfigTemplate = vmtaConfigTemplate;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof GlobalVmtaUpdater))
            return false;
        GlobalVmtaUpdater new1 = (GlobalVmtaUpdater)paramObject;
        if (!new1.exists(this))
            return false;
        if (getServerId() != new1.getServerId())
            return false;
        if (getIspId() != new1.getIspId())
            return false;
        String str1 = getDomain();
        String str2 = new1.getDomain();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getVmtaConfigTemplate();
        String str4 = new1.getVmtaConfigTemplate();
            return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof GlobalVmtaUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        n = n * 59 + getIspId();
        String str1 = getDomain();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getVmtaConfigTemplate();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getIspId() {
        return ispId;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getVmtaConfigTemplate() {
        return vmtaConfigTemplate;
    }

    public void setVmtaConfigTemplate(String vmtaConfigTemplate) {
        this.vmtaConfigTemplate = vmtaConfigTemplate;
    }

    @Override
    public String toString() {
        return "GlobalVmtaUpdater(serverId=" + getServerId() + ", ispId=" + getIspId() + ", domain=" + getDomain() + ", vmtaConfigTemplate=" + getVmtaConfigTemplate() + ")";
    }
}
