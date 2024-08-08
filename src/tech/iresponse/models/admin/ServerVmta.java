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
public class ServerVmta extends ActiveRecord implements Serializable {
    
    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "mta_server_id", type = "integer", nullable = false)
    public int mtaServerId;

    @Column(name = "mta_server_name", type = "text", nullable = false, length = 200)
    public String mtaServerName;

    @Column(name = "isp_id", type = "integer", nullable = true)
    public int ispId;

    @Column(name = "isp_name", type = "text", nullable = true, length = 200)
    public String ispName;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "type", type = "text", nullable = false, length = 100)
    public String type;

    @Column(name = "name", type = "text", nullable = false, length = 100)
    public String name;

    @Column(name = "domain", type = "text", nullable = false, length = 100)
    public String domain;

    @Column(name = "custom_domain", type = "text", nullable = true, length = 100)
    public String customDomain;

    @Column(name = "ip", type = "text", nullable = false, length = 100)
    public String ip;

    @Column(name = "ping_status", type = "text", nullable = false, length = 100)
    public String pingStatus;

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

    @Column(name = "smtp_1", type = "text", nullable = true, length = 255)
    public String smtp_1;

    @Column(name = "smtp_2", type = "text", nullable = true, length = 255)
    public String smtp_2;

    public ServerVmta() throws Exception{
        setDatabase("system");
        setSchema("admin");
        setTable("servers_vmtas");
    }

    public ServerVmta(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("servers_vmtas");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ServerVmta))
            return false;
        ServerVmta extends1 = (ServerVmta)paramObject;
        if (!extends1.exists(this))
            return false;
        if (getId() != extends1.getId())
            return false;
        if (getMtaServerId() != extends1.getMtaServerId())
            return false;
        String str1 = getMtaServerName();
        String str2 = extends1.getMtaServerName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getIspId() != extends1.getIspId())
            return false;
        String str3 = getIspName();
        String str4 = extends1.getIspName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getStatus();
        String str6 = extends1.getStatus();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getType();
        String str8 = extends1.getType();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getName();
        String str10 = extends1.getName();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getDomain();
        String str12 = extends1.getDomain();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getCustomDomain();
        String str14 = extends1.getCustomDomain();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getIp();
        String str16 = extends1.getIp();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getPingStatus();
        String str18 = extends1.getPingStatus();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getCreatedBy();
        String str20 = extends1.getCreatedBy();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getLastUpdatedBy();
        String str22 = extends1.getLastUpdatedBy();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        Date date1 = getCreatedDate();
        Date date2 = extends1.getCreatedDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getLastUpdatedDate();
        Date date4 = extends1.getLastUpdatedDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        String str23 = getSmtp_1();
        String str24 = extends1.getSmtp_1();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getSmtp_2();
        String str26 = extends1.getSmtp_2();
        return !((str25 == null) ? (str26 != null) : !str25.equals(str26));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ServerVmta;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getMtaServerId();
        String str1 = getMtaServerName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getIspId();
        String str2 = getIspName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getStatus();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getType();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getName();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getDomain();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getCustomDomain();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getIp();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getPingStatus();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getCreatedBy();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getLastUpdatedBy();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
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

    public String getMtaServerName() {
        return mtaServerName;
    }

    public void setMtaServerName(String mtaServerName) {
        this.mtaServerName = mtaServerName;
    }

    public int getIspId() {
        return ispId;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public String getIspName() {
        return ispName;
    }

    public void setIspName(String ispName) {
        this.ispName = ispName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPingStatus() {
        return pingStatus;
    }

    public void setPingStatus(String pingStatus) {
        this.pingStatus = pingStatus;
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

    public String getSmtp_1() { return smtp_1; }

    public void setSmtp_1(String smtp_1) { this.smtp_1 = smtp_1; }

    public String getSmtp_2() { return smtp_2; }

    public void setSmtp_2(String smtp_2) { this.smtp_2 = smtp_2; }

    @Override
    public String toString() {
        return "ServerVmta(id=" + getId() + ", mtaServerId=" + getMtaServerId() + ", mtaServerName=" + getMtaServerName() + ", ispId=" + getIspId() + ", ispName=" + getIspName() + ", status=" + getStatus() + ", type=" + getType() + ", name=" + getName() + ", domain=" + getDomain() + ", customDomain=" + getCustomDomain() + ", ip=" + getIp() + ", pingStatus=" + getPingStatus() + ", createdBy=" + getCreatedBy() + ", lastUpdatedBy=" + getLastUpdatedBy() + ", createdDate=" + getCreatedDate() + ", lastUpdatedDate=" + getLastUpdatedDate() + ", smtp_1=" + getSmtp_1() + ", smtp_2=" + getSmtp_2() + ")";
        }
    }
