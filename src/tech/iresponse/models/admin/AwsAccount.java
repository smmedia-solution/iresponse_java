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
public class AwsAccount extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "access_key", type = "text", nullable = false, length = 200)
    public String accessKey;

    @Column(name = "secret_key", type = "text", nullable = false, length = 200)
    public String secretKey;

    @Column(name = "default_images_ids", type = "text", nullable = true)
    public String defaultImagesIds;

    @Column(name = "proxy_status", type = "text", nullable = false, length = 100)
    public String proxyStatus;

    @Column(name = "proxy_ip", type = "text", nullable = true, length = 100)
    public String proxyIp;

    @Column(name = "proxy_port", type = "text", nullable = true, length = 5)
    public String proxyPort;

    @Column(name = "proxy_username", type = "text", nullable = true, length = 500)
    public String proxyUsername;

    @Column(name = "proxy_password", type = "text", nullable = true, length = 500)
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

    public AwsAccount() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("aws_accounts");
    }

    public AwsAccount(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("aws_accounts");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AwsAccount))
            return false;
        AwsAccount new1 = (AwsAccount)paramObject;
        if (!new1.exists(this))
            return false;
        if (getId() != new1.getId())
            return false;
        String str1 = getStatus();
        String str2 = new1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getName();
        String str4 = new1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getAccessKey();
        String str6 = new1.getAccessKey();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getSecretKey();
        String str8 = new1.getSecretKey();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getDefaultImagesIds();
        String str10 = new1.getDefaultImagesIds();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getProxyStatus();
        String str12 = new1.getProxyStatus();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProxyIp();
        String str14 = new1.getProxyIp();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProxyPort();
        String str16 = new1.getProxyPort();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getProxyUsername();
        String str18 = new1.getProxyUsername();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getProxyPassword();
        String str20 = new1.getProxyPassword();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getCreatedBy();
        String str22 = new1.getCreatedBy();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getLastUpdatedBy();
        String str24 = new1.getLastUpdatedBy();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = new1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = new1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }


    protected boolean exists(Object instance) {
        return instance instanceof AwsAccount;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getAccessKey();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getSecretKey();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getDefaultImagesIds();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getProxyStatus();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProxyIp();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getProxyPort();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getProxyUsername();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getProxyPassword();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getCreatedBy();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getLastUpdatedBy();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
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

    public String getName() {
        return this.name;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public String getDefaultImagesIds() {
        return this.defaultImagesIds;
    }

    public String getProxyStatus() {
        return this.proxyStatus;
    }

    public String getProxyIp() {
        return this.proxyIp;
    }

    public String getProxyPort() {
        return this.proxyPort;
    }

    public String getProxyUsername() {
        return this.proxyUsername;
    }

    public String getProxyPassword() {
        return this.proxyPassword;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setDefaultImagesIds(String defaultImagesIds) {
        this.defaultImagesIds = defaultImagesIds;
    }

    public void setProxyStatus(String proxyStatus) {
        this.proxyStatus = proxyStatus;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
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
        return "AwsAccount(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", accessKey=" + getAccessKey() + ", secretKey=" + getSecretKey() + ", defaultImagesIds=" + getDefaultImagesIds() + ", proxyStatus=" + getProxyStatus() + ", proxyIp=" + getProxyIp() + ", proxyPort=" + getProxyPort() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
