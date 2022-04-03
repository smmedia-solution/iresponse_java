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
public class SmtpUser extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "smtp_server_id", type = "integer", nullable = false)
    public int smtpServerId;

    @Column(name = "smtp_server_name", type = "text", nullable = false, length = 100)
    public String smtpServerName;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "username", type = "text", nullable = false, length = 100)
    public String username;

    @Column(name = "password", type = "text", nullable = false, length = 200)
    public String password;

    @Column(name = "proxy_ip", type = "text", nullable = true, length = 200)
    public String proxyIp;

    @Column(name = "proxy_port", type = "integer", nullable = true)
    public int proxyPort;

    @Column(name = "proxy_username", type = "text", nullable = true, length = 200)
    public String proxyUsername;

    @Column(name = "proxy_password", type = "text", nullable = true, length = 200)
    public String proxyPassword;

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

    public SmtpUser()throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("smtp_users");
    }

    public SmtpUser(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("smtp_users");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpUser))
            return false;
        SmtpUser throws1 = (SmtpUser)paramObject;
        if (!throws1.exists(this))
            return false;
        if (getId() != throws1.getId())
            return false;
        if (getSmtpServerId() != throws1.getSmtpServerId())
            return false;
        String str1 = getSmtpServerName();
        String str2 = throws1.getSmtpServerName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getStatus();
        String str4 = throws1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getUsername();
        String str6 = throws1.getUsername();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getPassword();
        String str8 = throws1.getPassword();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getProxyIp();
        String str10 = throws1.getProxyIp();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getProxyPort() != throws1.getProxyPort())
            return false;
        String str11 = getProxyUsername();
        String str12 = throws1.getProxyUsername();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProxyPassword();
        String str14 = throws1.getProxyPassword();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getCreatedBy();
        String str16 = throws1.getCreatedBy();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getLastUpdatedBy();
        String str18 = throws1.getLastUpdatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = throws1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = throws1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof SmtpUser;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getSmtpServerId();
        String str1 = getSmtpServerName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getStatus();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getUsername();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getPassword();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getProxyIp();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getProxyPort();
        String str6 = getProxyUsername();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProxyPassword();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getCreatedBy();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getLastUpdatedBy();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        Date date1 = getCreatedDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        Date date2 = getLastUpdatedDate();
        return n * 59 + ((date2 == null) ? 43 : date2.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSmtpServerId() {
        return smtpServerId;
    }

    public void setSmtpServerId(int smtpServerId) {
        this.smtpServerId = smtpServerId;
    }

    public String getSmtpServerName() {
        return smtpServerName;
    }

    public void setSmtpServerName(String smtpServerName) {
        this.smtpServerName = smtpServerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
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
        return "SmtpUser(id=" + getId() + ", smtpServerId=" + getSmtpServerId() + ", smtpServerName=" + getSmtpServerName() + ", status=" + getStatus() + ", username=" + getUsername() + ", password=" + getPassword() + ", proxyIp=" + getProxyIp() + ", proxyPort=" + getProxyPort() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}