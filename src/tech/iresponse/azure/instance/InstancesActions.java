package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.azure.AzureManager;

public class InstancesActions extends Thread {

    private int instanceId;
    private String action;
    private boolean changeIps;

    @Override
    public void run() {
        try {
            AzureInstance azureInstnce = new AzureInstance(Integer.valueOf(this.instanceId));
            if (azureInstnce.getEmpty()){
                throw new DatabaseException("Instance with id " + this.instanceId + " not found !");
            }

            AzureAccount azureAcc = new AzureAccount(Integer.valueOf(azureInstnce.accountId));
            if (azureAcc.getEmpty()){
                throw new DatabaseException("Account with id " + azureInstnce.accountId + " not found !");
            }

            AzureManager AzureMng = new AzureManager(azureAcc);
            if (!AzureMng.authenticate()){
                throw new DatabaseException("Could not connect to azure account " + azureAcc.name + " !");
            }

            switch (this.action) {
                case "stop":{
                    AzureMng.executeActions(azureInstnce.getName() + "_res_gr", azureInstnce.getName(), this.action, this.changeIps);
                    azureInstnce.status = "Stopped";
                    azureInstnce.update();
                    break;
                }
                case "start":{
                    AzureMng.executeActions(azureInstnce.getName() + "_res_gr", azureInstnce.getName(), this.action, this.changeIps);
                    azureInstnce.status = "Running";
                    azureInstnce.update();
                    break;
                }
                case "restart":{
                    AzureMng.executeActions(azureInstnce.getName() + "_res_gr", azureInstnce.getName(), this.action, this.changeIps);
                    azureInstnce.status = "Running";
                    azureInstnce.update();
                    break;
                }
                case "terminate":{
                    AzureMng.deleteResourceGroupsByName(azureInstnce.getName() + "_res_gr");
                    Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(azureInstnce.mtaServerId) }, Connector.AFFECTED_ROWS);
                    Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(azureInstnce.mtaServerId) }, Connector.AFFECTED_ROWS);
                    Database.get("system").executeUpdate("DELETE FROM admin.servers_vmtas WHERE mta_server_id = ?", new Object[] { Integer.valueOf(azureInstnce.mtaServerId) }, Connector.AFFECTED_ROWS);
                    azureInstnce.delete();
                    break;
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    @ConstructorProperties({"instanceId", "action", "changeIps"})
    public InstancesActions(int instanceId, String action, boolean changeIps) {
        this.instanceId = instanceId;
        this.action = action;
        this.changeIps = changeIps;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesActions))
            return false;
        InstancesActions do1 = (InstancesActions)paramObject;
        if (!do1.exists(this))
            return false;
        if (getInstanceId() != do1.getInstanceId())
            return false;
        String str1 = getAction();
        String str2 = do1.getAction();
        return ((str1 == null) ? (str2 != null) : !str1.equals(str2)) ? false : (!(isChangeIps() != do1.isChangeIps()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesActions;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getInstanceId();
        String str = getAction();
        n = n * 59 + ((str == null) ? 43 : str.hashCode());
        return n * 59 + (isChangeIps() ? 79 : 97);
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isChangeIps() {
        return changeIps;
    }

    public void setChangeIps(boolean changeIps) {
        this.changeIps = changeIps;
    }

    @Override
    public String toString() {
        return "InstancesActions(instanceId=" + getInstanceId() + ", action=" + getAction() + ", changeIps=" + isChangeIps() + ")";
    }
}
