package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.util.List;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class SMTPVmtaReseter extends Thread {

    private int serverId;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {

            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "SMTP" });
            if (vmtas.isEmpty()){
                throw new DatabaseException("Custom vmtas not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String str = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

            for (ServerVmta vmta : vmtas) {
                ssh.cmd(str + " rm -rf /etc/pmta/vmtas/" + vmta.name + ".conf");
                vmta.delete();
            }

            ssh.cmd(str + " /etc/init.d/pmta reload");
            ssh.cmd(str + " /etc/init.d/pmta restart");

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"serverId"})
    public SMTPVmtaReseter(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SMTPVmtaReseter))
            return false;
        SMTPVmtaReseter char1 = (SMTPVmtaReseter)paramObject;
        return !char1.exists(this) ? false : (!(getServerId() != char1.getServerId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SMTPVmtaReseter;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getServerId();
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "SMTPVmtaReseter(serverId=" + getServerId() + ")";
    }
}
