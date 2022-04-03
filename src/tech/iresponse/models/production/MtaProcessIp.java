package tech.iresponse.models.production;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class MtaProcessIp extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "process_id", type = "integer", nullable = false)
    public int processId;

    @Column(name = "server_vmta_id", type = "integer", nullable = false)
    public int serverVmtaId;

    @Column(name = "sent_total", type = "integer", nullable = true)
    public int sentTotal;

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

    public MtaProcessIp() throws Exception{
        setDatabase("system");
        setSchema("production");
        setTable("mta_processes_ips");
    }

    public MtaProcessIp(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("production");
        setTable("mta_processes_ips");
        load();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getServerVmtaId() {
        return serverVmtaId;
    }

    public void setServerVmtaId(int serverVmtaId) {
        this.serverVmtaId = serverVmtaId;
    }

    public int getSentTotal() {
        return sentTotal;
    }

    public void setSentTotal(int sentTotal) {
        this.sentTotal = sentTotal;
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

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaProcessIp))
            return false;
        MtaProcessIp for1 = (MtaProcessIp)paramObject;
        return !for1.exists(this) ? false : ((getId() != for1.getId()) ? false : ((getProcessId() != for1.getProcessId()) ? false : ((getServerVmtaId() != for1.getServerVmtaId()) ? false : ((getSentTotal() != for1.getSentTotal()) ? false : ((getDelivered() != for1.getDelivered()) ? false : ((getHardBounced() != for1.getHardBounced()) ? false : ((getSoftBounced() != for1.getSoftBounced()) ? false : ((getOpens() != for1.getOpens()) ? false : ((getClicks() != for1.getClicks()) ? false : ((getLeads() != for1.getLeads()) ? false : (!(getUnsubs() != for1.getUnsubs()))))))))))));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaProcessIp;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getProcessId();
        n = n * 59 + getServerVmtaId();
        n = n * 59 + getSentTotal();
        n = n * 59 + getDelivered();
        n = n * 59 + getHardBounced();
        n = n * 59 + getSoftBounced();
        n = n * 59 + getOpens();
        n = n * 59 + getClicks();
        n = n * 59 + getLeads();
        return n * 59 + getUnsubs();
    }

    @Override
    public String toString() {
        return "MtaProcessIp(id=" + getId() + ", processId=" + getProcessId() + ", serverVmtaId=" + getServerVmtaId() + ", sentTotal=" + getSentTotal() + ", delivered=" + getDelivered() + ", hardBounced=" + getHardBounced() + ", softBounced=" + getSoftBounced() + ", opens=" + getOpens() + ", clicks=" + getClicks() + ", leads=" + getLeads() + ", unsubs=" + getUnsubs() + ")";
    }
}
