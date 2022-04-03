package tech.iresponse.hetzner.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Hetzner;
import tech.iresponse.models.admin.HetznerProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(HetznerProcess process) {
        process.instancesCreated = Hetzner.INSTANCES_CREATED;
        process.update();
    }

    public static synchronized void updateInstancesInstalled(HetznerProcess process) {
        process.progress = ((1000L * Hetzner.INSTANCES_CREATED / process.nbInstances) / 10.0D) + "%";
        process.instancesInstalled = Hetzner.INSTANCES_INSTALLED;
        process.update();
    }

    public static synchronized void finishUpdate(HetznerProcess process) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            process.status = Hetzner.IS_ERROR_OCCURED ? "Error" : "Completed";
            process.progress = "100%";
            process.instancesCreated = Hetzner.INSTANCES_CREATED;
            process.instancesInstalled = Hetzner.INSTANCES_INSTALLED;
            process.finishTime = new Timestamp(l);
            process.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
