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
public class MtaServer extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "provider_id", type = "integer", nullable = false)
    public int providerId;

    @Column(name = "provider_name", type = "text", nullable = false, length = 200)
    public String providerName;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 200)
    public String name;

    @Column(name = "host_name", type = "text", nullable = true, length = 200)
    public String hostName;

    @Column(name = "main_ip", type = "text", nullable = false, length = 200)
    public String mainIp;

    @Column(name = "ssh_login_type", type = "text", nullable = false, length = 50)
    public String sshLoginType;

    @Column(name = "ssh_username", type = "text", nullable = false, length = 100)
    public String sshUsername;

    @Column(name = "ssh_password", type = "text", nullable = true, length = 100)
    public String sshPassword;

    @Column(name = "old_ssh_password", type = "text", nullable = true, length = 100)
    public String oldSshPassword;

    @Column(name = "ssh_pem_content", type = "text", nullable = true)
    public String sshPemContent;

    @Column(name = "ssh_passphrase", type = "text", nullable = true)
    public String sshPassphrase;

    @Column(name = "ssh_port", type = "integer", nullable = false)
    public int sshPort;

    @Column(name = "old_ssh_port", type = "integer", nullable = false)
    public int oldSshPort;

    @Column(name = "ips_count", type = "integer", nullable = true)
    public int ipsCount;

    @Column(name = "is_installed", type = "boolean", nullable = true)
    public boolean installed;

    @Column(name = "country_code", type = "text", nullable = true, length = 3)
    public String countryCode;

    @Column(name = "os", type = "text", nullable = true, length = 200)
    public String os;

    @Column(name = "dmarc_installed", type = "boolean", nullable = true)
    public boolean dmarcInstalled;

    @Column(name = "dkim_installed", type = "boolean", nullable = true)
    public boolean dkimInstalled;

    @Column(name = "ssh_connectivity_status", type = "text", nullable = false, length = 100)
    public String sshConnectivityStatus;

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

    public MtaServer() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("mta_servers");
    }

    public MtaServer(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("mta_servers");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaServer))
            return false;
        MtaServer throw1 = (MtaServer)paramObject;
        if (!throw1.exists(this))
            return false;
        if (getId() != throw1.getId())
            return false;
        if (getProviderId() != throw1.getProviderId())
            return false;
        String str1 = getProviderName();
        String str2 = throw1.getProviderName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getStatus();
        String str4 = throw1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getName();
        String str6 = throw1.getName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getHostName();
        String str8 = throw1.getHostName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getMainIp();
        String str10 = throw1.getMainIp();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getSshLoginType();
        String str12 = throw1.getSshLoginType();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getSshUsername();
        String str14 = throw1.getSshUsername();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getSshPassword();
        String str16 = throw1.getSshPassword();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getOldSshPassword();
        String str18 = throw1.getOldSshPassword();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getSshPemContent();
        String str20 = throw1.getSshPemContent();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getSshPassphrase();
        String str22 = throw1.getSshPassphrase();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        if (getSshPort() != throw1.getSshPort())
            return false;
        if (getOldSshPort() != throw1.getOldSshPort())
            return false;
        if (getIpsCount() != throw1.getIpsCount())
            return false;
        if (getInstalled() != throw1.getInstalled())
            return false;
        String str23 = getCountryCode();
        String str24 = throw1.getCountryCode();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getOs();
        String str26 = throw1.getOs();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        if (getDmarcInstalled() != throw1.getDmarcInstalled())
            return false;
        if (getDkimInstalled() != throw1.getDkimInstalled())
            return false;
        String str27 = getSshConnectivityStatus();
        String str28 = throw1.getSshConnectivityStatus();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        Date date1 = getExpirationDate();
        Date date2 = throw1.getExpirationDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        String str29 = getCreatedBy();
        String str30 = throw1.getCreatedBy();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        String str31 = getLastUpdatedBy();
        String str32 = throw1.getLastUpdatedBy();
        if ((str31 == null) ? (str32 != null) : !str31.equals(str32))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = throw1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = throw1.getLastUpdatedDate();
        return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getProviderId();
        String str1 = getProviderName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getStatus();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getHostName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getMainIp();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getSshLoginType();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getSshUsername();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getSshPassword();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getOldSshPassword();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getSshPemContent();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getSshPassphrase();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        n = n * 59 + getSshPort();
        n = n * 59 + getOldSshPort();
        n = n * 59 + getIpsCount();
        n = n * 59 + (getInstalled() ? 79 : 97);
        String str12 =getCountryCode();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getOs();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        n = n * 59 + (getDmarcInstalled() ? 79 : 97);
        n = n * 59 + (getDkimInstalled() ? 79 : 97);
        String str14 = getSshConnectivityStatus();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        Date date1 = getExpirationDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        String str15 = getCreatedBy();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        String str16 = getLastUpdatedBy();
        n = n * 59 + ((str16 == null) ? 43 : str16.hashCode());
        Date date2 = getCreatedDate();
        n = n * 59 + ((date2 == null) ? 43 : date2.hashCode());
        Date date3 = getLastUpdatedDate();
        return n * 59 + ((date3 == null) ? 43 : date3.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMainIp() {
        return mainIp;
    }

    public void setMainIp(String mainIp) {
        this.mainIp = mainIp;
    }

    public String getSshLoginType() {
        return sshLoginType;
    }

    public void setSshLoginType(String sshLoginType) {
        this.sshLoginType = sshLoginType;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
    }

    public String getOldSshPassword() {
        return oldSshPassword;
    }

    public void setOldSshPassword(String oldSshPassword) {
        this.oldSshPassword = oldSshPassword;
    }

    public String getSshPemContent() {
        return sshPemContent;
    }

    public void setSshPemContent(String sshPemContent) {
        this.sshPemContent = sshPemContent;
    }

    public String getSshPassphrase() {
        return sshPassphrase;
    }

    public void setSshPassphrase(String sshPassphrase) {
        this.sshPassphrase = sshPassphrase;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public int getOldSshPort() {
        return oldSshPort;
    }

    public void setOldSshPort(int oldSshPort) {
        this.oldSshPort = oldSshPort;
    }

    public int getIpsCount() {
        return ipsCount;
    }

    public void setIpsCount(int ipsCount) {
        this.ipsCount = ipsCount;
    }

    public boolean getInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public boolean getDmarcInstalled() {
        return dmarcInstalled;
    }

    public void setDmarcInstalled(boolean dmarcInstalled) {
        this.dmarcInstalled = dmarcInstalled;
    }

    public boolean getDkimInstalled() {
        return dkimInstalled;
    }

    public void setDkimInstalled(boolean dkimInstalled) {
        this.dkimInstalled = dkimInstalled;
    }

    public String getSshConnectivityStatus() {
        return sshConnectivityStatus;
    }

    public void setSshConnectivityStatus(String sshConnectivityStatus) {
        this.sshConnectivityStatus = sshConnectivityStatus;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
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
        return "MtaServer(id=" + getId() + ", providerId=" + getProviderId() + ", providerName=" + getProviderName() + ", status=" + getStatus() + ", name=" + getName() + ", hostName=" + getHostName() + ", mainIp=" + getMainIp() + ", sshLoginType=" + getSshLoginType() + ", sshUsername=" + getSshUsername() + ", sshPassword=" + getSshPassword() + ", oldSshPassword=" + getOldSshPassword() + ", sshPemContent=" + getSshPemContent() + ", sshPassphrase=" + getSshPassphrase() + ", sshPort=" + getSshPort() + ", oldSshPort=" + getOldSshPort() + ", ipsCount=" + getIpsCount() + ", installed=" + getInstalled() + ", countryCode=" +getCountryCode() + ", os=" + getOs() + ", dmarcInstalled=" + getDmarcInstalled() + ", dkimInstalled=" + getDkimInstalled() + ", sshConnectivityStatus=" + getSshConnectivityStatus() + ", expirationDate=" + getExpirationDate() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
        }
    }
