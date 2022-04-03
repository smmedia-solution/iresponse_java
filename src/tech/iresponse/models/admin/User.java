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
public class User extends ActiveRecord implements Serializable {
    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "production_id", type = "integer", nullable = false)
    public int productionId;

    @Column(name = "master_access", type = "text", nullable = false, length = 20)
    public String masterAccess;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "first_name", type = "text", nullable = false, length = 200)
    public String firstName;

    @Column(name = "last_name", type = "text", nullable = true, length = 200)
    public String lastName;

    @Column(name = "email", type = "text", nullable = false, length = 200)
    public String email;

    @Column(name = "password", type = "text", nullable = false, length = 200)
    public String password;

    @Column(name = "avatar_name", type = "text", nullable = true, length = 100)
    public String avatarName;

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

    public User() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("users");
    }

    public User(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("users");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof User))
            return false;
        User default1 = (User)paramObject;
        if (!default1.exist(this))
            return false;
        if (getId() != default1.getId())
            return false;
        if (getProductionId() != default1.getProductionId())
            return false;
        String str1 = getMasterAccess();
        String str2 = default1.getMasterAccess();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getStatus();
        String str4 = default1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getFirstName();
        String str6 = default1.getFirstName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getLastName();
        String str8 = default1.getLastName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getEmail();
        String str10 = default1.getEmail();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getPassword();
        String str12 = default1.getPassword();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getAvatarName();
        String str14 = default1.getAvatarName();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getCreatedBy();
        String str16 = default1.getCreatedBy();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getLastUpdatedBy();
        String str18 = default1.getLastUpdatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = default1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = default1.getLastUpdatedDate();
            return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exist(Object paramObject) { //do
        return paramObject instanceof User;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getProductionId();
        String str1 = getMasterAccess();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getStatus();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getFirstName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getLastName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getEmail();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getPassword();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getAvatarName();
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
        return this.id;
    }

    public int getProductionId() {
        return this.productionId;
    }

    public String getMasterAccess() {
        return this.masterAccess;
    }

    public String getStatus() {
        return this.status;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAvatarName() {
        return this.avatarName;
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

    public void setId(int paramInt) {
        this.id = paramInt;
    }

    public void setProductionId(int paramInt) {
        this.productionId = paramInt;
    }

    public void setMasterAccess(String paramString) {
        this.masterAccess = paramString;
    }

    public void setStatus(String paramString) {
        this.status = paramString;
    }

    public void setFirstName(String paramString) {
        this.firstName = paramString;
    }

    public void setLastName(String paramString) {
        this.lastName = paramString;
    }

    public void setEmail(String paramString) {
        this.email = paramString;
    }

    public void setPassword(String paramString) {
        this.password = paramString;
    }

    public void setAvatarName(String paramString) {
        this.avatarName = paramString;
    }

    public void setCreatedBy(String paramString) {
        this.createdBy = paramString;
    }

    public void setLastUpdatedBy(String paramString) {
        this.lastUpdatedBy = paramString;
    }

    public void setCreatedDate(Date paramDate) {
        this.createdDate = paramDate;
    }

    public void setLastUpdatedDate(Date paramDate) {
        this.lastUpdatedDate = paramDate;
    }

    @Override
    public String toString() {
        return "User(id=" + getId() + ", productionId=" + getProductionId() + ", masterAccess=" + getMasterAccess() + ", status=" + getStatus() + ", firstName=" + getFirstName() + ", lastName=" + getLastName() + ", email=" + getEmail() + ", password=" + getPassword() + ", avatarName=" + getAvatarName() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
