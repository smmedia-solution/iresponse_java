package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.SmtpProcesses;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.lists.Email;
import tech.iresponse.production.component.SmtpComponent;
import tech.iresponse.production.component.SmtpMimeMessage;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.drops.SmtpProductionHelper;
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;

public class SmtpTestMessage extends Thread {

    private SmtpComponent component;
    private SmtpServer server;
    private TestEmail email;
    private int tries = 0;
    private int max = 10;
    private Session session;

    @Override
    public void run() {
        try {
            if (this.email != null && this.server != null) {
                SmtpUser smtpUsr = SmtpTestServer.getSmtpUsers(this.email.getComponentId());
                this.session = SmtpTestServer.getSmtpUserList(smtpUsr.id);
                //Session sess = SmtpTestServer.getSmtpUserList(smtpUsr.id);
                //System.out.println("sess 1 : " + this.session);
                if (this.session != null) {
                    StringBuilder messageIdBuilder = new StringBuilder();
                    String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.getSaltString(13, false, false, true, false)).append('.').append(System.currentTimeMillis()).append('@').append(this.component.getStaticDomain()).toString() + ">";

                    Email newEmail = MtaDropHelper.createEmailObject(this.email);
                    String autoReply = this.email.getAutoReplyMailbox();

                    String returnPath = SmtpProductionHelper.replaceTags(this.component.getReturnPath(), this.component, smtpUsr, this.server.name, newEmail, autoReply, this.email.getPlaceholders());
                    String fromName = SmtpProductionHelper.replaceTags(this.component.getFromName(), this.component, smtpUsr, this.server.name, newEmail, autoReply, this.email.getPlaceholders());
                    String subject = SmtpProductionHelper.replaceTags(this.component.getSubject(), this.component, smtpUsr, this.server.name, newEmail, autoReply, this.email.getPlaceholders());

                    String headerBody = SmtpProcesses.getCurrentHeader() + "__HEADER__BODY__SEPARATOR__" + this.component.getBody();
                    headerBody = SmtpProductionHelper.replaceTags(headerBody, this.component, smtpUsr, this.server.name, newEmail, autoReply, this.email.getPlaceholders());
                    headerBody = StringUtils.replace(headerBody, "[return_path]", returnPath);
                    headerBody = StringUtils.replace(headerBody, "[from_name]", fromName);
                    headerBody = StringUtils.replace(headerBody, "[subject]", subject);
                    headerBody = StringUtils.replace(headerBody, "[mail_date]", (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")).format(new Date()));
                    headerBody = StringUtils.replace(headerBody, "[message_id]", messageId);

                    String open = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "op", this.component.getId(), "md", this.component.getOfferId(), smtpUsr.id, newEmail.listId, newEmail.id);
                    String click = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "cl", this.component.getId(), "md", this.component.getOfferId(), smtpUsr.id, newEmail.listId, newEmail.id);
                    String unsub = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "un", this.component.getId(), "md", this.component.getOfferId(), smtpUsr.id, newEmail.listId, newEmail.id);
                    String optout = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "oop", this.component.getId(), "md", this.component.getOfferId(), smtpUsr.id, newEmail.listId, newEmail.id);

                    if (this.component.getShortDomain() != null && !"".equals(this.component.getShortDomain())) {
                        headerBody = StringUtils.replace(headerBody, "[short_open]", this.component.getShortDomain() + open);
                        headerBody = StringUtils.replace(headerBody, "[short_url]", this.component.getShortDomain() + click);
                        headerBody = StringUtils.replace(headerBody, "[short_unsub]", this.component.getShortDomain() + unsub);
                        headerBody = StringUtils.replace(headerBody, "[short_optout]", this.component.getShortDomain() + optout);
                    } else {
                        headerBody = StringUtils.replace(headerBody, "[open]", open);
                        headerBody = StringUtils.replace(headerBody, "[url]", click);
                        headerBody = StringUtils.replace(headerBody, "[unsub]", unsub);
                        headerBody = StringUtils.replace(headerBody, "[optout]", optout);
                    }

                    if (headerBody.contains("[negative]")){
                        headerBody = StringUtils.replace(headerBody, "[negative]", FileUtils.readFileToString(new File(this.component.getNegativeFilePath()), "UTF-8"));
                    }

                    headerBody = MtaDropHelper.replaceUrlImage(MtaDropHelper.replaceRandomTags(headerBody), this.component.getStaticDomain());
                    headerBody = MtaDropHelper.replaceRandomTags(headerBody);

                    String[] headers = headerBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                    String headr = (headers.length > 0) ? headers[0] : "";

                    //System.out.println("sess 2 : " + sess);
                    SmtpMimeMessage message = new SmtpMimeMessage(this.session, new ByteArrayInputStream(headr.getBytes()));
                    message.setRecipients(MimeMessage.RecipientType.TO, (Address[])InternetAddress.parse(newEmail.email));
                    message.setSentDate(new Date());
                    String body = (headers.length > 1) ? headers[1] : "";
                    message.setContent(body, this.component.getContentType() + "; charset=" + this.component.getCharset());
                    message.saveChanges();
                    this.component.await();
                    updateSmtpTestMessages((Message)message, smtpUsr);
                    MtaDropHelper.updateProgress(this.component.getId(), 1, 1);
                }
            }
        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    public void updateSmtpTestMessages(Message msg, SmtpUser smtpUsr) {
        try {
            Transport.send(msg, smtpUsr.username.replaceAll("\r", "").replaceAll("\n", ""), smtpUsr.password.replaceAll("\r", "").replaceAll("\n", ""));
        } catch (Exception ex) {
            if (this.tries < this.max) {
                this.tries++;
                ThreadSleep.sleep(1000L);
                updateSmtpTestMessages(msg, smtpUsr);
            } else {
                Loggers.error(ex);
            }
        }
    }

    @ConstructorProperties({"component", "server", "email", "tries", "max"})
    public SmtpTestMessage(SmtpComponent component, SmtpServer server, TestEmail email, int tries, int max) {
    //public SmtpTestMessage(SmtpComponent component, SmtpServer server, TestEmail email, Session session, int tries, int max) {
        this.component = component;
        this.server = server;
        this.email = email;
        //this.session = session;
        this.tries = tries;
        this.max = max;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpTestMessage))
            return false;
        SmtpTestMessage else1 = (SmtpTestMessage)paramObject;
        if (!else1.exists(this))
            return false;
        SmtpComponent int1 = getComponent();
        SmtpComponent int2 = else1.getComponent();
        if ((int1 == null) ? (int2 != null) : !int1.equals(int2))
            return false;
        SmtpServer finally1 = getServer();
        SmtpServer finally2 = else1.getServer();
        if ((finally1 == null) ? (finally2 != null) : !finally1.equals(finally2))
            return false;
        TestEmail try1 = getEmail();
        TestEmail try2 = else1.getEmail();
        if ((try1 == null) ? (try2 != null) : !try1.equals(try2))
            return false;
        return ((getTries() != else1.getTries()) ? false : (!(getMax() != else1.getMax())));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpTestMessage;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SmtpComponent int1 = getComponent();
        n = n * 59 + ((int1 == null) ? 43 : int1.hashCode());
        SmtpServer finally1 = getServer();
        n = n * 59 + ((finally1 == null) ? 43 : finally1.hashCode());
        TestEmail try1 = getEmail();
        n = n * 59 + ((try1 == null) ? 43 : try1.hashCode());
        n = n * 59 + getTries();
        return n * 59 + getMax();
    }

    public SmtpComponent getComponent() {
        return component;
    }

    public void setComponent(SmtpComponent component) {
        this.component = component;
    }

    public SmtpServer getServer() {
        return server;
    }

    public void setServer(SmtpServer server) {
        this.server = server;
    }

    public TestEmail getEmail() {
        return email;
    }

    public void setEmail(TestEmail email) {
        this.email = email;
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String toString() {
        return "SmtpTestMessage(component=" + getComponent() + ", server=" + getServer() + ", email=" + getEmail() + ", tries=" + getTries() + ", max=" + getMax() + ")";
    }
}
