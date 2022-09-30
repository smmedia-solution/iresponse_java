package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class InstancesLogs extends Thread {

    private int instanceId;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            if (this.instanceId > 0) {
                AzureInstance azureInstce = new AzureInstance(Integer.valueOf(this.instanceId));
                if (!azureInstce.getEmpty()) {
                    MtaServer mtaServ = new MtaServer(Integer.valueOf(azureInstce.mtaServerId));
                    if (mtaServ.getEmpty()){
                        throw new DatabaseException("Server not found !");
                    }
                    ssh = Authentification.connectToServer(mtaServ);
                    if (ssh == null || !ssh.isConnected()){
                        throw new DatabaseException("Could not connect to this server !");
                    }
                    ssh.cmd("nohup php /var/www/scripts/stats.php >> /var/log/iresponse/stats.log 2>> /var/log/iresponse/stats.log &");
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
    public InstancesLogs(int instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesLogs))
            return false;
        InstancesLogs char1 = (InstancesLogs)paramObject;
        return !char1.exists(this) ? false : (!(getInstanceId() != char1.getInstanceId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesLogs;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getInstanceId();
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return "InstancesLogs(instanceId=" + getInstanceId() + ")";
    }
}
