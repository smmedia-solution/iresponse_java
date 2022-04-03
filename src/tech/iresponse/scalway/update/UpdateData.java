package tech.iresponse.scalway.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Scaleway;
import tech.iresponse.models.admin.ScalewayProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(ScalewayProcess process) {
        process.instancesCreated = Scaleway.INSTANCES_CREATED;
        process.update();
    }

    public static synchronized void updateInstancesInstalled(ScalewayProcess process) {
        process.progress = ((1000L * Scaleway.INSTANCES_CREATED / process.nbInstances) / 10.0D) + "%";
        process.instancesInstalled = Scaleway.INSTANCES_INSTALLED;
        process.update();
    }

    public static synchronized void finishUpdate(ScalewayProcess process) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            process.status = Scaleway.IS_ERROR_OCCURED ? "Error" : "Completed";
            process.progress = "100%";
            process.instancesCreated = Scaleway.INSTANCES_CREATED;
            process.instancesInstalled = Scaleway.INSTANCES_INSTALLED;
            process.finishTime = new Timestamp(l);
            process.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
