package tech.iresponse.tools.services;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

class PassAuth extends Authenticator {

    final MailboxExtractor mailExtr;

    PassAuth(MailboxExtractor mailEx) { this.mailExtr = mailEx; }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.mailExtr.getEmail(), this.mailExtr.getPassword());
    }

    /*protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(MailboxExtractor.do(this.do), MailboxExtractor.if(this.do));
    }*/
}

