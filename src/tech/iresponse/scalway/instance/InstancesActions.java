package tech.iresponse.scalway.instance;

import java.beans.ConstructorProperties;
import java.util.List;
import tech.iresponse.models.admin.ScalewayAccount;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.ScalewayInstance;
import tech.iresponse.scalway.ScalewayManager;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.logging.Loggers;

public class InstancesActions extends Thread {

    private int instanceId;
    private String action;

    @Override
    public void run() {
        try {
            ScalewayInstance sclwyInstance = new ScalewayInstance(Integer.valueOf(this.instanceId));
            if (!sclwyInstance.getEmpty()) {
                ScalewayAccount sclwyAcc = new ScalewayAccount(Integer.valueOf(sclwyInstance.accountId));
                if (!sclwyAcc.getEmpty()) {
                    ScalewayManager sclwyMng = new ScalewayManager();
                    sclwyMng.setAccount(sclwyAcc);
                    switch (this.action) {
                        case "stop":{
                            sclwyMng.executeAction(sclwyInstance.instanceId, sclwyInstance.region, "poweroff");
                            sclwyInstance.status = "Stopped";
                            sclwyInstance.update();
                            break;
                        }
                        case "start":{
                            sclwyMng.executeAction(sclwyInstance.instanceId, sclwyInstance.region, "poweron");
                            sclwyInstance.status = "Running";
                            sclwyInstance.update();
                            break;
                        }
                        case "reboot":{
                            sclwyMng.executeAction(sclwyInstance.instanceId, sclwyInstance.region, "reboot");
                            sclwyInstance.status = "Running";
                            sclwyInstance.update();
                            break;
                        }
                        case "restart":{
                            sclwyMng.executeAction(sclwyInstance.instanceId, sclwyInstance.region, "reboot");
                            sclwyInstance.status = "Running";
                            sclwyInstance.update();
                            break;
                        }
                        case "terminate":{
                            if (sclwyMng.executeAction(sclwyInstance.instanceId, sclwyInstance.region, "terminate")) {
                                Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(sclwyInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(sclwyInstance.mtaServerId) }, Connector.AFFECTED_ROWS);
                                List<ServerVmta> list = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(sclwyInstance.mtaServerId) });
                                if (list != null && list.isEmpty()){
                                    for (ServerVmta vmta : list){
                                        vmta.delete();
                                    }
                                }
                            }
                            sclwyInstance.delete();
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
