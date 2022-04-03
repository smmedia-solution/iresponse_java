package tech.iresponse.amazon.instance;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class InstancesLogs extends Thread {

    private int instanceId;

    public void run() {
        SSHConnector ssh = null;
        try {
            if (this.instanceId > 0) {
                AwsInstance awsInst = new AwsInstance(Integer.valueOf(this.instanceId));
                if (!awsInst.getEmpty()) {
                    MtaServer mtaServ = new MtaServer(Integer.valueOf(awsInst.mtaServerId));
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
        } catch (Exception e) {
            Loggers.error(e);
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
        InstancesLogs for1 = (InstancesLogs)paramObject;
        return !for1.exists(this) ? false : (!(getInstanceId() != for1.getInstanceId()));
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
