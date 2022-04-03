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
public class ProxyServer extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "mta_server_id", type = "integer", nullable = true)
    public int mtaServerId;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "proxy_username", type = "text", nullable = true, length = 100)
    public String proxyUsername;

    @Column(name = "proxy_password", type = "text", nullable = true, length = 200)
    public String proxyPassword;

    @Column(name = "http_port", type = "integer", nullable = false)
    public int httpPort;

    @Column(name = "socks_port", type = "integer", nullable = false)
    public int socksPort;

    @Column(name = "ips_count", type = "integer", nullable = false)
    public int ipsCount;

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

    public ProxyServer() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("proxy_servers");
    }

    public ProxyServer(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("proxy_servers");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ProxyServer))
            return false;
        ProxyServer return1 = (ProxyServer)paramObject;
        if (!return1.exists(this))
            return false;
        if (getId() != return1.getId())
            return false;
        if (getMtaServerId() != return1.getMtaServerId())
            return false;
        String str1 = getStatus();
        String str2 = return1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getName();
        String str4 = return1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getProxyUsername();
        String str6 = return1.getProxyUsername();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getProxyPassword();
        String str8 = return1.getProxyPassword();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        if (getHttpPort() != return1.getHttpPort())
            return false;
        if (getSocksPort() != return1.getSocksPort())
            return false;
        if (getIpsCount() != return1.getIpsCount())
            return false;
        String str9 = getCreatedBy();
        String str10 = return1.getCreatedBy();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getLastUpdatedBy();
        String str12 = return1.getLastUpdatedBy();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = return1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = return1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ProxyServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getMtaServerId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getProxyUsername();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getProxyPassword();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getHttpPort();
        n = n * 59 + getSocksPort();
        n = n * 59 + getIpsCount();
        String str5 = getCreatedBy();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getLastUpdatedBy();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
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

    public int getMtaServerId() {
        return mtaServerId;
    }

    public void setMtaServerId(int mtaServerId) {
        this.mtaServerId = mtaServerId;
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

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getSocksPort() {
        return socksPort;
    }

    public void setSocksPort(int socksPort) {
        this.socksPort = socksPort;
    }

    public int getIpsCount() {
        return ipsCount;
    }

    public void setIpsCount(int ipsCount) {
        this.ipsCount = ipsCount;
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
        return "ProxyServer(id=" + getId() + ", mtaServerId=" + getMtaServerId() + ", status=" + getStatus() + ", name=" + getName() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", httpPort=" + getHttpPort() + ", socksPort=" + getSocksPort() + ", ipsCount=" + getIpsCount() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
