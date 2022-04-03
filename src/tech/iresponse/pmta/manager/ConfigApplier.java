package tech.iresponse.pmta.manager;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Crypto;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class ConfigApplier extends Thread {

    private int serverId;
    private String path;
    private String content;

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

            ssh.uploadContent(Crypto.Base64Decode(this.content), this.path);
            ssh.shellCommand(prefix + " /etc/init.d/pmta reload");
            ssh.shellCommand(prefix + " /etc/init.d/pmta restart");

        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"serverId", "path", "content"})
    public ConfigApplier(int serverId, String path, String content) {
        this.serverId = serverId;
        this.path = path;
        this.content = content;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ConfigApplier))
            return false;
        ConfigApplier int1 = (ConfigApplier)paramObject;
        if (!int1.exists(this))
            return false;
        if (getServerId() != int1.getServerId())
            return false;
        String str1 = getPath();
        String str2 = int1.getPath();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getContent();
        String str4 = int1.getContent();
            return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ConfigApplier;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        String str1 = getPath();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getContent();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ConfigApplier(serverId=" + getServerId() + ", path=" + getPath() + ", content=" + getContent() + ")";
    }
}
