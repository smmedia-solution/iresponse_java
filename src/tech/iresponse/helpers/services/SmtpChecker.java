package tech.iresponse.helpers.services;

import java.beans.ConstructorProperties;
import javax.mail.Session;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Servers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.helpers.smtp.SmtpAccount;
import tech.iresponse.helpers.scripts.SMTPauth;

public class SmtpChecker extends Thread {

    private SmtpAccount account;

    public void run() {
        boolean bool = true;
        try {
            Session session = SMTPauth.checkSmtp(this.account);
            if (session != null) {
                bool = true;
            } else {
                throw new DatabaseException("Could not authenticate to this smtp !");
            }
        } catch (Exception ex) {
            Loggers.error((Throwable)ex);
            bool = false;
        }
        if (bool){
            Servers.updateSmtps(this.account);
        }
    }

    @ConstructorProperties({"account"})
    public SmtpChecker(SmtpAccount account) {
        this.account = account;
    }

    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpChecker))
            return false;
        SmtpChecker new1 = (SmtpChecker)paramObject;
        if (!new1.exists(this))
            return false;
        SmtpAccount do1 = getAccount();
        SmtpAccount do2 = new1.getAccount();
        return !((do1 == null) ? (do2 != null) : !do1.equals(do2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpChecker;
    }

    public int hashCode() {
        int n = 1;
        SmtpAccount do1 = getAccount();
        return n * 59 + ((do1 == null) ? 43 : do1.hashCode());
    }

    public SmtpAccount getAccount() {
        return account;
    }

    public void setAccount(SmtpAccount account) {
        this.account = account;
    }

    public String toString() {
        return "SmtpChecker(account=" + getAccount() + ")";
    }
}
