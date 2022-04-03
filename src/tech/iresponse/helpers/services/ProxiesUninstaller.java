package tech.iresponse.helpers.services;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ProxyServer;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class ProxiesUninstaller extends Thread {

    private int proxyId;

    public void run() {

        SSHConnector ssh = null;

        try {
            ProxyServer proxyServ = new ProxyServer(Integer.valueOf(this.proxyId));
            if (proxyServ.getEmpty() || proxyServ.mtaServerId == 0){
                throw new DatabaseException("Proxy server not found !");
            }

            MtaServer mtaServ = new MtaServer(Integer.valueOf(proxyServ.mtaServerId));
            ssh = Authentification.connectToServer(mtaServ);

            if (ssh != null && ssh.isConnected()) {
                String prefix = "root".equals(ssh.getUsername()) ? "" : "sudo ";
                ssh.cmd(prefix + " service 3proxy stop");
                ssh.cmd(prefix + " yum remove -y 3proxy");
                ssh.cmd(prefix + " rm -rf /etc/3proxy.cfg");
                ssh.cmd(prefix + " rm -rf /var/log/3proxy*");
            }
        } catch (Exception e) {
            Loggers.error(e);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"proxyId"})
    public ProxiesUninstaller(int proxyId) {
        this.proxyId = proxyId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ProxiesUninstaller))
            return false;
        ProxiesUninstaller if1 = (ProxiesUninstaller)paramObject;
        return !if1.exists(this) ? false : (!(getProxyId() != if1.getProxyId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ProxiesUninstaller;
    }

    @Override
    public int hashCode() {
        int n = 1;
        return n * 59 + getProxyId();
    }

    public int getProxyId() {
        return proxyId;
    }

    public void setProxyId(int proxyId) {
        this.proxyId = proxyId;
    }

    @Override
    public String toString() {
        return "ProxiesUninstaller(proxyId=" + getProxyId() + ")";
    }
}
