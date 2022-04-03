package tech.iresponse.models.production;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class SmtpProcess extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "text", nullable = true, length = 20)
    public String processId;

    @Column(name = "process_type", type = "text", nullable = false, length = 100)
    public String processType;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    @Column(name = "servers_ids", type = "text", nullable = false)
    public String serversIds;

    @Column(name = "user_id", type = "integer", nullable = false)
    public int userId;

    @Column(name = "total_emails", type = "integer", nullable = true)
    public int totalEmails;

    @Column(name = "progress", type = "integer", nullable = true)
    public int progress;

    @Column(name = "auto_responders_ids", type = "text", nullable = true)
    public String autoRespondersIds;

    @Column(name = "affiliate_network_id", type = "integer", nullable = true)
    public int affiliateNetworkId;

    @Column(name = "offer_id", type = "integer", nullable = true)
    public int offerId;

    @Column(name = "isp_id", type = "integer", nullable = true)
    public int ispId;

    @Column(name = "data_start", type = "integer", nullable = true)
    public int dataStart;

    @Column(name = "data_count", type = "integer", nullable = true)
    public int dataCount;

    @Column(name = "lists", type = "text", nullable = true)
    public String lists;

    @Column(name = "delivered", type = "integer", nullable = true)
    public int delivered;

    @Column(name = "hard_bounced", type = "integer", nullable = true)
    public int hardBounced;

    @Column(name = "soft_bounced", type = "integer", nullable = true)
    public int softBounced;

    @Column(name = "opens", type = "integer", nullable = true)
    public int opens;

    @Column(name = "clicks", type = "integer", nullable = true)
    public int clicks;

    @Column(name = "leads", type = "integer", nullable = true)
    public int leads;

    @Column(name = "unsubs", type = "integer", nullable = true)
    public int unsubs;

    @Column(name = "negative_file_path", type = "text", nullable = true)
    public String negativeFilePath;

    @Column(name = "content", type = "text", nullable = false)
    public String content;

    public SmtpProcess() throws Exception {
        setDatabase("system");
        setSchema("production");
        setTable("smtp_processes");
    }

    public SmtpProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("production");
        setTable("smtp_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpProcess))
            return false;
        SmtpProcess int1 = (SmtpProcess)paramObject;
        if (!int1.exists(this))
            return false;
        if (getId() != int1.getId())
            return false;
        String str1 = getStatus();
        String str2 = int1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessId();
        String str4 = int1.getProcessId();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getProcessType();
        String str6 = int1.getProcessType();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = int1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
            return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = int1.getFinishTime();
        if ((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4))
            return false;
        String str7 = getServersIds();
        String str8 = int1.getServersIds();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        if (getUserId() != int1.getUserId())
            return false;
        if (getTotalEmails() != int1.getTotalEmails())
            return false;
        if (getProgress() != int1.getProgress())
            return false;
        String str9 = getAutoRespondersIds();
        String str10 = int1.getAutoRespondersIds();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getAffiliateNetworkId() != int1.getAffiliateNetworkId())
            return false;
        if (getOfferId() != int1.getOfferId())
            return false;
        if (getIspId() != int1.getIspId())
            return false;
        if (getDataStart() != int1.getDataStart())
            return false;
        if (getDataCount() != int1.getDataCount())
            return false;
        String str11 = getLists();
        String str12 = int1.getLists();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        if (getDelivered() != int1.getDelivered())
            return false;
        if (getHardBounced() != int1.getHardBounced())
            return false;
        if (getSoftBounced() != int1.getSoftBounced())
            return false;
        if (getOpens() != int1.getOpens())
            return false;
        if (getClicks() != int1.getClicks())
            return false;
        if (getLeads() != int1.getLeads())
            return false;
        if (getUnsubs() != int1.getUnsubs())
            return false;
        String str13 = getNegativeFilePath();
        String str14 = int1.getNegativeFilePath();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getContent();
        String str16 = int1.getContent();
        return !((str15 == null) ? (str16 != null) : !str15.equals(str16));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpProcess;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getProcessId();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getProcessType();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        Timestamp timestamp1 = getStartTime();
        n = n * 59 + ((timestamp1 == null) ? 43 : timestamp1.hashCode());
        Timestamp timestamp2 = getFinishTime();
        n = n * 59 + ((timestamp2 == null) ? 43 : timestamp2.hashCode());
        String str4 = getServersIds();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getUserId();
        n = n * 59 + getTotalEmails();
        n = n * 59 + getProgress();
        String str5 = getAutoRespondersIds();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        n = n * 59 + getOfferId();
        n = n * 59 + getIspId();
        n = n * 59 + getDataStart();
        n = n * 59 + getDataCount();
        String str6 = getLists();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        n = n * 59 + getDelivered();
        n = n * 59 + getHardBounced();
        n = n * 59 + getSoftBounced();
        n = n * 59 + getOpens();
        n = n * 59 + getClicks();
        n = n * 59 + getLeads();
        n = n * 59 + getUnsubs();
        String str7 = getNegativeFilePath();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getContent();
        return n * 59 + ((str8 == null) ? 43 : str8.hashCode());
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

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public String getServersIds() {
        return serversIds;
    }

    public void setServersIds(String serversIds) {
        this.serversIds = serversIds;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalEmails() {
        return totalEmails;
    }

    public void setTotalEmails(int totalEmails) {
        this.totalEmails = totalEmails;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getAutoRespondersIds() {
        return autoRespondersIds;
    }

    public void setAutoRespondersIds(String autoRespondersIds) {
        this.autoRespondersIds = autoRespondersIds;
    }

    public int getAffiliateNetworkId() {
        return affiliateNetworkId;
    }

    public void setAffiliateNetworkId(int affiliateNetworkId) {
        this.affiliateNetworkId = affiliateNetworkId;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public int getIspId() {
        return ispId;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public int getDataStart() {
        return dataStart;
    }

    public void setDataStart(int dataStart) {
        this.dataStart = dataStart;
    }

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public String getLists() {
        return lists;
    }

    public void setLists(String lists) {
        this.lists = lists;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getHardBounced() {
        return hardBounced;
    }

    public void setHardBounced(int hardBounced) {
        this.hardBounced = hardBounced;
    }

    public int getSoftBounced() {
        return softBounced;
    }

    public void setSoftBounced(int softBounced) {
        this.softBounced = softBounced;
    }

    public int getOpens() {
        return opens;
    }

    public void setOpens(int opens) {
        this.opens = opens;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getLeads() {
        return leads;
    }

    public void setLeads(int leads) {
        this.leads = leads;
    }

    public int getUnsubs() {
        return unsubs;
    }

    public void setUnsubs(int unsubs) {
        this.unsubs = unsubs;
    }

    public String getNegativeFilePath() {
        return negativeFilePath;
    }

    public void setNegativeFilePath(String negativeFilePath) {
        this.negativeFilePath = negativeFilePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SmtpProcess(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", processType=" + getProcessType() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ", serversIds=" + getServersIds() + ", userId=" + getUserId() + ", totalEmails=" + getTotalEmails() + ", progress=" + getProgress() + ", autoRespondersIds=" + getAutoRespondersIds() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", offerId=" + getOfferId() + ", ispId=" + getIspId() + ", dataStart=" + getDataStart() + ", dataCount=" + getDataCount() + ", lists=" + getLists() + ", delivered=" + getDelivered() + ", hardBounced=" + getHardBounced() + ", softBounced=" + getSoftBounced() + ", opens=" + getOpens() + ", clicks=" + getClicks() + ", leads=" + getLeads() + ", unsubs=" + getUnsubs() + ", negativeFilePath=" + getNegativeFilePath() + ", content=" + getContent() + ")";
    }
}
