package tech.iresponse.mailboxes.extractors;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Mailbox;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.remote.SSHConnector;

public class MailboxesRemover extends Thread {

    private SSHConnector ssh;
    private int mailboxId;

    @Override
    public void run() {
        try {
            if (this.mailboxId > 0 && this.ssh != null && this.ssh.isConnected()) {
                Mailbox mailbx = new Mailbox(Integer.valueOf(this.mailboxId));
                if (mailbx.getEmpty()){
                    throw new DatabaseException("Mailbox not found !");
                }

                this.ssh.cmd("su - zimbra -c \"zmprov da " + mailbx.email + "\"");
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"ssh", "mailboxId"})
    public MailboxesRemover(SSHConnector ssh, int mailboxId) {
        this.ssh = ssh;
        this.mailboxId = mailboxId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MailboxesRemover))
            return false;
        MailboxesRemover if1 = (MailboxesRemover)paramObject;
        if (!if1.exists(this))
            return false;
        SSHConnector if2 = getSsh();
        SSHConnector if3 = if1.getSsh();
            return ((if2 == null) ? (if3 != null) : !if2.equals(if3)) ? false : (!(getMailboxId() != if1.getMailboxId()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MailboxesRemover;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SSHConnector if1 = getSsh();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        return n * 59 + getMailboxId();
    }

    public SSHConnector getSsh() {
        return ssh;
    }

    public void setSsh(SSHConnector ssh) {
        this.ssh = ssh;
    }

    public int getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(int mailboxId) {
        this.mailboxId = mailboxId;
    }

    @Override
    public String toString() {
        return "MailboxesRemover(ssh=" + getSsh() + ", mailboxId=" + getMailboxId() + ")";
    }
}
