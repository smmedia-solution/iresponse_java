package tech.iresponse.models.lists;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class BlackList extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "integer", nullable = true)
    public int processId;

    @Column(name = "emails_found", type = "integer", nullable = true)
    public int emailsFound;

    @Column(name = "progress", type = "text", nullable = false, length = 10)
    public String progress;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    public BlackList() throws Exception {
        setDatabase("system");
        setSchema("lists");
        setTable("blacklists");
    }

    public BlackList(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("lists");
        setTable("blacklists");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof BlackList))
            return false;
        BlackList do1 = (BlackList)paramObject;
        if (!do1.exists(this))
            return false;
        if (getId() != do1.getId())
        return false;
        String str1 = getStatus();
        String str2 = do1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getProcessId() != do1.getProcessId())
            return false;
        if (getEmailsFound() != do1.getEmailsFound())
            return false;
        String str3 = getProgress();
        String str4 = do1.getProgress();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = do1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = do1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof BlackList;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getProcessId();
        n = n * 59 + getEmailsFound();
        String str2 = getProgress();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        Timestamp timestamp1 = getStartTime();
        n = n * 59 + ((timestamp1 == null) ? 43 : timestamp1.hashCode());
        Timestamp timestamp2 = getFinishTime();
        return n * 59 + ((timestamp2 == null) ? 43 : timestamp2.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    public int getProcessId() {
        return this.processId;
    }

    public int getEmailsFound() {
        return this.emailsFound;
    }

    public String getProgress() {
        return this.progress;
    }

    public Timestamp getStartTime() {
        return this.startTime;
    }

    public Timestamp getFinishTime() {
        return this.finishTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public void setEmailsFound(int emailsFound) {
        this.emailsFound = emailsFound;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "BlackList(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", emailsFound=" + getEmailsFound() + ", progress=" + getProgress() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}