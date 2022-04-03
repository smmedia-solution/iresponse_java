package tech.iresponse.models.admin;

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
public class SmtpServer extends ActiveRecord implements Serializable {

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

    @Column(name = "encryption_type", type = "text", nullable = false, length = 10)
    public String encryptionType;

    @Column(name = "smtp_port", type = "integer", nullable = false)
    public int smtpPort;

    @Column(name = "users_count", type = "integer", nullable = false)
    public int usersCount;

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

    public SmtpServer() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("smtp_servers");
    }

    public SmtpServer(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("smtp_servers");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpServer))
            return false;
        SmtpServer switch1 = (SmtpServer)paramObject;
        if (!switch1.exists(this))
            return false;
        if (getId() != switch1.getId())
            return false;
        if (getProviderId() != switch1.getProviderId())
            return false;
        String str1 = getProviderName();
        String str2 = switch1.getProviderName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getStatus();
        String str4 = switch1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getName();
        String str6 = switch1.getName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getHostName();
        String str8 = switch1.getHostName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getEncryptionType();
        String str10 = switch1.getEncryptionType();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getSmtpPort() != switch1.getSmtpPort())
            return false;
        if (getUsersCount() != switch1.getUsersCount())
            return false;
        Date date1 = getExpirationDate();
        Date date2 = switch1.getExpirationDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        String str11 = getCreatedBy();
        String str12 = switch1.getCreatedBy();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getLastUpdatedBy();
        String str14 = switch1.getLastUpdatedBy();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = switch1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = switch1.getLastUpdatedDate();
        return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object instance) {
        return instance instanceof SmtpServer;
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
        String str5 = getEncryptionType();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getSmtpPort();
        n = n * 59 + getUsersCount();
        Date date1 = getExpirationDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        String str6 = getCreatedBy();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getLastUpdatedBy();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
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

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
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
        return "SmtpServer(id=" + getId() + ", providerId=" + getProviderId() + ", providerName=" + getProviderName() + ", status=" + getStatus() + ", name=" + getName() + ", hostName=" + getHostName() + ", encryptionType=" + getEncryptionType() + ", smtpPort=" + getSmtpPort() + ", usersCount=" + getUsersCount() + ", expirationDate=" + getExpirationDate() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}