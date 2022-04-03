package tech.iresponse.vultr.instance;

import java.beans.ConstructorProperties;
import java.util.List;
import tech.iresponse.models.admin.VultrAccount;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.VultrInstance;
import tech.iresponse.vultr.VultrManager;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.logging.Loggers;

public class InstancesActions extends Thread {

    private int instanceId;
    private String action;

    @Override
    public void run() {
        try {
            VultrInstance vltrInstance = new VultrInstance(Integer.valueOf(this.instanceId));
            if (!vltrInstance.getEmpty()) {
                VultrAccount vltrAcc = new VultrAccount(Integer.valueOf(vltrInstance.accountId));
                if (!vltrAcc.getEmpty()) {
                    VultrManager vltrMng = new VultrManager();
                    vltrMng.setAccount(vltrAcc);
                    switch (this.action) {
                        case "stop":{
                            vltrMng.executeAction(vltrInstance.instanceId, "halt");
                            vltrInstance.status = "Stopped";
                            vltrInstance.update();
                            break;
                        }
                        case "start":{
                            vltrMng.executeAction(vltrInstance.instanceId, "start");
                            vltrInstance.status = "Running";
                            vltrInstance.update();
                            break;
                        }
                        case "reboot":{
                            vltrMng.executeAction(vltrInstance.instanceId, "start");
                            vltrInstance.status = "Running";
                            vltrInstance.update();
                            break;
                        }
                        case "terminate":{
                            if (vltrMng.executeAction(vltrInstance.instanceId, "destroy")) {
                                Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(vltrInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(vltrInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(vltrInstance.mtaServerId) });
                                if (vmtas != null && vmtas.isEmpty())
                                for (ServerVmta vmta : vmtas){
                                    vmta.delete();
                                }
                            }
                            vltrInstance.delete();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"instanceId", "action"})
    public InstancesActions(int instanceId, String action) {
        this.instanceId = instanceId;
        this.action = action;
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
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesActions;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getInstanceId();
        String str = getAction();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
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

    @Override
    public String toString() {
        return "InstancesActions(instanceId=" + getInstanceId() + ", action=" + getAction() + ")";
    }
}
