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
public class AwsProcess extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "text", nullable = true, length = 20)
    public String processId;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = true, length = 100)
    public String accountName;

    @Column(name = "regions", type = "text", nullable = false)
    public String regions;

    @Column(name = "nb_instances", type = "integer", nullable = false)
    public int nbInstances;

    @Column(name = "nb_private_ips", type = "integer", nullable = false)
    public int nbPrivateIps;

    @Column(name = "storage", type = "integer", nullable = false)
    public int storage;

    @Column(name = "domains", type = "text", nullable = false)
    public String domains;

    @Column(name = "os", type = "text", nullable = false, length = 100)
    public String os;

    @Column(name = "instance_type", type = "text", nullable = false, length = 100)
    public String instanceType;

    @Column(name = "subnets_filter", type = "text", nullable = true)
    public String subnetsFilter;

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

    public AwsProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("aws_processes");
    }

    public AwsProcess(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("aws_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AwsProcess))
            return false;
        AwsProcess case1 = (AwsProcess) paramObject;
        if (!case1.exists(this))
            return false;
        if (getId() != case1.getId())
            return false;
        String str1 = getStatus();
        String str2 = case1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String pid1 = getProcessId();
        String pid2 = case1.getProcessId();
        if ((pid1 == null) ? (pid2 != null) : !pid1.equals(pid2))
            return false;
        if (getAccountId() != case1.getAccountId())
            return false;
        String str3 = getAccountName();
        String str4 = case1.getAccountName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getRegions();
        String str6 = case1.getRegions();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getNbPrivateIps() != case1.getNbPrivateIps())
            return false;
        if (getStorage() != case1.getStorage())
            return false;
        String str7 = getDomains();
        String str8 = case1.getDomains();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getOs();
        String str10 = case1.getOs();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getInstanceType();
        String str12 = case1.getInstanceType();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getSubnetsFilter();
        String str14 = case1.getSubnetsFilter();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProgress();
        String str16 = case1.getProgress();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        if (getInstancesCreated() != case1.getInstancesCreated())
            return false;
        if (getInstancesInstalled() != case1.getInstancesInstalled())
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = case1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = case1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof AwsProcess;
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
        String str3 = getRegions();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getNbInstances();
        n = n * 59 + getNbPrivateIps();
        n = n * 59 + getStorage();
        String str4 = getDomains();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getOs();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getInstanceType();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getSubnetsFilter();
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

    public String getRegions() {
        return this.regions;
    }

    public int getNbInstances() {
        return this.nbInstances;
    }

    public int getNbPrivateIps() {
        return this.nbPrivateIps;
    }

    public int getStorage() {
        return this.storage;
    }

    public String getDomains() {
        return this.domains;
    }

    public String getOs() {
        return this.os;
    }

    public String getInstanceType() {
        return this.instanceType;
    }

    public String getSubnetsFilter() {
        return this.subnetsFilter;
    }

    public String getProgress() {
        return this.progress;
    }

    public int getInstancesCreated() {
        return this.instancesCreated;
    }

    public int getInstancesInstalled() {
        return this.instancesInstalled;
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

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public void setNbInstances(int nbInstances) {
        this.nbInstances = nbInstances;
    }

    public void setNbPrivateIps(int nbPrivateIps) {
        this.nbPrivateIps = nbPrivateIps;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public void setSubnetsFilter(String subnetsFilter) {
        this.subnetsFilter = subnetsFilter;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setInstancesCreated(int instancesCreated) {
        this.instancesCreated = instancesCreated;
    }

    public void setInstancesInstalled(int instancesInstalled) {
        this.instancesInstalled = instancesInstalled;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "AwsProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", regions=" + getRegions() + ", nbInstances=" + getNbInstances() + ", nbPrivateIps=" + getNbPrivateIps() + ", storage=" + getStorage() + ", domains=" + getDomains() + ", os=" + getOs() + ", instanceType=" + getInstanceType() + ", subnetsFilter=" + getSubnetsFilter() + ", progress=" + getProgress() + ", instancesCreated=" + getInstancesCreated() + ", instancesInstalled=" + getInstancesInstalled() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}
