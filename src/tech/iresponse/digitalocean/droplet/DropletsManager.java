package tech.iresponse.digitalocean.droplet;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.orm.Connector;
import tech.iresponse.webservices.DigitalOcean;
import tech.iresponse.models.admin.DigitalOceanAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.DigitalOceanDroplet;
import tech.iresponse.models.admin.DigitalOceanProcess;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.digitalocean.DigitalOceanManager;
import tech.iresponse.digitalocean.update.UpdateData;
import tech.iresponse.core.Application;
import tech.iresponse.http.Agents;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.helpers.scripts.CloudServices;
import tech.iresponse.helpers.scripts.InstallationServices;
import tech.iresponse.remote.Authentification;

public class DropletsManager extends Thread {

    private DigitalOceanAccount account;
    private DigitalOceanProcess process;
    private ServerProvider provider;
    private String domainId;
    private String sshKeyId;

    @Override
    public void run() {
        try {
            if (!this.account.getEmpty()) {
                DigitalOceanManager doManager = new DigitalOceanManager();
                doManager.setAccount(this.account);

                Domain domin = new Domain(Integer.valueOf(TypesParser.safeParseInt(this.domainId)));
                if (domin.getEmpty()){
                    throw new DatabaseException("No domain found in this process !");
                }

                String hostName = "mail." + domin.value;
                String dropletId = doManager.getDropletInfos(hostName, this.process.region, this.process.os, this.process.size, this.sshKeyId);
                if (dropletId == null){
                    throw new DatabaseException("Droplet could not be created !");
                }

                HashMap dropletInfo = null;
                for (int b = 0; b < 10; b++) {
                    ThreadSleep.sleep(10000L);
                    dropletInfo = doManager.getDropletInfos(dropletId);
                    if ("active".equals(dropletInfo.get("status"))){
                        break;
                    }
                }

                if (dropletInfo == null){
                    throw new DatabaseException("Droplet could not be created !");
                }

                ThreadSleep.sleep(30000L);

                DigitalOcean.updateCountDropletCreated(1);
                UpdateData.updateDropletsCreated(this.process);

                DigitalOceanDroplet doDroplet = new DigitalOceanDroplet();
                doDroplet.status = "Running";
                doDroplet.accountId = this.account.id;
                doDroplet.accountName = this.account.name;
                doDroplet.name = "mail." + domin.value + " (" + dropletId + ")";
                doDroplet.dropletId = dropletId;
                doDroplet.region = (String)dropletInfo.get("region");
                doDroplet.platform = (String)dropletInfo.get("image");
                doDroplet.size = this.process.size;

                MtaServer mtaServ = this.getServerInfos(this.account, dropletInfo, this.provider, domin);
                doDroplet.mtaServerId = mtaServ.id;
                doDroplet.mtaServerName = mtaServ.name;
                doDroplet.createdBy = (Application.checkAndgetInstance().getUser()).email;
                doDroplet.createdDate = new Date(System.currentTimeMillis());
                doDroplet.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                doDroplet.lastUpdatedDate = new Date(System.currentTimeMillis());
                doDroplet.insert();

                SSHConnector ssh = null;
                try {
                    ssh = Authentification.connectToServer(mtaServ);
                    if (ssh == null || !ssh.isConnected()){
                        throw new DatabaseException("Could not connect to this droplet : " + mtaServ.name + " !");
                    }

                    String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
                    int version = String.valueOf(ssh.cmd("cat /etc/*release* | grep 'centos:7'")).replaceAll("\n", "").contains("centos:7") ? 7 : 6;

                    InstallationServices.installServices(ssh, mtaServ, prefix, version, false);

                    int vmtaId = InstallationServices.saveServerVmta(null, mtaServ.mainIp, "", domin.value, mtaServ);
                    Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Taken' , mta_server_id = '" + mtaServ.id + "', ip_id = '" + vmtaId + "' WHERE id = " + domin.id, null, Connector.AFFECTED_ROWS);

                    String[] ipsV6 = ssh.cmd(prefix + "ip addr show | grep 'inet6' | grep -i 'global' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/' | awk '{ print $1}'").split("\n");
                    for (int b2 = 0; b2 < ipsV6.length; b2++){
                        ipsV6[b2] = ipsV6[b2].replaceAll("\n", "").replaceAll("\r", "");
                    }

                    boolean updatedNetwork = CloudServices.setupNetwork(ssh, mtaServ, prefix, version, domin, mtaServ.mainIp, ipsV6);
                    CloudServices.installTracking(ssh, mtaServ, prefix, version, domin, updatedNetwork, true);
                    InstallationServices.installPmta(ssh, mtaServ, prefix);

                    mtaServ.os = "CentOS " + version + " 64bits";
                    mtaServ.sshConnectivityStatus = "Connected";
                    mtaServ.setIpsCount(1 + ipsV6.length);
                    mtaServ.setInstalled(true);
                    mtaServ.update();
                    DigitalOcean.updateCountDropletInstalled();

                } finally {
                    if (ssh != null) {
                        ssh.disconnect();
                    }
                }
                UpdateData.updateDropletsInstalled(this.process);
            }
        } catch (Throwable th) {
            Loggers.error(th);
            DigitalOcean.IS_ERROR_OCCURED = true;
        }
    }

    public static synchronized MtaServer getServerInfos(DigitalOceanAccount doAccount, HashMap dropletInfo, ServerProvider servProvider, Domain domin) throws Exception {
        String name = doAccount.name.trim();
        String nm = name + "_1";
        List list = Database.get("system").availableTables("SELECT name FROM admin.mta_servers WHERE name LIKE '" + name + "%' ORDER BY id DESC LIMIT 1", null, 0, "name");

        if (list != null && !list.isEmpty()) {
            String str = String.valueOf(list.get(0));
            if (str.contains("_")) {
                String[] arrayOfString = str.split(Pattern.quote("_"));
                if (arrayOfString.length > 0) {
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
        mtaServ.mainIp = (String)dropletInfo.get("main_ip");

        String result = Agents.get("https://freegeoip.live/json/" + mtaServ.mainIp, null, 20);
        if (result != null && result.contains("country_code")) {
            JSONObject response = new JSONObject(result);
            mtaServ.countryCode = response.has("country_code") ? response.getString("country_code") : "US";
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

    @ConstructorProperties({"account", "process", "provider", "domainId", "sshKeyId"})
    public DropletsManager(DigitalOceanAccount account, DigitalOceanProcess process, ServerProvider provider, String domainId, String sshKeyId) {
        this.account = account;
        this.process = process;
        this.provider = provider;
        this.domainId = domainId;
        this.sshKeyId = sshKeyId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DropletsManager))
            return false;
        DropletsManager if1 = (DropletsManager)paramObject;
        if (!if1.exists(this))
            return false;
        DigitalOceanAccount char1 = getAccount();
        DigitalOceanAccount char2 = if1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        DigitalOceanProcess goto1 = getProcess();
        DigitalOceanProcess goto2 = if1.getProcess();
        if ((goto1 == null) ? (goto2 != null) : !goto1.equals(goto2))
            return false;
        ServerProvider default1 = getProvider();
        ServerProvider default2 = if1.getProvider();
        if ((default1 == null) ? (default2 != null) : !default1.equals(default2))
            return false;
        String str1 = getDomainId();
        String str2 = if1.getDomainId();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getSshKeyId();
        String str4 = if1.getSshKeyId();
        return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DropletsManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        DigitalOceanAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        DigitalOceanProcess goto1 = getProcess();
        n = n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
        ServerProvider default1 = getProvider();
        n = n * 59 + ((default1 == null) ? 43 : default1.hashCode());
        String str1 = getDomainId();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getSshKeyId();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public DigitalOceanAccount getAccount() {
        return account;
    }

    public void setAccount(DigitalOceanAccount account) {
        this.account = account;
    }

    public DigitalOceanProcess getProcess() {
        return process;
    }

    public void setProcess(DigitalOceanProcess process) {
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

    public String getSshKeyId() {
        return sshKeyId;
    }

    public void setSshKeyId(String sshKeyId) {
        this.sshKeyId = sshKeyId;
    }

    @Override
    public String toString() {
        return "DropletsManager(account=" + getAccount() + ", process=" + getProcess() + ", provider=" + getProvider() + ", domainId=" + getDomainId() + ", sshKeyId=" + getSshKeyId() + ")";
    }
}
