package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.mail.Session;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.SmtpProcesses;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.production.component.SmtpComponent;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.helpers.scripts.SMTPauth;

public class SmtpTestServer extends Thread {

    private SmtpComponent component;
    private int serverId;
    private List<TestEmail> emails;
    private List<Integer> serverSmtpUsers;
    private static final int nbThreads = 500;
    public static volatile LinkedHashMap<Integer,Session> SMTP_SESSIONS = new LinkedHashMap<>();
    public static volatile HashMap<Integer,SmtpUser> SMTP_USERS = new HashMap<>();

    public void run() {
        try {
            SmtpServer smtpServ = new SmtpServer(Integer.valueOf(this.serverId));
            if (smtpServ.getEmpty()){
                throw new DatabaseException("Smtp server not found");
            }

            if (this.serverSmtpUsers.isEmpty()){
                throw new DatabaseException("No smtp user found !");
            }

            if (this.emails == null || this.emails.isEmpty()){
                throw new DatabaseException("No emails found !");
            }

            this.component.setTotalEmails(this.emails.size());
            if (this.component.getTotalEmails() == 0){
                throw new DatabaseException("Total emails should be greather than 0 !");
            }

            /*Session session = SMTPauth.connect(smtpServ);
            if (session == null){
                throw new DatabaseException("Could not connect to smtp server !");
            }*/

            /*Iterator<Integer> it = this.serverSmtpUsers.iterator();
            while (it.hasNext()) {
                int i = ((Integer)it.next()).intValue();
                updateSmtpUser(i);
            }*/
            for (int smtpUserId : this.serverSmtpUsers)
            {
                SmtpTestServer.updateSmtpUser(smtpUserId);
            }

            SmtpTestServer.SMTP_USERS.forEach((id, smtpUsr) -> {
                try {
                    Session session = SMTPauth.connect(smtpServ,smtpUsr);
                    SmtpTestServer.updateSmtpSessions(id, session);
                } catch (Exception exception) {
                    Loggers.error(exception);
                }
            });

            String str = this.component.getStaticDomain();
            switch (this.component.getLinkType()) {
                case "routing-bitly":
                case "attr-bitly":
                    this.component.setShortDomain(Bitly.shortBitly(str) + "#");
                    break;
                case "routing-gcloud":
                case "attr-gcloud":
                    this.component.setShortDomain(Google.shortGoogle(str) + "#");
                    break;
                case "routing-tinyurl":
                case "attr-tinyurl":
                    this.component.setShortDomain(Tinyurl.shortTinyurl(str) + "#");
                    break;
            }

            ExecutorService execService = Executors.newFixedThreadPool(500);
            //this.emails.forEach(email -> execService.submit(new SmtpTestMessage(this.component, smtpServ, email, session, 0, 10)));
            this.emails.forEach(email -> execService.submit(new SmtpTestMessage(this.component, smtpServ, email, 0, 10)));
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception exception) {
            Loggers.error(exception);
            SmtpProcesses.IS_ERROR_OCCURED = true;
        }
    }

    public static synchronized void updateSmtpUser(int smtpUsrId) throws Exception {
        SmtpTestServer.SMTP_USERS.put(smtpUsrId, new SmtpUser(smtpUsrId));
    }

    public static synchronized SmtpUser getSmtpUsers(int smtUsrId) {
        return (SmtpUser)SmtpTestServer.SMTP_USERS.get(smtUsrId);
    }

    public static synchronized void updateSmtpSessions(int smtpUsrId, Session sess) {
        SmtpTestServer.SMTP_SESSIONS.put(smtpUsrId, sess);
    }

    public static synchronized Session getSmtpUserList(int smtpUsrId) {
        return (Session)SmtpTestServer.SMTP_SESSIONS.get(smtpUsrId);
    }

    @ConstructorProperties({"component", "serverId", "emails", "serverSmtpUsers"})
    public SmtpTestServer(SmtpComponent component, int serverId, List emails, List serverSmtpUsers) {
        this.component = component;
        this.serverId = serverId;
        this.emails = emails;
        this.serverSmtpUsers = serverSmtpUsers;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpTestServer))
            return false;
        SmtpTestServer goto1 = (SmtpTestServer)paramObject;
        if (!goto1.exists(this))
            return false;
        SmtpComponent int1 = getComponent();
        SmtpComponent int2 = goto1.getComponent();
        if ((int1 == null) ? (int2 != null) : !int1.equals(int2))
            return false;
        if (getServerId() != goto1.getServerId())
            return false;
        List list1 = getEmails();
        List list2 = goto1.getEmails();
        if ((list1 == null) ? (list2 != null) : !list1.equals(list2))
            return false;
        List list3 = getServerSmtpUsers();
        List list4 = goto1.getServerSmtpUsers();
        return !((list3 == null) ? (list4 != null) : !list3.equals(list4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpTestServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SmtpComponent int1 = getComponent();
        n = n * 59 + ((int1 == null) ? 43 : int1.hashCode());
        n = n * 59 + getServerId();
        List list1 = getEmails();
        n = n * 59 + ((list1 == null) ? 43 : list1.hashCode());
        List list2 = getServerSmtpUsers();
        return n * 59 + ((list2 == null) ? 43 : list2.hashCode());
    }

    public SmtpComponent getComponent() {
        return component;
    }

    public void setComponent(SmtpComponent component) {
        this.component = component;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List getEmails() {
        return emails;
    }

    public void setEmails(List emails) {
        this.emails = emails;
    }

    public List getServerSmtpUsers() {
        return serverSmtpUsers;
    }

    public void setServerSmtpUsers(List serverSmtpUsers) {
        this.serverSmtpUsers = serverSmtpUsers;
    }

    @Override
    public String toString() {
        return "SmtpTestServer(component=" + getComponent() + ", serverId=" + getServerId() + ", emails=" + getEmails() + ", serverSmtpUsers=" + getServerSmtpUsers() + ")";
    }
}
