package tech.iresponse.azure.instance;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.resources.ResourceGroup;
import java.beans.ConstructorProperties;
import java.io.File;
import java.sql.Date;
import java.util.Calendar;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import tech.iresponse.webservices.Azure;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.AzureProcess;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.helpers.scripts.CloudServices;
import tech.iresponse.helpers.scripts.InstallationServices;
import tech.iresponse.azure.AzureManager;
import tech.iresponse.azure.vm.AzureVMConfig;
import tech.iresponse.azure.update.UpdateData;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.http.Agents;
import tech.iresponse.core.Application;

public class InstancesInstaller extends Thread {

    private AzureProcess process;
    private AzureAccount account;
    private ServerProvider provider;
    private Domain domain;
    private JSONObject map;
    private boolean enableCrons;
    private boolean usePrefixes;
    private int prefixesLength;

    @Override
    public void run() {
        try {
            if (this.process == null || this.process.getEmpty()){
                throw new DatabaseException("Azure process not found !");
            }

            String instnceName = Azure.getInstanceNames(this.account, this.process.region);
            AzureInstance azureInstnces = insertInstanceDataInfos(this.account, instnceName, this.process.region);

            if (azureInstnces == null || azureInstnces.id == 0){
                throw new DatabaseException("Could not create azure instance !");
            }

            AzureVMConfig vmConfig = new AzureVMConfig();
            vmConfig.setVirtualMachineName(instnceName);
            vmConfig.setVirtualMachineSize(this.process.instanceType);
            vmConfig.setRegion(this.process.region);
            vmConfig.setVirtualMachinePassword(Strings.getSaltString(32, true, true, true, false));

            vmConfig.setResourceGroupName(instnceName + "_res_gr");
            vmConfig.setNetworkSecurityGroupName(instnceName + "_sec_gr");
            vmConfig.setNetworkName(instnceName + "_net");
            vmConfig.setSubnetName(instnceName + "_subnet");
            vmConfig.setNetworkInterfaceName(instnceName + "_nic");

            if (this.usePrefixes){
                vmConfig.setPrefixesLength(this.prefixesLength);
            }

            VirtualMachine vm = null;
            AzureManager azureMng = new AzureManager(this.account);

            if (azureMng.authenticate()) {
                ResourceGroup resourceGrp = azureMng.getResourceGroupsByName(vmConfig.getResourceGroupName());
                if (resourceGrp == null){
                    resourceGrp = azureMng.createResourceGroups(vmConfig.getResourceGroupName(), vmConfig.getRegion());
                }
                if (resourceGrp != null) {
                    NetworkSecurityGroup ntworkSecurityGrp = azureMng.getNetworkSecurityGroups(resourceGrp.name(), vmConfig.getNetworkSecurityGroupName());
                    if (ntworkSecurityGrp == null){
                        ntworkSecurityGrp = azureMng.createNetworkSecurityGroups(resourceGrp.name(), vmConfig.getRegion(), vmConfig.getNetworkSecurityGroupName());
                    }
                    if (ntworkSecurityGrp != null) {
                        Network ntwork = azureMng.getNetworks(resourceGrp.name(), vmConfig.getNetworkName());
                        if (ntwork == null){
                            ntwork = azureMng.createNetworks(resourceGrp.name(), vmConfig.getRegion(), vmConfig.getNetworkName(), vmConfig.getNetworkCidr(), vmConfig.getSubnetName(), vmConfig.getSubnetCidr());
                        }
                        if (ntwork != null) {
                            NetworkInterface ntwrkIntrface = azureMng.getNetworkInterfaces(resourceGrp.name(), vmConfig.getNetworkInterfaceName());
                            if (ntwrkIntrface == null){
                                if (this.usePrefixes) {
                                    ntwrkIntrface = azureMng.createNetworkInterfaces(vmConfig, ntwork, ntworkSecurityGrp, this.process.nbPrivateIps);
                                } else {
                                    ntwrkIntrface = azureMng.createNetworkInterfaces(resourceGrp.name(), vmConfig.getRegion(), vmConfig.getNetworkInterfaceName(), ntwork, vmConfig.getSubnetName(), ntworkSecurityGrp, this.process.nbPrivateIps);
                                }
                            }
                            if (ntwrkIntrface != null){
                                vm = azureMng.createVirtualMachines(vmConfig, ntwrkIntrface);
                            }
                        }
                    }
                }
            }

            if (vm == null) {
                azureMng.deleteResourceGroupsByName(vmConfig.getResourceGroupName());
                azureInstnces.delete();
                throw new DatabaseException("Could not create virtual machine !");
            }

            Azure.updateCountInstnceCreated(1);
            UpdateData.updateInstancesCreated(this.process);

            MtaServer mtaServ = insertMtaServerDataInfos(azureInstnces, vm, this.domain, vmConfig.getVirtualMachineUsername(), vmConfig.getVirtualMachinePassword());
            if (mtaServ == null || mtaServ.getEmpty()){
                throw new DatabaseException("Could not create mta server !");
            }

            azureInstnces.mtaServerId = mtaServ.id;
            azureInstnces.update();

            SSHConnector ssh = null;
            try {
                ssh = Authentification.connectToServer(mtaServ);
                if (ssh == null || !ssh.isConnected()){
                    throw new DatabaseException("Could not connect to this instance : " + mtaServ.name + " !");
                }

                ssh.changePasswordServer(mtaServ.sshUsername, mtaServ.sshPassword);
                ssh.disconnect();

                mtaServ.sshUsername = "root";
                mtaServ.update();

                ThreadSleep.sleep(5000L);

                ssh = Authentification.connectToServer(mtaServ);
                if (ssh == null || !ssh.isConnected()){
                    throw new DatabaseException("Could not connect to this instance : " + mtaServ.name + " !");
                }

                if (vm.getPrimaryNetworkInterface() != null && vm.getPrimaryNetworkInterface().ipConfigurations() != null && !vm.getPrimaryNetworkInterface().ipConfigurations().isEmpty()) {
                    int countEth0 = 0;
                    for (Map.Entry entry : vm.getPrimaryNetworkInterface().ipConfigurations().entrySet()) {
                        if (!((NicIPConfiguration)entry.getValue()).isPrimary()) {
                            ssh.uploadContent("DEVICE=eth0:" + countEth0 + "\nBOOTPROTO=static\nONBOOT=yes\nIPADDR=" + ((NicIPConfiguration)entry.getValue()).privateIPAddress() + "\nNETMASK=255.255.255.0\nARPCHECK=no", "/etc/sysconfig/network-scripts/ifcfg-eth0:" + countEth0);
                            countEth0++;
                        }
                    }
                    ssh.shellCommand("systemctl restart network");
                }

                String templateSshdConfig = StringUtils.replace(FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/sshd_config.tpl"), "utf-8"), "$p_port", String.valueOf(mtaServ.sshPort));
                if (!"".equals(templateSshdConfig)) {
                    ssh.uploadContent(templateSshdConfig, "/etc/ssh/sshd_config");
                    if (vmConfig.getCentosVersion() == 7) {
                        ssh.shellCommand("systemctl restart sshd");
                    } else {
                        ssh.shellCommand("service sshd restart");
                    }
                }

                InstallationServices.installServices(ssh, mtaServ, vmConfig.getSudo(), vmConfig.getCentosVersion(), false);

                /*String azureMngerPython = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/azure/azure_manager.py"), "UTF-8");
                azureMngerPython = StringUtils.replace(azureMngerPython, "$p_client_id", this.account.clientId);
                azureMngerPython = StringUtils.replace(azureMngerPython, "$p_tenent_id", this.account.tenantId);
                azureMngerPython = StringUtils.replace(azureMngerPython, "$p_client_secret", this.account.token);
                azureMngerPython = StringUtils.replace(azureMngerPython, "$p_subscription_id", this.account.subscriptionId);
                azureMngerPython = StringUtils.replace(azureMngerPython, "$p_instance_id", azureInstnces.name);

                ssh.shellCommand("pip3 install azure.mgmt.network");
                ssh.shellCommand("pip3 install azure.identity");
                ssh.shellCommand("pip3 install azure.mgmt.compute");
                ssh.cmd("mkdir -p /usr/iresponse/");
                ssh.uploadContent(azureMngerPython, "/usr/iresponse/azure_manager.py");*/

                boolean networkUpdated = CloudServices.setupNetwork(ssh, mtaServ, vmConfig.getSudo(), vmConfig.getCentosVersion(), this.domain, vm);
                CloudServices.installTracking(ssh, mtaServ, vmConfig.getSudo(), vmConfig.getCentosVersion(), this.domain, networkUpdated, this.enableCrons);
                InstallationServices.installPmta(ssh, mtaServ, vmConfig.getSudo());

                mtaServ.os = "CentOS " + vmConfig.getCentosVersion() + " 64bits";
                mtaServ.sshConnectivityStatus = "Connected";
                mtaServ.setIpsCount(this.process.nbPrivateIps);
                mtaServ.setInstalled(true);
                mtaServ.update();

                Azure.updateCountInstnceInstalled();
                UpdateData.updateInstancesInstalled(this.process);

            } finally {
                if (ssh != null){
                    ssh.disconnect();
                }
            }

        } catch (Throwable th) {
            Loggers.error(th);
            Azure.IS_ERROR_OCCURED = true;
        }
    }

    private AzureInstance insertInstanceDataInfos(AzureAccount azureAcc, String name, String region) throws Exception {
        AzureInstance azureInstnces = new AzureInstance();
        azureInstnces.status = "Running";
        azureInstnces.accountId = azureAcc.id;
        azureInstnces.accountName = azureAcc.name;
        azureInstnces.name = name;
        azureInstnces.region = region;
        azureInstnces.regionName = this.map.getJSONObject(region).getString("name");
        azureInstnces.type = this.process.instanceType;
        azureInstnces.mtaServerId = -1;
        azureInstnces.mtaServerName = name;
        azureInstnces.createdBy = Application.checkUser();
        azureInstnces.createdDate = new Date(System.currentTimeMillis());
        azureInstnces.lastUpdatedBy = Application.checkUser();
        azureInstnces.lastUpdatedDate = new Date(System.currentTimeMillis());
        azureInstnces.id = azureInstnces.insert();
        return azureInstnces;
    }

    private MtaServer insertMtaServerDataInfos(AzureInstance azureInstnce, VirtualMachine vm, Domain domin, String usernme, String passwrd) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, 1);
        MtaServer mtaServ = new MtaServer();
        mtaServ.name = azureInstnce.getName();
        mtaServ.status = "Activated";
        mtaServ.sshConnectivityStatus = "Not Checked";
        mtaServ.providerId = this.provider.id;
        mtaServ.providerName = this.provider.name;
        mtaServ.expirationDate = new Date(calendar.getTimeInMillis());
        mtaServ.hostName = "mail." + domin.value;
        mtaServ.mainIp = vm.getPrimaryPublicIPAddress().ipAddress();
        String result = Agents.get("https://freegeoip.live/json/" + mtaServ.mainIp, null, 20);
        if (result != null && result.contains("country_code")) {
            JSONObject response = new JSONObject(result);
            mtaServ.countryCode = response.has("country_code") ? response.getString("country_code") : "US";
        } else {
            mtaServ.countryCode = "US";
        }
        mtaServ.sshPort = 22;
        mtaServ.sshLoginType = "user-pass";
        mtaServ.sshPassword = passwrd;
        mtaServ.sshUsername = usernme;
        mtaServ.createdBy = Application.checkUser();
        mtaServ.createdDate = new Date(System.currentTimeMillis());
        mtaServ.lastUpdatedBy = Application.checkUser();
        mtaServ.lastUpdatedDate = new Date(System.currentTimeMillis());
        mtaServ.id = mtaServ.insert();
        return mtaServ;
    }

    @ConstructorProperties({"process", "account", "provider", "domain", "map", "enableCrons", "usePrefixes", "prefixesLength"})
    public InstancesInstaller(AzureProcess process, AzureAccount account, ServerProvider provider, Domain domain, JSONObject map, boolean enableCrons, boolean usePrefixes, int prefixesLength) {
        this.process = process;
        this.account = account;
        this.provider = provider;
        this.domain = domain;
        this.map = map;
        this.enableCrons = enableCrons;
        this.usePrefixes = usePrefixes;
        this.prefixesLength = prefixesLength;
    }

    public AzureProcess getProcess() {
        return process;
    }

    public void setProcess(AzureProcess process) {
        this.process = process;
    }

    public AzureAccount getAccount() {
        return account;
    }

    public void setAccount(AzureAccount account) {
        this.account = account;
    }

    public ServerProvider getProvider() {
        return provider;
    }

    public void setProvider(ServerProvider provider) {
        this.provider = provider;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public JSONObject getMap() {
        return map;
    }

    public void setMap(JSONObject map) {
        this.map = map;
    }

    public boolean isEnableCrons() {
        return enableCrons;
    }

    public void setEnableCrons(boolean enableCrons) {
        this.enableCrons = enableCrons;
    }

    public boolean isUsePrefixes() {
        return usePrefixes;
    }

    public void setUsePrefixes(boolean usePrefixes) {
        this.usePrefixes = usePrefixes;
    }

    public int getPrefixesLength() {
        return prefixesLength;
    }

    public void setPrefixesLength(int prefixesLength) {
        this.prefixesLength = prefixesLength;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesInstaller))
            return false;
        InstancesInstaller case1 = (InstancesInstaller)paramObject;
        if (!case1.exists(this))
            return false;
        AzureProcess long1 = getProcess();
        AzureProcess long2 = case1.getProcess();
        if ((long1 == null) ? (long2 != null) : !long1.equals(long2))
            return false;
        AzureAccount char1 = getAccount();
        AzureAccount char2 = case1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        ServerProvider continue1 = getProvider();
        ServerProvider continue2 = case1.getProvider();
        if ((continue1 == null) ? (continue2 != null) : !continue1.equals(continue2))
            return false;
        Domain catch1 = getDomain();
        Domain catch2 = case1.getDomain();
        if ((catch1 == null) ? (catch2 != null) : !catch1.equals(catch2))
            return false;
        JSONObject jSONObject1 = getMap();
        JSONObject jSONObject2 = case1.getMap();
        return ((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2)) ? false : ((isEnableCrons() != case1.isEnableCrons()) ? false : ((isUsePrefixes() != case1.isUsePrefixes()) ? false : (!(getPrefixesLength() != case1.getPrefixesLength()))));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesInstaller;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureProcess long1 = getProcess();
        n = n * 59 + ((long1 == null) ? 43 : long1.hashCode());
        AzureAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        ServerProvider continue1 = getProvider();
        n = n * 59 + ((continue1 == null) ? 43 : continue1.hashCode());
        Domain catch1 = getDomain();
        n = n * 59 + ((catch1 == null) ? 43 : catch1.hashCode());
        JSONObject jSONObject = getMap();
        n = n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
        n = n * 59 + (isEnableCrons() ? 79 : 97);
        n = n * 59 + (isUsePrefixes() ? 79 : 97);
        return n * 59 + getPrefixesLength();
    }

    @Override
    public String toString() {
        return "InstancesInstaller(process=" + getProcess() + ", account=" + getAccount() + ", provider=" + getProvider() + ", domain=" + getDomain() + ", map=" + getMap() + ", enableCrons=" + isEnableCrons() + ", usePrefixes=" + isUsePrefixes() + ", prefixesLength=" + getPrefixesLength() + ")";
    }

}
