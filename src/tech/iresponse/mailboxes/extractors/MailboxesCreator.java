package tech.iresponse.mailboxes.extractors;

import java.beans.ConstructorProperties;
import java.sql.Date;
import org.json.JSONArray;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.Mailbox;
import tech.iresponse.utils.Strings;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.core.Application;
import tech.iresponse.remote.SSHConnector;

public class MailboxesCreator extends Thread {

    private SSHConnector ssh;
    private int domainId;
    private JSONArray prefixes;
    private String status;

    @Override
    public void run() {
        try {
            if (this.domainId > 0 && this.ssh != null && this.ssh.isConnected() && this.prefixes != null && this.prefixes.length() > 0) {

                Domain domin = new Domain(Integer.valueOf(this.domainId));
                if (domin.getEmpty()){
                    throw new DatabaseException("Domain not found !");
                }

                this.ssh.cmd("su - zimbra -c \"zmprov cd " + domin.value.trim() + "\"");

                Mailbox mailbox = null;
                String prfixs = "";

                for (int b = 0; b < this.prefixes.length(); b++) {

                    prfixs = String.valueOf(this.prefixes.get(b)).replace("\n", "").replace("\r", "");

                    if (!"".equals(prfixs)) {
                        Mailbox.delete(Mailbox.class, "email = ?", new Object[] { prfixs + "@" + domin.value.trim() });
                        String password = Strings.rndomSalt(25, false);
                        mailbox = new Mailbox();
                        mailbox.accountId = domin.accountId;
                        mailbox.accountName = domin.accountName;
                        mailbox.domainId = domin.id;
                        mailbox.domainName = domin.value;
                        mailbox.status = this.status;
                        mailbox.email = prfixs + "@" + domin.value.trim();
                        mailbox.password = password;
                        mailbox.createdBy = (Application.checkAndgetInstance().getUser()).email;
                        mailbox.createdDate = new Date(System.currentTimeMillis());
                        mailbox.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        mailbox.lastUpdatedDate = new Date(System.currentTimeMillis());
                        mailbox.insert();

                        this.ssh.cmd("su - zimbra -c \"zmprov da " + prfixs + "@" + domin.value.trim() + "\"");
                        this.ssh.cmd("su - zimbra -c \"zmprov ca " + prfixs + "@" + domin.value.trim() + " " + password + " displayName '" + prfixs.toUpperCase() + " " + domin.value.trim() + "'\"");
                    }
                }
            }

        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"ssh", "domainId", "prefixes", "status"})
    public MailboxesCreator(SSHConnector ssh, int domainId, JSONArray prefixes, String status) {
        this.ssh = ssh;
        this.domainId = domainId;
        this.prefixes = prefixes;
        this.status = status;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MailboxesCreator))
            return false;
        MailboxesCreator do1 = (MailboxesCreator)paramObject;
        if (!do1.exists(this))
            return false;
        SSHConnector if1 = getSsh();
        SSHConnector if2 = do1.getSsh();
        if ((if1 == null) ? (if2 != null) : !if1.equals(if2))
            return false;
        if (getDomainId() != do1.getDomainId())
            return false;
        JSONArray jSONArray1 = getPrefixes();
        JSONArray jSONArray2 = do1.getPrefixes();
        if ((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2))
            return false;
        String str1 = getStatus();
        String str2 = do1.getStatus();
            return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MailboxesCreator;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SSHConnector if1 = getSsh();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        n = n * 59 + getDomainId();
        JSONArray jSONArray = getPrefixes();
        n = n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
        String str = getStatus();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public SSHConnector getSsh() {
        return ssh;
    }

    public void setSsh(SSHConnector ssh) {
        this.ssh = ssh;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public JSONArray getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(JSONArray prefixes) {
        this.prefixes = prefixes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MailboxesCreator(ssh=" + getSsh() + ", domainId=" + getDomainId() + ", prefixes=" + getPrefixes() + ", status=" + getStatus() + ")";
    }
}
