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
public class Isp extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "schema_name", type = "text", nullable = false, length = 100)
    public String schemaName;

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

    public Isp() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("isps");
    }

    public Isp(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("isps");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Isp))
            return false;
        Isp class1 = (Isp)paramObject;
        if (!class1.exists(this))
            return false;
        if (getId() != class1.getId())
            return false;
        String str1 = getStatus();
        String str2 = class1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getName();
        String str4 = class1.getName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getSchemaName();
        String str6 = class1.getSchemaName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getCreatedBy();
        String str8 = class1.getCreatedBy();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getLastUpdatedBy();
        String str10 = class1.getLastUpdatedBy();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = class1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = class1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Isp;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getSchemaName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getCreatedBy();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getLastUpdatedBy();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
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

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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
        return "Isp(id=" + getId() + ", status=" + getStatus() + ", name=" + getName() + ", schemaName=" + getSchemaName() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}
