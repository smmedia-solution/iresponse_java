package tech.iresponse.azure.update;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import tech.iresponse.webservices.Azure;
import tech.iresponse.models.admin.AzureProcess;
import tech.iresponse.logging.Loggers;

public class UpdateData {

    public static synchronized void updateInstancesCreated(AzureProcess azureProcss) {
        azureProcss.instancesCreated = Azure.INSTANCES_CREATED;
        azureProcss.update();
    }

    public static synchronized void updateInstancesInstalled(AzureProcess azureProcss) {
        azureProcss.progress = ((1000L * Azure.INSTANCES_CREATED / azureProcss.nbInstances) / 10.0D) + "%";
        azureProcss.instancesInstalled = Azure.INSTANCES_INSTALLED;
        azureProcss.update();
    }

    public static synchronized void finishUpdate(AzureProcess azureProcss) {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long l = calendar.getTimeInMillis();
            azureProcss.status = Azure.IS_ERROR_OCCURED ? "Error" : "Completed";
            azureProcss.progress = "100%";
            azureProcss.instancesCreated = Azure.INSTANCES_CREATED;
            azureProcss.instancesInstalled = Azure.INSTANCES_INSTALLED;
            azureProcss.finishTime = new Timestamp(l);
            azureProcss.update();
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }
}

