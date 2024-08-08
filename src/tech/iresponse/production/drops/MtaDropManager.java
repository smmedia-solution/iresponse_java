package tech.iresponse.production.drops;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.production.MtaProcessIp;
import tech.iresponse.models.production.MtaProcess;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.models.lists.Email;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.Url;
import tech.iresponse.core.Application;

public class MtaDropManager {

    public static MtaComponent parse(MtaProcess mtaProcess) throws Exception{

        MtaComponent dropComponent = new MtaComponent();
        dropComponent.setContent(Crypto.Base64Decode(mtaProcess.content));
        JSONObject jsonDrop = JsonUtils.toJSON(dropComponent.getContent());
        if (jsonDrop == null){
            throw new DatabaseException("Mta process has no content !");
        }

        dropComponent.setId(mtaProcess.id);
        dropComponent.setStatus(mtaProcess.status);
        dropComponent.setProcessType(mtaProcess.processType);
        dropComponent.setTestThreads(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "test-threads", "multithread-servers")));

        //(new String[1])[0] = String.valueOf(TypesParser.safeParseInt(mtaProcess.serversIds.trim()));
        //dropComponent.setMtaServersIds(mtaProcess.serversIds.contains(",") ? mtaProcess.serversIds.trim().split(Pattern.quote(",")) : new String[1]);
        dropComponent.setMtaServersIds(mtaProcess.serversIds.contains(",") ? mtaProcess.serversIds.trim().split(Pattern.quote(",")) : new String[]{String.valueOf(TypesParser.safeParseInt(mtaProcess.serversIds.trim()))});
        dropComponent.setVmtasIds(new String[]{});

        if (JsonUtils.jsonContainsArray(jsonDrop, "selected-vmtas")) {
            JSONArray jSONArray = jsonDrop.getJSONArray("selected-vmtas");
            for (int b = 0; b < jSONArray.length(); b++){
                dropComponent.setVmtasIds((String[])ArrayUtils.add((Object[])dropComponent.getVmtasIds(), String.valueOf(jSONArray.getString(b))));
            }
        }

        dropComponent.setVmtasRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "vmta-rotation", "1")));
        dropComponent.setVmtasType(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "vmta-send-type", "default-vmtas")));
        dropComponent.setHasPtrTag(dropComponent.getContent().contains("[ptr]"));
        dropComponent.setEmailsProcessType(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "vmtas-emails-process", "vmtas-rotation")));
        dropComponent.setNumberOfEmails(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "number-of-emails", "1")));
        dropComponent.setEmailsPeriodValue(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "emails-period-value", "1")));
        dropComponent.setEmailsPeriodUnit(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "emails-period-type", "milliseconds")));
        dropComponent.setBatch(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "batch", "1")));
        dropComponent.setXdelay(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "x-delay", "1000")));

        if ("emails-per-period".equalsIgnoreCase(dropComponent.getEmailsProcessType())) {
            dropComponent.setBatch(1);
            switch (dropComponent.getEmailsPeriodUnit()) {
                case "seconds":
                    dropComponent.setEmailsPeriodValue(dropComponent.getEmailsPeriodValue() * 1000);
                    break;
                case "minutes":
                    dropComponent.setEmailsPeriodValue(dropComponent.getEmailsPeriodValue() * 60 * 1000);
                    break;
                case "hours":
                    dropComponent.setEmailsPeriodValue(dropComponent.getEmailsPeriodValue() * 60 * 60 * 1000);
                    break;
            }
            if (dropComponent.getEmailsPeriodValue() == 0) {
                dropComponent.setXdelay(0L);
            } else {
                dropComponent.setXdelay((int)Math.ceil((dropComponent.getEmailsPeriodValue() / dropComponent.getNumberOfEmails())));
            }
        }

        dropComponent.setOfferId(mtaProcess.offerId);
        dropComponent.setFromName(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "from-name", "")));
        dropComponent.setSubject(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "subject", "")));

        dropComponent.setHeaders(new String[]{});
        if (JsonUtils.jsonContainsArray(jsonDrop, "headers")) {
            JSONArray jSONheaders = jsonDrop.getJSONArray("headers");
            for (int b = 0; b < jSONheaders.length(); b++){
                dropComponent.setHeaders((String[])ArrayUtils.add((Object[])dropComponent.getHeaders(), String.valueOf(jSONheaders.get(b)).trim()));
            }
        }
        dropComponent.setHeadersRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "headers-rotation", "1")));
        dropComponent.setReturnPath(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "return-path", "return@[rdns]")).trim().replace("\n", "").replace("\r", ""));
        dropComponent.setTrackOpens(!"disabled".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "track-opens", "disabled"))));
        dropComponent.setCharset(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "creative-charset", "UTF-8")).trim().replace("\n", "").replace("\r", ""));
        dropComponent.setContentTransferEncoding(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "creative-content-transfert-encoding", "7bit")).trim().replace("\n", "").replace("\r", ""));
        dropComponent.setContentType(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "creative-content-type", "text/html")).trim().replace("\n", "").replace("\r", ""));
        dropComponent.setStaticDomain(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "static-domain", "")).trim().replace("\n", "").replace("\r", ""));
        dropComponent.setLinkType(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "link-type", "routing")));

        String strBody = String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "body", "")).trim();
        if (strBody != null && !"".equals(strBody) && MtaDropHelper.checkLinkRouting(dropComponent.getLinkType())) {
            Document document = Jsoup.parse(strBody);
            Elements elements = document.select("img");
            for (Element element : elements) {
                String src = String.valueOf(element.attr("src")).trim();
                if (src != null && src.contains("http")) {
                    String srcLink = src;
                    switch (dropComponent.getLinkType()) {
                        case "routing-bitly":
                        case "attr-bitly":
                            srcLink = Bitly.shortBitly(src);
                            break;
                        case "routing-gcloud":
                        case "attr-gcloud":
                            srcLink = Google.shortUrlG(src);
                            break;
                        case "routing-tinyurl":
                        case "attr-tinyurl":
                            srcLink = Tinyurl.shortTinyurl(src);
                            break;
                    }
                    strBody = StringUtils.replace(strBody, src, srcLink);
                }
            }
        }
        dropComponent.setBody(strBody);
        dropComponent.setNegativeFilePath(mtaProcess.negativeFilePath);
        dropComponent.setHasNegative((dropComponent.getContent().contains("[negative]") && !"".equals(dropComponent.getNegativeFilePath())));
        if (jsonDrop.has("placeholders")) {
            JSONArray jSONArray = jsonDrop.getJSONArray("placeholders");
            if (jSONArray.length() > 0) {
                ArrayList<String[]> arrayListplcholder = new ArrayList();
                for (int b = 0; b < jSONArray.length(); b++) {
                    String plceholder = "";
                    String[] arrayOfString1 = String.valueOf(jSONArray.getString(b)).split("\\r?\\n");
                    String[] arrayOfString2 = new String[0];
                    for (String str2 : arrayOfString1) {
                        plceholder = str2.replaceAll("\n", "").replaceAll("\r", "").trim();
                        if (!"".equals(plceholder))
                        arrayOfString2 = (String[])ArrayUtils.add((Object[])arrayOfString2, plceholder);
                    }
                    arrayListplcholder.add(arrayOfString2);
                }
                dropComponent.setPlaceholders(arrayListplcholder);
            }
        }
        if (jsonDrop.has("placeholders-rotations")) {
            JSONArray jSONArray = jsonDrop.getJSONArray("placeholders-rotations");
            if (jSONArray.length() > 0) {
                ArrayList<Integer> arrayListPlcholdeRotation = new ArrayList();
                for (int b = 0; b < jSONArray.length(); b++){
                    arrayListPlcholdeRotation.add(Integer.valueOf(TypesParser.safeParseInt(jSONArray.get(b))));
                }
                dropComponent.setPlaceholdersRotations(arrayListPlcholdeRotation);
            }
        } else if (dropComponent.getPlaceholders() != null && !dropComponent.getPlaceholders().isEmpty()) {
            ArrayList<Integer> arrayList = new ArrayList();
            int i = dropComponent.getPlaceholders().size();
            for (int b = 0; b < i; b++){
                arrayList.add(Integer.valueOf(1));
            }
            dropComponent.setPlaceholdersRotations(arrayList);
        }
        if (jsonDrop.has("placeholders-combinations")) {
            JSONArray jSONArray = jsonDrop.getJSONArray("placeholders-combinations");
            if (jSONArray.length() > 0) {
                ArrayList<Boolean> arrayListplcholderCombinations = new ArrayList();
                for (int b = 0; b < jSONArray.length(); b++){
                    arrayListplcholderCombinations.add(Boolean.valueOf("true".equalsIgnoreCase(String.valueOf(jSONArray.get(b)))));
                }
                dropComponent.setPlaceholdersCombinations(arrayListplcholderCombinations);
            }
        } else if (dropComponent.getPlaceholders() != null && !dropComponent.getPlaceholders().isEmpty()) {
            ArrayList<Boolean> arrayList = new ArrayList();
            int i = dropComponent.getPlaceholders().size();
            for (int b = 0; b < i; b++){
                arrayList.add(Boolean.valueOf(false));
            }
            dropComponent.setPlaceholdersCombinations(arrayList);
        }
        dropComponent.setTestAfter(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "test-after", "1000")));
        dropComponent.setTestRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "rcpt-rotation", "1")));
        dropComponent.setTestEmailsCombination("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "rcpt-combination", "off"))));
        dropComponent.setTestEmails(new String[]{});
        if (jsonDrop.has("rcpts")) {
            String rcpts = "";
            String[] arrayOfString = String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "rcpts", "")).split("\\r?\\n");
            for (String str2 : arrayOfString) {
                rcpts = str2.replaceAll("\n", "").replaceAll("\r", "").trim();
                if (!"".equals(rcpts) && rcpts.contains("@")){
                    dropComponent.setTestEmails((String[])ArrayUtils.add((Object[])dropComponent.getTestEmails(), rcpts));
                }
            }
        }

        dropComponent.setAutoReplyActivated("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "auto-reply-activated", "off"))));
        if (dropComponent.getAutoReplyActivated()) {
            dropComponent.setAutoReplyRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "auto-reply-rotation", "1")));
            dropComponent.setAutoReplyMailboxes(new String[]{});
            if (jsonDrop.has("auto-reply-mailboxes")) {
                String str1 = "";
                String[] arrayOfString = String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "auto-reply-mailboxes", "")).split("\\r?\\n");
                for (String str2 : arrayOfString) {
                    str1 = str2.replaceAll("\n", "").replaceAll("\r", "").trim();
                    if (!"".equals(str1) && str1.contains("@")){
                        dropComponent.setAutoReplyMailboxes((String[])ArrayUtils.add((Object[])dropComponent.getAutoReplyMailboxes(), str1));
                    }
                }
            }
        }
        if ("drop".equals(dropComponent.getProcessType())) {
            dropComponent.setSplitEmailsType(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "emails-split-type", "servers")));
            dropComponent.setIspId(mtaProcess.ispId);
            dropComponent.setDataStart(mtaProcess.dataStart);
            dropComponent.setDataDuplicate(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jsonDrop, "data-duplicate", "1")));
            dropComponent.setDataCount(mtaProcess.dataCount * dropComponent.getDataDuplicate());
            dropComponent.setTotalEmails(mtaProcess.totalEmails);
            //(new String[1])[0] = String.valueOf(TypesParser.safeParseInt(mtaProcess.lists.trim()));
            //String[] arrayOfLists = mtaProcess.lists.contains(",") ? mtaProcess.lists.trim().split(Pattern.quote(",")) : new String[1];
            String[] arrayOfLists = mtaProcess.lists.contains(",") ? mtaProcess.lists.trim().split(Pattern.quote(",")) : new String[]{String.valueOf(TypesParser.safeParseInt(mtaProcess.lists.trim()))};

            if (arrayOfLists.length == 0){
                throw new DatabaseException("This drop has no lists selected !");
            }
            List<DataList> list = (List)DataList.all(DataList.class, "id IN (" + String.join(",", (CharSequence[])arrayOfLists) + ")", null);
            if (list.isEmpty()){
                throw new DatabaseException("This drop has no lists selected !");
            }
            StringBuilder stringBuilder1 = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            StringBuilder stringBuilder3 = new StringBuilder();
            boolean bool = false;
            stringBuilder2.append(" WHERE (is_hard_bounced = 'f' OR is_hard_bounced IS NULL) AND (is_blacklisted = 'f' OR is_blacklisted IS NULL)");
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "seeds-filter", "off")))) {
                stringBuilder3.append("is_seed = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "fresh-filter", "off")))) {
                stringBuilder3.append("is_fresh = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "clean-filter", "off")))) {
                stringBuilder3.append("is_clean = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "openers-filter", "off")))) {
                stringBuilder3.append("is_opener = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "clickers-filter", "off")))) {
                stringBuilder3.append("is_clicker = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "leaders-filter", "off")))) {
                stringBuilder3.append("is_leader = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "unsubs-filter", "off")))) {
                stringBuilder3.append("is_unsub = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jsonDrop, "outputs-filter", "off")))) {
                stringBuilder3.append("is_optout = 't' OR ");
                bool = true;
            }
            if (bool == true)
            stringBuilder2.append(" AND (").append(stringBuilder3.toString().trim().substring(0, stringBuilder3.toString().length() - 4)).append(") ");
            if (JsonUtils.jsonContainsArray(jsonDrop, "verticals")) {
                String[] arrayOfString1 = new String[0];
                JSONArray vrts = jsonDrop.getJSONArray("verticals");
                for (int b = 0; b < vrts.length(); b++){
                    arrayOfString1 = (String[])ArrayUtils.add((Object[])arrayOfString1, "'" + String.valueOf(vrts.get(b)) + "'");
                }
                if (arrayOfString1.length > 0){
                    stringBuilder2.append(" AND string_to_array(verticals,',') @> ARRAY[").append(String.join(",", (CharSequence[])arrayOfString1)).append("]");
                }
            }
            if (JsonUtils.jsonContainsArray(jsonDrop, "countries")) {
                String[] arrayOfString1 = new String[0];
                JSONArray vrts = jsonDrop.getJSONArray("countries");
                for (int b = 0; b < vrts.length(); b++) {
                    arrayOfString1 = (String[]) ArrayUtils.add((Object[]) arrayOfString1, "'" + String.valueOf(vrts.get(b)) + "'");
                }
                if (arrayOfString1.length > 0){
                    stringBuilder2.append(" AND country_code = ANY(ARRAY[").append(String.join(",", (CharSequence[])arrayOfString1)).append("])");
                }
            }
            Offer ofer = null;
            if (mtaProcess.offerId > 0){
                ofer = new Offer(Integer.valueOf(mtaProcess.offerId));
            }
            for (DataList dtlist : list) {
                stringBuilder1.append("(SELECT t.id,'").append(dtlist.encryptEmails).append("' AS encrypt_emails,'").append(dtlist.tableSchema).append("' AS schema,'").append(dtlist.tableName).append("' AS table,t.list_id,t.first_name,t.last_name,t.email FROM ").append(dtlist.tableSchema).append(".").append(dtlist.tableName).append(" t").append(stringBuilder2);
                if (ofer != null && !ofer.getEmpty()) {
                    String supp = "sup_list_" + ofer.affiliateNetworkId + "_" + ofer.productionId + "_" + dtlist.id;
                    if (Database.get("clients").existeTable("suppressions", supp)){
                        stringBuilder1.append(" AND NOT EXISTS ( SELECT FROM suppressions.").append(supp).append(" WHERE email_md5 = t.email_md5 )");
                    }
                }
                stringBuilder1.append(" ORDER BY id ) UNION ALL ");
            }
            dropComponent.setQuery(stringBuilder1.toString());
            dropComponent.setQuery(dropComponent.getQuery().substring(0, dropComponent.getQuery().length() - 10));
        }
        return dropComponent;
    }

    public static int insertMtaProcesIp(int prossId, int serVmtaId) throws Exception {
        MtaProcessIp mtaprossIp = new MtaProcessIp();
        mtaprossIp.processId = prossId;
        mtaprossIp.serverVmtaId = serVmtaId;
        return mtaprossIp.insert();
    }

    public static String replaceTags(String value, MtaComponent component, ServerVmta vmta, String serverName, Email email, String autoReply) {
        return replaceTags(value, component, vmta, serverName, email, autoReply, null);
    }

    public static String replaceTags(String value, MtaComponent component, ServerVmta vmta, String serverName, Email email, String autoReply, List<String> placeholders) {
        String val = value;
        if (val != null && !"".equals(val) && val.contains("[") && val.contains("]")) {
            val = StringUtils.replace(val, "[process_id]", String.valueOf(component.getId()));
            val = StringUtils.replace(val, "[mailer_id]", String.valueOf((Application.checkAndgetInstance().getUser()).productionId));
            val = StringUtils.replace(val, "[ip]", vmta.ip);
            if (component.getAutoReplyActivated()){
                val = StringUtils.replace(val, "[auto_reply_mailbox]", autoReply);
            }
            if (component.getHasPtrTag()) {
                val = StringUtils.replace(val, "[ptr]", MtaDropHelper.getPtrRcrd(vmta.ip));
            }
            val = StringUtils.replace(val, "[static_domain]", "[domain]".equalsIgnoreCase(component.getStaticDomain()) ? vmta.domain : component.getStaticDomain());
            val = StringUtils.replace(val, "[server]", serverName);
            switch (component.getVmtasType()) {
                case "default-vmtas":
                    val = StringUtils.replace(val, "[rdns]", vmta.domain);
                    val = StringUtils.replace(val, "[domain]", Url.checkUrl(vmta.domain));
                    val = StringUtils.replace(val, "[custom_domain]", "");
                    break;
                case "custom-vmtas":
                    val = StringUtils.replace(val, "[rdns]", vmta.customDomain);
                    val = StringUtils.replace(val, "[domain]", vmta.customDomain);
                    val = StringUtils.replace(val, "[custom_domain]", vmta.customDomain);
                    break;
                case "merged-vmtas":
                    val = StringUtils.replace(val, "[rdns]", vmta.domain);
                    val = StringUtils.replace(val, "[domain]", Url.checkUrl(vmta.domain));
                    val = StringUtils.replace(val, "[custom_domain]", vmta.customDomain);
                    break;
                case "smtp-vmtas":
                    val = StringUtils.replace(val, "[rdns]", vmta.domain);
                    val = StringUtils.replace(val, "[domain]", Url.checkUrl(vmta.domain));
                    val = StringUtils.replace(val, "[custom_domain]", vmta.customDomain);
                    val = StringUtils.replace(val, "[smtp_1]", vmta.smtp_1);
                    val = StringUtils.replace(val, "[smtp_2]", vmta.smtp_2);
                    break;
            }
            if (email != null) {
                val = StringUtils.replace(val, "[email_id]", String.valueOf(email.id));
                val = StringUtils.replace(val, "[list_id]", String.valueOf(email.listId));
                val = StringUtils.replace(val, "[email]", email.email);
                val = StringUtils.replace(val, "[first_name]", email.firstName);
                val = StringUtils.replace(val, "[last_name]", email.lastName);
            }
            if (component.getPlaceholders() != null && !component.getPlaceholders().isEmpty()) {
                int b1 = (placeholders != null && !placeholders.isEmpty()) ? placeholders.size() : 0;
                int b2 = (placeholders != null && !placeholders.isEmpty()) ? b1 : component.getPlaceholders().size();
                for (int b3 = 0; b3 < b2; b3++) {
                    int i = b3 + 1;
                    if (val.contains("[placeholder" + i + "]")){
                        if (placeholders != null && !placeholders.isEmpty() && b3 < b1) {
                            val = StringUtils.replace(val, "[placeholder" + i + "]", placeholders.get(b3));
                        } else {
                            val = StringUtils.replace(val, "[placeholder" + i + "]", MtaProcesses.getCurrentPlaceHolder(b3));
                        }
                    }
                }
            }
        }
        return val;
    }

    public static void createMailMerge(StringBuilder pickup, MtaComponent component, Email email, ServerVmta vmta, String serverName, String msgHeaderBody, String open, String click, String unsub, String optout, String autoReply) {
        createMailMerge(pickup, component, email, vmta, serverName, msgHeaderBody, open, click, unsub, optout, autoReply, null);
    }

    public static void createMailMerge(StringBuilder pickup, MtaComponent component, Email email, ServerVmta vmta, String serverName, String msgHeaderBody, String open, String click, String unsub, String optout, String autoReply, List<String> placeholders) {
        if (component != null && email != null && !"".equals(email.email.trim()) && vmta != null) {
            StringBuilder mailMerge = new StringBuilder();
            StringBuilder messageIdBuilder = new StringBuilder();
            String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.rndomSalt(13, true)).append('.').append(System.currentTimeMillis()).append('@').append(vmta.domain).toString() + ">";
            mailMerge.append("XDFN ");
            if (component.getAutoReplyActivated()) {
                mailMerge.append("rcpt=\"");
                mailMerge.append(autoReply);
                mailMerge.append("\" ");
            } else {
                mailMerge.append("rcpt=\"");
                mailMerge.append(email.email);
                mailMerge.append("\" ");
            }
            mailMerge.append("mail_date=\"");
            mailMerge.append((new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")).format(new Date()));
            mailMerge.append("\" ");
            mailMerge.append("message_id=\"");
            mailMerge.append(messageId);
            mailMerge.append("\" ");
            mailMerge.append("ip=\"");
            mailMerge.append(vmta.ip);
            mailMerge.append("\" ");
            if (component.getHasPtrTag()) {
                mailMerge.append("ptr=\"");
                mailMerge.append(MtaDropHelper.getPtrRcrd(vmta.ip));
                mailMerge.append("\" ");
            }
            mailMerge.append("vmta_name=\"");
            mailMerge.append(vmta.name);
            mailMerge.append("\" ");
            switch (component.getVmtasType()) {
                case "default-vmtas":
                    mailMerge.append("rdns=\"");
                    mailMerge.append(vmta.domain);
                    mailMerge.append("\" ");
                    mailMerge.append("domain=\"");
                    mailMerge.append(Url.checkUrl(vmta.domain));
                    mailMerge.append("\" ");
                    mailMerge.append("custom_domain=\"");
                    mailMerge.append("");
                    mailMerge.append("\" ");
                    break;
                case "custom-vmtas":
                    mailMerge.append("rdns=\"");
                    mailMerge.append(vmta.customDomain);
                    mailMerge.append("\" ");
                    mailMerge.append("domain=\"");
                    mailMerge.append(vmta.customDomain);
                    mailMerge.append("\" ");
                    mailMerge.append("custom_domain=\"");
                    mailMerge.append(vmta.customDomain);
                    mailMerge.append("\" ");
                    break;
                case "merged-vmtas":
                    mailMerge.append("rdns=\"");
                    mailMerge.append(vmta.domain);
                    mailMerge.append("\" ");
                    mailMerge.append("domain=\"");
                    mailMerge.append(Url.checkUrl(vmta.domain));
                    mailMerge.append("\" ");
                    mailMerge.append("custom_domain=\"");
                    mailMerge.append(vmta.customDomain);
                    mailMerge.append("\" ");
                    break;
                case "smtp-vmtas":
                    mailMerge.append("rdns=\"");
                    mailMerge.append(vmta.domain);
                    mailMerge.append("\" ");
                    mailMerge.append("domain=\"");
                    mailMerge.append(Url.checkUrl(vmta.domain));
                    mailMerge.append("\" ");
                    mailMerge.append("custom_domain=\"");
                    mailMerge.append(vmta.customDomain);
                    mailMerge.append("\" ");
                    mailMerge.append("smtp_1=\"");
                    mailMerge.append(vmta.smtp_1);
                    mailMerge.append("\" ");
                    mailMerge.append("smtp_2=\"");
                    mailMerge.append(vmta.smtp_2);
                    mailMerge.append("\" ");
                    break;
            }
            mailMerge.append("server=\"");
            mailMerge.append(serverName);
            mailMerge.append("\" ");
            mailMerge.append("mailer_id=\"");
            mailMerge.append((Application.checkAndgetInstance().getUser()).productionId);
            mailMerge.append("\" ");
            mailMerge.append("process_id=\"");
            mailMerge.append(component.getId());
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
            if (component.getPlaceholders() != null && !component.getPlaceholders().isEmpty()) {
                int b1 = (placeholders != null && !placeholders.isEmpty()) ? placeholders.size() : 0;
                int b2 = (placeholders != null && !placeholders.isEmpty()) ? b1 : component.getPlaceholders().size();
                for (int b3 = 0; b3 < b2; b3++) {
                    int i = b3 + 1;
                    if (placeholders != null && !placeholders.isEmpty() && b3 < b1) {
                        mailMerge.append("placeholder").append(i).append("=\"");
                        mailMerge.append(placeholders.get(b3));
                        mailMerge.append("\" ");
                    } else {
                        mailMerge.append("placeholder").append(i).append("=\"");
                        mailMerge.append(MtaProcesses.getCurrentPlaceHolder(b3));
                        mailMerge.append("\" ");
                    }
                }
            }
            HashMap hashMap = MtaDropHelper.replaceRandomTags(mailMerge, msgHeaderBody);
            String str2 = "drop".equalsIgnoreCase(component.getProcessType()) ? "md" : "mt";
            String str3 = component.getReturnPath();
            Entry var19;
            if (hashMap != null && !hashMap.isEmpty()) {
                /*for (Map.Entry entry : hashMap.entrySet()){
                    str3 = StringUtils.replace(str3, "[" + (String) entry.getKey() + "]", (String) entry.getValue());
                }*/
                for(Iterator var25 = hashMap.entrySet().iterator(); var25.hasNext(); str3 = StringUtils.replace(str3, "[" + (String)var19.getKey() + "]", (String)var19.getValue())) {
                    var19 = (Entry)var25.next();
                }

            }
            mailMerge.append("\n");
            mailMerge.append("XDFN *vmta=\"");
            mailMerge.append(vmta.name);
            mailMerge.append("\" *jobId=\"");
            mailMerge.append(str2).append("_").append((Application.checkAndgetInstance().getUser()).productionId);
            mailMerge.append("\" *from=\"");
            mailMerge.append(MtaDropHelper.fixStaticTags(MtaDropHelper.replaceRandomTags(replaceTags(str3, component, vmta, serverName, email, autoReply, placeholders))));
            mailMerge.append("\" *envId=\"");
            mailMerge.append(component.getId());
            mailMerge.append("_");
            mailMerge.append(vmta.id);
            mailMerge.append("_");
            mailMerge.append(email.id);
            mailMerge.append("_");
            mailMerge.append(email.listId);
            mailMerge.append("\"\n");
            mailMerge.append("RCPT TO:<");
            if (component.getAutoReplyActivated()) {
                mailMerge.append(autoReply);
            } else {
                mailMerge.append(email.email);
            }
            mailMerge.append(">");
            mailMerge.append("\n");
            pickup.append(replaceTags(mailMerge.toString(), component, vmta, serverName, email, autoReply, placeholders));
            // new added base64 encoded
            /*String[] arrayOfString = msgHeaderBody.split(Pattern.quote("__HEADER__BODY__SEPARATOR__"));
            pickup.append("XPRT 1 LAST \n");
            if (arrayOfString.length > 0) {
                pickup.append(arrayOfString[0]);
            } else {
                pickup.append("");
            }
            pickup.append("\n\n");
            if (arrayOfString.length > 1) {
                // Base64 encoded
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[first_name]", email.firstName);
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[last_name]", email.lastName);
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[open]", open);
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[url]", click);
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[unsub]", unsub);
                arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[optout]", optout);
                if (component.getShortDomain() != null && !"".equals(component.getShortDomain())) {
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_open]", open);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_url]",  click);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_unsub]",  unsub);
                    arrayOfString[1] = StringUtils.replace(arrayOfString[1], "[short_optout]",  optout);
                }
                // End Base64 encoded
                pickup.append(MtaDropHelper.checkContentTransferEncod(arrayOfString[1], component.getContentTransferEncoding()));
            } else {
                pickup.append("");
            }*/
        }
    }

}
