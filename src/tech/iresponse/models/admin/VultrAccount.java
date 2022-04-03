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
public class VultrAccount extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "token", type = "text", nullable = false)
    public String token;

    @Column(name = "proxy_status", type = "text", nullable = false, length = 100)
    public String proxyStatus;

    @Column(name = "proxy_ip", type = "text", nullable = true, length = 100)
    public String proxyIp;

    @Column(name = "proxy_port", type = "text", nullable = true, length = 5)
    public String proxyPort;

    @Column(name = "proxy_username", type = "text", nullable = true)
    public String proxyUsername;

    @Column(name = "proxy_password", type = "text", nullable = true)
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

    public VultrAccount() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("vultr_accounts");
    }

    public VultrAccount(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("vultr_accounts");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof VultrAccount))
            return false;
        VultrAccount char1 = (VultrAccount)paramObject;
        if (!char1.exists(this))
            return false;
        if (getId() != char1.getId())
            return false;
        String str1 = getStatus();
        String str2 = char1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getName();
        String str4 = char1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getToken();
        String str6 = char1.getToken();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getProxyStatus();
        String str8 = char1.getProxyStatus();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getProxyIp();
        String str10 = char1.getProxyIp();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getProxyPort();
        String str12 = char1.getProxyPort();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProxyUsername();
        String str14 = char1.getProxyUsername();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProxyPassword();
        String str16 = char1.getProxyPassword();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getCreatedBy();
        String str18 = char1.getCreatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getLastUpdatedBy();
        String str20 = char1.getLastUpdatedBy();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = char1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = char1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof VultrAccount;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getToken();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getProxyStatus();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getProxyIp();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getProxyPort();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProxyUsername();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getProxyPassword();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getCreatedBy();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getLastUpdatedBy();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProxyStatus() {
        return proxyStatus;
    }

    public void setProxyStatus(String proxyStatus) {
        this.proxyStatus = proxyStatus;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
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
        return "VultrAccount(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", token=" + getToken() + ", proxyStatus=" + getProxyStatus() + ", proxyIp=" + getProxyIp() + ", proxyPort=" + getProxyPort() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
