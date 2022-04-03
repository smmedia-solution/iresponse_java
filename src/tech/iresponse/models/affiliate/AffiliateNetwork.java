package tech.iresponse.models.affiliate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Date;

import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class AffiliateNetwork extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "affiliate_id", type = "integer", nullable = false)
    public int affiliateId;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "network_id", type = "text", nullable = false, length = 200)
    public String networkId;

    @Column(name = "company_name", type = "text", nullable = false, length = 200)
    public String companyName;

    @Column(name = "website", type = "text", nullable = true, length = 100)
    public String website;

    @Column(name = "username", type = "text", nullable = false, length = 100)
    public String username;

    @Column(name = "password", type = "text", nullable = false, length = 100)
    public String password;

    @Column(name = "api_type", type = "text", nullable = true, length = 50)
    public String apiType;

    @Column(name = "api_url", type = "text", nullable = true, length = 200)
    public String apiUrl;

    @Column(name = "api_key", type = "text", nullable = true)
    public String apiKey;

    @Column(name = "sub_id_one", type = "text", nullable = true, length = 100)
    public String subIdOne;

    @Column(name = "sub_id_two", type = "text", nullable = true, length = 100)
    public String subIdTwo;

    @Column(name = "sub_id_three", type = "text", nullable = true, length = 100)
    public String subIdThree;

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

    public AffiliateNetwork() throws Exception {
        setDatabase("system");
        setSchema("affiliate");
        setTable("affiliate_networks");

    }

    public AffiliateNetwork(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("affiliate");
        setTable("affiliate_networks");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AffiliateNetwork))
            return false;
        AffiliateNetwork do1 = (AffiliateNetwork)paramObject;
        if (!do1.exists(this))
            return false;
        if (getId() != do1.getId())
            return false;
        String str1 = getStatus();
        String str2 = do1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getAffiliateId() != do1.getAffiliateId())
            return false;
        String str3 = getName();
        String str4 = do1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String h1n = getNetworkId();
        String h12n = do1.getNetworkId();
        if ((h1n == null) ? (h12n != null) : !h1n.equals(h12n))
            return false;
        String str5 = getCompanyName();
        String str6 = do1.getCompanyName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getWebsite();
        String str8 = do1.getWebsite();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getUsername();
        String str10 = do1.getUsername();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getPassword();
        String str12 = do1.getPassword();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getApiType();
        String str14 = do1.getApiType();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getApiUrl();
        String str16 = do1.getApiUrl();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getApiKey();
        String str18 = do1.getApiKey();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getSubIdOne();
        String str20 = do1.getSubIdOne();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getSubIdTwo();
        String str22 = do1.getSubIdTwo();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getSubIdThree();
        String str24 = do1.getSubIdThree();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getCreatedBby();
        String str26 = do1.getCreatedBby();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getLastUpdatedBy();
        String str28 = do1.getLastUpdatedBy();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = do1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = do1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof AffiliateNetwork;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getAffiliateId();
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String h1n = getNetworkId();
        n = n * 59 + ((h1n == null) ? 43 : h1n.hashCode());
        String str3 = getCompanyName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getWebsite();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getUsername();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getPassword();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getApiType();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getApiUrl();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getApiKey();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getSubIdOne();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getSubIdTwo();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getSubIdThree();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getCreatedBby();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getLastUpdatedBy();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
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

    public int getAffiliateId() {
        return this.affiliateId;
    }

    public String getName() {
        return this.name;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public String getWebsite() {
        return this.website;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getApiType() {
        return this.apiType;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getSubIdOne() {
        return this.subIdOne;
    }

    public String getSubIdTwo() {
        return this.subIdTwo;
    }

    public String getSubIdThree() {
        return this.subIdThree;
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

    public void setAffiliateId(int affiliateId) {
        this.affiliateId = affiliateId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSubIdOne(String subIdOne) {
        this.subIdOne = subIdOne;
    }

    public void setSubIdTwo(String subIdTwo) {
        this.subIdTwo = subIdTwo;
    }

    public void setSubIdThree(String subIdThree) {
        this.subIdThree = subIdThree;
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

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @Override
    public String toString() {
        return "AffiliateNetwork(id=" + getId() + ", status=" + getStatus() + ", affiliateId=" + getAffiliateId() + ", networkId=" + getNetworkId() + ", name=" + getName() + ", companyName=" + getCompanyName() + ", website=" + getWebsite() + ", username=" + getUsername() + ", password=" + getPassword() + ", apiType=" + getApiType() + ", apiUrl=" + getApiUrl() + ", apiKey=" + getApiKey() + ", subIdOne=" + getSubIdOne() + ", subIdTwo=" + getSubIdTwo() + ", subIdThree=" + getSubIdThree() + ", createdBy=" + getCreatedBby() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}