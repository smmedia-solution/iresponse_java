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
public class AzureInstance extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = false, length = 200)
    public String accountName;

    @Column(name = "mta_server_id", type = "integer", nullable = false)
    public int mtaServerId;

    @Column(name = "mta_server_name", type = "text", nullable = false, length = 200)
    public String mtaServerName;

    @Column(name = "type", type = "text", nullable = false, length = 100)
    public String type;

    @Column(name = "region", type = "text", nullable = false, length = 100)
    public String region;

    @Column(name = "region_name", type = "text", nullable = false)
    public String regionName;

    @Column(name = "public_ips", type = "text", nullable = true)
    public String publicIps;

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

    public AzureInstance() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("azure_instances");
    }

    public AzureInstance(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("azure_instances");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AzureInstance))
            return false;
        AzureInstance goto1 = (AzureInstance)paramObject;
        if (!goto1.exists(this))
            return false;
        if (getId() != goto1.getId())
            return false;
        String str1 = getStatus();
        String str2 = goto1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
           return false;
        String str3 = getName();
        String str4 = goto1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
           return false;
        if (getAccountId() != goto1.getAccountId())
           return false;
        String str5 = getAccountName();
        String str6 = goto1.getAccountName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
           return false;
        if (getMtaServerId() != goto1.getMtaServerId())
           return false;
        String str7 = getMtaServerName();
        String str8 = goto1.getMtaServerName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
           return false;
        String str9 = getType();
        String str10 = goto1.getType();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
           return false;
        String str11 = getRegion();
        String str12 = goto1.getRegion();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
           return false;
        String str13 = getRegionName();
        String str14 = goto1.getRegionName();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
           return false;
        String ssh1 = getPublicIps();
        String ssh2 = goto1.getPublicIps();
        if ((ssh1 == null) ? (ssh2 != null) : !ssh1.equals(ssh2))
           return false;
        String str15 = getCreatedBy();
        String str16 = goto1.getCreatedBy();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
           return false;
        String str17 = getLastUpdatedBy();
        String str18 = goto1.getLastUpdatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
           return false;
        Date date1 = getCreatedDate();
        Date date2 = goto1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
           return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = goto1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AzureInstance;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getAccountId();
        String str3 = getAccountName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getMtaServerId();
        String str4 = getMtaServerName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getType();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getRegion();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getRegionName();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String sh1 = getPublicIps();
        n = n * 59 + ((sh1 == null) ? 43 : sh1.hashCode());
        String str8 = getCreatedBy();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getLastUpdatedBy();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getPublicIps() {
        return publicIps;
    }

    public void setPublicIps(String publicIps) {
        this.publicIps = publicIps;
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
        return "AzureInstance(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", mtaServerId=" + getMtaServerId() + ", mtaServerName=" + getMtaServerName() + ", type=" + getType() + ", region=" + getRegion() + ", regionName=" + getRegionName() + ", publicIps=" + getPublicIps() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
