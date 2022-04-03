package tech.iresponse.production.drops;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.models.production.AutoResponder;
import tech.iresponse.models.lists.Email;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.component.AutoResponderComponent;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.Url;
import tech.iresponse.core.Application;

public class AutoResponderHelper {

    public static AutoResponderComponent parse(AutoResponder autoResp, int originalProcessId, String originalProcessType) throws Exception {
        AutoResponderComponent autoRespCompnent = new AutoResponderComponent();
        autoRespCompnent.setContent(Crypto.Base64Decode(autoResp.content));

        JSONObject jSONObject = JsonUtils.toJSON(autoRespCompnent.getContent());
        if (jSONObject == null){
            throw new DatabaseException("Mta process has no content !");
        }

        autoRespCompnent.setId(autoResp.id);
        autoRespCompnent.setProcessType(autoResp.type);
        autoRespCompnent.setOriginalProcessId(originalProcessId);
        autoRespCompnent.setOriginalProcessType(originalProcessType);
        autoRespCompnent.setServerId(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "server-id", "0")));
        autoRespCompnent.setComponentId(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "component-id", "0")));
        autoRespCompnent.setHasPtrTag(autoRespCompnent.getContent().contains("[ptr]"));

        if ("mta".equals(autoRespCompnent.getProcessType())) {
            autoRespCompnent.setMtaServer(new MtaServer(Integer.valueOf(autoRespCompnent.getServerId())));
            if (autoRespCompnent.getMtaServer() == null || autoRespCompnent.getMtaServer().getEmpty()){
                throw new DatabaseException("Mta sever not found !");
            }

            autoRespCompnent.setVmta(new ServerVmta(Integer.valueOf(autoRespCompnent.getComponentId())));
            if (autoRespCompnent.getVmta() == null || autoRespCompnent.getVmta().getEmpty()){
                throw new DatabaseException("Vmta not found !");
            }
        } else {
            autoRespCompnent.setSmtpServer(new SmtpServer(Integer.valueOf(autoRespCompnent.getServerId())));
            if (autoRespCompnent.getSmtpServer() == null || autoRespCompnent.getSmtpServer().getEmpty()){
                throw new DatabaseException("Smtp sever not found !");
            }

            autoRespCompnent.setSmtpUser(new SmtpUser(Integer.valueOf(autoRespCompnent.getComponentId())));
            if (autoRespCompnent.getSmtpUser() == null || autoRespCompnent.getSmtpUser().getEmpty()){
                throw new DatabaseException("Smtp user not found !");
            }
        }

        autoRespCompnent.setOfferId(autoResp.offerId);
        autoRespCompnent.setFromName(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "from-name", "")));
        autoRespCompnent.setSubject(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "subject", "")));
        autoRespCompnent.setHeader(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "header", "")));
        autoRespCompnent.setReturnPath(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "return-path", "return@[rdns]")).trim().replace("\n", "").replace("\r", ""));
        autoRespCompnent.setCharset(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-charset", "UTF-8")).trim().replace("\n", "").replace("\r", ""));
        autoRespCompnent.setContentTransferEncoding(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-content-transfert-encoding", "7bit")).trim().replace("\n", "").replace("\r", ""));
        autoRespCompnent.setContentType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-content-type", "text/html")).trim().replace("\n", "").replace("\r", ""));
        autoRespCompnent.setStaticDomain(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "static-domain", "")).trim().replace("\n", "").replace("\r", ""));
        autoRespCompnent.setLinkType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "link-type", "routing")));

        String body = String.valueOf(JsonUtils.getValueFromJson(jSONObject, "body", "")).trim();
        if (body != null && !"".equals(body) && MtaDropHelper.checkLinkRouting(autoRespCompnent.getLinkType())) {
            Document doc = Jsoup.parse(body);
            Elements elements = doc.select("img");
            for (Element element : elements) {
                String srcLink = String.valueOf(element.attr("src")).trim();
                if (srcLink != null && srcLink.contains("http")) {
                    String srcShort = srcLink;
                    switch (autoRespCompnent.getLinkType()) {
                        case "routing-bitly":
                        case "attr-bitly":{
                            srcShort = Bitly.shortBitly(srcLink);
                            break;
                        }
                        case "routing-gcloud":
                        case "attr-gcloud":{
                            srcShort = Google.shortGoogle(srcLink);
                            break;
                        }
                        case "routing-tinyurl":
                        case "attr-tinyurl":{
                            srcShort = Tinyurl.shortTinyurl(srcLink);
                            break;
                        }
                    }
                    body = StringUtils.replace(body, srcLink, srcShort);
                }
            }
        }

        autoRespCompnent.setBody(body);
        return autoRespCompnent;
    }

    public static String replaceTags(String value, AutoResponderComponent component, ServerVmta vmta, String mtaServ, Email email) {
        if (value != null && !"".equals(value) && value.contains("[") && value.contains("]")) {
            value = StringUtils.replace(value, "[process_id]", String.valueOf(component.getOriginalProcessId()));
            value = StringUtils.replace(value, "[mailer_id]", String.valueOf((Application.checkAndgetInstance().getUser()).productionId));
            value = StringUtils.replace(value, "[ip]", vmta.ip);
            if (component.isHasPtrTag()){
                value = StringUtils.replace(value, "[ptr]", MtaDropHelper.getPtrRcrd(vmta.ip));
            }
            value = StringUtils.replace(value, "[static_domain]", "[domain]".equalsIgnoreCase(component.getStaticDomain()) ? vmta.domain : component.getStaticDomain());
            value = StringUtils.replace(value, "[server]", mtaServ);
            value = StringUtils.replace(value, "[rdns]", vmta.domain);
            value = StringUtils.replace(value, "[domain]", Url.checkUrl(vmta.domain));
            value = StringUtils.replace(value, "[custom_domain]", "");
            if (email != null) {
                value = StringUtils.replace(value, "[email_id]", String.valueOf(email.id));
                value = StringUtils.replace(value, "[list_id]", String.valueOf(email.listId));
                value = StringUtils.replace(value, "[email]", email.email);
                value = StringUtils.replace(value, "[first_name]", email.firstName);
                value = StringUtils.replace(value, "[last_name]", email.lastName);
            }
        }
        return value;
    }

    public static String replaceTags(String value, AutoResponderComponent component, SmtpUser smtpusr, String mtaServ, Email email) {
        if (value != null && !"".equals(value) && value.contains("[") && value.contains("]")) {
            value = StringUtils.replace(value, "[process_id]", String.valueOf(component.getId()));
            value = StringUtils.replace(value, "[mailer_id]", String.valueOf((Application.checkAndgetInstance().getUser()).productionId));
            value = StringUtils.replace(value, "[SmtpUser]", smtpusr.username);
            value = StringUtils.replace(value, "[rdns]", component.getStaticDomain());
            value = StringUtils.replace(value, "[domain]", component.getStaticDomain());
            value = StringUtils.replace(value, "[custom_domain]", component.getStaticDomain());
            value = StringUtils.replace(value, "[ip]", component.getStaticDomain());
            value = StringUtils.replace(value, "[static_domain]", component.getStaticDomain());
            value = StringUtils.replace(value, "[server]", mtaServ);
            if (email != null) {
                value = StringUtils.replace(value, "[email_id]", String.valueOf(email.id));
                value = StringUtils.replace(value, "[list_id]", String.valueOf(email.listId));
                value = StringUtils.replace(value, "[email]", email.email);
                value = StringUtils.replace(value, "[first_name]", email.firstName);
                value = StringUtils.replace(value, "[last_name]", email.lastName);
            }
        }
        return value;
    }

    public static void createMailMerge(StringBuilder pickup, AutoResponderComponent component, Email email, ServerVmta vmta, String serverName, String msgHeaderBody, String open, String click, String unsub, String optout) {
        if (component != null && email != null && !"".equals(email.email.trim()) && vmta != null) {
            StringBuilder mailMerge = new StringBuilder();
            StringBuilder messageIdBuilder = new StringBuilder();
            String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.rndomSalt(13, true)).append('.').append(System.currentTimeMillis()).append('@').append(vmta.domain).toString() + ">";
            mailMerge.append("XDFN ");
            mailMerge.append("rcpt=\"");
            mailMerge.append(email.email);
            mailMerge.append("\" ");
            mailMerge.append("mail_date=\"");
            mailMerge.append((new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")).format(new Date()));
            mailMerge.append("\" ");
            mailMerge.append("message_id=\"");
            mailMerge.append(messageId);
            mailMerge.append("\" ");
            mailMerge.append("ip=\"");
            mailMerge.append(vmta.ip);
            mailMerge.append("\" ");
            if (component.isHasPtrTag()) {
                mailMerge.append("ptr=\"");
                mailMerge.append(MtaDropHelper.getPtrRcrd(vmta.ip));
                mailMerge.append("\" ");
            }
            mailMerge.append("vmta_name=\"");
            mailMerge.append(vmta.name);
            mailMerge.append("\" ");
            mailMerge.append("rdns=\"");
            mailMerge.append(vmta.domain);
            mailMerge.append("\" ");
            mailMerge.append("domain=\"");
            mailMerge.append(Url.checkUrl(vmta.domain));
            mailMerge.append("\" ");
            mailMerge.append("server=\"");
            mailMerge.append(serverName);
            mailMerge.append("\" ");
            mailMerge.append("mailer_id=\"");
            mailMerge.append((Application.checkAndgetInstance().getUser()).productionId);
            mailMerge.append("\" ");
            mailMerge.append("process_id=\"");
            mailMerge.append(component.getOriginalProcessId());
            mailMerge.append("\" ");
            mailMerge.append("list_id=\"");
            mailMerge.append(email.listId);
            mailMerge.append("\" ");
            mailMerge.append("email_id=\"");
            mailMerge.append(email.id);
            mailMerge.append("\" ");
            mailMerge.append("email=\"");
            mailMerge.append(email.email);
            mailMerge.append("\" ");
            mailMerge.append("first_name=\"");
            mailMerge.append(email.firstName);
            mailMerge.append("\" ");
            mailMerge.append("last_name=\"");
            mailMerge.append(email.lastName);
            mailMerge.append("\" ");
            mailMerge.append("return_path=\"");
            mailMerge.append(component.getReturnPath());
            mailMerge.append("\" ");
            mailMerge.append("from_name=\"");
            mailMerge.append(component.getFromName());
            mailMerge.append("\" ");
            mailMerge.append("subject=\"");
            mailMerge.append(component.getSubject());
            mailMerge.append("\" ");
            mailMerge.append("content_transfer_encoding=\"");
            mailMerge.append(component.getContentTransferEncoding());
            mailMerge.append("\" ");
            mailMerge.append("content_type=\"");
            mailMerge.append(component.getContentType());
            mailMerge.append("\" ");
            mailMerge.append("charset=\"");
            mailMerge.append(component.getCharset());
            mailMerge.append("\" ");
            mailMerge.append("static_domain=\"");
            mailMerge.append("[domain]".equalsIgnoreCase(component.getStaticDomain()) ? vmta.domain : component.getStaticDomain());
            mailMerge.append("\" ");
            if (component.getShortDomain() != null && !"".equals(component.getShortDomain())) {
                mailMerge.append("short_open=\"");
                mailMerge.append(open);
                mailMerge.append("\" ");
                mailMerge.append("short_url=\"");
                mailMerge.append(click);
                mailMerge.append("\" ");
                mailMerge.append("short_unsub=\"");
                mailMerge.append(unsub);
                mailMerge.append("\" ");
                mailMerge.append("short_optout=\"");
                mailMerge.append(optout);
                mailMerge.append("\" ");
            } else {
                mailMerge.append("open=\"");
                mailMerge.append(open);
                mailMerge.append("\" ");
                mailMerge.append("url=\"");
                mailMerge.append(click);
                mailMerge.append("\" ");
                mailMerge.append("unsub=\"");
                mailMerge.append(unsub);
                mailMerge.append("\" ");
                mailMerge.append("optout=\"");
                mailMerge.append(optout);
                mailMerge.append("\" ");
            }
            MtaDropHelper.replaceRandomTags(mailMerge, msgHeaderBody);
            mailMerge.append("\n");
            mailMerge.append("XDFN *vmta=\"");
            mailMerge.append(vmta.name);
            mailMerge.append("\" *jobId=\"");
            mailMerge.append(component.getOriginalProcessType()).append("_").append((Application.checkAndgetInstance().getUser()).productionId);
            mailMerge.append("\" *from=\"");
            mailMerge.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(replaceTags(component.getReturnPath(), component, vmta, serverName, email))));
            mailMerge.append("\" *envId=\"");
            mailMerge.append(component.getOriginalProcessId());
            mailMerge.append("_");
            mailMerge.append(vmta.id);
            mailMerge.append("_");
            mailMerge.append(email.id);
            mailMerge.append("_");
            mailMerge.append(email.listId);
            mailMerge.append("\"\n");
            mailMerge.append("RCPT TO:<");
            mailMerge.append(email.email);
            mailMerge.append(">");
            mailMerge.append("\n");
            pickup.append(replaceTags(mailMerge.toString(), component, vmta, serverName, email));
        }
    }
}
