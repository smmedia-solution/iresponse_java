package tech.iresponse.digitalocean.droplet;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import tech.iresponse.models.admin.DigitalOceanAccount;
import tech.iresponse.models.admin.DigitalOceanDroplet;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.logging.Loggers;
import tech.iresponse.digitalocean.DigitalOceanManager;

public class DropletsActions extends Thread {

    private int dropletId;
    private String action;

    @Override
    public void run() {
        try {
            DigitalOceanDroplet doDroplet = new DigitalOceanDroplet(Integer.valueOf(this.dropletId));
            if (!doDroplet.getEmpty()) {

                DigitalOceanAccount doAccount = new DigitalOceanAccount(Integer.valueOf(doDroplet.accountId));
                if (!doAccount.getEmpty()) {
                    DigitalOceanManager doManager = new DigitalOceanManager();
                    doManager.setAccount(doAccount);

                    switch (this.action) {
                        case "start":{
                            if (doManager.executeAction(doDroplet.dropletId, "power_on")) {
                                HashMap hashMap = null;
                                for (int b = 0; b < 10; b++) {
                                    ThreadSleep.sleep(6000L);
                                    hashMap = doManager.getDropletInfos(doDroplet.dropletId);
                                    if ("active".equals(hashMap.get("status"))){
                                        break;
                                    }
                                }
                            }
                            doDroplet.status = "Running";
                            doDroplet.update();
                            break;
                        }
                        case "stop":{
                            if (doManager.executeAction(doDroplet.dropletId, "power_off")) {
                                HashMap hashMap = null;
                                for (int b = 0; b < 10; b++) {
                                    ThreadSleep.sleep(6000L);
                                    hashMap = doManager.getDropletInfos(doDroplet.dropletId);
                                    if ("off".equals(hashMap.get("status"))){
                                        break;
                                    }
                                }
                            }
                            doDroplet.status = "Stopped";
                            doDroplet.update();
                            break;
                        }
                        case "restart":{
                            if (doManager.executeAction(doDroplet.dropletId, "reboot")) {
                                HashMap hashMap = null;
                                for (int b = 0; b < 20; b++) {
                                    ThreadSleep.sleep(6000L);
                                    hashMap = doManager.getDropletInfos(doDroplet.dropletId);
                                    if ("active".equals(hashMap.get("status"))){
                                        break;
                                    }
                                }
                            }
                            doDroplet.status = "Running";
                            doDroplet.update();
                            break;
                        }
                        case "terminate":{
                            if (doManager.deleteDroplet(doDroplet.dropletId)) {
                                Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(doDroplet.mtaServerId) }, Connector.AFFECTED_ROWS);
                                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(doDroplet.mtaServerId) }, Connector.AFFECTED_ROWS);
                                List<ServerVmta> serverVmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(doDroplet.mtaServerId) });
                                if (serverVmtas != null && serverVmtas.isEmpty()){
                                    for (ServerVmta vmta : serverVmtas){
                                        vmta.delete();
                                    }
                                }
                            }
                            doDroplet.delete();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"dropletId", "action"})
    public DropletsActions(int dropletId, String action) {
        this.dropletId = dropletId;
        this.action = action;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DropletsActions))
            return false;
        DropletsActions do1 = (DropletsActions)paramObject;
        if (!do1.exists(this))
            return false;
        if (getDropletId() != do1.getDropletId())
            return false;
        String str1 = getAction();
        String str2 = do1.getAction();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DropletsActions;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getDropletId();
        String str = getAction();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getDropletId() {
        return dropletId;
    }

    public void setDropletId(int dropletId) {
        this.dropletId = dropletId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "DropletsActions(dropletId=" + getDropletId() + ", action=" + getAction() + ")";
    }
}
