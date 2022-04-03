package tech.iresponse.helpers.services;

import java.beans.ConstructorProperties;
import java.io.File;
import java.sql.Date;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.models.admin.ProxyServer;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.Authentification;
import tech.iresponse.core.Application;

public class ProxiesInstaller extends Thread {

    private int serverId;
    private String proxyUsername;
    private String proxyPassword;
    private String httpProxyPort;
    private String socksProxyPort;

    @Override
    public void run() {

        SSHConnector ssh = null;
        boolean newProxie = false;
        try {

            MtaServer mtaServ = new MtaServer(Integer.valueOf(this.serverId));
            ssh = Authentification.connectToServer(mtaServ);

            ProxyServer proxyServ = (ProxyServer)ProxyServer.first(ProxyServer.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });
            if (proxyServ == null || proxyServ.getEmpty()) {
                proxyServ = new ProxyServer();
                newProxie = true;
            }

            if (ssh != null && ssh.isConnected()) {
                int ipsCount = 0;
                String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo ";
                boolean centosRel7 = String.valueOf(ssh.cmd("cat /etc/centos-release")).toLowerCase().contains("centos linux release 7");
                String[] ips = new String[0];

                if (centosRel7) {
                    ips = ssh.cmd(prefix + " ip addr show | grep 'inet ' | grep -v '127.0.0.1' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/'").split(Pattern.quote("\n"));
                } else {
                    ips = ssh.cmd(prefix + " ifconfig  | grep 'inet ad'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}'").split(Pattern.quote("\n"));
                }

                if (ips.length > 0) {
                    ssh.cmd(prefix + " service 3proxy stop");
                    ssh.cmd(prefix + " yum remove -y 3proxy");
                    ssh.cmd(prefix + " rm -rf /etc/3proxy.cfg");
                    ssh.cmd(prefix + " rm -rf /var/log/3proxy*");
                    if (centosRel7) {
                        ssh.cmd(prefix + " rpm -ivh http://download-ib01.fedoraproject.org/pub/epel/6/x86_64/Packages/3/3proxy-0.6.1-10.el6.x86_64.rpm");
                    } else {
                        ssh.cmd(prefix + " rpm -ivh http://download-ib01.fedoraproject.org/pub/epel/7/x86_64/Packages/3/3proxy-sysvinit-0.7-1.el7.x86_64.rpm");
                    }
                    ssh.cmd(prefix + " yum install -y 3proxy");
                    String proxyPath = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/3proxy.tpl"), "utf-8");
                    String socks_proxy = "";
                    String http_proxy = "";

                    for (String ip : ips) {
                        http_proxy = http_proxy + "proxy -n -p" + this.httpProxyPort + " -a -i" + ip + " -e" + ip + "\n";
                        socks_proxy = socks_proxy + "socks -n -p" + this.socksProxyPort + " -a -i" + ip + " -e" + ip + "\n";
                        ipsCount++;
                    }

                    proxyPath = StringUtils.replace(proxyPath, "$p_http_proxies", http_proxy);
                    proxyPath = StringUtils.replace(proxyPath, "$p_socks_proxies", socks_proxy);

                    if (this.proxyUsername != null && !"".equals(this.proxyUsername) && this.proxyPassword != null && !"".equals(this.proxyPassword)) {
                        proxyPath = StringUtils.replace(proxyPath, "$p_auth", "auth strong\nusers " + this.proxyUsername + ":CL:" + this.proxyPassword + "\n");
                    } else {
                        proxyPath = StringUtils.replace(proxyPath, "$p_auth", "auth none");
                    }

                    ssh.uploadContent(proxyPath, "/etc/3proxy.cfg");
                    ssh.cmd("service 3proxy start");
                    ssh.cmd("chkconfig 3proxy on");

                    if (newProxie) {
                        proxyServ.mtaServerId = mtaServ.id;
                        proxyServ.name = mtaServ.name;
                        proxyServ.socksPort = TypesParser.safeParseInt(this.socksProxyPort);
                        proxyServ.httpPort = TypesParser.safeParseInt(this.httpProxyPort);
                        proxyServ.proxyUsername = this.proxyUsername;
                        proxyServ.proxyPassword = this.proxyPassword;
                        proxyServ.ipsCount = ipsCount;
                        proxyServ.status = "Activated";
                        proxyServ.createdBy = (Application.checkAndgetInstance().getUser()).email;
                        proxyServ.createdDate = new Date(System.currentTimeMillis());
                        proxyServ.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        proxyServ.lastUpdatedDate = new Date(System.currentTimeMillis());
                        proxyServ.insert();
                    } else {
                        proxyServ.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        proxyServ.lastUpdatedDate = new Date(System.currentTimeMillis());
                        proxyServ.ipsCount = ipsCount;
                        proxyServ.update();
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

    @ConstructorProperties({"serverId", "proxyUsername", "proxyPassword", "httpProxyPort", "socksProxyPort"})
    public ProxiesInstaller(int serverId, String proxyUsername, String proxyPassword, String httpProxyPort, String socksProxyPort) {
        this.serverId = serverId;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.httpProxyPort = httpProxyPort;
        this.socksProxyPort = socksProxyPort;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ProxiesInstaller))
            return false;
        ProxiesInstaller do1 = (ProxiesInstaller)paramObject;
        if (!do1.exists(this))
            return false;
        if (getServerId() != do1.getServerId())
            return false;
        String str1 = getProxyUsername();
        String str2 = do1.getProxyUsername();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProxyPassword();
        String str4 = do1.getProxyPassword();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getHttpProxyPort();
        String str6 = do1.getHttpProxyPort();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getSocksProxyPort();
        String str8 = do1.getSocksProxyPort();
        return !((str7 == null) ? (str8 != null) : !str7.equals(str8));
    }

    protected boolean exists(Object instance) {
        return instance instanceof ProxiesInstaller;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getServerId();
        String str1 = getProxyUsername();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getProxyPassword();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getHttpProxyPort();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getSocksProxyPort();
        return n * 59 + ((str4 == null) ? 43 : str4.hashCode());
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(String httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public String getSocksProxyPort() {
        return socksProxyPort;
    }

    public void setSocksProxyPort(String socksProxyPort) {
        this.socksProxyPort = socksProxyPort;
    }

    @Override
    public String toString() {
        return "ProxiesInstaller(serverId=" + getServerId() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", httpProxyPort=" + getHttpProxyPort() + ", socksProxyPort=" + getSocksProxyPort() + ")";
    }
}