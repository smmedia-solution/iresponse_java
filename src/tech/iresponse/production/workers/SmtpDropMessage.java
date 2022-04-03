package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
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
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.production.drops.SmtpProductionHelper;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;

public class SmtpDropMessage extends Thread {

    private SmtpComponent component;
    private SmtpServer server;
    private Map email;
    private SmtpUser smtpUser;
    private Session session;
    private int tries = 0;
    private int max = 10;

    @Override
    public void run() {
        try {
            if (this.email != null && !this.email.isEmpty() && this.server != null) {
                StringBuilder messageIdBuilder = new StringBuilder();
                String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.getSaltString(13, false, false, true, false)).append('.').append(System.currentTimeMillis()).append('@').append(this.component.getStaticDomain()).toString() + ">";

                Email newEmail = MtaDropHelper.createEmailMap(this.email);
                String autoReply = this.component.isAutoReplyActivated() ? SmtpProcesses.getCurrentAutoReply() : "";

                int currentPlaceHolder = (this.component.getPlaceholders() != null) ? this.component.getPlaceholders().size() : 0;

                ArrayList<String> placeholders = new ArrayList(currentPlaceHolder);
                if (currentPlaceHolder > 0){
                    for (int i = 0; i < currentPlaceHolder; i++){
                        placeholders.add(SmtpProcesses.getCurrentPlaceHolder(i));
                    }
                }

                String returnPath = SmtpProductionHelper.replaceTags(this.component.getReturnPath(), this.component, this.smtpUser, this.server.name, newEmail, autoReply, placeholders);
                String fromName = SmtpProductionHelper.replaceTags(this.component.getFromName(), this.component, this.smtpUser, this.server.name, newEmail, autoReply, placeholders);
                String subject = SmtpProductionHelper.replaceTags(this.component.getSubject(), this.component, this.smtpUser, this.server.name, newEmail, autoReply, placeholders);

                String headerBody = SmtpProcesses.getCurrentHeader() + "__HEADER__BODY__SEPARATOR__" + this.component.getBody();
                headerBody = SmtpProductionHelper.replaceTags(headerBody, this.component, this.smtpUser, this.server.name, newEmail, autoReply, placeholders);
                headerBody = StringUtils.replace(headerBody, "[return_path]", returnPath);
                headerBody = StringUtils.replace(headerBody, "[from_name]", fromName);
                headerBody = StringUtils.replace(headerBody, "[subject]", subject);
                headerBody = StringUtils.replace(headerBody, "[mail_date]", (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")).format(new Date()));
                headerBody = StringUtils.replace(headerBody, "[message_id]", messageId);

                String open = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "op", this.component.getId(), "md", this.component.getOfferId(), this.smtpUser.id, newEmail.listId, newEmail.id);
                String click = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "cl", this.component.getId(), "md", this.component.getOfferId(), this.smtpUser.id, newEmail.listId, newEmail.id);
                String unsub = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "un", this.component.getId(), "md", this.component.getOfferId(), this.smtpUser.id, newEmail.listId, newEmail.id);
                String optout = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "oop", this.component.getId(), "md", this.component.getOfferId(), this.smtpUser.id, newEmail.listId, newEmail.id);

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

                headerBody = MtaDropHelper.fixStaticTags(headerBody);
                headerBody = MtaDropHelper.replaceUrlImage(MtaDropHelper.replaceRandomTags(headerBody), this.component.getStaticDomain());

                String[] headers = headerBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                String headr = (headers.length > 0) ? headers[0] : "";

                SmtpMimeMessage message = new SmtpMimeMessage(this.session, new ByteArrayInputStream(headr.getBytes()));
                message.setRecipients(MimeMessage.RecipientType.TO, (Address[])InternetAddress.parse(newEmail.email));
                message.setSentDate(new Date());
                String str12 = (headers.length > 1) ? headers[1] : "";
                message.setContent(str12, this.component.getContentType() + "; charset=" + this.component.getCharset());
                message.saveChanges();
                this.component.await();
                updateSmtpMessages((Message)message);

                MtaDropHelper.updateProgress(this.component.getId(), 1, 1);
                this.component.updateSmtpUsersTotals(this.smtpUser.id);
            }
        } catch (Throwable throwable) {
            Loggers.error(throwable);
        }
    }

    public void updateSmtpMessages(Message msg) {
        try {
            Transport.send(msg, this.smtpUser.username.replaceAll("\r", "").replaceAll("\n", ""), this.smtpUser.password.replaceAll("\r", "").replaceAll("\n", ""));
        } catch (Exception ex) {
            if (this.tries < this.max) {
                this.tries++;
                ThreadSleep.sleep(1000L);
                updateSmtpMessages(msg);
            } else {
                Loggers.error(ex);
            }
        }
    }

    @ConstructorProperties({"component", "server", "email", "smtpUser", "session", "tries", "max"})
    public SmtpDropMessage(SmtpComponent component, SmtpServer server, Map email, SmtpUser smtpUser, Session session, int tries, int max) {
        this.component = component;
        this.server = server;
        this.email = email;
        this.smtpUser = smtpUser;
        this.session = session;
        this.tries = tries;
        this.max = max;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpDropMessage))
            return false;
        SmtpDropMessage case1 = (SmtpDropMessage)paramObject;
        if (!case1.exists(this))
            return false;
        SmtpComponent int1 = getComponent();
        SmtpComponent int2 = case1.getComponent();
        if ((int1 == null) ? (int2 != null) : !int1.equals(int2))
            return false;
        SmtpServer finally1 = getServer();
        SmtpServer finally2 = case1.getServer();
        if ((finally1 == null) ? (finally2 != null) : !finally1.equals(finally2))
            return false;
        Map map1 = getEmail();
        Map map2 = case1.getEmail();
        if ((map1 == null) ? (map2 != null) : !map1.equals(map2))
            return false;
        SmtpUser package1 = getSmtpUser();
        SmtpUser package2 = case1.getSmtpUser();
        if ((package1 == null) ? (package2 != null) : !package1.equals(package2))
            return false;
        Session session1 = getSession();
        Session session2 = case1.getSession();
        return ((session1 == null) ? (session2 != null) : !session1.equals(session2)) ? false : ((getTries() != case1.getTries()) ? false : (!(getMax() != case1.getMax())));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpDropMessage;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SmtpComponent int1 = getComponent();
        n = n * 59 + ((int1 == null) ? 43 : int1.hashCode());
        SmtpServer finally1 = getServer();
        n = n * 59 + ((finally1 == null) ? 43 : finally1.hashCode());
        Map map = getEmail();
        n = n * 59 + ((map == null) ? 43 : map.hashCode());
        SmtpUser package1 = getSmtpUser();
        n = n * 59 + ((package1 == null) ? 43 : package1.hashCode());
        Session session = getSession();
        n = n * 59 + ((session == null) ? 43 : session.hashCode());
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

    public Map getEmail() {
        return email;
    }

    public void setEmail(Map email) {
        this.email = email;
    }

    public SmtpUser getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(SmtpUser smtpUser) {
        this.smtpUser = smtpUser;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
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

    @Override
    public String toString() {
        return "SmtpDropMessage(component=" + getComponent() + ", server=" + getServer() + ", email=" + getEmail() + ", smtpUser=" + getSmtpUser() + ", session=" + getSession() + ", tries=" + getTries() + ", max=" + getMax() + ")";
    }
}
