package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.mail.Session;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.SmtpProcesses;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.production.component.Rotator;
import tech.iresponse.production.component.SmtpComponent;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.drops.SmtpDropUsersUpdater;
import tech.iresponse.helpers.scripts.SMTPauth;

public class SmtpDropServer extends Thread {

    private SmtpComponent component;
    private int serverId;
    private List<Integer> smtpUsersIds;
    private int start;
    private int count;
    private static final int nbThread = 500;

    @Override
    public void run() {
        try {
            SmtpServer smtpServ = new SmtpServer(Integer.valueOf(this.serverId));
            if (smtpServ.getEmpty()){
                throw new DatabaseException("Smtp server not found");
            }

            if (this.smtpUsersIds.isEmpty()){
                throw new DatabaseException("No smtp users found !");
            }

            Session session = SMTPauth.connect(smtpServ);
            if (session == null){
                throw new DatabaseException("Could not connect to smtp server !");
            }

            String cond = "";
            cond = this.smtpUsersIds.stream().filter(smtpUsrId -> (smtpUsrId.intValue() > 0)).map(smtpUsrId -> "'" + smtpUsrId + "',").reduce(cond, String::concat);
            cond = cond.substring(0, cond.length() - 1);

            this.component.setSmtpUsersRotator(new Rotator((List)SmtpUser.all(SmtpUser.class, "id IN (" + cond + ")", null), this.component.getSmtpUsersRotation()));

            String query = "SELECT sub.* FROM (" + this.component.getQuery() + ") AS sub";
            if (this.component.getDataDuplicate() > 1){
                query = query + " CROSS JOIN generate_series(1," + this.component.getDataDuplicate() + ") as x";
            }
            query = query + " OFFSET " + this.start + " LIMIT " + this.count;

            List list = Database.get("clients").executeQuery(query, null, 1);
            this.component.setTotalEmails(!list.isEmpty() ? list.size() : 0);
            if (this.component.getTotalEmails() == 0) {
                throw new DatabaseException("Total emails should be greather than 0 !");
            }

            ExecutorService execService = Executors.newFixedThreadPool(this.component.getSmtpUsersRotator().getList().size());
            /*for (SmtpUser stpUsr : this.component.getSmtpUsersRotator().getList()) {
                execService.submit((Runnable)new SmtpDropUsersUpdater(stpUsr, this.component));
            }*/
            Iterator<SmtpUser> it = (Iterator<SmtpUser>)this.component.getSmtpUsersRotator().getList().iterator();
            while (it.hasNext()) {
                execService.submit((Runnable)new SmtpDropUsersUpdater((SmtpUser)it.next(), this.component));
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }

            String stcDomain = this.component.getStaticDomain();
            switch (this.component.getLinkType()) {
                case "routing-bitly":
                case "attr-bitly":
                    this.component.setShortDomain(Bitly.shortBitly(stcDomain) + "#");
                    break;
                case "routing-gcloud":
                case "attr-gcloud":
                    this.component.setShortDomain(Google.shortGoogle(stcDomain) + "#");
                    break;
                case "routing-tinyurl":
                case "attr-tinyurl":
                    this.component.setShortDomain(Tinyurl.shortTinyurl(stcDomain) + "#");
                    break;
            }

            execService = Executors.newFixedThreadPool(500);
            int progress = 0;

            for (Map<Object, Object> map : (Iterable<Map<Object, Object>>)list) {
                execService.submit(new SmtpDropMessage(this.component, smtpServ, map, this.component.getCurrentSmtpUser(), session, 0, 10));
                progress = SmtpProcesses.updateCounter();
                if (progress > 0 && progress % this.component.getTestAfter() == 0 && this.component.getTestEmails() != null && (this.component.getTestEmails()).length > 0){
                    for (String email : this.component.getTestEmails()) {
                        map = new HashMap<>();
                        map.put("table", "");
                        map.put("schema", "");
                        map.put("list_id", Integer.valueOf(0));
                        map.put("encrypt_emails", "Disabled");
                        map.put("id", Integer.valueOf(0));
                        map.put("email", email.trim());
                        map.put("first_name", email.trim().split(Pattern.quote("@"))[0]);
                        map.put("last_name", email.trim().split(Pattern.quote("@"))[0]);
                        execService.submit(new SmtpDropMessage(this.component, smtpServ, map, this.component.getCurrentSmtpUser(), session, 0, 10));
                    }
                }
            }
            execService.shutdown();
            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            Loggers.error(ex);
            SmtpProcesses.IS_ERROR_OCCURED = true;
        } finally {
            if (this.component != null && !this.component.getSmtpUsersTotals().isEmpty()){
                this.component.getSmtpUsersTotals().forEach((smtpUsrId, delivred) -> {
                    try {
                        Database.get("system").executeUpdate("UPDATE production.smtp_processes_users SET sent_total = " + delivred + " , delivered = " + delivred + " WHERE smtp_user_id = " + smtpUsrId + " AND process_id = " + this.component.getId(), null, 0);
                    } catch (Exception e) {
                        Loggers.error(e);
                    }
                });
            }
        }
    }

    @ConstructorProperties({"component", "serverId", "smtpUsersIds", "start", "count"})
    public SmtpDropServer(SmtpComponent component, int serverId, List smtpUsersIds, int start, int count) {
        this.component = component;
        this.serverId = serverId;
        this.smtpUsersIds = smtpUsersIds;
        this.start = start;
        this.count = count;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpDropServer))
            return false;
        SmtpDropServer char1 = (SmtpDropServer)paramObject;
        if (!char1.exists(this))
            return false;
        SmtpComponent int1 = getComponent();
        SmtpComponent int2 = char1.getComponent();
        if ((int1 == null) ? (int2 != null) : !int1.equals(int2))
            return false;
        if (getServerId() != char1.getServerId())
            return false;
        List list1 = getSmtpUsersIds();
        List list2 = char1.getSmtpUsersIds();
        return ((list1 == null) ? (list2 != null) : !list1.equals(list2)) ? false : ((getStart() != char1.getStart()) ? false : (!(getCount() != char1.getCount())));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpDropServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SmtpComponent int1 = getComponent();
        n = n * 59 + ((int1 == null) ? 43 : int1.hashCode());
        n = n * 59 + getServerId();
        List list = getSmtpUsersIds();
        n = n * 59 + ((list == null) ? 43 : list.hashCode());
        n = n * 59 + getStart();
        return n * 59 + getCount();
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

    public List<Integer> getSmtpUsersIds() {
        return smtpUsersIds;
    }

    public void setSmtpUsersIds(List<Integer> smtpUsersIds) {
        this.smtpUsersIds = smtpUsersIds;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "SmtpDropServer(component=" + getComponent() + ", serverId=" + getServerId() + ", smtpUsersIds=" + getSmtpUsersIds() + ", start=" + getStart() + ", count=" + getCount() + ")";
    }
}
