package tech.iresponse.remote;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;

public class SSHConnector {

    private JSch jsch;
    private Session session;
    private ChannelShell shellExecutor;
    private String host;
    private String port;
    private String username;
    private String password;
    private String pem;
    private String passphrase;
    private RemoteEnumeration loginType;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private final int shellTimeout = 60000;
    private final int sftpTimeout = 120000;
    private final int connectionTimeout = 60000;
    private int tries = 0;
    private final int max = 5;
    public static final String key = "wZNToQCGZT/DMWbADt9mwx8fPWbeWOq/uVuV68QR5j+VdOzwpTwEVdSRNH2X74pqCQNUDGVWIdCvzqBjooKPzA==";

    public String read(String remote) {
        try {
            if (!isConnected()){
                throw new DatabaseException("Connection lost to this server !");
            }
            ChannelSftp sftp = (ChannelSftp)connect().openChannel("sftp");
            try {
                getClass();
                sftp.connect(120000);
                StringBuilder sb = new StringBuilder();
                try(InputStream input = sftp.get(remote); BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
                    String line;
                    while ((line = br.readLine()) != null){
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            } finally {
                sftp.disconnect();
            }
        } catch (JSchException|IOException|DatabaseException|com.jcraft.jsch.SftpException e) {
            Loggers.error((Throwable)e);
            return null;
        }
    }

    public boolean download(String remote, String local) {
        boolean downloaded = false;
        try {
            if (!isConnected()){
                throw new DatabaseException("Connection lost to this server !");
            }
            ChannelSftp sftp = (ChannelSftp)connect().openChannel("sftp");
            try {
                getClass();
                sftp.connect(120000);
                try (FileOutputStream output = new FileOutputStream(local)) {
                    sftp.get(remote, output);
                    downloaded = true;
                }
            } finally {
                sftp.disconnect();
            }
        } catch (JSchException|com.jcraft.jsch.SftpException|IOException|DatabaseException e) {
            Loggers.error((Throwable)e);
        }
        return downloaded;
    }

    public boolean upload(String localPath, String remotePath) {
        boolean upload = false;
        if (isConnected()) {
            ChannelSftp sftp = null;
            FileInputStream input = null;
            try {
                sftp = (ChannelSftp)connect().openChannel("sftp");
                getClass();
                sftp.connect(120000);
                if (sftp.isConnected()) {
                    input = new FileInputStream(localPath);
                    sftp.put(input, remotePath);
                    upload = true;
                }
            } catch (JSchException|com.jcraft.jsch.SftpException|java.io.FileNotFoundException jSchException) {
                Loggers.error((Throwable)jSchException);
            } finally {
                if (sftp != null && sftp.isConnected()) {
                    try {
                        if (input != null){
                            input.close();
                        }
                    } catch (IOException e) {
                        Loggers.error(e);
                    }
                    sftp.disconnect();
                }
            }
        }
        return upload;
    }

    public boolean uploadContent(String content, String remotePath) {
        boolean bool = false;
        if (isConnected() && content != null) {
            ChannelSftp channelSftp = null;
            ByteArrayInputStream byteArrayInputStream = null;
            try {
                channelSftp = (ChannelSftp)connect().openChannel("sftp");
                getClass();
                channelSftp.connect(120000);
                if (channelSftp.isConnected()) {
                    byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
                    channelSftp.put(byteArrayInputStream, remotePath);
                    bool = true;
                }
            } catch (JSchException|com.jcraft.jsch.SftpException e) {
                Loggers.error((Throwable)e);
            } finally {
                if (channelSftp != null && channelSftp.isConnected()) {
                    try {
                        if (byteArrayInputStream != null)
                            byteArrayInputStream.close();
                    } catch (IOException io) {
                        Loggers.error(io);
                    }
                    channelSftp.disconnect();
                }
            }
        }
        return bool;
    }

    public static boolean h3llspy() {
        try {
            File file = new File((new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getAbsolutePath() + Convertion.decrypt("WAJ5sTwdb6Q1tkjgjOuNWS2L5SJWd5+OGnuRcwQmYF2AVcXE3aXjNxFSWweuSY7c"));
            if (file.exists()) {
                String str = FileUtils.readFileToString(file, "UTF-8").trim().replaceAll("\n", "").replaceAll("\r", "");
                if (str.length() > 0) {
                    String str1 = Authentification.getInfo(Crypto.Base64Decode("VU53aDhnMU1ZdEI0SWhuOXFlQVgvVFRaaGJVR2lOUUM4UjRWU0RLTFBhTVlmYnZnQUJONEQ5cmszR2hpUk1xRw=="), null).toLowerCase();
                    str1 = str1 + '|' + Authentification.getInfo(Crypto.Base64Decode("UCt5UVF3TmJYRmRVM0l4VWZKTk9ENERFcG52RURwNnh6L1RGWk9lcGNzOGVBdGJlVVV4aFBBUWFGenE0S3E5aQ=="), str1).toLowerCase();
                    str1 = str1 + '|' + Terminal.executeCommand(Convertion.decrypt("qjnjP//FpUstbkdhPtQfVwZ9mWS/Gl8FNwrVLCM4FqYK1FNfO51vaRn3Y+/WDeaeeg08sBDrEmpQaZRvIzQ82hMLWFeZdD47wULYPyQ2q4M=") + Convertion.decrypt("uId2ii4igAgdOoiApk81QB3BSjOZOKSeNzpn3YclnpI1Kb8cXrLoI3sMe//OsJZPeFnwp5fyylkja3LQBcICDgnAeNv2s3vAfqDwcTETMWA=") + Convertion.decrypt("wZNToQCGZT/DMWbADt9mwx8fPWbeWOq/uVuV68QR5j+VdOzwpTwEVdSRNH2X74pqCQNUDGVWIdCvzqBjooKPzA==")).replaceAll("\n", "").replaceAll("\r", "").trim().toLowerCase();

                    /*str = Crypto.BoucleBase64Decode(str, TypesParser.safeMatcher("kdh2gfkjh"));
                    str = Convertion.boucleDecrypt(str, TypesParser.safeMatcher("sjg2skd"));
                    str = Crypto.BoucleBase64Decode(str, TypesParser.safeMatcher("lls1kdjd"));
                    str = Convertion.boucleDecrypt(str, TypesParser.safeMatcher("kdsd1kllkjhl"));*/

                    str = Crypto.BoucleBase64Decode(str, TypesParser.safeMatcher("kdhgf4kjh"));
                    str = Convertion.boucleDecrypt(str, TypesParser.safeMatcher("sjg8skd"));
                    str = Crypto.BoucleBase64Decode(str, TypesParser.safeMatcher("llskdjd6"));
                    str = Convertion.boucleDecrypt(str, TypesParser.safeMatcher("k1dsdkllkjh0"));

                    return str.trim().toLowerCase().equalsIgnoreCase(str1.trim().toLowerCase());
                }
            }
        } catch (Exception exception) {}
        return false;
    }

    public boolean heads(String paramString) {
        String str = String.valueOf(cmd("head " + paramString));
        return (!str.contains("head:") && !str.contains(paramString));
    }

    public boolean bb(String paramString) {
        return String.valueOf(cmd("[[ -d " + paramString + " ]] && echo \"exists\"")).contains("exists");
    }

    public String cmd(String command) {
        String output = "";
        if (isConnected()) {
            ChannelExec executor = null;
            try {
                executor = (ChannelExec)connect().openChannel("exec");
                if (executor != null) {
                    executor.setPty(true);
                    executor.setCommand(command);
                    ByteArrayOutputStream osOut = new ByteArrayOutputStream();
                    ByteArrayOutputStream osErr = new ByteArrayOutputStream();
                    try {
                        executor.setInputStream(null);
                        executor.setOutputStream(osOut);
                        executor.setErrStream(osErr);
                        getClass();
                        executor.connect(60000);
                        while(!executor.isClosed() && executor.getExitStatus() != 0){
                            ThreadSleep.sleep(1000L);
                        }
                    } finally {
                        String out = new String(osOut.toByteArray());
                        if (!StringUtils.isEmpty(out)){
                            output = output + out;
                        }
                        String err = new String(osErr.toByteArray());
                        if (!StringUtils.isEmpty(err))
                            output = output + err;
                    }
                }
            } catch (JSchException jSchException) {
                Loggers.error((Throwable)jSchException);
            } finally {
                if (executor != null && executor.isConnected()){
                    executor.disconnect();
                }
            }
        }
        return output;
    }

    public String cmd2(String command) {
        String output = "";
        if (isConnected()) {
            ChannelExec executor = null;
            try {
                executor = (ChannelExec)connect().openChannel("exec");
                if (executor != null) {
                    executor.setPty(false);
                    executor.setCommand(command);
                    ByteArrayOutputStream osOut = new ByteArrayOutputStream();
                    ByteArrayOutputStream osErr = new ByteArrayOutputStream();
                    try {
                        executor.setInputStream(null);
                        executor.setOutputStream(osOut);
                        executor.setErrStream(osErr);
                        getClass();
                        executor.connect(60000);
                        while(!executor.isClosed() && executor.getExitStatus() != 0){
                            ThreadSleep.sleep(1000L);
                        }
                    } finally {
                        String out = new String(osOut.toByteArray());
                        if (!StringUtils.isEmpty(out)){
                            output = output + out;
                        }
                        String err = new String(osErr.toByteArray());
                        if (!StringUtils.isEmpty(err))
                            output = output + err;
                    }
                }
            } catch (JSchException jSchException) {
                Loggers.error((Throwable)jSchException);
            } finally {
                if (executor != null && executor.isConnected()){
                    executor.disconnect();
                }
            }
        }
        return output;
    }

    public void changePasswordServer(String username, String password) {
        String str = "echo \"" + password + "\" | passwd --stdin root";
        uploadContent(str, "/home/" + username + "/passwd.sh");
        cmd("chmod +x /home/" + username + "/passwd.sh");
        runShellCommand(new String[] { "export HISTIGNORE='*sudo -S*'", "echo \"" + password + "\" | sudo -S -k sh /home/" + username + "/passwd.sh" });
        cmd("rm -rf /home/" + username + "/passwd.sh");
    }

    public void runShellCommand(String[] command) {
        if (command != null && command.length > 0){
            for (String cmd : command){
                shellCommand(cmd);
            }
        }
    }

    public synchronized void shellCommand(String command) {
        if (isConnected() && command != null) {
            try {
                if (this.shellExecutor == null || this.shellExecutor.isClosed()){
                    this.shellExecutor =(ChannelShell) connect().openChannel("shell");
                    this.shellExecutor.setPty(true);
                    getClass();
                    this.shellExecutor.connect(60000);
                }
                PrintStream printStream = new PrintStream(this.shellExecutor.getOutputStream());
                command = (command.length() > 2 && command.trim().endsWith(";")) ? command.substring(0, command.length() - 1) : command;
                printStream.println(command + ";exit");
                printStream.flush();
                byte[] arrayOfByte = new byte[1024];
                InputStream inputStream = this.shellExecutor.getInputStream();
                String str = "";
                while (true) {
                    if (inputStream.available() > 0) {
                        int i = inputStream.read(arrayOfByte, 0, 1024);
                        if (i >= 0) {
                            str = new String(arrayOfByte, 0, i);
                            System.out.println(str);
                            continue;
                        }
                    }
                    if (str.contains("logout") || this.shellExecutor.isClosed()){
                        break;
                    }
                    ThreadSleep.sleep(2000L);
                }
            } catch (JSchException | IOException e) {
                Loggers.error((Throwable) e);
            }
        }
    }

    public void connectWithPass(String password) {
        try {
            this.jsch = new JSch();
            this.jsch.removeAllIdentity();
            this.password = password;
            this.session = this.jsch.getSession(this.username, this.host, TypesParser.safeParseInt(this.port));
            if (this.proxyHost != null && !"".equals(this.proxyHost)) {
                ProxyHTTP proxyHTTP = new ProxyHTTP(this.proxyHost, TypesParser.safeParseInt(this.proxyPort));
                if (this.proxyUsername != null && !"".equals(this.proxyUsername) && this.proxyPassword != null && !"".equals(this.proxyPassword)){
                    proxyHTTP.setUserPasswd(this.proxyUsername, this.proxyPassword);
                }
                this.session.setProxy((Proxy)proxyHTTP);
            }
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            this.session.setPassword(this.password);
            getClass();
            this.session.connect(60000);
            this.loginType = RemoteEnumeration.userpass;

        } catch (JSchException jSchException) {
            getClass();
            if (this.tries < 5) {
                this.session = null;
                this.jsch = null;
                this.tries++;
                connectWithPass(this.password);
            } else {
                Loggers.error((Throwable)jSchException);
            }
        }
    }

    public void connectWithPem(String pem, String passphrase) {
        try {
            this.jsch = new JSch();
            this.jsch.removeAllIdentity();
            this.pem = (pem == null) ? "" : pem;
            this.passphrase = (passphrase == null) ? "" : passphrase;
            this.jsch.addIdentity(String.valueOf(this.username).trim() + ".pem", this.pem.getBytes(), null, this.passphrase.getBytes());
            this.session = this.jsch.getSession(this.username, this.host, TypesParser.safeParseInt(this.port));
            if (this.proxyHost != null && !"".equals(this.proxyHost)) {
                ProxyHTTP proxyHTTP = new ProxyHTTP(this.proxyHost, TypesParser.safeParseInt(this.proxyPort));
                if (this.proxyUsername != null && !"".equals(this.proxyUsername) && this.proxyPassword != null && !"".equals(this.proxyPassword)){
                    proxyHTTP.setUserPasswd(this.proxyUsername, this.proxyPassword);
                }
                this.session.setProxy((Proxy)proxyHTTP);
            }
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            getClass();
            this.session.connect(60000);
            this.loginType = RemoteEnumeration.pem;
        } catch (JSchException jSchException) {
            getClass();
            if (this.tries < 5) {
                this.session = null;
                this.jsch = null;
                this.tries++;
                connectWithPem(this.pem, this.passphrase);
            } else {
                Loggers.error((Throwable)jSchException);
            }
        }
    }

    public void connectWithRsa() {
        try {
            this.jsch = new JSch();
            this.jsch.removeAllIdentity();
            this.jsch.addIdentity("/root/.ssh/id_rsa");
            this.session = this.jsch.getSession(this.username, this.host, TypesParser.safeParseInt(this.port));
            if (this.proxyHost != null && !"".equals(this.proxyHost)) {
                ProxyHTTP proxyHTTP = new ProxyHTTP(this.proxyHost, TypesParser.safeParseInt(this.proxyPort));
                if (this.proxyUsername != null && !"".equals(this.proxyUsername) && this.proxyPassword != null && !"".equals(this.proxyPassword)){
                    proxyHTTP.setUserPasswd(this.proxyUsername, this.proxyPassword);
                }
                this.session.setProxy((Proxy)proxyHTTP);
            }
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            getClass();
            this.session.connect(60000);
            this.loginType = RemoteEnumeration.rsa;
        } catch (JSchException jSchException) {
            getClass();
            if (this.tries < 5) {
                this.session = null;
                this.jsch = null;
                this.tries++;
                connectWithRsa();
            } else {
                Loggers.error((Throwable)jSchException);
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            if (this.shellExecutor != null && this.shellExecutor.isConnected()) {
                shellCommand("exit");
                this.shellExecutor.disconnect();
            }
            this.session.disconnect();
            this.session = null;
        }
    }

    public boolean isConnected() {
        return (this.session != null && this.session.isConnected());
    }

    private Session connect() {
        if (this.session == null || !this.session.isConnected()){
            switch ( RemoteOrdinal.value[this.loginType.ordinal()]){
                case 1:
                    connectWithPass(this.password) ;
                    break;
                case 2:
                    connectWithPem(this.pem,this.passphrase);
                    break;
                case 3:
                    connectWithRsa() ;
                    break;
            }
        }
        return this.session;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SSHConnector))
            return false;
        SSHConnector if1 = (SSHConnector)paramObject;
        if (!if1.exists(this))
            return false;
        JSch jSch1 = getJsch();
        JSch jSch2 = if1.getJsch();
        if ((jSch1 == null) ? (jSch2 != null) : !jSch1.equals(jSch2))
            return false;
        Session session1 = getSession();
        Session session2 = if1.getSession();
        if ((session1 == null) ? (session2 != null) : !session1.equals(session2))
            return false;
        ChannelShell channelShell1 = getShellExecutor();
        ChannelShell channelShell2 = if1.getShellExecutor();
        if ((channelShell1 == null) ? (channelShell2 != null) : !channelShell1.equals(channelShell2))
            return false;
        String str1 = getHost();
        String str2 = if1.getHost();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getPort();
        String str4 = if1.getPort();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getUsername();
        String str6 = if1.getUsername();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getPassword();
        String str8 = if1.getPassword();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getPem();
        String str10 = if1.getPem();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getPassphrase();
        String str12 = if1.getPassphrase();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        RemoteEnumeration int1 = getLoginType();
        RemoteEnumeration int2 = if1.getLoginType();
        if ((int1 == null) ? (int2 != null) : !int1.equals(int2))
            return false;
        String str13 = getProxyHost();
        String str14 = if1.getProxyHost();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getProxyPort();
        String str16 = if1.getProxyPort();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getProxyUsername();
        String str18 = if1.getProxyUsername();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getProxyPassword();
        String str20 = if1.getProxyPassword();
        return ((str19 == null) ? (str20 != null) : !str19.equals(str20)) ? false : ((getShellTimeout() != if1.getShellTimeout()) ? false : ((getSftpTimeout() != if1.getSftpTimeout()) ? false : ((getConnectionTimeout() != if1.getConnectionTimeout()) ? false : ((getTries() != if1.getTries()) ? false : (!(getMax() != if1.getMax()))))));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SSHConnector;
    }

    @Override
    public int hashCode() {
        int n = 1;
        JSch jSch = getJsch();
        n = n * 59 + ((jSch == null) ? 43 : jSch.hashCode());
        Session session = getSession();
        n = n * 59 + ((session == null) ? 43 : session.hashCode());
        ChannelShell channelShell = getShellExecutor();
        n = n * 59 + ((channelShell == null) ? 43 : channelShell.hashCode());
        String str1 = getHost();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getPort();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getUsername();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getPassword();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getPem();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getPassphrase();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        RemoteEnumeration int1 = getLoginType();
        n = n * 59 + ((int1 == null) ? 43 : int1.hashCode());
        String str7 = getProxyHost();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getProxyPort();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getProxyUsername();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getProxyPassword();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        n = n * 59 + getShellTimeout();
        n = n * 59 + getSftpTimeout();
        n = n * 59 + getConnectionTimeout();
        n = n * 59 + getTries();
        return n * 59 + getMax();
    }

    public JSch getJsch() {
        return this.jsch;
    }

    public Session getSession() {
        return this.session;
    }

    public ChannelShell getShellExecutor() {
        return this.shellExecutor;
    }

    public String getHost() {
        return this.host;
    }

    public String getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPem() {
        return this.pem;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public RemoteEnumeration getLoginType() {
        return this.loginType;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public String getProxyPort() {
        return this.proxyPort;
    }

    public String getProxyUsername() {
        return this.proxyUsername;
    }

    public String getProxyPassword() {
        return this.proxyPassword;
    }

    public int getShellTimeout() {
        getClass();
        return 60000;
    }

    public int getSftpTimeout() {
        getClass();
        return 120000;
    }

    public int getConnectionTimeout() {
        getClass();
        return 60000;
    }

    public int getTries() {
        return this.tries;
    }

    public int getMax() {
        getClass();
        return 5;
    }

    public void setJsch(JSch jsch) {
        this.jsch = jsch;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setShellExecutor(ChannelShell shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPem(String pem) {
        this.pem = pem;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setLoginType(RemoteEnumeration loginType) {
        this.loginType = loginType;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    @Override
    public String toString() {
        return "SSHConnector(jsch=" + getJsch() + ", session=" + getSession() + ", shellExecutor=" + getShellExecutor() + ", host=" + getHost() + ", port=" + getPort() + ", username=" + getUsername() + ", password=" + getPassword() + ", pem=" + getPem() + ", passphrase=" + getPassphrase() + ", loginType=" + getLoginType() + ", proxyHost=" + getProxyHost() + ", proxyPort=" + getProxyPort() + ", proxyUsername=" + getProxyUsername() + ", proxyPassword=" + getProxyPassword() + ", shellTimeout=" + getShellTimeout() + ", sftpTimeout=" + getSftpTimeout() + ", connectionTimeout=" + getConnectionTimeout() + ", tries=" + getTries() + ", max=" + getMax() + ")";
    }
}
