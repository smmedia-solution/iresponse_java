package tech.iresponse.models.lists;

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
public class DataList extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "data_provider_id", type = "integer", nullable = false)
    public int dataProviderId;

    @Column(name = "data_provider_name", type = "text", nullable = false, length = 200)
    public String dataProviderName;

    @Column(name = "name", type = "text", nullable = false, length = 200)
    public String name;

    @Column(name = "table_name", type = "text", nullable = false, length = 100)
    public String tableName;

    @Column(name = "table_schema", type = "text", nullable = false, length = 100)
    public String tableSchema;

    @Column(name = "isp_id", type = "integer", nullable = false)
    public int ispId;

    @Column(name = "isp_name", type = "text", nullable = false, length = 200)
    public String ispName;

    @Column(name = "total_count", type = "integer", nullable = true)
    public int totalCount;

    @Column(name = "encrypt_emails", type = "text", nullable = false, length = 20)
    public String encryptEmails;

    @Column(name = "created_by", type = "text", nullable = false, length = 200)
    public String createdBby;

    @Column(name = "last_updated_by", type = "text", nullable = true, length = 200)
    public String lastUpdatedBy;

    @Column(name = "created_date", type = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date createdDate;

    @Column(name = "last_updated_date", type = "date", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date lastUpdatedDate;

    public DataList() throws Exception {
        setDatabase("system");
        setSchema("lists");
        setTable("data_lists");
    }

    public DataList(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("lists");
        setTable("data_lists");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DataList))
            return false;
        DataList for1 = (DataList)paramObject;
        if (!for1.exists(this))
            return false;
        if (getId() != for1.getId())
            return false;
        String str1 = getStatus();
        String str2 = for1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getDataProviderId() != for1.getDataProviderId())
            return false;
        String str3 = getDataProviderName();
        String str4 = for1.getDataProviderName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getName();
        String str6 = for1.getName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getTableName();
        String str8 = for1.getTableName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getTableSchema();
        String str10 = for1.getTableSchema();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getIspId() != for1.getIspId())
            return false;
        String str11 = getIspName();
        String str12 = for1.getIspName();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        if (getTotalCount() != for1.getTotalCount())
            return false;
        String str13 = getEncryptEmails();
        String str14 = for1.getEncryptEmails();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getCreatedBby();
        String str16 = for1.getCreatedBby();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getLastUpdatedBy();
        String str18 = for1.getLastUpdatedBy();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = for1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = for1.getLastUpdatedDate();
        return !((date3 == null) ? (date4 != null) : !date3.equals(date4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof DataList;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getDataProviderId();
        String str2 = getDataProviderName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getTableName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getTableSchema();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getIspId();
        String str6 = getIspName();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        n = n * 59 + getTotalCount();
        String str7 = getEncryptEmails();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getCreatedBby();
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

    public String getStatus() {
        return this.status;
    }

    public int getDataProviderId() {
        return this.dataProviderId;
    }

    public String getDataProviderName() {
        return this.dataProviderName;
    }

    public String getName() {
        return this.name;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getTableSchema() {
        return this.tableSchema;
    }

    public int getIspId() {
        return this.ispId;
    }

    public String getIspName() {
        return this.ispName;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public String getEncryptEmails() {
        return this.encryptEmails;
    }

    public String getCreatedBby() {
        return this.createdBby;
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

    public void setDataProviderId(int dataProviderId) {
        this.dataProviderId = dataProviderId;
    }

    public void setDataProviderName(String dataProviderName) {
        this.dataProviderName = dataProviderName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public void setIspName(String ispName) {
        this.ispName = ispName;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setEncryptEmails(String encryptEmails) {
        this.encryptEmails = encryptEmails;
    }

    public void setCreatedBby(String createdBby) {
        this.createdBby = createdBby;
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
        return "DataList(id=" + getId() + ", status=" + getStatus() + ", dataProviderId=" + getDataProviderId() + ", dataProviderName=" + getDataProviderName() + ", name=" + getName() + ", tableName=" + getTableName() + ", tableSchema=" + getTableSchema() + ", ispId=" + getIspId() + ", ispName=" + getIspName() + ", totalCount=" + getTotalCount() + ", encryptEmails=" + getEncryptEmails() + ", createdBy=" + getCreatedBby() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ")";
    }
}