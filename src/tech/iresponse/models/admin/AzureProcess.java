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
public class AzureProcess extends ActiveRecord implements Serializable {

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

    @Column(name = "region", type = "text", nullable = false)
    public String region;

    @Column(name = "region_name", type = "text", nullable = false)
    public String regionName;

    @Column(name = "nb_instances", type = "integer", nullable = false)
    public int nbInstances;

    @Column(name = "nb_private_ips", type = "integer", nullable = false)
    public int nbPrivateIps;

    @Column(name = "domains", type = "text", nullable = false)
    public String domains;

    @Column(name = "instance_type", type = "text", nullable = false, length = 100)
    public String instanceType;

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

    public AzureProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("azure_processes");
    }

    public AzureProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("azure_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AzureProcess))
            return false;
        AzureProcess long1 = (AzureProcess)paramObject;
        if (!long1.exists(this))
            return false;
        if (getId() != long1.getId())
            return false;
        String str1 = getStatus();
        String str2 = long1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessId();
        String str4 = long1.getProcessId();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getAccountId() != long1.getAccountId())
            return false;
        String str5 = getAccountName();
        String str6 = long1.getAccountName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getRegion();
        String str8 = long1.getRegion();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getRegionName();
        String str10 = long1.getRegionName();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getNbInstances() != long1.getNbInstances())
            return false;
        if (getNbPrivateIps() != long1.getNbPrivateIps())
            return false;
        String str11 = getDomains();
        String str12 = long1.getDomains();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getInstanceType();
        String str14 = long1.getInstanceType();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProgress();
        String str16 = long1.getProgress();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        if (getInstancesCreated() != long1.getInstancesCreated())
            return false;
        if (getInstancesInstalled() != long1.getInstancesInstalled())
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = long1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = long1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AzureProcess;
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
        String str4 = getRegion();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getRegionName();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getNbInstances();
        n = n * 59 + getNbPrivateIps();
        String str6 = getDomains();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getInstanceType();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getProgress();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public int getNbInstances() {
        return nbInstances;
    }

    public void setNbInstances(int nbInstances) {
        this.nbInstances = nbInstances;
    }

    public int getNbPrivateIps() {
        return nbPrivateIps;
    }

    public void setNbPrivateIps(int nbPrivateIps) {
        this.nbPrivateIps = nbPrivateIps;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
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
        return "AzureProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", region=" + getRegion() + ", regionName=" + getRegionName() + ", nbInstances=" + getNbInstances() + ", nbPrivateIps=" + getNbPrivateIps() + ", domains=" + getDomains() + ", instanceType=" + getInstanceType() + ", progress=" + getProgress() + ", instancesCreated=" + getInstancesCreated() + ", instancesInstalled=" + getInstancesInstalled() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}
