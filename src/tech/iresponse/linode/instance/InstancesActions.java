package tech.iresponse.linode.instance;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import tech.iresponse.models.admin.LinodeAccount;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.LinodeInstance;
import tech.iresponse.linode.LinodeManager;
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
            LinodeInstance linodeInstce = new LinodeInstance(Integer.valueOf(this.instanceId));
            if (!linodeInstce.getEmpty()) {
                LinodeAccount linodeAcc = new LinodeAccount(Integer.valueOf(linodeInstce.accountId));
                if (!linodeAcc.getEmpty()) {
                    LinodeManager linodeMnger = new LinodeManager();
                    linodeMnger.setAccount(linodeAcc);
                    switch (this.action) {
                        case "shutdown":{
                            if (linodeMnger.executeAction(linodeInstce.instanceId, "shutdown")) {
                                HashMap hashMap = null;
                                for (int b = 0; b < 10; b++) {
                                    ThreadSleep.sleep(6000L);
                                    hashMap = linodeMnger.getInstanceInfos(linodeInstce.instanceId);
                                    if ("shutting_down".equals(hashMap.get("status"))){
                                        break;
                                    }
                                }
                            }
                            linodeInstce.status = "shutdown";
                            linodeInstce.update();
                        }
                        case "reboot":{
                            if (linodeMnger.executeAction(linodeInstce.instanceId, "reboot")) {
                                HashMap hashMap = null;
                                for (int b = 0; b < 10; b++) {
                                    ThreadSleep.sleep(6000L);
                                    hashMap = linodeMnger.getInstanceInfos(linodeInstce.instanceId);{
                                        if ("running".equals(hashMap.get("status")));
                                    }
                                    break;
                                }
                            }
                            linodeInstce.status = "Running";
                            linodeInstce.update();
                        }
                        case "terminate":{
                            if (linodeMnger.deleteInstance(linodeInstce.instanceId)) {
                                Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(linodeInstce.mtaServerId) }, Connector.AFFECTED_ROWS);
                                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(linodeInstce.mtaServerId) }, Connector.AFFECTED_ROWS);
                                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(linodeInstce.mtaServerId) });
                                if (vmtas != null && vmtas.isEmpty())
                                for (ServerVmta vmta : vmtas){
                                    vmta.delete();
                                }
                            }
                            linodeInstce.delete();
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
