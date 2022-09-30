package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class StopAutoChangeIps extends Thread {

    private AzureInstance instance;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            if (this.instance.getEmpty()){
                throw new DatabaseException("Instance with id " + this.instance.getId() + " not found !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.instance.mtaServerId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("MtaServer with id " + this.instance.mtaServerId + " not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh != null && ssh.isConnected()){
                ssh.cmd2("kill -9 $(ps aux | grep azure_manager.py | grep -v 'grep' | awk '{print $2}')");
            }

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"instance"})
    public StopAutoChangeIps(AzureInstance instance) {
        this.instance = instance;
    }

    public AzureInstance getInstance() {
        return instance;
    }

    public void setInstance(AzureInstance instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof StopAutoChangeIps))
            return false;
        StopAutoChangeIps long1 = (StopAutoChangeIps)paramObject;
        if (!long1.exists(this))
            return false;
        AzureInstance goto1 = getInstance();
        AzureInstance goto2 = long1.getInstance();
        return !((goto1 == null) ? (goto2 != null) : !goto1.equals(goto2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof StopAutoChangeIps;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureInstance goto1 = getInstance();
        return n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
    }

    @Override
    public String toString() {
        return "StopAutoChangeIps(instance=" + getInstance() + ")";
    }
}
