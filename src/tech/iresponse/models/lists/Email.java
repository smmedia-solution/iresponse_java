package tech.iresponse.models.lists;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class Email extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false, indexed = true)
    public int id;

    @Column(name = "list_id", type = "integer", nullable = false, indexed = true)
    public int listId;

    @Column(name = "email", type = "text", nullable = false, indexed = true)
    public String email;

    @Column(name = "email_md5", type = "text", nullable = false, indexed = true)
    public String emailMd5;

    @Column(name = "first_name", type = "text", nullable = true)
    public String firstName;

    @Column(name = "last_name", type = "text", nullable = true)
    public String lastName;

    @Column(name = "verticals", type = "text", nullable = true)
    public String verticals;

    @Column(name = "is_seed", type = "boolean", nullable = true)
    public boolean isSeed;

    @Column(name = "is_fresh", type = "boolean", nullable = true)
    public boolean isFresh;

    @Column(name = "is_clean", type = "boolean", nullable = true)
    public boolean isClean;

    @Column(name = "is_opener", type = "boolean", nullable = true)
    public boolean isOpener;

    @Column(name = "is_clicker", type = "boolean", nullable = true)
    public boolean isClicker;

    @Column(name = "is_leader", type = "boolean", nullable = true)
    public boolean isLeader;

    @Column(name = "is_unsub", type = "boolean", nullable = true)
    public boolean isUnsub;

    @Column(name = "is_optout", type = "boolean", nullable = true)
    public boolean isOptout;

    @Column(name = "is_blacklisted", type = "boolean", nullable = true)
    public boolean isBlacklisted;

    @Column(name = "is_hard_bounced", type = "boolean", nullable = true)
    public boolean isHardBounced;

    @Column(name = "last_action_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp lastActionTime;

    @Column(name = "last_action_type", type = "text", nullable = true, length = 100)
    public String lastActionType;

    @Column(name = "agent", type = "text", nullable = true)
    public String agent;

    @Column(name = "ip", type = "text", nullable = true, length = 100)
    public String ip;

    @Column(name = "country_code", type = "text", nullable = true, length = 10)
    public String countryCode;

    @Column(name = "country", type = "text", nullable = true)
    public String country;

    @Column(name = "region", type = "text", nullable = true)
    public String region;

    @Column(name = "city", type = "text", nullable = true, length = 100)
    public String city;

    @Column(name = "language", type = "text", nullable = true, length = 100)
    public String language;

    @Column(name = "device_type", type = "text", nullable = true, length = 100)
    public String deviceType;

    @Column(name = "device_name", type = "text", nullable = true, length = 100)
    public String deviceName;

    @Column(name = "os", type = "text", nullable = true, length = 100)
    public String os;

    @Column(name = "browser_name", type = "text", nullable = true, length = 100)
    public String browserName;

    @Column(name = "browser_version", type = "text", nullable = true, length = 100)
    public String browserVersion;

    public Email() throws Exception {
        setDatabase("clients");
        setSchema("");
        setTable("");
    }

    public Email(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("clients");
        setSchema("");
        setTable("");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Email))
            return false;
        Email new1 = (Email)paramObject;
        if (!new1.exists(this))
            return false;
        if (getId() != new1.getId())
            return false;
        if (getListId() != new1.getListId())
            return false;
        String str1 = getEmail();
        String str2 = new1.getEmail();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getEmailMd5();
        String str4 = new1.getEmailMd5();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getFirstName();
        String str6 = new1.getFirstName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getLastName();
        String str8 = new1.getLastName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getVerticals();
        String str10 = new1.getVerticals();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getIsSeed() != new1.getIsSeed())
            return false;
        if (getIsFresh() != new1.getIsFresh())
            return false;
        if (getIsClean() != new1.getIsClean())
            return false;
        if (getIsOpener() != new1.getIsOpener())
            return false;
        if (getIsClicker() != new1.getIsClicker())
            return false;
        if (getIsLeader() != new1.getIsLeader())
            return false;
        if (getIsUnsub() != new1.getIsUnsub())
            return false;
        if (getIsOptout() != new1.getIsOptout())
            return false;
        if (getIsBlacklisted() != new1.getIsBlacklisted())
            return false;
        if (getIsHardBounced() != new1.getIsHardBounced())
            return false;
        Timestamp timestamp1 = getLastActionTime();
        Timestamp timestamp2 = new1.getLastActionTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        String str11 = getLastActionType();
        String str12 = new1.getLastActionType();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getAgent();
        String str14 = new1.getAgent();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getIp();
        String str16 = new1.getIp();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getCountryCode();
        String str18 = new1.getCountryCode();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getCountry();
        String str20 = new1.getCountry();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getRegion();
        String str22 = new1.getRegion();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getCity();
        String str24 = new1.getCity();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getLanguage();
        String str26 = new1.getLanguage();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getDeviceType();
        String str28 = new1.getDeviceType();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getDeviceName();
        String str30 = new1.getDeviceName();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        String str31 = getOs();
        String str32 = new1.getOs();
        if ((str31 == null) ? (str32 != null) : !str31.equals(str32))
            return false;
        String str33 = getBrowserName();
        String str34 = new1.getBrowserName();
        if ((str33 == null) ? (str34 != null) : !str33.equals(str34))
            return false;
        String str35 = getBrowserVersion();
        String str36 = new1.getBrowserVersion();
        return !((str35 == null) ? (str36 != null) : !str35.equals(str36));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Email;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getListId();
        String str1 = getEmail();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getEmailMd5();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getFirstName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getLastName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getVerticals();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + (getIsSeed() ? 79 : 97);
        n = n * 59 + (getIsFresh() ? 79 : 97);
        n = n * 59 + (getIsClean() ? 79 : 97);
        n = n * 59 + (getIsOpener() ? 79 : 97);
        n = n * 59 + (getIsClicker() ? 79 : 97);
        n = n * 59 + (getIsLeader() ? 79 : 97);
        n = n * 59 + (getIsUnsub() ? 79 : 97);
        n = n * 59 + (getIsOptout() ? 79 : 97);
        n = n * 59 + (getIsBlacklisted() ? 79 : 97);
        n = n * 59 + (getIsHardBounced() ? 79 : 97);
        Timestamp timestamp = getLastActionTime();
        n = n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
        String str6 = getLastActionType();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getAgent();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getIp();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getCountryCode();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getCountry();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getRegion();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getCity();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getLanguage();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getDeviceType();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getDeviceName();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        String str16 = getOs();
        n = n * 59 + ((str16 == null) ? 43 : str16.hashCode());
        String str17 = getBrowserName();
        n = n * 59 + ((str17 == null) ? 43 : str17.hashCode());
        String str18 = getBrowserVersion();
        return n * 59 + ((str18 == null) ? 43 : str18.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public int getListId() {
        return this.listId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getEmailMd5() {
        return this.emailMd5;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getVerticals() {
        return this.verticals;
    }

    public boolean getIsSeed() {
        return this.isSeed;
    }

    public boolean getIsFresh() {
        return this.isFresh;
    }

    public boolean getIsClean() {
        return this.isClean;
    }

    public boolean getIsOpener() {
        return this.isOpener;
    }

    public boolean getIsClicker() {
        return this.isClicker;
    }

    public boolean getIsLeader() {
        return this.isLeader;
    }

    public boolean getIsUnsub() {
        return this.isUnsub;
    }

    public boolean getIsOptout() {
        return this.isOptout;
    }

    public boolean getIsBlacklisted() {
        return this.isBlacklisted;
    }

    public boolean getIsHardBounced() {
        return this.isHardBounced;
    }

    public Timestamp getLastActionTime() {
        return this.lastActionTime;
    }

    public String getLastActionType() {
        return this.lastActionType;
    }

    public String getAgent() {
        return this.agent;
    }

    public String getIp() {
        return this.ip;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public String getCountry() {
        return this.country;
    }

    public String getRegion() {
        return this.region;
    }

    public String getCity() {
        return this.city;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getOs() {
        return this.os;
    }

    public String getBrowserName() {
        return this.browserName;
    }

    public String getBrowserVersion() {
        return this.browserVersion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailMd5(String emailMd5) {
        this.emailMd5 = emailMd5;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setVerticals(String verticals) {
        this.verticals = verticals;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
    }

    public void setFresh(boolean fresh) {
        isFresh = fresh;
    }

    public void setClean(boolean clean) {
        isClean = clean;
    }

    public void setOpener(boolean opener) {
        isOpener = opener;
    }

    public void setClicker(boolean clicker) {
        isClicker = clicker;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public void setUnsub(boolean unsub) {
        isUnsub = unsub;
    }

    public void setOptout(boolean optout) {
        isOptout = optout;
    }

    public void setBlacklisted(boolean blacklisted) {
        isBlacklisted = blacklisted;
    }

    public void setHardBounced(boolean hardBounced) {
        isHardBounced = hardBounced;
    }

    public void setLastActionTime(Timestamp lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public void setLastActionType(String lastActionType) {
        this.lastActionType = lastActionType;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    @Override
    public String toString() {
        return "Email(id=" + getId() + ", listId=" + getListId() + ", email=" + getEmail() + ", emailMd5=" + getEmailMd5() + ", firstName=" + getFirstName() + ", lastName=" + getLastName() + ", verticals=" + getVerticals() + ", isSeed=" + getIsSeed() + ", isFresh=" + getIsFresh() + ", isClean=" + getIsClean() + ", isOpener=" + getIsOpener() + ", isClicker=" + getIsClicker() + ", isLeader=" + getIsLeader() + ", isUnsub=" + getIsUnsub() + ", isOptout=" + getIsOptout() + ", isBlacklisted=" + getIsBlacklisted() + ", isHardBounced=" + getIsHardBounced() + ", lastActionTime=" + getLastActionTime() + ", lastActionType=" + getLastActionType() + ", agent=" + getAgent() + ", ip=" + getIp() + ", countryCode=" + getCountryCode() + ", country=" + getCountry() + ", region=" + getRegion() + ", city=" + getCity() + ", language=" + getLanguage() + ", deviceType=" + getDeviceType() + ", deviceName=" + getDeviceName() + ", os=" + getOs() + ", browserName=" + getBrowserName() + ", browserVersion=" + getBrowserVersion() + ")";
    }
}