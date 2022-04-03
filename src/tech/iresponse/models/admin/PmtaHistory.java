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
public class PmtaHistory extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "server_id", type = "integer", nullable = false)
    public int serverId;

    @Column(name = "user_id", type = "integer", nullable = false)
    public int userId;

    @Column(name = "action", type = "text", nullable = false, length = 50)
    public String action;

    @Column(name = "target", type = "text", nullable = false, length = 50)
    public String target;

    @Column(name = "isps", type = "text", nullable = true)
    public String isps;

    @Column(name = "vmtas", type = "text", nullable = true)
    public String vmtas;

    @Column(name = "results", type = "text", nullable = true)
    public String results;

    @Column(name = "action_time", type = "timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp actionTime;

    public PmtaHistory() throws Exception {
        setDatabase("system");
        setSchema("admin");
        setTable("pmta_commands_history");
    }

    public PmtaHistory(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("system");
        setSchema("admin");
        setTable("pmta_commands_history");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof PmtaHistory))
            return false;
        PmtaHistory native1 = (PmtaHistory)paramObject;
        if (!native1.exists(this))
            return false;
        if (getId() != native1.getId())
            return false;
        if (getServerId() != native1.getServerId())
            return false;
        if (getUserId() != native1.getUserId())
            return false;
        String str1 = getAction();
        String str2 = native1.getAction();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getTarget();
        String str4 = native1.getTarget();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getIsps();
        String str6 = native1.getIsps();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getVmtas();
        String str8 = native1.getVmtas();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getResults();
        String str10 = native1.getResults();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        Timestamp timestamp1 = getActionTime();
        Timestamp timestamp2 = native1.getActionTime();
            return !((timestamp1 == null) ? (timestamp2 != null) : !timestamp1.equals(timestamp2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof PmtaHistory;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        n = n * 59 + getServerId();
        n = n * 59 + getUserId();
        String str1 = getAction();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getTarget();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getIsps();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getVmtas();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getResults();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        Timestamp timestamp = getActionTime();
        return n * 59 + ((timestamp == null) ? 43 : timestamp.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIsps() {
        return isps;
    }

    public void setIsps(String isps) {
        this.isps = isps;
    }

    public String getVmtas() {
        return vmtas;
    }

    public void setVmtas(String vmtas) {
        this.vmtas = vmtas;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public Timestamp getActionTime() {
        return actionTime;
    }

    public void setActionTime(Timestamp actionTime) {
        this.actionTime = actionTime;
    }

    @Override
    public String toString() {
        return "PmtaHistory(id=" + getId() + ", serverId=" + getServerId() + ", userId=" + getUserId() + ", action=" + getAction() + ", target=" + getTarget() + ", isps=" + getIsps() + ", vmtas=" + getVmtas() + ", results=" + getResults() + ", actionTime=" + getActionTime() + ")";
    }
}
