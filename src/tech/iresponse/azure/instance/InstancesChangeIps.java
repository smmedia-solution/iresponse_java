package tech.iresponse.azure.instance;

import com.microsoft.azure.management.network.NetworkInterface;
import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.azure.AzureManager;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.core.Application;

public class InstancesChangeIps extends Thread {

    private int instanceId;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            AzureInstance azureInstnce = new AzureInstance(Integer.valueOf(this.instanceId));
            if (azureInstnce.getEmpty()){
                throw new DatabaseException("Instance with id " + azureInstnce.getId() + " not found !");
            }
            AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureInstnce.accountId));
            if (azureAcc.getEmpty()){
                throw new DatabaseException("Account with id " + azureAcc.getId() + " not found !");
            }
            MtaServer mtaServ = new MtaServer(Integer.valueOf(azureInstnce.mtaServerId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("MtaServer with id " + azureInstnce.mtaServerId + " not found !");
            }
            AzureManager azureMng = new AzureManager(azureAcc);
            if (azureMng.authenticate()) {
                String changeIpsCallback = String.valueOf(Application.getSettingsParam("azure_change_ips_callback"));
                NetworkInterface ntwokIntrface = azureMng.getNetworkInterfaces(azureInstnce.getName() + "_res_gr", azureInstnce.getName() + "_nic");
                if (ntwokIntrface != null) {
                    if ("pause-resume".equals(changeIpsCallback)) {
                        ssh = Authentification.connectToServer(mtaServ);
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
                        ssh = Authentification.connectToServer(mtaServ);
                        if (ssh != null && ssh.isConnected()){
                            if ("schedule".equals(changeIpsCallback)) {
                                ssh.cmd("pmta schedule */*");
                            } else {
                                ssh.cmd("service pmta restart");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"instanceId"})
    public InstancesChangeIps(int instanceId) {
        this.instanceId = instanceId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesChangeIps))
            return false;
        InstancesChangeIps new1 = (InstancesChangeIps)paramObject;
        return !new1.exists(this) ? false : (!(getInstanceId() != new1.getInstanceId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesChangeIps;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getInstanceId();
    }

    @Override
    public String toString() {
        return "InstancesChangeIps(instanceId=" + getInstanceId() + ")";
    }
}
