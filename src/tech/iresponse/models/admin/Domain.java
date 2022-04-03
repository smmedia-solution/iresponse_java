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
public class Domain extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "account_id", type = "integer", nullable = true)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = false, length = 100)
    public String accountName;

    @Column(name = "account_type", type = "text", nullable = false, length = 50)
    public String accountType;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "mta_server_id", type = "integer", nullable = true)
    public int mtaServerId;

    @Column(name = "ip_id", type = "integer", nullable = true)
    public int ipId;

    @Column(name = "value", type = "text", nullable = false, length = 100)
    public String value;

    @Column(name = "availability", type = "text", nullable = false, length = 50)
    public String availability;

    @Column(name = "has_brand", type = "text", nullable = false, length = 10)
    public String hasBrand;

    @Column(name = "expiration_date", type = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date expirationDate;

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

    public Domain() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("domains");
    }

    public Domain(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("domains");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Domain))
            return false;
        Domain long1 = (Domain)paramObject;
        if (!long1.exists(this))
            return false;
        if (getId() != long1.getId())
            return false;
        if (getAccountId() != long1.getAccountId())
            return false;
        String str1 = getAccountName();
        String str2 = long1.getAccountName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getAccountType();
        String str4 = long1.getAccountType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getStatus();
        String str6 = long1.getStatus();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getMtaServerId() != long1.getMtaServerId())
            return false;
        if (getIpId() != long1.getIpId())
            return false;
        String str7 = getValue();
        String str8 = long1.getValue();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getAvailability();
        String str10 = long1.getAvailability();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getHasBrand();
        String str12 = long1.getHasBrand();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        Date date1 = getExpirationDate();
        Date date2 = long1.getExpirationDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        String str13 = getCreatedBy();
        String str14 = long1.getCreatedBy();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getLastUpdatedBy();
        String str16 = long1.getLastUpdatedBy();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = long1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = long1.getLastUpdatedDate();
        return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Domain;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getAccountId();
        String str1 = getAccountName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getAccountType();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getStatus();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getMtaServerId();
        n = n * 59 + getIpId();
        String str4 = getValue();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getAvailability();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getHasBrand();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        Date date1 = getExpirationDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        String str7 = getCreatedBy();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getLastUpdatedBy();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        Date date2 = getCreatedDate();
        n = n * 59 + ((date2 == null) ? 43 : date2.hashCode());
        Date date3 = getLastUpdatedDate();
        return n * 59 + ((date3 == null) ? 43 : date3.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public String getStatus() {
        return this.status;
    }

    public int getMtaServerId() {
        return this.mtaServerId;
    }

    public int getIpId() {
        return this.ipId;
    }

    public String getValue() {
        return this.value;
    }

    public String getAvailability() {
        return this.availability;
    }

    public String getHasBrand() {
        return this.hasBrand;
    }

    public Date getExpirationDate() {
        return this.expirationDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getLastUpdatedBy() {
        return this.lastUpdatedBy;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public Date getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMtaServerId(int mtaServerId) {
        this.mtaServerId = mtaServerId;
    }

    public void setIpId(int ipId) {
        this.ipId = ipId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setHasBrand(String hasBrand) {
        this.hasBrand = hasBrand;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String toString() {
        return "Domain(id=" + getId() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", accountType=" + getAccountType() + ", status=" + getStatus() + ", mtaServerId=" + getMtaServerId() + ", ipId=" + getIpId() + ", value=" + getValue() + ", availability=" + getAvailability() + ", hasBrand=" + getHasBrand() + ", expirationDate=" + getExpirationDate() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
