package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.models.production.MtaProcess;
import tech.iresponse.models.production.SmtpProcess;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.SaveAuditLog;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;

public class ActionExecuter extends Thread {

    private int processId;
    private String action;
    private String type;

    public void run() {
        try {
            Object process = this.type.contains("m") ? new MtaProcess(Integer.valueOf(this.processId)) : new SmtpProcess(Integer.valueOf(this.processId));
            String processId = this.type.contains("m") ? ((MtaProcess)process).processId : ((SmtpProcess)process).processId;
            String status = this.type.contains("m") ? ((MtaProcess)process).status : ((SmtpProcess)process).status;

            switch (this.action) {
                case "stop":
                    if (!"In Progress".equalsIgnoreCase(status) && !"Paused".equalsIgnoreCase(status)){
                        throw new DatabaseException("This process is not in progress !");
                    }
                    Terminal.killProcess(processId);
                    if (this.type.contains("m")) {
                        ((MtaProcess)process).status = "Interrupted";
                        ((MtaProcess)process).finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                        break;
                    }
                    ((SmtpProcess)process).status = "Interrupted";
                    ((SmtpProcess)process).finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                    break;
                case "pause":
                    if (!"In Progress".equalsIgnoreCase(status)){
                        throw new DatabaseException("This process is not in progress !");
                    }
                    processId = processId.contains("_") ? processId.split("_")[0] : processId;
                    Terminal.executeCommand("kill -STOP " + processId);
                    if (this.type.contains("m")) {
                        ((MtaProcess)process).status = "Paused";
                        break;
                    }
                    ((SmtpProcess)process).status = "Paused";
                    break;
                case "resume":
                    if (!"Paused".equalsIgnoreCase(status)){
                        throw new DatabaseException("This process is not paused !");
                    }
                    processId = processId.contains("_") ? processId.split("_")[0] : processId;
                    Terminal.executeCommand("kill -CONT " + processId);
                    if (this.type.contains("m")) {
                        ((MtaProcess)process).status= "In Progress";
                        break;
                    }
                    ((SmtpProcess)process).status = "In Progress";
                    break;
            }

            if (this.type.contains("m")) {
                ((MtaProcess)process).update();
            } else {
                ((SmtpProcess)process).update();
            }

            SaveAuditLog.insertAuditLog(this.processId, this.type.contains("m") ? "MtaProcess" : "SmtpProcess", "Execute Action", StringUtils.capitalize(this.action));

        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"processId", "action", "type"})
    public ActionExecuter(int processId, String action, String type) {
        this.processId = processId;
        this.action = action;
        this.type = type;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ActionExecuter))
            return false;
        ActionExecuter do1 = (ActionExecuter)paramObject;
        if (!do1.exists(this))
            return false;
        if (getProcessId() != do1.getProcessId())
            return false;
        String str1 = getAction();
        String str2 = do1.getAction();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getType();
        String str4 = do1.getType();
        return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ActionExecuter;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getProcessId();
        String str1 = getAction();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getType();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ActionExecuter(processId=" + getProcessId() + ", action=" + getAction() + ", type=" + getType() + ")";
    }
}
