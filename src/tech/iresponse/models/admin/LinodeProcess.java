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
public class LinodeProcess extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "text", nullable = true, length = 20)
    public String processId;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "region", type = "text", nullable = false, length = 200)
    public String region;

    @Column(name = "nb_instances", type = "integer", nullable = false)
    public int nbInstances;

    @Column(name = "domains", type = "text", nullable = false)
    public String domains;

    @Column(name = "os", type = "text", nullable = false, length = 100)
    public String os;

    @Column(name = "size", type = "text", nullable = false, length = 100)
    public String size;

    @Column(name = "progress", type = "text", nullable = false, length = 10)
    public String progress;

    @Column(name = "instances_created", type = "integer", nullable = true)
    public int instancesCreated;

    @Column(name = "instances_installed", type = "integer", nullable = true)
    public int instancesInstalled;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    public LinodeProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("linode_processes");
    }

    public LinodeProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("linode_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof LinodeProcess))
            return false;
        LinodeProcess goto1 = (LinodeProcess)paramObject;
        if (!goto1.exists(this))
            return false;
        if (getId() != goto1.getId())
            return false;
        String str1 = getStatus();
        String str2 = goto1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessId();
        String str4 = goto1.getProcessId();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getAccountId() != goto1.getAccountId())
            return false;
        String str5 = getRegion();
        String str6 = goto1.getRegion();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getNbInstances() != goto1.getNbInstances())
            return false;
        String str7 = getDomains();
        String str8 = goto1.getDomains();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getOs();
        String str10 = goto1.getOs();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getSize();
        String str12 = goto1.getSize();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProgress();
        String str14 = goto1.getProgress();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        if (getInstancesCreated() != goto1.getInstancesCreated())
            return false;
        if (getInstancesInstalled() != goto1.getInstancesInstalled())
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = goto1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = goto1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof LinodeProcess;
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
        String str3 = getRegion();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getNbInstances();
        String str4 = getDomains();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getOs();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getSize();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProgress();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        n = n * 59 + getInstancesCreated();
        n = n * 59 + getInstancesInstalled();
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getNbInstances() {
        return nbInstances;
    }

    public void setNbInstances(int nbInstances) {
        this.nbInstances = nbInstances;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public int getInstancesCreated() {
        return instancesCreated;
    }

    public void setInstancesCreated(int instancesCreated) {
        this.instancesCreated = instancesCreated;
    }

    public int getInstancesInstalled() {
        return instancesInstalled;
    }

    public void setInstancesInstalled(int instancesInstalled) {
        this.instancesInstalled = instancesInstalled;
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
        return "LinodeProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", accountId=" + getAccountId() + ", region=" + getRegion() + ", nbInstances=" + getNbInstances() + ", domains=" + getDomains() + ", os=" + getOs() + ", size=" + getSize() + ", progress=" + getProgress() + ", instancesCreated=" + getInstancesCreated() + ", instancesInstalled=" + getInstancesInstalled() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";

    }
}
