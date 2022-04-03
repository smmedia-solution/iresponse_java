package tech.iresponse.helpers.scripts;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import tech.iresponse.models.admin.SmtpUser;

final class PassAuth extends Authenticator {

    final SmtpUser smtpUsr;

    PassAuth (SmtpUser smusr) {
        this.smtpUsr = smusr;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.smtpUsr.username.replaceAll("\r", "").replaceAll("\n", ""), this.smtpUsr.password.replaceAll("\r", "").replaceAll("\n", ""));
    }
}

