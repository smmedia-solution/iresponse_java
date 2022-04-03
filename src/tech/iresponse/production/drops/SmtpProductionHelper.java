package tech.iresponse.production.drops;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.webservices.SmtpProcesses;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.lists.Email;
import tech.iresponse.models.production.SmtpProcess;
import tech.iresponse.models.production.SmtpProcessUser;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.production.component.SmtpComponent;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;

public class SmtpProductionHelper {

    public static SmtpComponent parse(SmtpProcess smtpProcss) throws Exception{
        SmtpComponent smtpCompnt = new SmtpComponent();
        smtpCompnt.setContent(Crypto.Base64Decode(smtpProcss.content));

        JSONObject jSONObject = JsonUtils.toJSON(smtpCompnt.getContent());
        if (jSONObject == null){
            throw new DatabaseException("Smtp process has no content !");
        }

        smtpCompnt.setId(smtpProcss.id);
        smtpCompnt.setStatus(smtpProcss.status);
        smtpCompnt.setProcessType(smtpProcss.processType);
        smtpCompnt.setTestThreads(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "test-threads", "multithread-servers")));

        //(new String[1])[0] = String.valueOf(TypesParser.safeParseInt(smtpProcss.serversIds.trim()));
        smtpCompnt.setSmtpServersIds(smtpProcss.serversIds.contains(",") ? smtpProcss.serversIds.trim().split(Pattern.quote(",")) : new String[]{ String.valueOf(TypesParser.safeParseInt(smtpProcss.serversIds.trim())) });
        smtpCompnt.setSmtpUsersIds(new String[0]);

        if (JsonUtils.jsonContainsArray(jSONObject, "selected-vmtas")) {
            JSONArray jSONArray = jSONObject.getJSONArray("selected-vmtas");
            for (int b = 0; b < jSONArray.length(); b++){
                smtpCompnt.setSmtpUsersIds((String[])ArrayUtils.add((Object[])smtpCompnt.getSmtpUsersIds(), String.valueOf(jSONArray.getString(b))));
            }
        }

        smtpCompnt.setSmtpUsersRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "vmta-rotation", "1")));
        smtpCompnt.setEmailsProcessType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "vmtas-emails-process", "vmtas-rotation")));
        smtpCompnt.setNumberOfEmails(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "number-of-emails", "1")));
        smtpCompnt.setEmailsPeriodValue(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "emails-period-value", "1")));
        smtpCompnt.setEmailsPeriodUnit(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "emails-period-type", "milliseconds")));
        smtpCompnt.setBatch(1);
        smtpCompnt.setXdelay(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "x-delay", "0")));

        if (smtpCompnt.getEmailsPeriodValue() > 0) {
            switch (smtpCompnt.getEmailsPeriodUnit()) {
                case "seconds":
                    smtpCompnt.setEmailsPeriodValue(smtpCompnt.getEmailsPeriodValue() * 1000);
                    break;
                case "minutes":
                    smtpCompnt.setEmailsPeriodValue(smtpCompnt.getEmailsPeriodValue() * 60 * 1000);
                    break;
                case "hours":
                    smtpCompnt.setEmailsPeriodValue(smtpCompnt.getEmailsPeriodValue() * 60 * 60 * 1000);
                    break;
            }
            smtpCompnt.setXdelay((int)Math.ceil((smtpCompnt.getEmailsPeriodValue() / smtpCompnt.getNumberOfEmails())));
        }

        smtpCompnt.setOfferId(smtpProcss.offerId);
        smtpCompnt.setFromName(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "from-name", "")));
        smtpCompnt.setSubject(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "subject", "")));
        smtpCompnt.setHeaders(new String[0]);

        if (JsonUtils.jsonContainsArray(jSONObject, "headers")) {
            JSONArray jSONArray = jSONObject.getJSONArray("headers");
            for (int b = 0; b < jSONArray.length(); b++){
                smtpCompnt.setHeaders((String[])ArrayUtils.add((Object[])smtpCompnt.getHeaders(), String.valueOf(jSONArray.get(b)).trim()));
            }
        }

        smtpCompnt.setHeadersRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "headers-rotation", "1")));
        smtpCompnt.setReturnPath(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "return-path", "return@[rdns]")).trim().replace("\n", "").replace("\r", ""));
        smtpCompnt.setTrackOpens(!"disabled".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "track-opens", "disabled"))));
        smtpCompnt.setCharset(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-charset", "UTF-8")).trim().replace("\n", "").replace("\r", ""));
        smtpCompnt.setContentTransferEncoding(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-content-transfert-encoding", "7bit")).trim().replace("\n", "").replace("\r", ""));
        smtpCompnt.setContentType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "creative-content-type", "text/html")).trim().replace("\n", "").replace("\r", ""));
        smtpCompnt.setStaticDomain(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "static-domain", "")).trim().replace("\n", "").replace("\r", ""));
        smtpCompnt.setLinkType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "link-type", "routing")));
        String body = String.valueOf(JsonUtils.getValueFromJson(jSONObject, "body", "")).trim();

        if (body != null && !"".equals(body) && MtaDropHelper.checkLinkRouting(smtpCompnt.getLinkType())) {
            Document doc = Jsoup.parse(body);
            Elements elements = doc.select("img");
            for (Element element : elements) {
                String src = String.valueOf(element.attr("src")).trim();
                if (src != null && src.contains("http")) {
                    String lnk = src;
                    switch (smtpCompnt.getLinkType()) {
                        case "routing-bitly":
                        case "attr-bitly":
                            lnk = Bitly.shortBitly(src);
                            break;
                        case "routing-gcloud":
                        case "attr-gcloud":
                            lnk = Google.shortUrlG(src);
                            break;
                        case "routing-tinyurl":
                        case "attr-tinyurl":
                            lnk = Tinyurl.shortTinyurl(src);
                            break;
                    }
                    body = StringUtils.replace(body, src, lnk);
                }
            }
        }

        smtpCompnt.setBody(body);
        smtpCompnt.setNegativeFilePath(smtpProcss.negativeFilePath);

        if (jSONObject.has("placeholders")) {
            JSONArray placeholders = jSONObject.getJSONArray("placeholders");
            if (placeholders.length() > 0) {
                ArrayList<String[]> newPlceHolder = new ArrayList();
                for (int b = 0; b < placeholders.length(); b++) {
                    String str1 = "";
                    String[] arrayOfString1 = String.valueOf(placeholders.getString(b)).split("\\r?\\n");
                    String[] arrayOfString2 = new String[0];
                    for (String str2 : arrayOfString1) {
                        str1 = str2.replaceAll("\n", "").replaceAll("\r", "").trim();
                        if (!"".equals(str1)){
                            arrayOfString2 = (String[])ArrayUtils.add((Object[])arrayOfString2, str1);
                        }
                    }
                    newPlceHolder.add(arrayOfString2);
                }
                smtpCompnt.setPlaceholders(newPlceHolder);
            }
        }

        if (jSONObject.has("placeholders-rotations")) {
            JSONArray placeholdersRotations = jSONObject.getJSONArray("placeholders-rotations");
            if (placeholdersRotations.length() > 0) {
            ArrayList<Integer> plcRot = new ArrayList();
            for (int b = 0; b < placeholdersRotations.length(); b++){
                plcRot.add(Integer.valueOf(TypesParser.safeParseInt(placeholdersRotations.get(b))));
            }
            smtpCompnt.setPlaceholdersRotations(plcRot);
            }
        } else if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
            ArrayList<Integer> plcRot = new ArrayList();
            int i = smtpCompnt.getPlaceholders().size();
            for (int b = 0; b < i; b++){
                plcRot.add(Integer.valueOf(1));
            }
            smtpCompnt.setPlaceholdersRotations(plcRot);
        }

        if (jSONObject.has("placeholders-combinations")) {
            JSONArray placeholdersCombinations = jSONObject.getJSONArray("placeholders-combinations");
            if (placeholdersCombinations.length() > 0) {
                ArrayList<Boolean> plcComb = new ArrayList();
                for (int b = 0; b < placeholdersCombinations.length(); b++){
                    plcComb.add(Boolean.valueOf("true".equalsIgnoreCase(String.valueOf(placeholdersCombinations.get(b)))));
                }
                smtpCompnt.setPlaceholdersCombinations(plcComb);
            }
        } else if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
            ArrayList<Boolean> plcComb = new ArrayList();
            int i = smtpCompnt.getPlaceholders().size();
            for (int b = 0; b < i; b++){
                plcComb.add(Boolean.valueOf(false));
            }
            smtpCompnt.setPlaceholdersCombinations(plcComb);
        }

        smtpCompnt.setTestAfter(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "test-after", "1000")));
        smtpCompnt.setTestRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "rcpt-rotation", "1")));
        smtpCompnt.setTestEmailsCombination("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "rcpt-combination", "off"))));
        smtpCompnt.setTestEmails(new String[0]);

        if (jSONObject.has("rcpts")) {
            String str1 = "";
            String[] rcpts = String.valueOf(JsonUtils.getValueFromJson(jSONObject, "rcpts", "")).split("\\r?\\n");
            for (String rcp : rcpts) {
                str1 = rcp.replaceAll("\n", "").replaceAll("\r", "").trim();
                if (!"".equals(str1) && str1.contains("@")){
                    smtpCompnt.setTestEmails((String[])ArrayUtils.add((Object[])smtpCompnt.getTestEmails(), str1));
                }
            }
        }

        smtpCompnt.setAutoReplyActivated("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "auto-reply-activated", "off"))));

        if (smtpCompnt.isAutoReplyActivated()) {
            smtpCompnt.setAutoReplyRotation(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "auto-reply-rotation", "1")));
            smtpCompnt.setAutoReplyMailboxes(new String[0]);
            if (jSONObject.has("auto-reply-mailboxes")) {
                String autoReplyMlbox = "";
                String[] autoReplyMailboxes = String.valueOf(JsonUtils.getValueFromJson(jSONObject, "auto-reply-mailboxes", "")).split("\\r?\\n");
                for (String str2 : autoReplyMailboxes) {
                    autoReplyMlbox = str2.replaceAll("\n", "").replaceAll("\r", "").trim();
                    if (!"".equals(autoReplyMlbox) && autoReplyMlbox.contains("@")){
                        smtpCompnt.setAutoReplyMailboxes((String[])ArrayUtils.add((Object[])smtpCompnt.getAutoReplyMailboxes(), autoReplyMlbox));
                    }
                }
            }
        }

        if ("drop".equals(smtpCompnt.getProcessType())) {
            smtpCompnt.setSplitEmailsType(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "emails-split-type", "servers")));
            smtpCompnt.setIspId(smtpProcss.ispId);
            smtpCompnt.setDataStart(smtpProcss.dataStart);
            smtpCompnt.setDataCount(smtpProcss.dataCount);
            smtpCompnt.setDataDuplicate(TypesParser.safeParseInt(JsonUtils.getValueFromJson(jSONObject, "data-duplicate", "1")));
            smtpCompnt.setTotalEmails(smtpProcss.totalEmails);

            //(new String[1])[0] = String.valueOf(TypesParser.safeParseInt(smtpProcss.lists.trim()));
            String[] listSelctd = smtpProcss.lists.contains(",") ? smtpProcss.lists.trim().split(Pattern.quote(",")) : new String[]{ String.valueOf(TypesParser.safeParseInt(smtpProcss.lists.trim())) };
            if (listSelctd.length == 0){
                throw new DatabaseException("This drop has no lists selected !");
            }

            List<DataList> dtList = (List)DataList.all(DataList.class, "id IN (" + String.join(",", (CharSequence[])listSelctd) + ")", null);
            if (dtList.isEmpty()){
                throw new DatabaseException("This drop has no lists selected !");
            }

            StringBuilder stringBuilder1 = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            StringBuilder stringBuilder3 = new StringBuilder();
            boolean bool = false;
            stringBuilder2.append(" WHERE (is_hard_bounced = 'f' OR is_hard_bounced IS NULL) AND (is_blacklisted = 'f' OR is_blacklisted IS NULL)");
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "seeds-filter", "off")))) {
                stringBuilder3.append("is_seed = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "fresh-filter", "off")))) {
                stringBuilder3.append("is_fresh = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "clean-filter", "off")))) {
                stringBuilder3.append("is_clean = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "openers-filter", "off")))) {
                stringBuilder3.append("is_opener = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "clickers-filter", "off")))) {
                stringBuilder3.append("is_clicker = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "leaders-filter", "off")))) {
                stringBuilder3.append("is_leader = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "unsubs-filter", "off")))) {
                stringBuilder3.append("is_unsub = 't' OR ");
                bool = true;
            }
            if ("on".equalsIgnoreCase(String.valueOf(JsonUtils.getValueFromJson(jSONObject, "outputs-filter", "off")))) {
                stringBuilder3.append("is_optout = 't' OR ");
                bool = true;
            }

            if (bool == true){
                stringBuilder2.append(" AND (").append(stringBuilder3.toString().trim().substring(0, stringBuilder3.toString().length() - 4)).append(") ");
            }

            if (JsonUtils.jsonContainsArray(jSONObject, "verticals")) {
                String[] vrtcl = new String[0];
                JSONArray verticals = jSONObject.getJSONArray("verticals");
                for (int b = 0; b < verticals.length(); b++){
                    vrtcl = (String[])ArrayUtils.add((Object[])vrtcl, "'" + String.valueOf(verticals.get(b)) + "'");
                }
                if (vrtcl.length > 0){
                    stringBuilder2.append(" AND string_to_array(verticals,',') @> ARRAY[").append(String.join(",", (CharSequence[])vrtcl)).append("]");
                }
            }

            if (JsonUtils.jsonContainsArray(jSONObject, "countries")) {
                String[] contry = new String[0];
                JSONArray countries = jSONObject.getJSONArray("countries");
                for (int b = 0; b < countries.length(); b++){
                    contry = (String[])ArrayUtils.add((Object[])contry, "'" + String.valueOf(countries.get(b)) + "'");
                }
                if (contry.length > 0){
                    stringBuilder2.append(" AND country_code = ANY(ARRAY[").append(String.join(",", (CharSequence[])contry)).append("])");
                }
            }

            Offer offre = null;
            if (smtpProcss.offerId > 0){
                offre = new Offer(Integer.valueOf(smtpProcss.offerId));
            }

            for (DataList dtlst : dtList) {
                stringBuilder1.append("(SELECT t.id,'").append(dtlst.encryptEmails).append("' AS encrypt_emails,'").append(dtlst.tableSchema).append("' AS schema,'").append(dtlst.tableName).append("' AS table,t.list_id,t.first_name,t.last_name,t.email FROM ").append(dtlst.tableSchema).append(".").append(dtlst.tableName).append(" t").append(stringBuilder2);
                if (offre != null && !offre.getEmpty()) {
                    String tblSupp = "sup_list_" + offre.affiliateNetworkId + "_" + offre.productionId + "_" + dtlst.id;
                    if (Database.get("clients").existeTable("suppressions", tblSupp)){
                        stringBuilder1.append(" AND NOT EXISTS ( SELECT FROM suppressions.").append(tblSupp).append(" WHERE email_md5 = t.email_md5 )");
                    }
                }
                stringBuilder1.append(" ORDER BY id ) UNION ALL ");
            }
            smtpCompnt.setQuery(stringBuilder1.toString());
            smtpCompnt.setQuery(smtpCompnt.getQuery().substring(0, smtpCompnt.getQuery().length() - 10));
        }
        return smtpCompnt;
    }

    public static int createSmtpProcessUser(int processId, int smtpUserId) throws Exception {
        SmtpProcessUser smtpProcssUsr = new SmtpProcessUser();
        smtpProcssUsr.processId = processId;
        smtpProcssUsr.smtpUserId = smtpUserId;
        return smtpProcssUsr.insert();
    }

    public static String replaceTags(String value, SmtpComponent smtpCompnt, SmtpUser smtpUsr, String server, Email email, String autoRply, List<String> placeholders) {
        if (value != null && !"".equals(value) && value.contains("[") && value.contains("]")) {

            value = StringUtils.replace(value, "[process_id]", String.valueOf(smtpCompnt.getId()));
            value = StringUtils.replace(value, "[mailer_id]", String.valueOf((Application.checkAndgetInstance().getUser()).productionId));
            value = StringUtils.replace(value, "[smtp_user]", smtpUsr.username);
            value = StringUtils.replace(value, "[rdns]", smtpCompnt.getStaticDomain());
            value = StringUtils.replace(value, "[domain]", smtpCompnt.getStaticDomain());
            value = StringUtils.replace(value, "[custom_domain]", smtpCompnt.getStaticDomain());
            value = StringUtils.replace(value, "[ip]", smtpCompnt.getStaticDomain());
            value = StringUtils.replace(value, "[static_domain]", smtpCompnt.getStaticDomain());
            value = StringUtils.replace(value, "[server]", server);

            if (email != null) {
                value = StringUtils.replace(value, "[email_id]", String.valueOf(email.id));
                value = StringUtils.replace(value, "[list_id]", String.valueOf(email.listId));
                value = StringUtils.replace(value, "[email]", email.email);
                value = StringUtils.replace(value, "[first_name]", email.firstName);
                value = StringUtils.replace(value, "[last_name]", email.lastName);
            }

            if (smtpCompnt.isAutoReplyActivated()){
                value = StringUtils.replace(value, "[auto_reply_mailbox]", autoRply);
            }

            if (smtpCompnt.getPlaceholders() != null && !smtpCompnt.getPlaceholders().isEmpty()) {
                int plcHolder1 = (placeholders != null && !placeholders.isEmpty()) ? placeholders.size() : 0;
                int b1 = (placeholders != null && !placeholders.isEmpty()) ? plcHolder1 : smtpCompnt.getPlaceholders().size();

                for (int b3 = 0; b3 < b1; b3++) {
                    int i = b3 + 1;
                    if (value.contains("[placeholder" + i + "]")){
                        if (placeholders != null && !placeholders.isEmpty() && b3 < plcHolder1) {
                            value = StringUtils.replace(value, "[placeholder" + i + "]", placeholders.get(b3));
                        } else {
                            value = StringUtils.replace(value, "[placeholder" + i + "]", SmtpProcesses.getCurrentPlaceHolder(b3));
                        }
                    }
                }
            }

            value = StringUtils.replace(value, "[content_transfer_encoding]", smtpCompnt.getContentTransferEncoding());
            value = StringUtils.replace(value, "[content_type]", smtpCompnt.getContentType());
            value = StringUtils.replace(value, "[charset]", smtpCompnt.getCharset());
        }
        return value;
    }
}
