package tech.iresponse.linode.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Linode;
import tech.iresponse.models.admin.LinodeProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(LinodeProcess linodePross) {
        linodePross.instancesCreated = Linode.INSTANCES_CREATED;
        linodePross.update();
    }

    public static synchronized void updateInstancesInstalled(LinodeProcess linodePross) {
        linodePross.progress = ((1000L * Linode.INSTANCES_CREATED / linodePross.nbInstances) / 10.0D) + "%";
        linodePross.instancesInstalled = Linode.INSTANCES_INSTALLED;
        linodePross.update();
    }

    public static synchronized void finishUpdate(LinodeProcess linodePross) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            linodePross.status = Linode.IS_ERROR_OCCURED ? "Error" : "Completed";
            linodePross.progress = "100%";
            linodePross.instancesCreated = Linode.INSTANCES_CREATED;
            linodePross.instancesInstalled = Linode.INSTANCES_INSTALLED;
            linodePross.finishTime = new Timestamp(l);
            linodePross.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
