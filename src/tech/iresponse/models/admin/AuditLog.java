package tech.iresponse.models.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class AuditLog extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "action_by", type = "text", nullable = false, length = 200)
    public String actionBy;

    @Column(name = "record_id", type = "integer", nullable = false)
    public int recordId;

    @Column(name = "record_name", type = "text", nullable = false, length = 200)
    public String recordName;

    @Column(name = "record_type", type = "text", nullable = false, length = 100)
    public String recordType;

    @Column(name = "action_type", type = "text", nullable = false, length = 100)
    public String actionType;

    @Column(name = "action_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp actionTime;

    public AuditLog() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("audit_logs");
    }

    public AuditLog(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("audit_logs");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AuditLog))
            return false;
        AuditLog int1 = (AuditLog)paramObject;
        if (!int1.exists(this))
            return false;
        if (getId() != int1.getId())
            return false;
        String str1 = getActionBy();
        String str2 = int1.getActionBy();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getRecordId() != int1.getRecordId())
            return false;
        String str3 = getRecordName();
        String str4 = int1.getRecordName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getRecordType();
        String str6 = int1.getRecordType();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getActionType();
        String str8 = int1.getActionType();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        Timestamp timestamp1 = getActionTime();
        Timestamp timestamp2 = int1.getActionTime();
        return !((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AuditLog;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getActionBy();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getRecordId();
        String str2 = getRecordName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getRecordType();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getActionType();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        Timestamp timestamp = getActionTime();
        return n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Timestamp getActionTime() {
        return actionTime;
    }

    public void setActionTime(Timestamp actionTime) {
        this.actionTime = actionTime;
    }

    @Override
    public String toString() {
        return "AuditLog(id=" + getId() + ", actionBy=" + getActionBy() + ", recordId=" + getRecordId() + ", recordName=" + getRecordName() + ", recordType=" + getRecordType() + ", actionType=" + getActionType() + ", actionTime=" + getActionTime() + ")";
    }
}
