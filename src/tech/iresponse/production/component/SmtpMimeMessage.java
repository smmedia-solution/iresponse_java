package tech.iresponse.production.component;

import java.io.InputStream;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;

public class SmtpMimeMessage extends MimeMessage {

    public SmtpMimeMessage(Session session, InputStream ins) throws MessagingException {
        super(session, ins);
    }

    protected void updateMessageID() throws MessagingException {
        String[] messageId = getHeader("Message-ID");
        removeHeader("Message-ID");
        if (messageId != null && messageId.length > 0){
            setHeader("Message-ID", messageId[0]);
        }
    }
}