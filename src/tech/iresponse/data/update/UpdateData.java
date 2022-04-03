package tech.iresponse.data.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.DataLists;
import tech.iresponse.models.lists.BlackList;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateCounter(BlackList blckList, int size, int nbEmail) throws Exception {
        blckList.progress = ((1000L * DataLists.getCount() / size) / 10.0D) + "%";
        blckList.emailsFound += nbEmail;
        blckList.update();
        DataLists.updateCount();
    }

    public static synchronized void finishUpdate(BlackList blckList, boolean error) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            blckList.status = error ? "Interrupted" : "Completed";
            blckList.progress = "100%";
            blckList.finishTime = new Timestamp(l);
            blckList.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}

