package tech.iresponse.models.affiliate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class Offer extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "affiliate_network_id", type = "integer", nullable = false)
    public int affiliateNetworkId;

    @Column(name = "affiliate_network_name", type = "text", nullable = false, length = 200)
    public String affiliateNetworkName;

    @Column(name = "production_id", type = "text", nullable = false, length = 200)
    public String productionId;

    @Column(name = "campaign_id", type = "text", nullable = false, length = 200)
    public String campaignId;

    @Column(name = "verticals_ids", type = "text", nullable = true)
    public String verticalsIds;

    @Column(name = "name", type = "text", nullable = false, length = 200)
    public String name;

    @Column(name = "countries", type = "text", nullable = false)
    public String countries;

    @Column(name = "description", type = "text", nullable = true)
    public String description;

    @Column(name = "rules", type = "text", nullable = true)
    public String rules;

    @Column(name = "expiration_date", type = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date expirationDate;

    @Column(name = "type", type = "text", nullable = false, length = 10)
    public String type;

    @Column(name = "payout", type = "text", nullable = false, length = 100)
    public String payout;

    @Column(name = "available_days", type = "text", nullable = false, length = 200)
    public String availableDays;

    @Column(name = "auto_sup", type = "boolean", nullable = true)
    public boolean autoSup;

    @Column(name = "default_suppression_link", type = "text", nullable = true)
    public String defaultSuppressionLink;

    @Column(name = "last_suppression_updated_date", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp lastSuppressionUpdatedDate;

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

    public Offer() throws Exception, NoSuchFieldException {
        setDatabase("system");
        setSchema("affiliate");
        setTable("offers");
    }

    public Offer(Object paramObject) throws Exception, NoSuchFieldException {
        super(paramObject);
        setDatabase("system");
        setSchema("affiliate");
        setTable("offers");
        load();
    }

    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Offer))
            return false;
        Offer new1 = (Offer)paramObject;
        if (!new1.exists(this))
            return false;
        if (getId() != new1.getId())
            return false;
        String str1 = getStatus();
        String str2 = new1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getAffiliateNetworkId() != new1.getAffiliateNetworkId())
            return false;
        String str3 = getAffiliateNetworkName();
        String str4 = new1.getAffiliateNetworkName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getProductionId();
        String str6 = new1.getProductionId();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getCampaignId();
        String str8 = new1.getCampaignId();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getVerticalsIds();
        String str10 = new1.getVerticalsIds();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getName();
        String str12 = new1.getName();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getCountries();
        String str14 = new1.getCountries();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getDescription();
        String str16 = new1.getDescription();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getRules();
        String str18 = new1.getRules();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        Date date1 = getExpirationDate();
        Date date2 = new1.getExpirationDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        String str19 = getType();
        String str20 = new1.getType();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getPayout();
        String str22 = new1.getPayout();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getAvailableDays();
        String str24 = new1.getAvailableDays();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        if (getAutoSup() != new1.getAutoSup())
            return false;
        String str25 = getDefaultSuppressionLink();
        String str26 = new1.getDefaultSuppressionLink();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        Timestamp timestamp1 = getLastSuppressionUpdatedDate();
        Timestamp timestamp2 = new1.getLastSuppressionUpdatedDate();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        String str27 = getCreatedBby();
        String str28 = new1.getCreatedBby();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getLastUpdatedBy();
        String str30 = new1.getLastUpdatedBy();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = new1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = new1.getLastUpdatedDate();
        return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Offer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        String str2 = getAffiliateNetworkName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getProductionId();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getCampaignId();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getVerticalsIds();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getName();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getCountries();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getDescription();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getRules();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        Date date1 = getExpirationDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        String str10 = getType();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getPayout();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getAvailableDays();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        n = n * 59 + (getAutoSup() ? 79 : 97);
        String str13 = getDefaultSuppressionLink();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        Timestamp timestamp = getLastSuppressionUpdatedDate();
        n = n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
        String str14 = getCreatedBby();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getLastUpdatedBy();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        Date date2 = getCreatedDate();
        n = n * 59 + ((date2 == null) ? 43 : date2.hashCode());
        Date date3 = getLastUpdatedDate();
        return n * 59 + ((date3 == null) ? 43 : date3.hashCode());
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

    public String getAffiliateNetworkName() {
        return this.affiliateNetworkName;
    }

    public String getProductionId() {
        return this.productionId;
    }

    public String getCampaignId() {
        return this.campaignId;
    }

    public String getVerticalsIds() {
        return this.verticalsIds;
    }

    public String getName() {
        return this.name;
    }

    public String getCountries() {
        return this.countries;
    }

    public String getDescription() {
        return this.description;
    }

    public String getRules() {
        return this.rules;
    }

    public Date getExpirationDate() {
        return this.expirationDate;
    }

    public String getType() {
        return this.type;
    }

    public String getPayout() {
        return this.payout;
    }

    public String getAvailableDays() {
        return this.availableDays;
    }

    public boolean getAutoSup() {
        return this.autoSup;
    }

    public String getDefaultSuppressionLink() {
        return this.defaultSuppressionLink;
    }

    public Timestamp getLastSuppressionUpdatedDate() {
        return this.lastSuppressionUpdatedDate;
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

    public void setAffiliateNetworkName(String affiliateNetworkName) {
        this.affiliateNetworkName = affiliateNetworkName;
    }

    public void setProductionId(String productionId) {
        this.productionId = productionId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public void setVerticalsIds(String verticalsIds) {
        this.verticalsIds = verticalsIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayout(String payout) {
        this.payout = payout;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public void setAutoSup(boolean autoSup) {
        this.autoSup = autoSup;
    }

    public void setDefaultSuppressionLink(String defaultSuppressionLink) {
        this.defaultSuppressionLink = defaultSuppressionLink;
    }

    public void setLastSuppressionUpdatedDate(Timestamp lastSuppressionUpdatedDate) {
        this.lastSuppressionUpdatedDate = lastSuppressionUpdatedDate;
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
        return "Offer(id=" + getId() + ", status=" + getStatus() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", affiliateNetworkName=" + getAffiliateNetworkName() + ", productionId=" + getProductionId() + ", campaignId=" + getCampaignId() + ", verticalsIds=" + getVerticalsIds() + ", name=" + getName() + ", countries=" + getCountries() + ", description=" + getDescription() + ", rules=" + getRules() + ", expirationDate=" + getExpirationDate() + ", type=" + getType() + ", payout=" + getPayout() + ", availableDays=" + getAvailableDays() + ", autoSup=" + getAutoSup() + ", defaultSuppressionLink=" + getDefaultSuppressionLink() + ", lastSuppressionUpdatedDate=" + getLastSuppressionUpdatedDate() + ", createdBy=" + getCreatedBby() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}