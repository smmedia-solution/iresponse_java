package tech.iresponse.affiliate.affiliate;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.models.affiliate.Suppression;
import tech.iresponse.webservices.Affiliate;

public class UpdateData {

    public static synchronized void updateCounter(Suppression process, int size, int nbemail) {
        process.progress = ((1000L * Affiliate.getNbrEmailFound() / size) / 10.0D) + "%";
        process.emailsFound += nbemail;
        process.update();
        Affiliate.countEmailFound();
    }

    public static synchronized void finishUpdate(Suppression supp, boolean msg) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long time = calendar.getTimeInMillis();
            supp.status = msg ? "Error" : "Completed";
            supp.progress = "100%";
            supp.finishTime = new Timestamp(time);
            supp.update();
        } catch (Exception exception) {
            tech.iresponse.logging.Loggers.error(exception);
        }
    }
}