package tech.iresponse.hetzner.instance;

import java.beans.ConstructorProperties;
import java.util.List;
import tech.iresponse.models.admin.HetznerAccount;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.HetznerInstance;
import tech.iresponse.hetzner.HetznerManager;
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
            HetznerInstance htznerInstance = new HetznerInstance(Integer.valueOf(this.instanceId));
            if (!htznerInstance.getEmpty()) {
                HetznerAccount htznerAcc = new HetznerAccount(Integer.valueOf(htznerInstance.accountId));
                if (!htznerAcc.getEmpty()) {
                    HetznerManager htznerMng = new HetznerManager();
                    htznerMng.setAccount(htznerAcc);
                    switch (this.action) {
                        case "stop": {
                            htznerMng.executeAction(htznerInstance.instanceId, "shutdown");
                            htznerInstance.status = "Stopped";
                            htznerInstance.update();
                            break;
                        }
                        case "start": {
                            htznerMng.executeAction(htznerInstance.instanceId, "poweron");
                            htznerInstance.status = "Running";
                            htznerInstance.update();
                            break;
                        }
                        case "restart": {
                            htznerMng.executeAction(htznerInstance.instanceId, "shutdown");
                            ThreadSleep.sleep(10000L);
                            htznerMng.executeAction(htznerInstance.instanceId, "poweron");
                            htznerInstance.status = "Running";
                            htznerInstance.update();
                            break;
                        }
                        case "terminate": {
                            if (htznerMng.deleteInstance(htznerInstance.instanceId)) {
                                Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(htznerInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(htznerInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(htznerInstance.mtaServerId) });
                                if (vmtas != null && vmtas.isEmpty()) {
                                    for (ServerVmta vmta : vmtas) {
                                        vmta.delete();
                                    }
                                }
                            }
                            htznerInstance.delete();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
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
