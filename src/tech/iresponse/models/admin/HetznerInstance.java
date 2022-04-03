package tech.iresponse.models.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Date;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class HetznerInstance extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "instance_id", type = "text", nullable = false, length = 100)
    public String instanceId;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = false, length = 200)
    public String accountName;

    @Column(name = "mta_server_id", type = "integer", nullable = false)
    public int mtaServerId;

    @Column(name = "mta_server_name", type = "text", nullable = false, length = 200)
    public String mtaServerName;

    @Column(name = "size", type = "text", nullable = false, length = 100)
    public String size;

    @Column(name = "platform", type = "text", nullable = false, length = 100)
    public String platform;

    @Column(name = "region", type = "text", nullable = false, length = 100)
    public String region;

    @Column(name = "created_by", type = "text", nullable = false, length = 200)
    public String createdBy;

    @Column(name = "last_updated_by", type = "text", nullable = true, length = 200)
    public String lastUpdatedBy;

    @Column(name = "created_date", type = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date createdDate;

    @Column(name = "last_updated_date", type = "date", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date lastUpdatedDate;

    public HetznerInstance() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("hetzner_instances");
    }

    public HetznerInstance(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("hetzner_instances");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof HetznerInstance))
            return false;
        HetznerInstance else1 = (HetznerInstance)paramObject;
        if (!else1.exists(this))
            return false;
        if (getId() != else1.getId())
            return false;
        String str1 = getStatus();
        String str2 = else1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str5 = getName();
        String str6 = else1.getName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str3 = getInstanceId();
        String str4 = else1.getInstanceId();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getAccountId() != else1.getAccountId())
            return false;
        String str7 = getAccountName();
        String str8 = else1.getAccountName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        if (getMtaServerId() != else1.getMtaServerId())
            return false;
        String str9 = getMtaServerName();
        String str10 = else1.getMtaServerName();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getSize();
        String str12 = else1.getSize();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getPlatform();
        String str14 = else1.getPlatform();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getRegion();
        String str16 = else1.getRegion();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getCreatedBy();
        String str18 = else1.getCreatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getLastUpdatedBy();
        String str20 = else1.getLastUpdatedBy();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = else1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = else1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof HetznerInstance;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str3 = getName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str2 = getInstanceId();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getAccountId();
        String str4 = getAccountName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getMtaServerId();
        String str5 = getMtaServerName();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getSize();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getPlatform();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getRegion();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getCreatedBy();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getLastUpdatedBy();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        Date date1 = getCreatedDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        Date date2 = getLastUpdatedDate();
        return n * 59 + ((date2 == null) ? 43 : date2.hashCode());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public int getMtaServerId() {
        return mtaServerId;
    }

    public void setMtaServerId(int mtaServerId) {
        this.mtaServerId = mtaServerId;
    }

    public String getMtaServerName() {
        return mtaServerName;
    }

    public void setMtaServerName(String mtaServerName) {
        this.mtaServerName = mtaServerName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Override
    public String toString() {
        return "HetznerInstance(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", instanceId=" + getInstanceId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", mtaServerId=" + getMtaServerId() + ", mtaServerName=" + getMtaServerName() + ", size=" + getSize() + ", platform=" + getPlatform() + ", region=" + getRegion() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
