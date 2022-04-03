package tech.iresponse.models.actions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class Click extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "unique_token", type = "text", nullable = false)
    public String uniqueToken;

    @Column(name = "process_id", type = "integer", nullable = false)
    public int processId;

    @Column(name = "process_type", type = "text", nullable = false, length = 10)
    public String processType;

    @Column(name = "user_production_id", type = "integer", nullable = false)
    public int userProductionId;

    @Column(name = "user_full_name", type = "text", nullable = false)
    public String userFullName;

    @Column(name = "vmta_id", type = "integer", nullable = true)
    public int vmtaId;

    @Column(name = "smtp_user_id", type = "integer", nullable = true)
    public int smtpUserId;

    @Column(name = "affiliate_network_id", type = "integer", nullable = false)
    public int affiliateNetworkId;

    @Column(name = "offer_production_id", type = "text", nullable = false, length = 200)
    public String offerProductionId;

    @Column(name = "list_id", type = "integer", nullable = false)
    public int listId;

    @Column(name = "client_id", type = "integer", nullable = false)
    public int clientId;

    @Column(name = "action_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp actionTime;

    @Column(name = "process_updated", type = "boolean", nullable = true)
    public boolean processUpdated;

    @Column(name = "agent", type = "text", nullable = true)
    public String agent;

    @Column(name = "action_ip", type = "text", nullable = true, length = 15)
    public String actionIp;

    @Column(name = "country_code", type = "text", nullable = true, length = 10)
    public String countryCode;

    @Column(name = "country", type = "text", nullable = true, length = 100)
    public String country;

    @Column(name = "region", type = "text", nullable = true, length = 100)
    public String region;

    @Column(name = "city", type = "text", nullable = true, length = 100)
    public String city;

    @Column(name = "device_type", type = "text", nullable = true, length = 100)
    public String deviceType;

    @Column(name = "device_name", type = "text", nullable = true, length = 100)
    public String deviceName;

    @Column(name = "operating_system", type = "text", nullable = true, length = 50)
    public String operatingSystem;

    @Column(name = "browser_name", type = "text", nullable = true, length = 50)
    public String browserName;

    @Column(name = "browser_version", type = "text", nullable = true, length = 50)
    public String browserVersion;

    public Click() throws Exception {
        setDatabase("system");
        setSchema("actions");
        setTable("clicks");
    }

    public Click(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("actions");
        setTable("clicks");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Click))
            return false;
        Click if1 = (Click)paramObject;
        if (!if1.exists(this))
            return false;
        if (getId() != if1.getId())
            return false;
        String str1 = getUniqueToken();
        String str2 = if1.getUniqueToken();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getProcessId() != if1.getProcessId())
            return false;
        String str3 = getProcessType();
        String str4 = if1.getProcessType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getUserProductionId() != if1.getUserProductionId())
            return false;
        String str5 = getUserFullName();
        String str6 = if1.getUserFullName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getVmtaId() != if1.getVmtaId())
            return false;
        if (getSmtpUserId() != if1.getSmtpUserId())
            return false;
        if (getAffiliateNetworkId() != if1.getAffiliateNetworkId())
            return false;
        String str7 = getOfferProductionId();
        String str8 = if1.getOfferProductionId();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        if (getListId() != if1.getListId())
            return false;
        if (getClientId() != if1.getClientId())
            return false;
        Timestamp timestamp1 = getActionTime();
        Timestamp timestamp2 = if1.getActionTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        if (isProcessUpdated() != if1.isProcessUpdated())
            return false;
        String str9 = getAgent();
        String str10 = if1.getAgent();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getActionIp();
        String str12 = if1.getActionIp();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getCountryCode();
        String str14 = if1.getCountryCode();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getCountry();
        String str16 = if1.getCountry();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getRegion();
        String str18 = if1.getRegion();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getCity();
        String str20 = if1.getCity();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getDeviceType();
        String str22 = if1.getDeviceType();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getDeviceName();
        String str24 = if1.getDeviceName();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getOperatingSystem();
        String str26 = if1.getOperatingSystem();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getBrowserName();
        String str28 = if1.getBrowserName();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getBrowserVersion();
        String str30 = if1.getBrowserVersion();
            return !((str29 == null) ? (str30 != null) : !str29.equals(str30));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Click;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getUniqueToken();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getProcessId();
        String str2 = getProcessType();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getUserProductionId();
        String str3 = getUserFullName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getVmtaId();
        n = n * 59 + getSmtpUserId();
        n = n * 59 + getAffiliateNetworkId();
        String str4 = getOfferProductionId();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getListId();
        n = n * 59 + getClientId();
        Timestamp timestamp = getActionTime();
        n = n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
        n = n * 59 + (isProcessUpdated() ? 79 : 97);
        String str5 = getAgent();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getActionIp();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getCountryCode();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getCountry();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getRegion();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getCity();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getDeviceType();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getDeviceName();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getOperatingSystem();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getBrowserName();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getBrowserVersion();
        return n * 59 + ((str15 == null) ? 43 : str15.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniqueToken() {
        return uniqueToken;
    }

    public void setUniqueToken(String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public int getUserProductionId() {
        return userProductionId;
    }

    public void setUserProductionId(int userProductionId) {
        this.userProductionId = userProductionId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public int getVmtaId() {
        return vmtaId;
    }

    public void setVmtaId(int vmtaId) {
        this.vmtaId = vmtaId;
    }

    public int getSmtpUserId() {
        return smtpUserId;
    }

    public void setSmtpUserId(int smtpUserId) {
        this.smtpUserId = smtpUserId;
    }

    public int getAffiliateNetworkId() {
        return affiliateNetworkId;
    }

    public void setAffiliateNetworkId(int affiliateNetworkId) {
        this.affiliateNetworkId = affiliateNetworkId;
    }

    public String getOfferProductionId() {
        return offerProductionId;
    }

    public void setOfferProductionId(String offerProductionId) {
        this.offerProductionId = offerProductionId;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Timestamp getActionTime() {
        return actionTime;
    }

    public void setActionTime(Timestamp actionTime) {
        this.actionTime = actionTime;
    }

    public boolean isProcessUpdated() {
        return processUpdated;
    }

    public void setProcessUpdated(boolean processUpdated) {
        this.processUpdated = processUpdated;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getActionIp() {
        return actionIp;
    }

    public void setActionIp(String actionIp) {
        this.actionIp = actionIp;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    @Override
    public String toString() {
        return "Click(id=" + getId() + ", uniqueToken=" + getUniqueToken() + ", processId=" + getProcessId() + ", processType=" + getProcessType() + ", userProductionId=" + getUserProductionId() + ", userFullName=" + getUserFullName() + ", vmtaId=" + getVmtaId() + ", smtpUserId=" + getSmtpUserId() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", offerProductionId=" + getOfferProductionId() + ", listId=" + getListId() + ", clientId=" + getClientId() + ", actionTime=" + getActionTime() + ", processUpdated=" + isProcessUpdated() + ", agent=" + getAgent() + ", actionIp=" + getActionIp() + ", countryCode=" + getCountryCode() + ", country=" + getCountry() + ", region=" + getRegion() + ", city=" + getCity() + ", deviceType=" + getDeviceType() + ", deviceName=" + getDeviceName() + ", operatingSystem=" + getOperatingSystem() + ", browserName=" + getBrowserName() + ", browserVersion=" + getBrowserVersion() + ")";
    }
}
