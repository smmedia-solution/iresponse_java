package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.orm.Database;

public class MtaDropsUpdater extends Thread {

    private int mtaDropId;

    public void run() {
        try {
            if (this.mtaDropId > 0){
                Database.get("system").executeUpdate("UPDATE production.mta_processes d SET delivered = COALESCE((SELECT SUM(ips.delivered) FROM production.mta_processes_ips ips WHERE ips.process_id = d.id),0) , soft_bounced = COALESCE((SELECT SUM(ips.soft_bounced) FROM production.mta_processes_ips ips WHERE ips.process_id = d.id),0) , hard_bounced = COALESCE((SELECT SUM(ips.hard_bounced) FROM production.mta_processes_ips ips WHERE ips.process_id = d.id),0) WHERE id = " + this.mtaDropId, null, 0);
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    public MtaDropsUpdater() {}

    @ConstructorProperties({"mtaDropId"})
    public MtaDropsUpdater(int mtaDropId) {
        this.mtaDropId = mtaDropId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaDropsUpdater))
        return false;
        MtaDropsUpdater try1 = (MtaDropsUpdater)paramObject;
        return !try1.exists(this) ? false : (!(getMtaDropId() != try1.getMtaDropId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaDropsUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getMtaDropId();
    }

    public int getMtaDropId() {
        return mtaDropId;
    }

    public void setMtaDropId(int mtaDropId) {
        this.mtaDropId = mtaDropId;
    }

    @Override
    public String toString() {
        return "MtaDropsUpdater(mtaDropId=" + getMtaDropId() + ")";
    }
}
