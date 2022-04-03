package tech.iresponse.remote;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.MtaServer;

public class Authentification {

    public static SSHConnector connectToServer(MtaServer mtaServ) {
        if (mtaServ != null && mtaServ.id > 0 && !"".equals(mtaServ.sshLoginType)) {
            SSHConnector ssh = new SSHConnector();
            ssh.setHost(mtaServ.mainIp);
            ssh.setPort(String.valueOf(mtaServ.sshPort));
            ssh.setUsername(mtaServ.sshUsername);

            switch (mtaServ.sshLoginType) {
                case "user-pass":
                    ssh.connectWithPass(mtaServ.sshPassword);
                    break;
                case "pem":
                    ssh.connectWithPem(mtaServ.sshPemContent, mtaServ.sshPassphrase);
                    break;
                case "rsa":
                    ssh.connectWithRsa();
                    break;
            }
            return ssh;
        }
        return null;
    }

    public static SSHConnector connectToServer(ManagementServer mngServ) {
        if (mngServ != null && mngServ.id > 0 && !"".equals(mngServ.sshLoginType)) {
            SSHConnector ssh = new SSHConnector();
            ssh.setHost(mngServ.mainIp);
            ssh.setPort(String.valueOf(mngServ.sshPort));
            ssh.setUsername(mngServ.sshUsername);
            switch (mngServ.sshLoginType) {
                case "user-pass":
                    ssh.connectWithPass(mngServ.sshPassword);
                    break;
                case "pem":
                    ssh.connectWithPem(mngServ.sshPemContent, mngServ.sshPassphrase);
                    break;
                case "rsa":
                    ssh.connectWithRsa();
                    break;
            }
            return ssh;
        }
        return null;
    }

    public static SSHConnector connectToServer(String ip, int port, String username, String password) {
        if (!"".equals(ip) && port > 0 && !"".equals(username) && !"".equals(password)) {
            SSHConnector ssh = new SSHConnector();
            ssh.setHost(ip);
            ssh.setPort(String.valueOf(port));
            ssh.setUsername(username);
            ssh.connectWithPass(password);
            return ssh;
        }
        return null;
    }

    public static SSHConnector connectToServer(String ip, int port, String username, String pem, String passphrase) {
        if (!"".equals(ip) && port > 0 && !"".equals(username) && !"".equals(pem)) {
            SSHConnector ssh = new SSHConnector();
            ssh.setHost(ip);
            ssh.setPort(String.valueOf(port));
            ssh.setUsername(username);
            ssh.connectWithPem(pem, passphrase);
            return ssh;
        }
        return null;
    }

    public static SSHConnector connectToServer(String ip, int port, String username) {
        if (!"".equals(ip) && port > 0 && !"".equals(username)) {
            SSHConnector ssh = new SSHConnector();
            ssh.setHost(ip);
            ssh.setPort(String.valueOf(port));
            ssh.setUsername(username);
            ssh.connectWithRsa();
            return ssh;
        }
        return null;
    }

    public static String getInfo(String type, String ip) {
        try {
            switch (type) {
                case "UNwh8g1MYtB4Ihn9qeAX/TTZhbUGiNQC8R4VSDKLPaMYfbvgABN4D9rk3GhiRMqG":
                    Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                    while (enumeration.hasMoreElements()) {
                        NetworkInterface networkInterface1 = enumeration.nextElement();
                        Enumeration<InetAddress> enumeration1 = networkInterface1.getInetAddresses();
                        while (enumeration1.hasMoreElements()) {
                            InetAddress inetAddress = enumeration1.nextElement();
                            if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address){
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                    break;
                case "P+yQQwNbXFdU3IxUfJNOD4DEpnvEDp6xz/TFZOepcs8eAtbeUUxhPAQaFzq4Kq9i":
                    NetworkInterface networkInterface = (ip == null || "".equals(ip)) ? NetworkInterface.getByInetAddress(InetAddress.getByName(getInfo("UNwh8g1MYtB4Ihn9qeAX/TTZhbUGiNQC8R4VSDKLPaMYfbvgABN4D9rk3GhiRMqG", null))) : NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
                    byte[] adressMac = networkInterface.getHardwareAddress();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < adressMac.length; i++) {
                        sb.append(String.format("%02X%s", adressMac[i], (i < adressMac.length - 1) ? ":" : "" ));
                    }
                    return sb.toString();
            }
        } catch (SocketException|java.net.UnknownHostException socketException) {}
        return null;
    }
}
