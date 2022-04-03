package tech.iresponse.helpers.scripts;

import com.sun.mail.util.MailSSLSocketFactory;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.helpers.smtp.SmtpAccount;

public class SMTPauth {

    public static Session connect(SmtpServer smtpServ) throws Exception{
        try {
            if (smtpServ != null && smtpServ.hostName != null && !Strings.isEmpty(smtpServ.hostName)) {
                Properties prop = new Properties();
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.host", smtpServ.hostName.replaceAll("\r", "").replaceAll("\n", ""));
                prop.put("mail.smtp.port", Integer.valueOf(smtpServ.smtpPort));
                prop.put("mail.smtp.timeout", "10000");
                prop.put("mail.smtp.connectiontimeout", "10000");
                if ("ssl".equalsIgnoreCase(smtpServ.encryptionType)) {
                    MailSSLSocketFactory mailSsl = new MailSSLSocketFactory();
                    mailSsl.setTrustAllHosts(true);
                    prop.put("mail.smtp.ssl.enable", "true");
                    prop.put("mail.smtp.ssl.socketFactory", mailSsl);
                } else if ("tls".equalsIgnoreCase(smtpServ.encryptionType)) {
                    prop.put("mail.smtp.starttls.enable", "true");
                }
                return Session.getInstance(prop);
            }
        } catch (Exception exception) {
            throw new DatabaseException("Could not connect to this server !", exception);
        }
        return null;
    }

    public static Session connect(SmtpServer smtpServ, SmtpUser smtpUsr) throws Exception {
        try {
            if (smtpServ != null && smtpServ.hostName != null && !Strings.isEmpty(smtpServ.hostName) && smtpUsr != null) {

                Properties prop = new Properties();

                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.host", smtpServ.hostName.replaceAll("\r", "").replaceAll("\n", ""));
                prop.put("mail.smtp.port", Integer.valueOf(smtpServ.smtpPort));
                prop.put("mail.smtp.timeout", "10000");
                prop.put("mail.smtp.connectiontimeout", "10000");

                if ("ssl".equalsIgnoreCase(smtpServ.encryptionType)) {
                    MailSSLSocketFactory mailSsl = new MailSSLSocketFactory();
                    mailSsl.setTrustAllHosts(true);
                    prop.put("mail.smtp.ssl.enable", "true");
                    prop.put("mail.smtp.ssl.socketFactory", mailSsl);
                } else if ("tls".equalsIgnoreCase(smtpServ.encryptionType)) {
                    prop.put("mail.smtp.starttls.enable", "true");
                }

                if (!"".equals(smtpUsr.proxyIp) && smtpUsr.proxyPort > 0) {
                    prop.put("mail.smtp.socks.host", smtpUsr.proxyIp);
                    prop.put("mail.smtp.socks.port", String.valueOf(smtpUsr.proxyPort));
                }

                if (smtpUsr.proxyUsername != null && !"".equals(smtpUsr.proxyUsername) && smtpUsr.proxyPassword != null && !"".equals(smtpUsr.proxyPassword)) {
                    prop.setProperty("http.proxyUsername", smtpUsr.proxyUsername);
                    prop.setProperty("http.proxyPassword", smtpUsr.proxyPassword);
                }

                PassAuth stpuser = new PassAuth(smtpUsr);
                Session session = Session.getInstance(prop, stpuser);
                try (Transport trsprt = session.getTransport("smtp")) {
                    trsprt.connect();
                    trsprt.close();
                }
                return session;
            }
        } catch (AuthenticationFailedException authenticationFailedException) {
            throw new DatabaseException("Could not authenticate with this user !", authenticationFailedException);
        } catch (MessagingException messagingException) {
            throw new DatabaseException("Could not connect to this server !", messagingException);
        } catch (Exception exception) {
            throw new DatabaseException("Internal Server Error !", exception);
        }
        return null;
    }

    public static Session checkSmtp(SmtpAccount smtpAcc) throws Exception {
        try {
            if (smtpAcc != null && smtpAcc.getHost() != null && !Strings.isEmpty(smtpAcc.getHost())) {
                Properties prop = new Properties();
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.host", smtpAcc.getHost().replaceAll("\r", "").replaceAll("\n", ""));
                prop.put("mail.smtp.port", smtpAcc.getPort());
                prop.put("mail.smtp.timeout", "10000");
                prop.put("mail.smtp.connectiontimeout", "10000");

                if ("ssl".equalsIgnoreCase(smtpAcc.getEncryption())) {
                    MailSSLSocketFactory mailSsl = new MailSSLSocketFactory();
                    mailSsl.setTrustAllHosts(true);
                    prop.put("mail.smtp.ssl.enable", "true");
                    prop.put("mail.smtp.ssl.socketFactory", mailSsl);
                } else if ("tls".equalsIgnoreCase(smtpAcc.getEncryption())) {
                    prop.put("mail.smtp.starttls.enable", "true");
                }

                if (!"".equals(smtpAcc.getProxyHost()) && TypesParser.safeParseInt(smtpAcc.getProxyPort()) > 0) {
                    prop.put("mail.smtp.socks.host", smtpAcc.getProxyHost());
                    prop.put("mail.smtp.socks.port", smtpAcc.getProxyPort());
                }

                if (smtpAcc.getProxyUsername() != null && !"".equals(smtpAcc.getProxyUsername()) && smtpAcc.getProxyPassword() != null && !"".equals(smtpAcc.getProxyPassword())) {
                    prop.setProperty("http.proxyUsername", smtpAcc.getProxyUsername());
                    prop.setProperty("http.proxyPassword", smtpAcc.getProxyPassword());
                }

                AuthSmtpAcc auth = new AuthSmtpAcc(smtpAcc);
                Session session = Session.getInstance(prop, auth);
                try (Transport trsport = session.getTransport("smtp")) {
                    trsport.connect();
                    trsport.close();
                }
                return session;
            }
        } catch (AuthenticationFailedException au) {
            throw new DatabaseException("Could not authenticate with this user !", au);
        } catch (MessagingException msg) {
            throw new DatabaseException("Could not connect to this server !", msg);
        } catch (Exception e) {
            throw new DatabaseException("Internal Server Error !", e);
        }
        return null;
    }
}
