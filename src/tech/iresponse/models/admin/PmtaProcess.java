package tech.iresponse.models.admin;

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
public class PmtaProcess extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "provider_id", type = "integer", nullable = false)
    public int providerId;

    @Column(name = "provider_name", type = "text", nullable = false, length = 200)
    public String providerName;

    @Column(name = "server_id", type = "integer", nullable = false)
    public int serverId;

    @Column(name = "server_name", type = "text", nullable = false)
    public String serverName;

    @Column(name = "user_full_name", type = "text", nullable = false)
    public String userFullName;

    @Column(name = "queues", type = "text", nullable = true, length = 100)
    public String queues;

    @Column(name = "vmtas", type = "text", nullable = true)
    public String vmtas;

    @Column(name = "pause_wait", type = "integer", nullable = false)
    public int pauseWait;

    @Column(name = "resume_wait", type = "integer", nullable = false)
    public int resumeWait;

    @Column(name = "action_start_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp actionStartTime;

    public PmtaProcess() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("pmta_processes");
    }

    public PmtaProcess(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("pmta_processes");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof PmtaProcess))
            return false;
        PmtaProcess public1 = (PmtaProcess)paramObject;
        if (!public1.exists(this))
            return false;
        if (getId() != public1.getId())
            return false;
        if (getProviderId() != public1.getProviderId())
            return false;
        String str1 = getProviderName();
        String str2 = public1.getProviderName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getServerId() != public1.getServerId())
            return false;
        String str3 = getServerName();
        String str4 = public1.getServerName();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getUserFullName();
        String str6 = public1.getUserFullName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getQueues();
        String str8 = public1.getQueues();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getVmtas();
        String str10 = public1.getVmtas();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (getPauseWait() != public1.getPauseWait())
            return false;
        if (getResumeWait() != public1.getResumeWait())
            return false;
        Timestamp timestamp1 = getActionStartTime();
        Timestamp timestamp2 = public1.getActionStartTime();
            return !((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof PmtaProcess;
  }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getProviderId();
        String str1 = getProviderName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getServerId();
        String str2 = getServerName();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getUserFullName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getQueues();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getVmtas();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getPauseWait();
        n = n * 59 + getResumeWait();
        Timestamp timestamp = getActionStartTime();
        return n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getQueues() {
        return queues;
    }

    public void setQueues(String queues) {
        this.queues = queues;
    }

    public String getVmtas() {
        return vmtas;
    }

    public void setVmtas(String vmtas) {
        this.vmtas = vmtas;
    }

    public int getPauseWait() {
        return pauseWait;
    }

    public void setPauseWait(int pauseWait) {
        this.pauseWait = pauseWait;
    }

    public int getResumeWait() {
        return resumeWait;
    }

    public void setResumeWait(int resumeWait) {
        this.resumeWait = resumeWait;
    }

    public Timestamp getActionStartTime() {
        return actionStartTime;
    }

    public void setActionStartTime(Timestamp actionStartTime) {
        this.actionStartTime = actionStartTime;
    }

    @Override
    public String toString() {
        return "PmtaProcess(id=" + getId() + ", providerId=" + getProviderId() + ", providerName=" + getProviderName() + ", serverId=" + getServerId() + ", serverName=" + getServerName() + ", userFullName=" + getUserFullName() + ", queues=" + getQueues() + ", vmtas=" + getVmtas() + ", pauseWait=" + getPauseWait() + ", resumeWait=" + getResumeWait() + ", actionStartTime=" + getActionStartTime() + ")";
    }
}
