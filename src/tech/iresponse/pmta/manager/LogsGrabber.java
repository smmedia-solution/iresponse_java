package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import org.json.JSONArray;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Pmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class LogsGrabber extends Thread {

    private int serverId;
    private JSONArray processesIds;
    private String processesType;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
            String[] files = String.valueOf(ssh.cmd(prefix + " find /etc/pmta/bounces/backup/ -name *.csv")).split("\n");
            HashMap<Object, Object> log = null;

            for (String file : files) {
                String[] content = String.valueOf(ssh.cmd(prefix + " cat " + file)).split("\n");
                for (String line : content) {
                    if (line != null && !line.startsWith("type,bounceCat,")) {
                        String[] arrayOfString2 = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                        if (arrayOfString2.length == 6 && String.valueOf(arrayOfString2[3]).startsWith(this.processesType + "_")) {
                            boolean found = false;
                            for (int b = 0; b < this.processesIds.length(); b++) {
                                if (String.valueOf(arrayOfString2[4]).startsWith(this.processesIds.getString(b) + "_")) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                log = new HashMap<>();
                                log.put("server", mtaServ.name);
                                log.put("category", arrayOfString2[1].replaceAll("\n", "").replaceAll("\r", ""));
                                log.put("vmta", arrayOfString2[2].replaceAll("\n", "").replaceAll("\r", ""));
                                log.put("process_id", Integer.valueOf(TypesParser.safeParseInt(String.valueOf(arrayOfString2[4]).split("_")[0])));
                                log.put("message", arrayOfString2[5].replaceAll("\n", "").replaceAll("\r", ""));
                                Pmta.updatePmtaLogs(log);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"serverId", "processesIds", "processesType"})
    public LogsGrabber(int serverId, JSONArray processesIds, String processesType) {
        this.serverId = serverId;
        this.processesIds = processesIds;
        this.processesType = processesType;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof LogsGrabber))
            return false;
        LogsGrabber byte1 = (LogsGrabber)paramObject;
        if (!byte1.exists(this))
            return false;
        if (getServerId() != byte1.getServerId())
            return false;
        JSONArray jSONArray1 = getProcessesIds();
        JSONArray jSONArray2 = byte1.getProcessesIds();
        if ((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2))
            return false;
        String str1 = getProcessesType();
        String str2 = byte1.getProcessesType();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof LogsGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        JSONArray jSONArray = getProcessesIds();
        n = n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
        String str = getProcessesType();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public JSONArray getProcessesIds() {
        return processesIds;
    }

    public void setProcessesIds(JSONArray processesIds) {
        this.processesIds = processesIds;
    }

    public String getProcessesType() {
        return processesType;
    }

    public void setProcessesType(String processesType) {
        this.processesType = processesType;
    }

    @Override
    public String toString() {
        return "LogsGrabber(serverId=" + getServerId() + ", processesIds=" + getProcessesIds() + ", processesType=" + getProcessesType() + ")";
    }
}
