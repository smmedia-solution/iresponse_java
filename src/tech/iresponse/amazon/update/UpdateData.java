package tech.iresponse.amazon.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;
import tech.iresponse.webservices.Amazon;
import tech.iresponse.models.admin.AwsProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateIstanceCreated(AwsProcess aws) {
        aws.instancesCreated = Amazon.INSTANCES_CREATED;
        aws.update();
    }

    public static synchronized void updateInstanceInstalled(AwsProcess aws) {
        aws.progress = ((1000L * Amazon.INSTANCES_INSTALLED / (aws.nbInstances * (aws.regions.split(Pattern.quote(","))).length)) / 10.0D) + "%";
        aws.instancesInstalled = Amazon.INSTANCES_INSTALLED;
        aws.update();
    }

    public static synchronized void finishUpdate(AwsProcess aws) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long time = calendar.getTimeInMillis();
            aws.status = Amazon.IS_ERROR_OCCURED ? "Error" : "Completed";
            aws.progress = "100%";
            aws.instancesCreated = Amazon.INSTANCES_CREATED;
            aws.instancesInstalled = Amazon.INSTANCES_INSTALLED;
            aws.finishTime = new Timestamp(time);
            aws.update();
        } catch (Exception e) {
            Loggers.error(e);
        }
    }
}
