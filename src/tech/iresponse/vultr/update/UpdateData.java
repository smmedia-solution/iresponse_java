package tech.iresponse.vultr.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Vultr;
import tech.iresponse.models.admin.VultrProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(VultrProcess linodePross) {
        linodePross.instancesCreated = Vultr.INSTANCES_CREATED;
        linodePross.update();
    }

    public static synchronized void updateInstancesInstalled(VultrProcess linodePross) {
        linodePross.progress = ((1000L * Vultr.INSTANCES_CREATED / linodePross.nbInstances) / 10.0D) + "%";
        linodePross.instancesInstalled = Vultr.INSTANCES_INSTALLED;
        linodePross.update();
    }

    public static synchronized void finishUpdate(VultrProcess linodePross) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            linodePross.status = Vultr.IS_ERROR_OCCURED ? "Error" : "Completed";
            linodePross.progress = "100%";
            linodePross.instancesCreated = Vultr.INSTANCES_CREATED;
            linodePross.instancesInstalled = Vultr.INSTANCES_INSTALLED;
            linodePross.finishTime = new Timestamp(l);
            linodePross.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
