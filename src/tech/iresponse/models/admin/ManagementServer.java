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
public class ManagementServer extends ActiveRecord implements Serializable {

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

    @Column(name = "ssh_pem_content", type = "text", nullable = true)
    public String sshPemContent;

    @Column(name = "ssh_passphrase", type = "text", nullable = true, length = 200)
    public String sshPassphrase;

    @Column(name = "ssh_port", type = "integer", nullable = false)
    public int sshPort;

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

    public ManagementServer() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("management_servers");
    }

    public ManagementServer(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("management_servers");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ManagementServer))
            return false;
        ManagementServer super1 = (ManagementServer)paramObject;
        if (!super1.exists(this))
            return false;
        if (getId() != super1.getId())
            return false;
        if (getProviderId() != super1.getProviderId())
            return false;
        String str1 = getProviderName();
        String str2 = super1.getProviderName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getStatus();
        String str4 = super1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getName();
        String str6 = super1.getName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str9 = getHostName();
        String str10 = super1.getHostName();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getMainIp();
        String str12 = super1.getMainIp();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getSshLoginType();
        String str14 = super1.getSshLoginType();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getSshUsername();
        String str16 = super1.getSshUsername();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getSshPassword();
        String str18 = super1.getSshPassword();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getSshPemContent();
        String str20 = super1.getSshPemContent();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getSshPassphrase();
        String str22 = super1.getSshPassphrase();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        if (getSshPort() != super1.getSshPort())
            return false;
        Date date1 = getExpirationDate();
        Date date2 = super1.getExpirationDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        String str23 = getCreatedBy();
        String str24 = super1.getCreatedBy();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getLastUpdatedBy();
        String str26 = super1.getLastUpdatedBy();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = super1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = super1.getLastUpdatedDate();
        return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object instance) {
        return instance instanceof ManagementServer;
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
        String str5 = getHostName();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getMainIp();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getSshLoginType();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getSshUsername();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getSshPassword();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getSshPemContent();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getSshPassphrase();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        n = n * 59 + getSshPort();
        Date date1 = getExpirationDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        String str12 = getCreatedBy();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getLastUpdatedBy();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        Date date2 = getCreatedDate();
        n = n * 59 + ((date2 == null) ? 43 : date2.hashCode());
        Date date3 = getLastUpdatedDate();
        return n * 59 + ((date3 == null) ? 43 : date3.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public int getProviderId() {
        return this.providerId;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getStatus() {
        return this.status;
    }

    public String getName() {
        return this.name;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getMainIp() {
        return this.mainIp;
    }

    public String getSshLoginType() {
        return this.sshLoginType;
    }

    public String getSshUsername() {
        return this.sshUsername;
    }

    public String getSshPassword() {
        return this.sshPassword;
    }

    public String getSshPemContent() {
        return this.sshPemContent;
    }

    public String getSshPassphrase() {
        return this.sshPassphrase;
    }

    public int getSshPort() {
        return this.sshPort;
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

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setMainIp(String mainIp) {
        this.mainIp = mainIp;
    }

    public void setSshLoginType(String sshLoginType) {
        this.sshLoginType = sshLoginType;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
    }

    public void setSshPemContent(String sshPemContent) {
        this.sshPemContent = sshPemContent;
    }

    public void setSshPassphrase(String sshPassphrase) {
        this.sshPassphrase = sshPassphrase;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
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

    @Override
    public String toString() {
        return "ManagementServer(id=" + getId() + ", providerId=" + getProviderId() + ", providerName=" + getProviderName() + ", status=" + getStatus() + ", name=" + getName() + ", hostName=" + getHostName() + ", mainIp=" + getMainIp() + ", sshLoginType=" + getSshLoginType() + ", sshUsername=" + getSshUsername() + ", sshPassword=" + getSshPassword() + ", sshPemContent=" + getSshPemContent() + ", sshPassphrase=" + getSshPassphrase() + ", sshPort=" + getSshPort() + ", expirationDate=" + getExpirationDate() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
