package tech.iresponse.helpers.smtp;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmtpAccount implements Serializable {

    private String host;
    private String port;
    private String encryption;
    private String username;
    private String password;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpAccount))
            return false;
        SmtpAccount do1 = (SmtpAccount)paramObject;
        if (!do1.exists(this))
            return false;
        String str1 = getHost();
        String str2 = do1.getHost();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getPort();
        String str4 = do1.getPort();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getEncryption();
        String str6 = do1.getEncryption();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getUsername();
        String str8 = do1.getUsername();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getPassword();
        String str10 = do1.getPassword();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getProxyHost();
        String str12 = do1.getProxyHost();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getProxyPort();
        String str14 = do1.getProxyPort();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProxyUsername();
        String str16 = do1.getProxyUsername();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getProxyPassword();
        String str18 = do1.getProxyPassword();
        return !((str17 == null) ? (str18 != null) : !str17.equals(str18));
    }

    protected boolean exists(Object instance) {
        return instance instanceof SmtpAccount;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getHost();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getPort();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getEncryption();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getUsername();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getPassword();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getProxyHost();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getProxyPort();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getProxyUsername();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getProxyPassword();
        return n * 59 + ((str9 == null) ? 43 : str9.hashCode());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
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

    @Override
    public String toString() {
        return "SmtpAccount(host=" + getHost() + ", port=" + getPort() + ", encryption=" + getEncryption() + ", username=" + getUsername() + ", password=" + getPassword() + ", proxyHost=" + getProxyHost() + ", proxyPort=" + getProxyPort() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ")";
    }
}