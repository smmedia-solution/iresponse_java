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
public class Link extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "affiliate_network_id", type = "integer", nullable = false)
    public int affiliateNetworkId;

    @Column(name = "offer_id", type = "integer", nullable = false)
    public int offerId;

    @Column(name = "creative_id", type = "integer", nullable = false)
    public int creativeId;

    @Column(name = "type", type = "text", nullable = false, length = 10)
    public String type;

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

    public Link() throws Exception {
        setDatabase("system");
        setSchema("affiliate");
        setTable("links");
    }

    public Link(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("affiliate");
        setTable("links");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Link))
            return false;
        Link int1 = (Link)paramObject;
        if (!int1.exists(this))
            return false;
        if (getId() != int1.getId())
            return false;
        String str1 = getStatus();
        String str2 = int1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getAffiliateNetworkId() != int1.getAffiliateNetworkId())
            return false;
        if (getOfferId() != int1.getOfferId())
            return false;
        if (getCreativeId() != int1.getCreativeId())
            return false;
        String str3 = getType();
        String str4 = int1.getType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getValue();
        String str6 = int1.getValue();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getCreatedBby();
        String str8 = int1.getCreatedBby();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getLastUpdatedBy();
        String str10 = int1.getLastUpdatedBy();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = int1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = int1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Link;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        n = n * 59 + getOfferId();
        n = n * 59 + getCreativeId();
        String str2 = getType();
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

    public int getCreativeId() {
        return this.creativeId;
    }

    public String getType() {
        return this.type;
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

    public void setCreativeId(int creativeId) {
        this.creativeId = creativeId;
    }

    public void setType(String type) {
        this.type = type;
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
        return "Link(id=" + getId() + ", status=" + getStatus() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", offerId=" + getOfferId() + ", creativeId=" + getCreativeId() + ", type=" + getType() + ", value=" + getValue() + ", createdBy=" + getCreatedBby() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}