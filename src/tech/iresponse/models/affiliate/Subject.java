package tech.iresponse.models.affiliate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Date;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class Subject extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "affiliate_network_id", type = "integer", nullable = false)
    public int affiliateNetworkId;

    @Column(name = "offer_id", type = "integer", nullable = false)
    public int offerId;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "value", type = "text", nullable = false)
    public String value;

    @Column(name = "created_by", type = "text", nullable = false, length = 200)
    public String createdBby;

    @Column(name = "last_updated_by", type = "text", nullable = true, length = 200)
    public String lastUpdatedBy;

    @Column(name = "created_date", type = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date createdDate;

    @Column(name = "last_updated_date", type = "date", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date lastUpdatedDate;

    public Subject() throws Exception {
        setDatabase("system");
        setSchema("affiliate");
        setTable("subjects");
    }

    public Subject(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("affiliate");
        setTable("subjects");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Subject))
            return false;
        Subject try1 = (Subject)paramObject;
        if (!try1.exists(this))
            return false;
        if (getId() != try1.getId())
            return false;
        String str1 = getStatus();
        String str2 = try1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getAffiliateNetworkId() != try1.getAffiliateNetworkId())
            return false;
        if (getOfferId() != try1.getOfferId())
            return false;
        String str3 = getName();
        String str4 = try1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getValue();
        String str6 = try1.getValue();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getCreatedBby();
        String str8 = try1.getCreatedBby();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getLastUpdatedBy();
        String str10 = try1.getLastUpdatedBy();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = try1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = try1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Subject;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        n = n * 59 + getOfferId();
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getValue();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getCreatedBby();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getLastUpdatedBy();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        Date date1 = getCreatedDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        Date date2 = getLastUpdatedDate();
        return n * 59 + ((date2 == null) ? 43 : date2.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    public int getAffiliateNetworkId() {
        return this.affiliateNetworkId;
    }

    public int getOfferId() {
        return this.offerId;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getCreatedBby() {
        return this.createdBby;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAffiliateNetworkId(int affiliateNetworkId) {
        this.affiliateNetworkId = affiliateNetworkId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCreatedBby(String createdBby) {
        this.createdBby = createdBby;
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

    @Override
    public String toString() {
        return "Subject(id=" + getId() + ", status=" + getStatus() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", offerId=" + getOfferId() + ", name=" + getName() + ", value=" + getValue() + ", createdBy=" + getCreatedBby() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}