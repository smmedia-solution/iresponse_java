package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.PmtaProcess;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class AutoProcessStopper extends Thread {

    private int processId;

    @Override
    public void run() {
        PmtaProcess pmtaProcss = null;
        SSHConnector ssh = null;
        MtaServer mtaServ = null;

        try {
            pmtaProcss = new PmtaProcess(Integer.valueOf(this.processId));
            if (pmtaProcss.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            mtaServ = new MtaServer(Integer.valueOf(pmtaProcss.serverId));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("No mta server found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            //String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.cmd("kill -9 $(ps aux | grep 'proccess_mta_" + pmtaProcss.id + ".sh' | grep -v 'grep' | awk '{print $2}')");
                ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".log");
            } else {
                ssh.cmd("kill -9 $(ps aux | grep 'proccess_mta_" + pmtaProcss.id + ".sh' | grep -v 'grep' | awk '{print $2}')");
                ssh.cmd("rm -rf /home/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("rm -rf /home/proccess_mta_" + pmtaProcss.id + ".log");
            }

            pmtaProcss.delete();

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"processId"})
    public AutoProcessStopper(int processId) {
        this.processId = processId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AutoProcessStopper))
            return false;
        AutoProcessStopper if1 = (AutoProcessStopper)paramObject;
        return !if1.exists(this) ? false : (!(getProcessId() != if1.getProcessId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AutoProcessStopper;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getProcessId();
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    @Override
    public String toString() {
        return "AutoProcessStopper(processId=" + getProcessId() + ")";
    }
}
