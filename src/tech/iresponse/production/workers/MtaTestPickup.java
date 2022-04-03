package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.models.lists.Email;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.drops.MtaDropManager;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.utils.Strings;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class MtaTestPickup extends Thread {

    private MtaComponent component;
    private MtaServer server;
    private List<TestEmail> emails;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            StringBuilder pickup = new StringBuilder();
            int pickupTotal = 0;

            if (this.emails != null && !this.emails.isEmpty()) {
                Email email = MtaDropHelper.createEmailObject(this.emails.get(0));
                ServerVmta vmta = (ServerVmta)MtaTestServer.VMTAS.get(Integer.valueOf(((TestEmail)this.emails.get(0)).getComponentId()));
                String headerBody = MtaProcesses.getCurrentHeader() + "__HEADER__BODY__SEPARATOR__" + this.component.getBody();
                String autoReply = ((TestEmail)this.emails.get(0)).getAutoReplyMailbox();
                headerBody = MtaDropHelper.fixStaticTags(headerBody);
                pickup.append("XACK ON \n");
                pickup.append("XMRG FROM: <");
                pickup.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(MtaDropManager.replaceTags(this.component.getReturnPath(), this.component, vmta, this.server.name, email, autoReply, ((TestEmail)this.emails.get(0)).getPlaceholders()))));
                pickup.append(">\n");
                String open = "";
                String click = "";
                String unsub = "";
                String optout = "";
                for (TestEmail tstEmail : this.emails) {
                    email = MtaDropHelper.createEmailObject(tstEmail);
                    vmta = (ServerVmta)MtaTestServer.VMTAS.get(Integer.valueOf(tstEmail.getComponentId()));
                    autoReply = tstEmail.getAutoReplyMailbox();
                    open = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "op", this.component.getId(), "md", this.component.getOfferId(), vmta.id, 0, 0);
                    click = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "cl", this.component.getId(), "md", this.component.getOfferId(), vmta.id, 0, 0);
                    unsub = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "un", this.component.getId(), "md", this.component.getOfferId(), vmta.id, 0, 0);
                    optout = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "oop", this.component.getId(), "md", this.component.getOfferId(), vmta.id, 0, 0);
                    if (this.component.getShortDomain() != null && !"".equals(this.component.getShortDomain())) {
                        open = this.component.getShortDomain() + open;
                        click = this.component.getShortDomain() + click;
                        unsub = this.component.getShortDomain() + unsub;
                        optout = this.component.getShortDomain() + optout;
                    }
                    //MtaDropManager.createMailMerge(pickup, this.component, MtaDropHelper.createEmailObject(tstEmail), vmta, this.server.name, headerBody, open, click, unsub, optout, autoReply, tstEmail.getPlaceholders());
                    MtaDropManager.createMailMerge(pickup, this.component, email, vmta, this.server.name, headerBody, open, click, unsub, optout, autoReply, tstEmail.getPlaceholders());
                    pickupTotal++;
                }

                String[] arrayOfString = headerBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                pickup.append("XPRT 1 LAST \n");
                if (arrayOfString.length > 0) {
                    pickup.append(arrayOfString[0]);
                } else {
                    pickup.append("");
                }
                pickup.append("\n\n");
                if (arrayOfString.length > 1) {
                    // Base64 encoded
                    /*arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[first_name]", email.firstName);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[last_name]", email.lastName);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[open]", open);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[url]", click);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[unsub]", unsub);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[optout]", optout);
                    if (this.component.getShortDomain() != null && !"".equals(this.component.getShortDomain())) {
                        arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_open]", this.component.getShortDomain() + open);
                        arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_url]", this.component.getShortDomain() + click);
                        arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_unsub]", this.component.getShortDomain() + unsub);
                        arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_optout]", this.component.getShortDomain() + optout);
                    }*/
                    // End Base64 encoded
                    pickup.append(MtaDropHelper.checkContentTransferEncod(arrayOfString[1], this.component.getContentTransferEncoding()));
                } else {
                    pickup.append("");
                }
                pickup.append("\n.\n");
                ssh = Authentification.connectToServer(this.server);
                if (ssh == null || !ssh.isConnected()){
                    throw new DatabaseException("Could not connect to server : " + this.server.name + " !");
                }
                String pck = "pck_" + this.component.getId() + "_" + Strings.rndomSalt(20, false);
                ssh.uploadContent(MtaDropHelper.replaceBaseUrl(MtaDropHelper.replaceNegative(pickup.toString(), this.component.getHasNegative())), "/var/spool/iresponse/tmp/" + pck);
                if (this.component.getHasNegative()) {
                        ssh.cmd("sed -e '/p_tag_replacable_negative/{r " + this.component.getNegativeFilePath() + "' -e 'd}' /var/spool/iresponse/tmp/" + pck + " > /var/spool/iresponse/tmp/" + pck + "_neg");
                        ssh.cmd("mv /var/spool/iresponse/tmp/" + pck + "_neg /var/spool/iresponse/tmp/" + pck);
                }
                this.component.await();
                ssh.cmd("mv /var/spool/iresponse/tmp/" + pck + " /var/spool/iresponse/pickup/");
                MtaDropHelper.updateProgress(this.component.getId(), pickupTotal, 0);
            }
        } catch(Throwable t) {
            Loggers.error(t);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"component", "server", "emails"})
    public MtaTestPickup(MtaComponent component, MtaServer server, List emails) {
        this.component = component;
        this.server = server;
        this.emails = emails;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaTestPickup))
            return false;
        MtaTestPickup try1 = (MtaTestPickup)paramObject;
        if (!try1.exists(this))
            return false;
        MtaComponent cp = getComponent();
        MtaComponent cp2 = try1.getComponent();
        if ((cp == null) ? (cp2 != null) : !cp.equals(cp2))
            return false;
        MtaServer throw1 = getServer();
        MtaServer throw2 = try1.getServer();
        if ((throw1 == null) ? (throw2 != null) : !throw1.equals(throw2))
            return false;
        List list1 = getEmails();
        List list2 = try1.getEmails();
        return !((list1 == null) ? (list2 != null) : !list1.equals(list2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaTestPickup;
    }

    @Override
    public int hashCode() {
        int n = 1;
        MtaComponent cp = getComponent();
        n = n * 59 + ((cp == null) ? 43 : cp.hashCode());
        MtaServer throw1 = getServer();
        n = n * 59 + ((throw1 == null) ? 43 : throw1.hashCode());
        List list = getEmails();
        return n * 59 + ((list == null) ? 43 : list.hashCode());
    }

    public MtaComponent getComponent() {
        return component;
    }

    public void setComponent(MtaComponent component) {
        this.component = component;
    }

    public MtaServer getServer() {
        return server;
    }

    public void setServer(MtaServer server) {
        this.server = server;
    }

    public List getEmails() {
        return emails;
    }

    public void setEmails(List emails) {
        this.emails = emails;
    }

    @Override
    public String toString() {
        return "MtaTestPickup(component=" + getComponent() + ", server=" + getServer() + ", emails=" + getEmails() + ")";
    }
}
