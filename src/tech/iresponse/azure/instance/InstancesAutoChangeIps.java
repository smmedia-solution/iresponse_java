package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import java.io.File;
import com.microsoft.azure.management.network.NetworkInterface;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.azure.AzureManager;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.core.Application;
import tech.iresponse.utils.ThreadSleep;

public class InstancesAutoChangeIps extends Thread {

    private AzureAccount account;
    private AzureInstance instance;
    private int processTimeValue;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            if (this.instance.getEmpty()){
                throw new DatabaseException("Instance with id " + this.instance.getId() + " not found !");
            }

            if (this.account.getEmpty()){
                throw new DatabaseException("Account with id " + this.account.getId() + " not found !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.instance.mtaServerId));
            if (mtaServ.getEmpty() && !"Connected".equals(mtaServ.sshConnectivityStatus) && !"Activated".equalsIgnoreCase(mtaServ.status)) {
                throw new DatabaseException("MtaServer with id " + this.instance.mtaServerId + " not found or not connected !");
            }
            AzureManager azureMng = new AzureManager(this.account);
            if (azureMng.authenticate()) {
                String changeIpsCallback = String.valueOf(Application.getSettingsParam("azure_change_ips_callback"));
                NetworkInterface ntwokIntrface = azureMng.getNetworkInterfaces(this.instance.getName() + "_res_gr", this.instance.getName() + "_nic");
                if (ntwokIntrface != null) {
                    ssh = Authentification.connectToServer(mtaServ);
                    if ("pause-resume".equals(changeIpsCallback)) {
                        if (ssh != null && ssh.isConnected()){
                            ssh.cmd("pmta pause queue */*");
                        }
                    }
                    azureMng.updateNetworkInterfaces(ntwokIntrface);
                    azureMng.updatePublicIPAddresses(ntwokIntrface);
                    if ("pause-resume".equals(changeIpsCallback)) {
                        if (ssh != null && ssh.isConnected()){
                            ssh.cmd("pmta resume queue */*");
                        }
                    } else if ("schedule".equals(changeIpsCallback) || "restart".equals(changeIpsCallback)) {
                        //ssh = Authentification.connectToServer(mtaServ);
                        if (ssh != null && ssh.isConnected()){
                            if ("schedule".equals(changeIpsCallback)) {
                                ssh.cmd("pmta schedule */*");
                            } else {
                                ssh.cmd("service pmta restart");
                            }
                        }
                    }

                }
                //ThreadSleep.sleep(this.processTimeValue);
            }

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"account", "instance", "processTimeValue"})
    public InstancesAutoChangeIps(AzureAccount account, AzureInstance instance, int processTimeValue) {
        this.account = account;
        this.instance = instance;
        this.processTimeValue = processTimeValue;
    }

    public AzureAccount getAccount() {
        return account;
    }

    public void setAccount(AzureAccount account) {
        this.account = account;
    }

    public AzureInstance getInstance() {
        return instance;
    }

    public void setInstance(AzureInstance instance) {
        this.instance = instance;
    }

    public int getProcessTimeValue() {
        return processTimeValue;
    }

    public void setProcessTimeValue(int processTimeValue) {
        this.processTimeValue = processTimeValue;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesAutoChangeIps))
            return false;
        InstancesAutoChangeIps if1 = (InstancesAutoChangeIps)paramObject;
        if (!if1.exists(this))
            return false;
        AzureAccount char1 = getAccount();
        AzureAccount char2 = if1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        AzureInstance goto1 = getInstance();
        AzureInstance goto2 = if1.getInstance();
        return ((goto1 == null) ? (goto2 != null) : !goto1.equals(goto2)) ? false : (!(getProcessTimeValue() != if1.getProcessTimeValue()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesAutoChangeIps;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        AzureInstance goto1 = getInstance();
        n = n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
        return n * 59 + getProcessTimeValue();
    }

    @Override
    public String toString() {
        return "InstancesAutoChangeIps(account=" + getAccount() + ", instance=" + getInstance() + ", processTimeValue=" + getProcessTimeValue() + ")";
    }
}
