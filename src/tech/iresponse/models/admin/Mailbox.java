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
public class Mailbox extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "account_id", type = "integer", nullable = false)
    public int accountId;

    @Column(name = "account_name", type = "text", nullable = false, length = 100)
    public String accountName;

    @Column(name = "domain_id", type = "integer", nullable = false)
    public int domainId;

    @Column(name = "domain_name", type = "text", nullable = false, length = 100)
    public String domainName;

    @Column(name = "email", type = "text", nullable = false, length = 100)
    public String email;

    @Column(name = "password", type = "text", nullable = false, length = 100)
    public String password;

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

    public Mailbox() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("mailboxes");
    }

    public Mailbox(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("mailboxes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Mailbox))
            return false;
        Mailbox short1 = (Mailbox)paramObject;
        if (!short1.exists(this))
            return false;
        if (getId() != short1.getId())
            return false;
        String str1 = getStatus();
        String str2 = short1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getAccountId() != short1.getAccountId())
            return false;
        String str3 = getAccountName();
        String str4 = short1.getAccountName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (getDomainId() != short1.getDomainId())
            return false;
        String str5 = getDomainName();
        String str6 = short1.getDomainName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getEmail();
        String str8 = short1.getEmail();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getPassword();
        String str10 = short1.getPassword();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str13 = getCreatedBy();
        String str14 = short1.getCreatedBy();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getLastUpdatedBy();
        String str16 = short1.getLastUpdatedBy();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        Date date3 = getCreatedDate();
        Date date4 = short1.getCreatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        Date date5 = getLastUpdatedDate();
        Date date6 = short1.getLastUpdatedDate();
            return !((date5 == null) ? (date6 != null) : !date5.equals(date6));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Mailbox;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getAccountId();
        String str2 = getAccountName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getDomainId();
        String str3 = getDomainName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getEmail();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getPassword();
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

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        return "Mailbox(id=" + getId() + ", status=" + getStatus() + ", accountId=" + getAccountId() + ", accountName=" + getAccountName() + ", domainId=" + getDomainId() + ", domainName=" + getDomainName() + ", email=" + getEmail() + ", password=" + getPassword() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
