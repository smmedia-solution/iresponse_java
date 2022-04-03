package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.lists.Email;
import tech.iresponse.models.production.AutoResponder;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.production.component.AutoResponderComponent;
import tech.iresponse.production.component.SmtpMimeMessage;
import tech.iresponse.helpers.scripts.SMTPauth;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.drops.AutoResponderHelper;
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.drops.MtaDropHelper;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Strings;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class AutoResponderExecuter extends Thread {

    private int autoResponderId;
    private DataList list;
    private int clientId;
    private int originalProcessId;
    private String originalProcessType;

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            AutoResponder autoResp = new AutoResponder(Integer.valueOf(this.autoResponderId));
            if (autoResp.getEmpty()){
                throw new DatabaseException("No auto responder found !");
            }

            if ("Inactivated".equalsIgnoreCase(autoResp.status)){
                throw new DatabaseException("This auto responder is inactivated !");
            }

            String str1 = this.list.id + "_" + this.clientId + "_" + this.originalProcessId;
            if (autoResp.clientsExcluded != null && autoResp.clientsExcluded.contains(str1)){
                throw new DatabaseException("We have already sent a response to this client !");
            }

            AutoResponderComponent autoRespCompnent = AutoResponderHelper.parse(autoResp, this.originalProcessId, this.originalProcessType);
            if (autoRespCompnent == null){
                throw new DatabaseException("Error while trying to parse this auto responder !");
            }

            String query = "SELECT id,'" + this.list.encryptEmails + "' AS encrypt_emails,'" + this.list.tableSchema + "' AS schema,'" + this.list.tableName + "' AS table,list_id,first_name,last_name,email FROM " + this.list.tableSchema + "." + this.list.tableName + " WHERE id = " + this.clientId + " AND (is_hard_bounced = 'f' OR is_hard_bounced IS NULL) AND (is_blacklisted = 'f' OR is_blacklisted IS NULL)";
            Offer ofr = null;
            if (autoResp.offerId > 0){
                ofr = new Offer(Integer.valueOf(autoResp.offerId));
            }

            if (ofr != null && !ofr.getEmpty()) {
                String suppTable = "sup_list_" + ofr.affiliateNetworkId + "_" + ofr.productionId + "_" + this.list.id;
                if (Database.get("clients").existeTable("suppressions", suppTable)){
                    query = query + " AND NOT EXISTS ( SELECT FROM suppressions." + suppTable + " WHERE email_md5 = t.email_md5 )";
                }
            }

            List<Map> list = Database.get("clients").executeQuery(query, null, 1);
            if (list == null || list.isEmpty()){
                throw new DatabaseException("No client email found !");
            }

            Email email = MtaDropHelper.createEmailMap(list.get(0));
            if (email == null || email.getEmpty()){
                throw new DatabaseException("No client email object found !");
            }

            String domain = "mta".equalsIgnoreCase(autoResp.type) ? ("[domain]".equalsIgnoreCase(autoRespCompnent.getStaticDomain()) ? (autoRespCompnent.getVmta()).domain : autoRespCompnent.getStaticDomain()) : autoRespCompnent.getStaticDomain();

            switch (autoRespCompnent.getLinkType()) {
                case "routing-bitly":
                case "attr-bitly":{
                    autoRespCompnent.setShortDomain(Bitly.shortBitly(domain) + "#");
                    break;
                }
                case "routing-gcloud":
                case "attr-gcloud":{
                    autoRespCompnent.setShortDomain(Google.shortGoogle(domain) + "#");
                    break;
                }
                case "routing-tinyurl":
                case "attr-tinyurl":{
                    autoRespCompnent.setShortDomain(Tinyurl.shortTinyurl(domain) + "#");
                    break;
                }
            }

            if ("mta".equalsIgnoreCase(autoResp.type)) {
                ssh = Authentification.connectToServer(autoRespCompnent.getMtaServer());
                if (ssh == null || !ssh.isConnected()){
                    throw new DatabaseException("Could not connect to server : " + (autoRespCompnent.getMtaServer()).name + " !");
                }

                String prefix = "root".equals(ssh.getUsername()) ? "" : "sudo";
                StringBuilder sb = new StringBuilder();
                String msgHeaderBody = autoRespCompnent.getHeader() + "__HEADER__BODY__SEPARATOR__" + autoRespCompnent.getBody();
                String open = "";
                String click = "";
                String unsub = "";
                String optout = "";
                msgHeaderBody = MtaDropHelper.fixStaticTags(msgHeaderBody);

                sb.append("XACK ON \n");
                sb.append("XMRG FROM: <reply@");
                sb.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(AutoResponderHelper.replaceTags(autoRespCompnent.getReturnPath(), autoRespCompnent, autoRespCompnent.getVmta(), (autoRespCompnent.getMtaServer()).name, email))));
                sb.append(">\n");

                open = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "op", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getVmta()).id, email.listId, email.id);
                click = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "cl", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getVmta()).id, email.listId, email.id);
                unsub = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "un", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getVmta()).id, email.listId, email.id);
                optout = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "oop", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getVmta()).id, email.listId, email.id);

                if (autoRespCompnent.getShortDomain() != null && !"".equals(autoRespCompnent.getShortDomain())) {
                    open = autoRespCompnent.getShortDomain() + open;
                    click = autoRespCompnent.getShortDomain() + click;
                    unsub = autoRespCompnent.getShortDomain() + unsub;
                    optout = autoRespCompnent.getShortDomain() + optout;
                }

                AutoResponderHelper.createMailMerge(sb, autoRespCompnent, email, autoRespCompnent.getVmta(), (autoRespCompnent.getMtaServer()).name, msgHeaderBody, open, click, unsub, optout);

                String[] msgBody = msgHeaderBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                sb.append("XPRT 1 LAST \n");
                if (msgBody.length > 0) {
                    sb.append(msgBody[0]);
                } else {
                    sb.append("");
                }

                sb.append("\n\n");

                if (msgBody.length > 1) {
                    sb.append(MtaDropHelper.checkContentTransferEncod(msgBody[1], autoRespCompnent.getContentTransferEncoding()));
                } else {
                    sb.append("");
                }

                sb.append("\n.\n");
                String pickup = "pck_" + autoRespCompnent.getOriginalProcessId() + "_" + Strings.rndomSalt(20, false);
                ssh.uploadContent(MtaDropHelper.replaceBaseUrl(MtaDropHelper.replaceNegative(sb.toString(), false)), "/var/spool/iresponse/tmp/" + pickup);
                ssh.cmd(prefix + " mv /var/spool/iresponse/tmp/" + pickup + " /var/spool/iresponse/pickup/");
            } else {
                Session session = SMTPauth.connect(autoRespCompnent.getSmtpServer(), autoRespCompnent.getSmtpUser());
                if (session == null){
                    throw new DatabaseException("Could not connect to server : " + (autoRespCompnent.getSmtpServer()).name + " !");
                }

                StringBuilder sbIdBuilder = new StringBuilder();
                String msgId = "<" + sbIdBuilder.append(sbIdBuilder.hashCode()).append('.').append(Strings.getSaltString(13, false, false, true, false)).append('.').append(System.currentTimeMillis()).append('@').append(autoRespCompnent.getStaticDomain()).toString() + ">";
                String retrnPath = AutoResponderHelper.replaceTags(autoRespCompnent.getReturnPath(), autoRespCompnent, autoRespCompnent.getSmtpUser(), (autoRespCompnent.getSmtpServer()).name, email);
                String fromName = AutoResponderHelper.replaceTags(autoRespCompnent.getFromName(), autoRespCompnent, autoRespCompnent.getSmtpUser(), (autoRespCompnent.getSmtpServer()).name, email);
                String subject = AutoResponderHelper.replaceTags(autoRespCompnent.getSubject(), autoRespCompnent, autoRespCompnent.getSmtpUser(), (autoRespCompnent.getSmtpServer()).name, email);
                String msgHeaderBody = autoRespCompnent.getHeader() + "__HEADER__BODY__SEPARATOR__" + autoRespCompnent.getBody();
                msgHeaderBody = MtaDropHelper.fixStaticTags(msgHeaderBody);
                msgHeaderBody = AutoResponderHelper.replaceTags(msgHeaderBody, autoRespCompnent, autoRespCompnent.getSmtpUser(), (autoRespCompnent.getSmtpServer()).name, email);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[return_path]", retrnPath);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[from_name]", fromName);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[subject]", subject);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[mail_date]", (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")).format(new Date()));
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[message_id]", msgId);

                String open = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "op", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getSmtpUser()).id, email.listId, email.id);
                String click = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "cl", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getSmtpUser()).id, email.listId, email.id);
                String unsub = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "un", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getSmtpUser()).id, email.listId, email.id);
                String optout = MtaLinkRouting.createLinkRouting(autoRespCompnent.getLinkType(), "oop", autoRespCompnent.getOriginalProcessId(), autoRespCompnent.getOriginalProcessType(), autoRespCompnent.getOfferId(), (autoRespCompnent.getSmtpUser()).id, email.listId, email.id);

                if (autoRespCompnent.getShortDomain() != null && !"".equals(autoRespCompnent.getShortDomain())) {
                    open = autoRespCompnent.getShortDomain() + open;
                    click = autoRespCompnent.getShortDomain() + click;
                    unsub = autoRespCompnent.getShortDomain() + unsub;
                    optout = autoRespCompnent.getShortDomain() + optout;
                }

                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[open]", open);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[url]", click);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[unsub]", unsub);
                msgHeaderBody = StringUtils.replace(msgHeaderBody, "[optout]", optout);
                msgHeaderBody = MtaDropHelper.replaceUrlImage(MtaDropHelper.replaceRandomTags(msgHeaderBody), autoRespCompnent.getStaticDomain());

                String[] arrayOfString = msgHeaderBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
                String str13 = (arrayOfString.length > 0) ? arrayOfString[0] : "";

                SmtpMimeMessage mimMsg = new SmtpMimeMessage(session, new ByteArrayInputStream(str13.getBytes()));
                mimMsg.setRecipients(MimeMessage.RecipientType.TO, (Address[])InternetAddress.parse(email.email));
                mimMsg.setSentDate(new Date());
                String str14 = (arrayOfString.length > 1) ? arrayOfString[1] : "";
                mimMsg.setContent(str14, autoRespCompnent.getContentType());
                Transport.send((Message)mimMsg);
            }

            query = "UPDATE production.auto_responders SET clients_excluded = TRIM(BOTH ',' FROM (COALESCE(clients_excluded || ' ', '') || '," + str1 + "')) WHERE (clients_excluded IS NULL OR NOT ('" + str1 + "' = ANY(string_to_array(clients_excluded,',')))) AND id = " + this.autoResponderId;
            Database.get("system").executeUpdate(query, null, 0);

        } catch (Throwable th) {
            Loggers.error(th);
        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
    }

    @ConstructorProperties({"autoResponderId", "list", "clientId", "originalProcessId", "originalProcessType"})
    public AutoResponderExecuter(int autoResponderId, DataList list, int clientId, int originalProcessId, String originalProcessType) {
        this.autoResponderId = autoResponderId;
        this.list = list;
        this.clientId = clientId;
        this.originalProcessId = originalProcessId;
        this.originalProcessType = originalProcessType;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AutoResponderExecuter))
            return false;
        AutoResponderExecuter if1 = (AutoResponderExecuter)paramObject;
        if (!if1.exists(this))
            return false;
        if (getAutoResponderId() != if1.getAutoResponderId())
            return false;
        DataList for1 = getList();
        DataList for2 = if1.getList();
        if ((for1 == null) ? (for2 != null) : !for1.equals(for2))
            return false;
        if (getClientId() != if1.getClientId())
            return false;
        if (getOriginalProcessId() != if1.getOriginalProcessId())
            return false;
        String str1 = getOriginalProcessType();
        String str2 = if1.getOriginalProcessType();
            return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AutoResponderExecuter;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getAutoResponderId();
        DataList for1 = getList();
        n = n * 59 + ((for1 == null) ? 43 : for1.hashCode());
        n = n * 59 + getClientId();
        n = n * 59 + getOriginalProcessId();
        String str = getOriginalProcessType();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getAutoResponderId() {
        return autoResponderId;
    }

    public void setAutoResponderId(int autoResponderId) {
        this.autoResponderId = autoResponderId;
    }

    public DataList getList() {
        return list;
    }

    public void setList(DataList list) {
        this.list = list;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getOriginalProcessId() {
        return originalProcessId;
    }

    public void setOriginalProcessId(int originalProcessId) {
        this.originalProcessId = originalProcessId;
    }

    public String getOriginalProcessType() {
        return originalProcessType;
    }

    public void setOriginalProcessType(String originalProcessType) {
        this.originalProcessType = originalProcessType;
    }

    @Override
    public String toString() {
        return "AutoResponderExecuter(autoResponderId=" + getAutoResponderId() + ", list=" + getList() + ", clientId=" + getClientId() + ", originalProcessId=" + getOriginalProcessId() + ", originalProcessType=" + getOriginalProcessType() + ")";
    }
}
