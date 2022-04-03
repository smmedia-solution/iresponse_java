package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.sql.Date;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.core.Application;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.remote.Authentification;

public class IndividualVmtaUpdater extends Thread {

    private int serverId;
    private JSONArray vmtasMap;
    private int ispId;
    private String action;
    private String vmtaConfigTemplate;

    @Override
    public void run() {
        SSHConnector ssh = null;

        try {
            if (this.vmtasMap == null || this.vmtasMap.length() == 0){
                throw new DatabaseException("Vmtas mapping not found !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            boolean foundReset = "reset".equals(this.action);
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
            JSONObject map = null;
            String usr = Application.checkUser();
            Date date = new Date(System.currentTimeMillis());

            for (int b = 0; b < this.vmtasMap.length(); b++) {
                map = this.vmtasMap.getJSONObject(b);
                if (map != null) {
                    ServerVmta vmtas = (ServerVmta)ServerVmta.first(ServerVmta.class, "ip = ? AND isp_id = ? AND type = ? AND created_by = ?", new Object[] { map.getString("vmta-ip"), Integer.valueOf(this.ispId), "Custom", usr });

                    if (foundReset) {
                        if (vmtas != null && vmtas.id > 0) {
                            ssh.cmd("rm -rf /etc/pmta/vmtas/" + vmtas.name + ".conf");
                            vmtas.delete();
                        }
                    } else {
                        boolean update = false;
                        if (vmtas != null && vmtas.id > 0) {
                            update = true;
                        } else {
                            ServerVmta newVmta = (ServerVmta)ServerVmta.first(ServerVmta.class, "ip = ? AND type = ?", new Object[] { map.getString("vmta-ip"), "Default" });
                            vmtas = new ServerVmta(Integer.valueOf(newVmta.id));
                            vmtas.id = 0;
                            vmtas.type = "Custom";
                            vmtas.name += "_" + this.ispId;
                            vmtas.ispId = this.ispId;
                        }

                        vmtas.customDomain = map.getString("domain");
                        String vmtaContent = StringUtils.replace(this.vmtaConfigTemplate, "$p_vmta", vmtas.name);
                        vmtaContent = StringUtils.replace(vmtaContent, "$p_ip", vmtas.ip);
                        vmtaContent = StringUtils.replace(vmtaContent, "$p_domain", map.getString("domain"));
                        vmtaContent = StringUtils.replace(vmtaContent, "$p_dkim", "");

                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.uploadContent(vmtaContent, "/home/" + mtaServ.sshUsername + "/" + vmtas.name + ".conf");
                            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/" + vmtas.name + ".conf /etc/pmta/vmtas/" + vmtas.name + ".conf");
                        } else {
                            ssh.uploadContent(vmtaContent, "/etc/pmta/vmtas/" + vmtas.name + ".conf");
                        }

                        if (update) {
                            vmtas.lastUpdatedBy = usr;
                            vmtas.lastUpdatedDate = date;
                            vmtas.update();
                        } else {
                            vmtas.createdBy = usr;
                            vmtas.createdDate = date;
                            vmtas.lastUpdatedBy = usr;
                            vmtas.lastUpdatedDate = date;
                            vmtas.insert();
                        }
                    }
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

    @ConstructorProperties({"serverId", "vmtasMap", "ispId", "action", "vmtaConfigTemplate"})
    public IndividualVmtaUpdater(int serverId, JSONArray vmtasMap, int ispId, String action, String vmtaConfigTemplate) {
        this.serverId = serverId;
        this.vmtasMap = vmtasMap;
        this.ispId = ispId;
        this.action = action;
        this.vmtaConfigTemplate = vmtaConfigTemplate;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof IndividualVmtaUpdater))
            return false;
        IndividualVmtaUpdater try1 = (IndividualVmtaUpdater)paramObject;
        if (!try1.exists(this))
            return false;
        if (getServerId() != try1.getServerId())
            return false;
        JSONArray jSONArray1 = getVmtasMap();
        JSONArray jSONArray2 = try1.getVmtasMap();
        if ((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2))
            return false;
        if (getIspId() != try1.getIspId())
            return false;
        String str1 = getAction();
        String str2 = try1.getAction();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getVmtaConfigTemplate();
        String str4 = try1.getVmtaConfigTemplate();
            return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof IndividualVmtaUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        JSONArray jSONArray = getVmtasMap();
        n = n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
        n = n * 59 + getIspId();
        String str1 = getAction();
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

    public JSONArray getVmtasMap() {
        return vmtasMap;
    }

    public void setVmtasMap(JSONArray vmtasMap) {
        this.vmtasMap = vmtasMap;
    }

    public int getIspId() {
        return ispId;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVmtaConfigTemplate() {
        return vmtaConfigTemplate;
    }

    public void setVmtaConfigTemplate(String vmtaConfigTemplate) {
        this.vmtaConfigTemplate = vmtaConfigTemplate;
    }

    @Override
    public String toString() {
        return "IndividualVmtaUpdater(serverId=" + getServerId() + ", vmtasMap=" + getVmtasMap() + ", ispId=" + getIspId() + ", action=" + getAction() + ", vmtaConfigTemplate=" + getVmtaConfigTemplate() + ")";
    }
}
