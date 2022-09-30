package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import tech.iresponse.core.Application;
import tech.iresponse.azure.AzureManager;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;

public class InstancesGetPublicIps extends Thread {

    private int instanceId;

    @Override
    public void run() {
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

            int nbips = mtaServ.ipsCount;
            String ips = "";
            AzureManager azureMng = new AzureManager(azureAcc);
            if (azureMng.authenticate()) {
                PublicIPAddress pips = null;
                for (int i = 2; i <= nbips; i++) {
                    pips = azureMng.getPublicIPAddresses(azureInstnce.getName() + "_res_gr",azureInstnce.getName() + "_nic_pip_" + i);
                    ips += azureMng.getAllPublicIps(pips);
                }
                azureInstnce.publicIps = ips;
                azureInstnce.update();

            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    @ConstructorProperties({"instanceId"})
    public InstancesGetPublicIps(int instanceId) {
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
        if (!(paramObject instanceof InstancesGetPublicIps))
            return false;
        InstancesGetPublicIps new1 = (InstancesGetPublicIps)paramObject;
        return !new1.exists(this) ? false : (!(getInstanceId() != new1.getInstanceId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesGetPublicIps;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getInstanceId();
    }

    @Override
    public String toString() {
        return "InstancesGetPublicIps(instanceId=" + getInstanceId() + ")";
    }
}
