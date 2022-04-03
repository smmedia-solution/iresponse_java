package tech.iresponse.digitalocean.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.DigitalOcean;
import tech.iresponse.models.admin.DigitalOceanProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateDropletsCreated(DigitalOceanProcess dop) {
        dop.dropletsCreated = DigitalOcean.DROPLETS_CREATED;
        dop.update();
    }

    public static synchronized void updateDropletsInstalled(DigitalOceanProcess dop) {
        dop.progress = ((1000L * DigitalOcean.DROPLETS_CREATED / dop.nbDroplets) / 10.0D) + "%";
        dop.dropletsInstalled = DigitalOcean.DROPLETS_INSTALLED;
        dop.update();
    }

    public static synchronized void finishUpdate(DigitalOceanProcess dop) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            dop.status = DigitalOcean.IS_ERROR_OCCURED ? "Error" : "Completed";
            dop.progress = "100%";
            dop.dropletsCreated = DigitalOcean.DROPLETS_CREATED;
            dop.dropletsInstalled = DigitalOcean.DROPLETS_INSTALLED;
            dop.finishTime = new Timestamp(l);
            dop.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
