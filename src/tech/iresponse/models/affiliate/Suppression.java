package tech.iresponse.models.affiliate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.sql.Timestamp;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class Suppression extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "process_id", type = "text", nullable = true, length = 20)
    public String processId;

    @Column(name = "affiliate_network_id", type = "integer", nullable = false)
    public int affiliateNetworkId;

    @Column(name = "affiliate_network_name", type = "text", nullable = false, length = 200)
    public String affiliateNetworkName;

    @Column(name = "offer_id", type = "integer", nullable = false)
    public int offerId;

    @Column(name = "offer_name", type = "text", nullable = false)
    public String offerName;

    @Column(name = "lists_ids", type = "text", nullable = false)
    public String listsIds;

    @Column(name = "progress", type = "text", nullable = false, length = 10)
    public String progress;

    @Column(name = "emails_found", type = "integer", nullable = true)
    public int emailsFound;

    @Column(name = "start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp startTime;

    @Column(name = "finish_time", type = "timestamp", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp finishTime;

    public Suppression() throws Exception{
        setDatabase("system");
        setSchema("affiliate");
        setTable("suppressions");
    }

    public Suppression(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("affiliate");
        setTable("suppressions");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Suppression))
            return false;
        Suppression byte1 = (Suppression)paramObject;
        if (!byte1.exists(this))
            return false;
        if (getId() != byte1.getId())
            return false;
        String str1 = getStatus();
        String str2 = byte1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String h3 = getProcessId();
        String h4 = byte1.getProcessId();
        if ((h3 == null) ? (h4 != null) : !h3.equals(h4))
            return false;
        if (getAffiliateNetworkId() != byte1.getAffiliateNetworkId())
           return false;
        String str3 = getAffiliateNetworkName();
        String str4 = byte1.getAffiliateNetworkName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
           return false;
        if (getOfferId() != byte1.getOfferId())
           return false;
        String str5 = getOfferName();
        String str6 = byte1.getOfferName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
           return false;
        String str7 = getListsIds();
        String str8 = byte1.getListsIds();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
           return false;
        String str9 = getProgress();
        String str10 = byte1.getProgress();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
           return false;
        if (getEmailsFound() != byte1.getEmailsFound())
           return false;
        Timestamp timestamp1 = getStartTime();
        Timestamp timestamp2 = byte1.getStartTime();
        if ((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2))
           return false;
        Timestamp timestamp3 = getFinishTime();
        Timestamp timestamp4 = byte1.getFinishTime();
        return !((timestamp3 == null) ? (timestamp4 != null) : !timestamp3.equals(timestamp4));
    }

    protected boolean exists(Object instance) {
        return instance instanceof Suppression;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String h11 = getProcessId();
        n = n * 59 + ((h11 == null) ? 43 : h11.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        String str2 = getAffiliateNetworkName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + getOfferId();
        String str3 = getOfferName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getListsIds();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getProgress();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getEmailsFound();
        Timestamp timestamp1 = getStartTime();
        n = n * 59 + ((timestamp1 == null) ? 43 : timestamp1.hashCode());
        Timestamp timestamp2 = getFinishTime();
        return n * 59 + ((timestamp2 == null) ? 43 : timestamp2.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    public String getProcessId() {
        return this.processId;
    }

    public int getAffiliateNetworkId() {
        return this.affiliateNetworkId;
    }

    public String getAffiliateNetworkName() {
        return this.affiliateNetworkName;
    }

    public int getOfferId() {
        return this.offerId;
    }

    public String getOfferName() {
        return this.offerName;
    }

    public String getListsIds() {
        return this.listsIds;
    }

    public String getProgress() {
        return this.progress;
    }

    public int getEmailsFound() {
        return this.emailsFound;
    }

    public Timestamp getStartTime() {
        return this.startTime;
    }

    public Timestamp getFinishTime() {
        return this.finishTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setAffiliateNetworkId(int affiliateNetworkId) {
        this.affiliateNetworkId = affiliateNetworkId;
    }

    public void setAffiliateNetworkName(String affiliateNetworkName) {
        this.affiliateNetworkName = affiliateNetworkName;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public void setListsIds(String listsIds) {
        this.listsIds = listsIds;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setEmailsFound(int emailsFound) {
        this.emailsFound = emailsFound;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "Suppression(id=" + getId() + ", status=" + getStatus() + ", processId=" + getProcessId() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", affiliateNetworkName=" + getAffiliateNetworkName() + ", offerId=" + getOfferId() + ", offerName=" + getOfferName() + ", listsIds=" + getListsIds() + ", progress=" + getProgress() + ", emailsFound=" + getEmailsFound() + ", startTime=" + getStartTime() + ", finishTime=" + getFinishTime() + ")";
    }
}
