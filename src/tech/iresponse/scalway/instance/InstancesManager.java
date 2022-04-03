package tech.iresponse.scalway.instance;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONObject;
import tech.iresponse.http.Agents;
import tech.iresponse.logging.Loggers;
import tech.iresponse.orm.Connector;
import tech.iresponse.models.admin.ScalewayAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.ScalewayInstance;
import tech.iresponse.models.admin.ScalewayProcess;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.scalway.ScalewayManager;
import tech.iresponse.scalway.update.UpdateData;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;
import tech.iresponse.helpers.scripts.CloudServices;
import tech.iresponse.helpers.scripts.InstallationServices;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.webservices.Scaleway;

public class InstancesManager extends Thread {

    private ScalewayAccount account;
    private ScalewayProcess process;
    private ServerProvider provider;
    private String domainId;

    @Override
    public void run() {
        try {
            if (!this.account.getEmpty()) {
                ScalewayManager sclwyMng = new ScalewayManager();
                sclwyMng.setAccount(this.account);
                Domain domin = new Domain(Integer.valueOf(TypesParser.safeParseInt(this.domainId)));
                if (domin.getEmpty()) {
                    throw new DatabaseException("No domain found in this process !");
                }
                String hostName = "mail." + domin.value;
                String instancesId = sclwyMng.createInstance(hostName, this.process.region, this.process.os, this.process.size);
                if (instancesId == null) {
                    throw new DatabaseException("Scaleway Instance could not be created !");
                }
                sclwyMng.executeAction(instancesId, this.process.region, "poweron");
                ThreadSleep.sleep(180000L);
                Scaleway.updateCountInstancesCreated(1);
                UpdateData.updateInstancesCreated(this.process);
                HashMap instances = sclwyMng.getInstance(instancesId, this.process.region, null);
                if (instances == null) {
                    throw new DatabaseException("Scaleway Instance could not be created !");
                }
                ScalewayInstance sclwyInstance = new ScalewayInstance();
                sclwyInstance.status = "Running";
                sclwyInstance.accountId = this.account.id;
                sclwyInstance.accountName = this.account.name;
                sclwyInstance.name = "mail." + domin.value + " (" + (String)instances.get("id") + ")";
                sclwyInstance.instanceId = (String)instances.get("id");
                sclwyInstance.region = (String)instances.get("region");
                sclwyInstance.platform = (String)instances.get("image");
                sclwyInstance.size = this.process.size;
                MtaServer mtaServ = getServerInfos(this.account, instances, this.provider, domin);
                sclwyInstance.mtaServerId = mtaServ.id;
                sclwyInstance.mtaServerName = mtaServ.name;
                sclwyInstance.createdBy = (Application.checkAndgetInstance().getUser()).email;
                sclwyInstance.lastUpdatedDate = new Date(System.currentTimeMillis());
                sclwyInstance.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                sclwyInstance.lastUpdatedDate = new Date(System.currentTimeMillis());
                sclwyInstance.insert();
                SSHConnector ssh = null;
                try {
                    ssh = Authentification.connectToServer(mtaServ);
                    if (ssh == null || !ssh.isConnected()) {
                        throw new DatabaseException("Could not connect to this Scaleway Instance : " + mtaServ.name + " !");
                    }
                    String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
                    int version = 7;
                    InstallationServices.installServices(ssh, mtaServ, prefix, version, false);
                    int vmtaId = InstallationServices.saveServerVmta(null, (String)instances.get("private_ip"), "", domin.value, mtaServ);
                    String[] ips = ssh.cmd(prefix + "ip addr show | grep 'inet6' | grep -i 'global' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/' | awk '{ print $1}'").split("\n");
                    for (int i = 0; i < ips.length; i++) {
                        ips[i] = ips[i].replaceAll("\n", "").replaceAll("\r", "");
                    }
                    Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Taken' , mta_server_id = '" + mtaServ.id + "', ip_id = '" + vmtaId + "' WHERE id = " + domin.id, null, Connector.AFFECTED_ROWS);
                    boolean updatedNetwork = CloudServices.setupNetwork(ssh, mtaServ, prefix, version, domin, mtaServ.mainIp, ips);
                    CloudServices.installTracking(ssh, mtaServ, prefix, version, domin, updatedNetwork, true);
                    InstallationServices.installPmta(ssh, mtaServ, prefix);
                    mtaServ.os = "CentOS " + version + " 64bits";
                    mtaServ.sshConnectivityStatus = "Connected";
                    mtaServ.setIpsCount(1 + ips.length);
                    mtaServ.setInstalled(true);
                    mtaServ.update();
                    Scaleway.updateCountInstancsInstalled();
                } finally {
                    if (ssh != null) {
                        ssh.disconnect();
                    }
                }
                UpdateData.updateInstancesInstalled(this.process);
            }
        } catch (Throwable th) {
            Loggers.error(th);
            Scaleway.IS_ERROR_OCCURED = true;
        }
    }

    public static synchronized MtaServer getServerInfos(ScalewayAccount sclwyAcc, HashMap instanceInfo, ServerProvider servProvider, Domain domin) throws Exception {
        String name = sclwyAcc.name.trim();
        String nm = name + "_1";
        List list = Database.get("system").availableTables("SELECT name FROM admin.mta_servers WHERE name LIKE '" + name + "%' ORDER BY id DESC LIMIT 1", null, 0, "name");
        if (list != null && !list.isEmpty()) {
            String str = String.valueOf(list.get(0));
            if (str.contains("_")) {
                String[] arrayOfString = str.split(Pattern.quote("_"));
                if (arrayOfString.length > 0){
                    nm = name + "_" + (TypesParser.safeParseInt(arrayOfString[arrayOfString.length - 1]) + 1);
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, 1);
        MtaServer mtaServ = new MtaServer();
        mtaServ.name = nm;
        mtaServ.status = "Activated";
        mtaServ.sshConnectivityStatus = "Not Checked";
        mtaServ.providerId = servProvider.id;
        mtaServ.providerName = servProvider.name;
        mtaServ.expirationDate = new Date(calendar.getTimeInMillis());
        mtaServ.hostName = "mail." + domin.value;
        mtaServ.mainIp = (String)instanceInfo.get("main_ip");

        String result = Agents.get("https://freegeoip.live/json/" + mtaServ.mainIp, null, 20);
        if (result != null && result.contains("country_code")) {
            JSONObject jSONObject = new JSONObject(result);
            mtaServ.countryCode = jSONObject.has("country_code") ? jSONObject.getString("country_code") : "US";
        } else {
            mtaServ.countryCode = "US";
        }
        mtaServ.oldSshPort = 22;
        mtaServ.sshPort = 22;
        mtaServ.sshLoginType = "rsa";
        mtaServ.sshUsername = "root";
        mtaServ.createdBy = (Application.checkAndgetInstance().getUser()).email;
        mtaServ.createdDate = new Date(System.currentTimeMillis());
        mtaServ.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
        mtaServ.lastUpdatedDate = new Date(System.currentTimeMillis());
        mtaServ.id = mtaServ.insert();
        return mtaServ;
    }

    @ConstructorProperties({"account", "process", "provider", "domainId"})
    public InstancesManager(ScalewayAccount account, ScalewayProcess process, ServerProvider provider, String domainId) {
        this.account = account;
        this.process = process;
        this.provider = provider;
        this.domainId = domainId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesManager))
            return false;
        InstancesManager if1 = (InstancesManager)paramObject;
        if (!if1.exists(this))
            return false;
        ScalewayAccount const1 = getAccount();
        ScalewayAccount const2 = if1.getAccount();
        if ((const1 == null) ? (const2 != null) : !const1.equals(const2))
            return false;
        ScalewayProcess float1 = getProcess();
        ScalewayProcess float2 = if1.getProcess();
        if ((float1 == null) ? (float2 != null) : !float1.equals(float2))
            return false;
        ServerProvider default1 = getProvider();
        ServerProvider default2 = if1.getProvider();
        if ((default1 == null) ? (default2 != null) : !default1.equals(default2))
            return false;
        String str1 = getDomainId();
        String str2 = if1.getDomainId();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        ScalewayAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        ScalewayProcess goto1 = getProcess();
        n = n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
        ServerProvider default1 = getProvider();
        n = n * 59 + ((default1 == null) ? 43 : default1.hashCode());
        String str1 = getDomainId();
        return n * 59 + ((str1 == null) ? 43 : str1.hashCode());
    }

    public ScalewayAccount getAccount() {
        return account;
    }

    public void setAccount(ScalewayAccount account) {
        this.account = account;
    }

    public ScalewayProcess getProcess() {
        return process;
    }

    public void setProcess(ScalewayProcess process) {
        this.process = process;
    }

    public ServerProvider getProvider() {
        return provider;
    }

    public void setProvider(ServerProvider provider) {
        this.provider = provider;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String toString() {
        return "InstancesManager(account=" + getAccount() + ", process=" + getProcess() + ", provider=" + getProvider() + ", domainId=" + getDomainId() + ")";
    }
}
