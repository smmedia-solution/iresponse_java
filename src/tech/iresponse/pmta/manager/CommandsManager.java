package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import java.sql.Timestamp;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Pmta;
import tech.iresponse.models.admin.User;
import tech.iresponse.models.admin.PmtaHistory;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class CommandsManager extends Thread {

    private JSONObject bundle;

    @Override
    public void run() {

        SSHConnector ssh = null;
        MtaServer mtaServ = null;
        PmtaHistory pmtaHistry = null;

        try {
            mtaServ = new MtaServer(Integer.valueOf(this.bundle.getInt("server")));
            if (mtaServ.getEmpty()){
                throw new DatabaseException("Server not found !");
            }

            ssh = Authentification.connectToServer(mtaServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to this server !");
            }

            String bashExecuteComm = "";
            User usr = Application.checkAndgetInstance().getUser();
            String target = this.bundle.getString("target");
            String command = this.bundle.getString("command");
            JSONArray vmtas = (this.bundle.has("vmtas") && this.bundle.get("vmtas") instanceof JSONArray) ? this.bundle.getJSONArray("vmtas") : new JSONArray();
            JSONArray queues = (this.bundle.has("queues") && this.bundle.get("queues") instanceof JSONArray) ? this.bundle.getJSONArray("queues") : new JSONArray();
            int scheduleTimes = TypesParser.safeParseInt(this.bundle.get("schedule-times"));
            int schedulePeriod = TypesParser.safeParseInt(this.bundle.get("schedule-period"));

            String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

            pmtaHistry = new PmtaHistory();
            pmtaHistry.serverId = mtaServ.id;
            pmtaHistry.userId = usr.id;
            pmtaHistry.target = target;
            pmtaHistry.action = command;
            pmtaHistry.actionTime = new Timestamp(System.currentTimeMillis());

            String[] arrayVmtas = new String[0];
            String[] ispsQueue = new String[0];
            boolean found = false;

            switch (command) {
                case "reload":
                    bashExecuteComm = bashExecuteComm + prefix + " /etc/init.d/pmta reload;";
                    found = true;
                    break;
                case "reset":
                    bashExecuteComm = bashExecuteComm + prefix + " pmta reset counters;";
                    found = true;
                    break;
                case "stop":
                    bashExecuteComm = bashExecuteComm + prefix + " /etc/init.d/pmta stop;";
                    found = true;
                    break;
                case "restart":
                    bashExecuteComm = bashExecuteComm + prefix + " /etc/init.d/pmta restart;";
                    found = true;
                    break;
                case "clean":
                    switch (target) {
                        case "my-jobs":
                            bashExecuteComm = bashExecuteComm + prefix + " pmta delete --jobId=md_" + usr.productionId + ";";
                            bashExecuteComm = bashExecuteComm + prefix + " pmta delete --jobId=mt_" + usr.productionId;
                            break;
                        case "my-drops":
                            bashExecuteComm = bashExecuteComm + prefix + " pmta delete --jobId=md_" + usr.productionId;
                            break;
                        case "my-tests":
                            bashExecuteComm = bashExecuteComm + prefix + " pmta delete --jobId=mt_" + usr.productionId;
                            break;
                    }
                    bashExecuteComm = bashExecuteComm + prefix + " pmta delete";
                    break;
                case "resume":
                    bashExecuteComm = bashExecuteComm + prefix + " pmta resume queue";
                    break;
                case "pause":
                    bashExecuteComm = bashExecuteComm + prefix + " pmta pause queue";
                    break;
                case "schedule":
                    bashExecuteComm = bashExecuteComm + prefix + " pmta schedule";
                    break;
            }

            if (!found) {
                String cleanQueue = "clean".equalsIgnoreCase(command) ? "--queue=" : "";
                String[] commands = bashExecuteComm.split(Pattern.quote(";"));
                bashExecuteComm = "";

                if (commands.length > 0){
                    for (String cmd : commands) {
                        String finalCmd = "";

                        if (queues.length() == 0) {
                            if (vmtas.length() == 0) {
                                finalCmd = finalCmd + cmd + " " + cleanQueue + "*/*;";
                            } else {
                                for (int b = 0; b < vmtas.length(); b++) {
                                    String[] arrayOfString3 = vmtas.getString(b).split(Pattern.quote("|"));
                                    if (arrayOfString3 != null && arrayOfString3.length > 0) {
                                        arrayVmtas = (String[])ArrayUtils.add((Object[])arrayVmtas, arrayOfString3[2].toLowerCase());
                                        finalCmd = finalCmd + cmd + " " + cleanQueue + "*/" + arrayOfString3[2].toLowerCase() + ";";
                                    }
                                }
                            }
                        } else {
                            for (int b = 0; b < queues.length(); b++) {
                                String str7 = queues.getString(b).toLowerCase();
                                ispsQueue = (String[])ArrayUtils.add((Object[])ispsQueue, str7);

                                if (vmtas.length() == 0) {
                                    finalCmd = finalCmd + cmd + " " + cleanQueue + str7 + "/*;";
                                } else {
                                    for (int b1 = 0; b1 < vmtas.length(); b1++) {
                                        String[] arrayOfString3 = vmtas.getString(b1).split(Pattern.quote("|"));
                                        if (arrayOfString3 != null && arrayOfString3.length > 0) {
                                            arrayVmtas = (String[])ArrayUtils.add((Object[])arrayVmtas, arrayOfString3[2].toLowerCase());
                                            finalCmd = finalCmd + cmd + " " + cleanQueue + str7 + "/" + arrayOfString3[2].toLowerCase() + ";";
                                        }
                                    }
                                }
                            }
                        }

                        if (!"".equals(finalCmd)){
                            bashExecuteComm = bashExecuteComm + finalCmd;
                        }
                    }
                }
            }

            if (!Strings.isEmpty(bashExecuteComm)) {
                pmtaHistry.isps = String.join(",", (CharSequence[])ispsQueue);
                pmtaHistry.vmtas = String.join(",", (CharSequence[])arrayVmtas);
                String result = "";

                if ("schedule".equals(command)) {
                    for (int b = 0; b < scheduleTimes; b++) {
                        result = ssh.cmd(bashExecuteComm);
                        ThreadSleep.sleep(schedulePeriod);
                    }
                } else {
                    result = ssh.cmd(bashExecuteComm);
                }

                pmtaHistry.results = result;
                pmtaHistry.insert();
                Pmta.updateLogs(mtaServ.name, result);
            }

        } catch (Exception ex) {
            Loggers.error(ex);
            if (mtaServ != null){
                Pmta.updateLogs(mtaServ.name, ex.getMessage());
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"bundle"})
    public CommandsManager(JSONObject bundle) {
        this.bundle = bundle;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof CommandsManager))
            return false;
        CommandsManager for1 = (CommandsManager)paramObject;
        if (!for1.exists(this))
            return false;
        JSONObject jSONObject1 = getBundle();
        JSONObject jSONObject2 = for1.getBundle();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof CommandsManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        JSONObject jSONObject = getBundle();
        return n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
    }

    public JSONObject getBundle() {
        return bundle;
    }

    public void setBundle(JSONObject bundle) {
        this.bundle = bundle;
    }

    @Override
    public String toString() {
        return "CommandsManager(bundle=" + getBundle() + ")";
    }
}
