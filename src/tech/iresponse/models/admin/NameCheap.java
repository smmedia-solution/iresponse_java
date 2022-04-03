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
public class NameCheap extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "username", type = "text", nullable = false, length = 100)
    public String username;

    @Column(name = "api_key", type = "text", nullable = false, length = 200)
    public String apiKey;

    @Column(name = "white_listed_ip", type = "text", nullable = false, length = 15)
    public String whiteListedIp;

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

    public NameCheap() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("namecheap_accounts");
    }

    public NameCheap(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("namecheap_accounts");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof NameCheap))
            return false;
        NameCheap while1 = (NameCheap)paramObject;
        if (!while1.exists(this))
            return false;
        if (getId() != while1.getId())
            return false;
        String str1 = getStatus();
        String str2 = while1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getName();
        String str4 = while1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getUsername();
        String str6 = while1.getUsername();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getApiKey();
        String str8 = while1.getApiKey();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getWhiteListedIp();
        String str10 = while1.getWhiteListedIp();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getCreatedBy();
        String str12 = while1.getCreatedBy();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getLastUpdatedBy();
        String str14 = while1.getLastUpdatedBy();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = while1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = while1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof NameCheap;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getUsername();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getApiKey();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getWhiteListedIp();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getCreatedBy();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getLastUpdatedBy();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
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

    public String getUsername() {
        return this.username;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getWhiteListedIp() {
        return this.whiteListedIp;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setWhiteListedIp(String whiteListedIp) {
        this.whiteListedIp = whiteListedIp;
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
        return "NameCheap(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", username=" + getUsername() + ", apiKey=" + getApiKey() + ", whiteListedIp=" + getWhiteListedIp() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
