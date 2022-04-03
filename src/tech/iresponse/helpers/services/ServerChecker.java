package tech.iresponse.helpers.services;

import java.beans.ConstructorProperties;
import java.util.List;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.http.Agents;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class ServerChecker extends Thread {

    private MtaServer mtaServer;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            ssh = Authentification.connectToServer(this.mtaServer);
            if (ssh != null && ssh.isConnected()) {
                int version = String.valueOf(ssh.cmd("cat /etc/*release* | grep 'centos:7'")).replaceAll("\n", "").contains("centos:7") ? 7 : 6;
                boolean contain32 = String.valueOf(ssh.cmd("file /sbin/init")).contains("32-bit");
                String systemVersion = (contain32 == true) ? "32bits" : "64bits";
                this.mtaServer.os = "CentOS " + version + " " + systemVersion;

                String response = Agents.get("https://freegeoip.live/json/" + this.mtaServer.mainIp, null, 20);
                if (response != null && response.contains("country_code")) {
                    JSONObject countryCode = new JSONObject(response);
                    this.mtaServer.countryCode = countryCode.has("country_code") ? countryCode.getString("country_code") : "US";
                } else {
                    this.mtaServer.countryCode = "US";
                }

                List<ServerVmta> listServ = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(this.mtaServer.id) });
                Process proc = null;
                if (listServ != null && !listServ.isEmpty()){
                    for (ServerVmta vmta : listServ) {
                        proc = Runtime.getRuntime().exec("ping -c 1 " + vmta.ip);
                        int i = proc.waitFor();
                        if (i == 0) {
                            vmta.pingStatus = "Connected";
                        } else {
                            vmta.pingStatus = "Timedout";
                        }
                        vmta.update();
                    }
                }
                this.mtaServer.sshConnectivityStatus = "Connected";
            } else {
                this.mtaServer.sshConnectivityStatus = "Down";
            }
            this.mtaServer.update();
        } catch (Exception exception) {
            Loggers.error(exception);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"mtaServer"})
    public ServerChecker(MtaServer mtaServer) {
        this.mtaServer = mtaServer;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ServerChecker))
            return false;
        ServerChecker for1 = (ServerChecker)paramObject;
        if (!for1.exists(this))
            return false;
        MtaServer throw1 = getMtaServer();
        MtaServer throw2 = for1.getMtaServer();
        return !((throw1 == null) ? (throw2 != null) : !throw1.equals(throw2));
    }

    protected boolean exists(Object instance) {
        return instance instanceof ServerChecker;
    }

    @Override
    public int hashCode() {
        int n = 1;
        MtaServer throw1 = getMtaServer();
        return n * 59 + ((throw1 == null) ? 43 : throw1.hashCode());
    }

    public MtaServer getMtaServer() {
        return this.mtaServer;
    }

    public void setMtaServer(MtaServer mtaServer) {
        this.mtaServer = mtaServer;
    }

    public String toString() {
        return "ServerChecker(mtaServer=" + getMtaServer() + ")";
    }
}