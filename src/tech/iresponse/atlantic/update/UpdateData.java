package tech.iresponse.atlantic.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Atlantic;
import tech.iresponse.models.admin.AtlanticProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(AtlanticProcess process) {
        process.instancesCreated = Atlantic.INSTANCES_CREATED;
        process.update();
    }

    public static synchronized void updateInstancesInstalled(AtlanticProcess process) {
        process.progress = ((1000L * Atlantic.INSTANCES_CREATED / process.nbInstances) / 10.0D) + "%";
        process.instancesInstalled = Atlantic.INSTANCES_INSTALLED;
        process.update();
    }

    public static synchronized void finishUpdate(AtlanticProcess process) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            process.status = Atlantic.IS_ERROR_OCCURED ? "Error" : "Completed";
            process.progress = "100%";
            process.instancesCreated = Atlantic.INSTANCES_CREATED;
            process.instancesInstalled = Atlantic.INSTANCES_INSTALLED;
            process.finishTime = new Timestamp(l);
            process.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
