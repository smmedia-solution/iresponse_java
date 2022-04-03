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
public class AwsAccountProcess extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "text", nullable = true, length = 20)
    public String processId;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = false, length = 100)
    public String accountName;

    @Column(name = "region", type = "text", nullable = false, length = 100)
    public String region;

    @Column(name = "process_type", type = "text", nullable = false, length = 100)
    public String processType;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    public AwsAccountProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("aws_accounts_processes");
    }

    public AwsAccountProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("aws_accounts_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AwsAccountProcess))
            return false;
        AwsAccountProcess try1 = (AwsAccountProcess)paramObject;
        if (!try1.exists(this))
            return false;
        if (getId() != try1.getId())
            return false;
        String str1 = getStatus();
        String str2 = try1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String pid1 = getProcessId();
        String pid2 = try1.getProcessId();
        if ((pid1 == null) ? (pid2 != null) : !pid1.equals(pid2))
            return false;
        if (getAccountId() != try1.getAccountId())
            return false;
        String str3 = getAccountName();
        String str4 = try1.getAccountName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getRegion();
        String str6 = try1.getRegion();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getProcessType();
        String str8 = try1.getProcessType();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = try1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = try1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof AwsAccountProcess;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String pid = getProcessId();
        n = n * 59 + ((pid == null) ? 43 : pid.hashCode());
        n = n * 59 + getAccountId();
        String str2 = getAccountName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getRegion();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getProcessType();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
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

    public String getProcessId() {
        return this.processId;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getRegion() {
        return this.region;
    }

    public String getProcessType() {
        return this.processType;
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

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "AwsAccountProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", region=" + getRegion() + ", processType=" + getProcessType() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}