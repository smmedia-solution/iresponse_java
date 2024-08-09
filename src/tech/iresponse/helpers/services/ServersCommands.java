package tech.iresponse.helpers.services;

import java.beans.ConstructorProperties;
import java.util.regex.Pattern;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Servers;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class ServersCommands extends Thread {

    private int serverId;
    private String command;

    @Override
    public void run() {
        SSHConnector ssh = null;
        MtaServer mtaServ = null;
        try {
            mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            ssh = Authentification.connectToServer(mtaServ);
            String log = "";

            if (ssh != null && ssh.isConnected()) {

                String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";
                String release = String.valueOf(ssh.cmd("awk -F= '/^PRETTY_NAME=/{print $2}' /etc/os-release")).replaceAll("\n", "");
                boolean isUbuntu = release.toLowerCase().contains("ubuntu");

                switch (this.command) {
                    case "reboot-server":
                        ssh.cmd(prefix + " reboot -f");
                        log = log + "Server is about to reboot now !\n";
                        break;
                    case "refresh-ram":
                        ssh.cmd(prefix + " sync ; echo 3 > /proc/sys/vm/drop_caches");
                        log = log + "Ram and Cache are cleared !\n";
                        break;
                    case "clean-logs":
                        ssh.cmd("> /var/log/3proxy.log");
                        ssh.cmd("> /var/log/btmp");
                        ssh.cmd("> /var/log/secure");
                        if (isUbuntu){
                            ssh.cmd("> /var/log/apache2/*");
                        } else {
                            ssh.cmd("> /var/log/httpd/*");
                        }
                        ssh.cmd("rm -rf /var/log/pmta.log.*");
                        ssh.cmd("> /var/log/pmta.log");
                        ssh.cmd("rm -rf /var/spool/iresponse/tmp/*");
                        ssh.cmd("rm -rf /var/spool/iresponse/bad/*");
                        log = log + "All logs are cleared !\n";
                        break;
                    case "stop-apache":
                        if (release.toLowerCase().contains("ubuntu")){
                            log = log + ssh.cmd(prefix + " systemctl stop apache2");
                        } else {
                            log = log + ssh.cmd(prefix + " systemctl stop httpd");
                        }
                        break;
                    case "start-apache":
                        if(isUbuntu){
                            log = log + ssh.cmd(prefix + " systemctl start apache2");
                        } else {
                            log = log + ssh.cmd(prefix + " systemctl start httpd");
                        }
                        break;
                    case "restart-apache":
                        if (isUbuntu){
                            log = log + ssh.cmd(prefix + " systemctl restart apache2");
                        } else {
                            log = log + ssh.cmd(prefix + " systemctl restart httpd");
                        }
                        break;
                    case "get-info":
                        log = log + "Linux Distribution : " + release + " ";

                        log = log + "RAM : " + ssh.cmd(prefix + " free -m | grep Mem: | cut -f2 | awk '{ print $2}'").replaceAll("\n", "").replaceAll("\r", "") + " Mb \n";
                        String[] storage = ssh.cmd(prefix + " df -h | grep '/dev/' | cut -f2").split(Pattern.quote("\n"))[0].replaceAll("( )+", " ").split(" ");
                        if (storage.length >= 5) {
                            log = log + "Storage : Total => " + storage[1] + "b , Used => " + storage[2] + "b , Available => " + storage[3] + "b , Used Percentage => " + storage[4] + " \n";
                        }
                        String[] inodes = ssh.cmd(prefix + " df -ih | grep '/dev/' | cut -f2").split(Pattern.quote("\n"))[0].replaceAll("( )+", " ").split(" ");
                        if (inodes.length >= 5) {
                            log = log + "Inodes : Total => " + inodes[1] + "b , Used => " + inodes[2] + "b , Available => " + inodes[3] + "b , Used Percentage => " + inodes[4] + " \n";
                        }
                        break;
                    case "get-ips":
                        log = log + ssh.cmd(prefix + " ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/'");
                        break;
                    default:
                        throw new DatabaseException("Unsupported action !");
                }
            } else {
                throw new DatabaseException("Mta server : " + mtaServ.name + " not connected!");
            }

            Servers.updateLogs(mtaServ.name, log);

        } catch (Exception e) {
            Loggers.error(e);
            if (mtaServ != null){
                Servers.updateLogs(mtaServ.name, e.getMessage());
            }
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"serverId", "command"})
    public ServersCommands(int serverId, String paramString) {
        this.serverId = serverId;
        this.command = command;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ServersCommands))
            return false;
        ServersCommands int1 = (ServersCommands)paramObject;
        if (!int1.exists(this))
            return false;
        if (getServerId() != int1.getServerId())
            return false;
        String str1 = getCommand();
        String str2 = int1.getCommand();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ServersCommands;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        String str = getCommand();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "ServersCommands(serverId=" + getServerId() + ", command=" + getCommand() + ")";
    }
}
