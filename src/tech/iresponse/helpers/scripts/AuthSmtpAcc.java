package tech.iresponse.helpers.scripts;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import tech.iresponse.helpers.smtp.SmtpAccount;

final class AuthSmtpAcc extends Authenticator {

    final SmtpAccount smtpAcc;

    AuthSmtpAcc (SmtpAccount smtpAcc) {
        this.smtpAcc = smtpAcc;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.smtpAcc.getUsername().replaceAll("\r", "").replaceAll("\n", ""), this.smtpAcc.getPassword().replaceAll("\r", "").replaceAll("\n", ""));
    }
}
