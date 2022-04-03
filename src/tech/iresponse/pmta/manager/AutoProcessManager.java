package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.PmtaProcess;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.remote.Authentification;

public class AutoProcessManager extends Thread {

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

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

            String[] queues = new String[0];
            String[] pmtaQueues = new String[0];

            String bashExecuteComm = "#!/bin/bash\nexecuteAutoCommands_" + pmtaProcss.id + "(){\n";

            if (pmtaProcss.queues != null && !"".equals(pmtaProcss.queues)){
                queues = pmtaProcss.queues.split(",");
            }

            if (pmtaProcss.vmtas != null && !"".equals(pmtaProcss.vmtas)) {
                List<ServerVmta> vmtas = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND id = ANY (ARRAY[" + pmtaProcss.vmtas + "])", new Object[] { Integer.valueOf(mtaServ.id) });
                if (vmtas != null && !vmtas.isEmpty()){
                    for (ServerVmta vmta : vmtas){
                        pmtaQueues = (String[])ArrayUtils.add((Object[])pmtaQueues, vmta.name);
                    }
                }
            }

            if (queues.length == 0) {
                if (pmtaQueues.length == 0) {
                    bashExecuteComm = bashExecuteComm + prefix + "pmta pause queue */*;\n";
                } else {
                    for (String strQueues : pmtaQueues){
                        bashExecuteComm = bashExecuteComm + prefix + "pmta pause queue */" + strQueues + ";\n";
                    }
                }
            } else {
                for (String strQueues : queues) {
                    if (pmtaQueues.length == 0) {
                        bashExecuteComm = bashExecuteComm + prefix + "pmta pause queue " + strQueues + "/*;\n";
                    } else {
                        for (String str3 : pmtaQueues){
                            bashExecuteComm = bashExecuteComm + prefix + "pmta pause queue " + strQueues + "/" + str3 + ";\n";
                        }
                    }
                }
            }

            bashExecuteComm = bashExecuteComm + "sleep " + pmtaProcss.pauseWait + ";\n";
            if (queues.length == 0) {
                if (pmtaQueues.length == 0) {
                    bashExecuteComm = bashExecuteComm + prefix + "pmta resume queue */*;\n";
                } else {
                    for (String strQueues : pmtaQueues){
                        bashExecuteComm = bashExecuteComm + prefix + "pmta resume queue */" + strQueues + ";\n";
                    }
                }
            } else {
                for (String strQueues : queues) {
                    if (pmtaQueues.length == 0) {
                        bashExecuteComm = bashExecuteComm + prefix + "pmta resume queue " + strQueues + "/*;\n";
                    } else {
                        for (String str3 : pmtaQueues){
                            bashExecuteComm = bashExecuteComm + prefix + "pmta resume queue " + strQueues + "/" + str3 + ";\n";
                        }
                    }
                }
            }

            bashExecuteComm = bashExecuteComm + "sleep " + pmtaProcss.resumeWait + ";\n";
            bashExecuteComm = bashExecuteComm + "executeAutoCommands_" + pmtaProcss.id + ";\n}\nexecuteAutoCommands_" + pmtaProcss.id + ";";

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.uploadContent(bashExecuteComm, "/home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("chmod +x /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("nohup sh /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".sh > /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".log 2> /home/" + mtaServ.sshUsername + "/proccess_mta_" + pmtaProcss.id + ".log &");
            } else {
                ssh.uploadContent(bashExecuteComm, "/home/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("chmod +x /home/proccess_mta_" + pmtaProcss.id + ".sh");
                ssh.cmd("nohup sh /home/proccess_mta_" + pmtaProcss.id + ".sh > /home/proccess_mta_" + pmtaProcss.id + ".log 2> /home/proccess_mta_" + pmtaProcss.id + ".log &");
            }

        } catch (Exception ex) {
            if (pmtaProcss != null && !pmtaProcss.getEmpty()){
                try {
                    pmtaProcss.delete();
                } catch (Throwable throwable) {}
            }
            Loggers.error(ex);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"processId"})
    public AutoProcessManager(int processId) {
        this.processId = processId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AutoProcessManager))
            return false;
        AutoProcessManager do1 = (AutoProcessManager)paramObject;
        return !do1.exists(this) ? false : (!(getProcessId() != do1.getProcessId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AutoProcessManager;
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
        return "AutoProcessManager(processId=" + getProcessId() + ")";
    }
}
