package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.models.lists.Email;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.drops.MtaDropManager;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.utils.Strings;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class MtaDropPickup extends Thread {

    private MtaComponent component;
    private MtaServer server;
    private List<LinkedHashMap<String, Object>> emails;
    private List<ServerVmta> vmtas;

    public void run() {
        SSHConnector ssh = null;
        try {
            StringBuilder pickup = new StringBuilder();
            int pickupTotal = 0;
            int emailCount = 0;
            int vmtaSize = this.vmtas.size();
            if (this.emails != null && !this.emails.isEmpty() && this.vmtas != null && !this.vmtas.isEmpty() && vmtaSize == this.emails.size()) {
                ServerVmta servVmta = this.vmtas.get(pickupTotal);
                Email email = MtaDropHelper.createEmailMap(this.emails.get(0));
                String msgHeaderBody = MtaProcesses.getCurrentHeader() + "__HEADER__BODY__SEPARATOR__" + this.component.getBody();
                String autoReply = this.component.getAutoReplyActivated() ? MtaProcesses.getCurrentAutoReply() : "";
                String open = "";
                String click = "";
                String unsub = "";
                String optout = "";
                msgHeaderBody = MtaDropHelper.fixStaticTags(msgHeaderBody);
                List<String> placeHolders = null;
                if (this.component.getPlaceholders() != null && !this.component.getPlaceholders().isEmpty()) {
                    int k = this.component.getPlaceholders().size();
                    placeHolders = new ArrayList(k);
                    for (int b1 = 0; b1 < k; b1++){
                        placeHolders.add(MtaProcesses.getCurrentPlaceHolder(b1));
                    }
                }

                pickup.append("XACK ON \n");
                pickup.append("XMRG FROM: <");
                pickup.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(MtaDropManager.replaceTags(this.component.getReturnPath(), this.component, servVmta, this.server.name, email, autoReply, placeHolders))));
                //pickup.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(MtaDropManager.replaceTags(this.component.getReturnPath(), this.component, servVmta, this.server.name, this.emails.get(0), autoReply, placeHolders))));
                pickup.append(">\n");
                //for (Map row : this.emails) {
                for (LinkedHashMap<String, Object> row : this.emails) {
                    email = MtaDropHelper.createEmailMap(row);
                    open = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "op", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, email.listId, email.id);
                    click = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "cl", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, email.listId, email.id);
                    unsub = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "un", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, email.listId, email.id);
                    optout = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "oop", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, email.listId, email.id);
                    if (this.component.getShortDomain() != null && !"".equals(this.component.getShortDomain())) {
                        open = this.component.getShortDomain() + open;
                        click = this.component.getShortDomain() + click;
                        unsub = this.component.getShortDomain() + unsub;
                        optout = this.component.getShortDomain() + optout;
                    }
                    MtaDropManager.createMailMerge(pickup, this.component, email, servVmta, this.server.name, msgHeaderBody, open, click, unsub, optout, autoReply);
                    this.component.updateVmtasTotals(servVmta.id);
                    emailCount = MtaProcesses.updateCounter();
                    if (emailCount > 0 && emailCount % this.component.getTestAfter() == 0 && this.component.getTestEmails() != null && (this.component.getTestEmails()).length > 0) {
                        String openTestEmail = "";
                        String clickTestEmail = "";
                        String unsubTestEmail = "";
                        String optoutTestEmail = "";
                        for (String str : this.component.getTestEmails()) {
                            email = new Email();
                            email.setSchema("");
                            email.setTable("");
                            email.email = str.trim();
                            email.firstName = str.trim().split(Pattern.quote("@"))[0];
                            email.lastName = email.firstName;
                            openTestEmail = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "op", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, 0, 0);
                            clickTestEmail = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "cl", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, 0, 0);
                            unsubTestEmail = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "un", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, 0, 0);
                            optoutTestEmail = MtaLinkRouting.createLinkRouting(this.component.getLinkType(), "oop", this.component.getId(), "md", this.component.getOfferId(), servVmta.id, 0, 0);
                            if (this.component.getShortDomain() != null && !"".equals(this.component.getShortDomain())) {
                                openTestEmail = this.component.getShortDomain() + openTestEmail;
                                clickTestEmail = this.component.getShortDomain() + clickTestEmail;
                                unsubTestEmail = this.component.getShortDomain() + unsubTestEmail;
                                optoutTestEmail = this.component.getShortDomain() + optoutTestEmail;
                            }
                            MtaDropManager.createMailMerge(pickup, this.component, email, servVmta, this.server.name, msgHeaderBody, openTestEmail, clickTestEmail, unsubTestEmail, optoutTestEmail, autoReply);
                        }
                    }
                    if (++pickupTotal < vmtaSize){
                        servVmta = this.vmtas.get(pickupTotal);
                    }
                }
                String[] msgBody = msgHeaderBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                pickup.append("XPRT 1 LAST \n");
                if (msgBody.length > 0) {
                    pickup.append(msgBody[0]);
                } else {
                    pickup.append("");
                }
                pickup.append("\n\n");
                if (msgBody.length > 1) {
                    pickup.append(MtaDropHelper.checkContentTransferEncod(msgBody[1], this.component.getContentTransferEncoding()));
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
        } catch (Throwable t) {
            Loggers.error(t);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"component", "server", "emails", "vmtas"})
    public MtaDropPickup(MtaComponent component, MtaServer server, List<LinkedHashMap<String, Object>> emails, List<ServerVmta> vmtas) {
        this.component = component;
        this.server = server;
        this.emails = emails;
        this.vmtas = vmtas;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaDropPickup))
            return false;
        MtaDropPickup int1 = (MtaDropPickup)paramObject;
        if (!int1.exists(this))
            return false;
        MtaComponent if1 = getComponent();
        MtaComponent if2 = int1.getComponent();
        if ((if1 == null) ? (if2 != null) : !if1.equals(if2))
            return false;
        MtaServer throw1 = getServer();
        MtaServer throw2 = int1.getServer();
        if ((throw1 == null) ? (throw2 != null) : !throw1.equals(throw2))
            return false;
        List list1 = getEmails();
        List list2 = int1.getEmails();
        if ((list1 == null) ? (list2 != null) : !list1.equals(list2))
        return false;
        List list3 = getVmtas();
        List list4 = int1.getVmtas();
        return !((list3 == null) ? (list4 != null) : !list3.equals(list4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaDropPickup;
    }

    @Override
    public int hashCode() {
        int n  = 1;
        MtaComponent if1 = getComponent();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        MtaServer throw1 = getServer();
        n = n * 59 + ((throw1 == null) ? 43 : throw1.hashCode());
        List<LinkedHashMap<String, Object>> list1 = getEmails();
        n = n * 59 + ((list1 == null) ? 43 : list1.hashCode());
        List<ServerVmta> list2 = getVmtas();
        return n * 59 + ((list2 == null) ? 43 : list2.hashCode());
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

    public List<LinkedHashMap<String, Object>> getEmails() {
        return emails;
    }

    public void setEmails(List<LinkedHashMap<String, Object>> emails) {
        this.emails = emails;
    }

    public List<ServerVmta> getVmtas() {
        return vmtas;
    }

    public void setVmtas(List<ServerVmta> vmtas) {
        this.vmtas = vmtas;
    }

    @Override
    public String toString() {
        return "MtaDropPickup(component=" + getComponent() + ", server=" + getServer() + ", emails=" + getEmails() + ", vmtas=" + getVmtas() + ")";
    }
}
