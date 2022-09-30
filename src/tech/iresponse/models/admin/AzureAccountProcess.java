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
public class AzureAccountProcess extends ActiveRecord implements Serializable {

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

    @Column(name = "regions", type = "text", nullable = false)
    public String regions;

    @Column(name = "process_type", type = "text", nullable = false, length = 100)
    public String processType;

    @Column(name = "processtime_unit", type = "text", nullable = false, length = 100)
    public String processtimeUnit;

    @Column(name = "processtime_value", type = "text", nullable = false, length = 100)
    public String processtimeValue;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    public AzureAccountProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("azure_accounts_processes");
    }

    public AzureAccountProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("azure_accounts_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AzureAccountProcess))
            return false;
        AzureAccountProcess else1 = (AzureAccountProcess)paramObject;
        if (!else1.exists(this))
            return false;
        if (getId() != else1.getId())
            return false;
        String str1 = getStatus();
        String str2 = else1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessId();
        String str4 = else1.getProcessId();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getAccountId() != else1.getAccountId())
            return false;
        String str5 = getAccountName();
        String str6 = else1.getAccountName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getRegions();
        String str8 = else1.getRegions();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getProcessType();
        String str10 = else1.getProcessType();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getProcesstimeUnit();
        String str12 = else1.getProcesstimeUnit();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProcesstimeValue();
        String str14 = else1.getProcesstimeValue();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = else1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = else1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AzureAccountProcess;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getProcessId();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getAccountId();
        String str3 = getAccountName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getRegions();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getProcessType();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getProcesstimeUnit();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProcesstimeValue();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        Timestamp timestamp1 = getStartTime();
        n = n * 59 + ((timestamp1 == null) ? 43 : timestamp1.hashCode());
        Timestamp timestamp2 = getFinishTime();
        return n * 59 + ((timestamp2 == null) ? 43 : timestamp2.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getProcesstimeUnit() {
        return processtimeUnit;
    }

    public void setProcesstimeUnit(String processtimeUnit) {
        this.processtimeUnit = processtimeUnit;
    }

    public String getProcesstimeValue() {
        return processtimeValue;
    }

    public void setProcesstimeValue(String processtimeValue) {
        this.processtimeValue = processtimeValue;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "AzureAccountProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", regions=" + getRegions() + ", processType=" + getProcessType() + ", processtimeUnit=" + getProcesstimeUnit() + ", processtimeValue=" + getProcesstimeValue() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}
