package tech.iresponse.production.drops;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.orm.Connector;
import tech.iresponse.webservices.SmtpProcesses;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.lists.Email;
import tech.iresponse.models.production.MtaProcess;
import tech.iresponse.models.production.SmtpProcess;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;

public class MtaDropHelper {

    public static volatile List xdfns = Arrays.asList(new String[] {
        "rcpt", "mail_date", "message_id", "ip", "ptr", "vmta_name", "rdns", "domain", "rdns", "domain",
        "custom_domain", "static_domain", "server", "mailer_id", "mailer_production_id", "process_id", "list_id", "email_id", "email", "first_name",
        "last_name", "return_path", "from_name", "subject", "content_transfer_encoding", "content_type", "charset", "open", "url", "unsub",
        "optout", "short_open", "short_url", "short_unsub", "short_optout" });

    public static volatile List AlphaN = Arrays.asList(new String[] { "a", "al", "au", "an", "anu", "anl", "n", "hu", "hl" });

    public static volatile List uniqueAlpaN = Arrays.asList(new String[] { "ua", "ual", "uau", "uan", "uanu", "uanl", "un", "uhu", "uhl" });

    public static final int id = 0;

    public static final int id11 = 1;

    public static final String headerBodySeparator = "__HEADER__BODY__SEPARATOR__";

    public static Email createEmailMap(Map paramMap) throws Exception {
        Email email = new Email();
        email.id = TypesParser.safeParseInt(String.valueOf(paramMap.get("id")));
        if (!"".equalsIgnoreCase(String.valueOf(paramMap.get("table")))) {
            email.setSchema(String.valueOf(paramMap.get("schema")));
            email.setTable(String.valueOf(paramMap.get("table")));
        } else {
            email.setSchema("");
            email.setTable("");
        }
        email.map(paramMap);
        email.email = "enabled".equalsIgnoreCase(String.valueOf(paramMap.get("encrypt_emails"))) ? Convertion.decrypt(email.email, "") : email.email;
        if (email.email == null || "".equals(email.firstName) || "null".equalsIgnoreCase(email.firstName)) {
            email.firstName = email.email.split(Pattern.quote("@"))[0];
        } else {
            email.firstName = "enabled".equalsIgnoreCase(String.valueOf(paramMap.get("encrypt_emails"))) ? Convertion.decrypt(email.firstName, "") : email.firstName;
        }
        if (email.lastName == null || "".equals(email.lastName) || "null".equalsIgnoreCase(email.lastName)) {
            email.lastName = email.email.split(Pattern.quote("@"))[0];
        } else {
            email.lastName = "enabled".equalsIgnoreCase(String.valueOf(paramMap.get("encrypt_emails"))) ? Convertion.decrypt(email.lastName, "") : email.lastName;
        }
        email.listId = TypesParser.safeParseInt(String.valueOf(paramMap.get("list_id")));
        email.setEmpty(false);
        return email;
    }

    public static Email createEmailObject(TestEmail tstEmail) throws Exception {
        Email email = new Email();
        email.setSchema("");
        email.setTable("");
        email.email = tstEmail.getEmail().trim();
        email.firstName = email.email.split(Pattern.quote("@"))[0];
        email.lastName = email.firstName;
        email.listId = 0;
        return email;
    }

    public static HashMap replaceRandomTags(StringBuilder mailMerge, String msgHeaderBody) {
        HashMap<Object, Object> hashMap = new HashMap<>();
        if (mailMerge != null) {
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher matcher = p.matcher(msgHeaderBody);
            ArrayList<String> arrayList = new ArrayList();
            while (matcher.find()) {
                String match = matcher.group(1);
                String[] tagParts = (match != null && match.contains("_")) ? match.split(Pattern.quote("_")) : null;
                if (tagParts != null && tagParts.length > 0) {
                    String tag = tagParts[0];
                    if (uniqueAlpaN.contains(tag.toLowerCase())) {
                        int size = 0;
                        if (tagParts.length == 3) {
                            size = TypesParser.safeParseInt(tagParts[2]);
                        } else if (tagParts.length == 4) {
                            int j = TypesParser.safeParseInt(tagParts[2]);
                            int k = TypesParser.safeParseInt(tagParts[3]);
                            size = (k > j) ? ThreadLocalRandom.current().nextInt(j, k) : j;
                        }
                        if (size > 0) {
                            String str2 = "";
                            switch (tag) {
                                case "ua":
                                    str2 = Strings.getSaltString(size, true, true, false, false);
                                    break;
                                case "ual":
                                    str2 = Strings.getSaltString(size, true, true, false, false).toLowerCase();
                                    break;
                                case "uau":
                                    str2 = Strings.getSaltString(size, true, true, false, false).toUpperCase();
                                    break;
                                case "uan":
                                    str2 = Strings.getSaltString(size, true, true, true, false);
                                    break;
                                case "uanl":
                                    str2 = Strings.getSaltString(size, true, true, true, false).toLowerCase();
                                    break;
                                case "uanu":
                                    str2 = Strings.getSaltString(size, true, true, true, false).toUpperCase();
                                    break;
                                case "un":
                                    str2 = Strings.getSaltString(size, false, false, true, false);
                                    break;
                                case "uhu":
                                    str2 = Strings.rndomSalt(size, true);
                                    break;
                                case "uhl":
                                    str2 = Strings.rndomSalt(size, false);
                                    break;
                            }
                            if (!"".equals(str2)) {
                                mailMerge.append(match).append("=\"");
                                mailMerge.append(str2);
                                mailMerge.append("\" ");
                                arrayList.add(match);
                            }
                        }
                        continue;
                    }
                    if (AlphaN.contains(tag.toLowerCase()) && !arrayList.contains(match)) {
                        int size = 0;
                        if (tagParts.length == 2) {
                            size = TypesParser.safeParseInt(tagParts[1]);
                        } else {
                        int j = TypesParser.safeParseInt(tagParts[1]);
                        int k = TypesParser.safeParseInt(tagParts[2]);
                        size = (k > j) ? ThreadLocalRandom.current().nextInt(j, k) : j;
                        }
                        if (size > 0) {
                            String str2 = "";
                            switch (tag) {
                                case "a":
                                str2 = Strings.getSaltString(size, true, true, false, false);
                                break;
                                case "al":
                                str2 = Strings.getSaltString(size, true, true, false, false).toLowerCase();
                                break;
                            case "au":
                                str2 = Strings.getSaltString(size, true, true, false, false).toUpperCase();
                                break;
                            case "an":
                                str2 = Strings.getSaltString(size, true, true, true, false);
                                break;
                            case "anl":
                                str2 = Strings.getSaltString(size, true, true, true, false).toLowerCase();
                                break;
                            case "anu":
                                str2 = Strings.getSaltString(size, true, true, true, false).toUpperCase();
                                break;
                            case "n":
                                str2 = Strings.getSaltString(size, false, false, true, false);
                                break;
                            case "hu":
                                str2 = Strings.rndomSalt(size, true);
                                break;
                            case "hl":
                                str2 = Strings.rndomSalt(size, false);
                                break;
                            }
                            if (!"".equals(str2)) {
                                hashMap.put(match, str2);
                                mailMerge.append(match).append("=\"");
                                mailMerge.append(str2);
                                mailMerge.append("\" ");
                                arrayList.add(match);
                            }
                        }
                    }
                }
            }
        }
        return hashMap;
    }

    public static String replaceRandomTags(String value) {
        if (value != null && !"".equals(value)) {
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher m = p.matcher(value);
            while (m.find()) {
                String match = m.group(1);
                String[] tagParts = (match != null && match.contains("_")) ? match.split(Pattern.quote("_")) : null;
                if (tagParts != null && tagParts.length > 0) {
                    String tag = tagParts[0];
                    if (AlphaN.contains(tag.toLowerCase()) || uniqueAlpaN.contains(tag.toLowerCase())) {
                        int size = 0;
                        if (uniqueAlpaN.contains(tag.toLowerCase())) {
                            if (tagParts.length == 3) {
                                size = TypesParser.safeParseInt(tagParts[2]);
                            } else if (tagParts.length == 4) {
                                int start = TypesParser.safeParseInt(tagParts[2]);
                                int end = TypesParser.safeParseInt(tagParts[3]);
                                size = (end > start) ? ThreadLocalRandom.current().nextInt(start, end) : start;
                            }
                        } else if (tagParts.length == 2) {
                            size = TypesParser.safeParseInt(tagParts[1]);
                        } else if (tagParts.length == 3) {
                            int start = TypesParser.safeParseInt(tagParts[1]);
                            int end = TypesParser.safeParseInt(tagParts[2]);
                            size = (end > start) ? ThreadLocalRandom.current().nextInt(start, end) : start;
                        }
                        if (size > 0){
                            switch (tag) {
                                case "ua":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, false, false));
                                case "ual":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, false, false).toLowerCase());
                                case "uau":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, false, false).toUpperCase());
                                case "uan":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, true, false));
                                case "uanl":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, true, false).toLowerCase());
                                case "uanu":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, true, true, true, false).toUpperCase());
                                case "un":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.getSaltString(size, false, false, true, false));
                                case "uhu":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.rndomSalt(size, true));
                                case "uhl":
                                value = value.replaceFirst(Pattern.quote("[" + match + "]"), Strings.rndomSalt(size, false));
                                case "a":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, false, false));
                                case "al":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, false, false).toLowerCase());
                                case "au":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, false, false).toUpperCase());
                                case "an":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, true, false));
                                case "anl":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, true, false).toLowerCase());
                                case "anu":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, true, true, true, false).toUpperCase());
                                case "n":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.getSaltString(size, false, false, true, false));
                                case "hu":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.rndomSalt(size, true));
                                case "hl":
                                value = StringUtils.replace(value, "[" + match + "]", Strings.rndomSalt(size, false));
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    public static String fixStaticTags(String value) {
        if (value != null && !"".equals(value)) {
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher m = p.matcher(value);
            while (m.find()) {
                String match = m.group(1);
                String[] tagParts = (match != null && match.contains("_")) ? match.split(Pattern.quote("_")) : null;
                if (tagParts != null && tagParts.length > 1) {
                    String tag = tagParts[0];
                    if (uniqueAlpaN.contains(tag.toLowerCase())) {
                        String str2 = StringUtils.replace(match, tag, tag + "_" + Strings.getSaltString(5, true, true, false, false));
                        value = value.replaceFirst(Pattern.quote("[" + match + "]"), "[" + str2 + "]");
                    }
                }
            }
        }
        return value;
    }

    public static String replaceNegative(String value, boolean paramBoolean) {
        if (value != null && !"".equals(value)) {
            if (paramBoolean){
                value = StringUtils.replace(value, "[negative]", "p_tag_replacable_negative");
            }
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            Matcher m = p.matcher(value);
            ArrayList<String> arrayList = new ArrayList();
            while (m.find()) {
                String match = m.group(1);
                String[] tagParts = (match != null && match.contains("_")) ? match.split(Pattern.quote("_")) : null;
                boolean bool1 = false;
                if (tagParts != null && tagParts.length > 0) {
                    String tag = tagParts[0];
                    if (AlphaN.contains(tag.toLowerCase()) || uniqueAlpaN.contains(tag.toLowerCase())){
                        bool1 = true;
                    }
                }
                boolean bool2 = (match != null && match.contains("placeholder")) ? true : false;
                if (!bool2 && !xdfns.contains(match) && !bool1 && !arrayList.contains(match)) {
                    arrayList.add(match);
                    value = StringUtils.replace(value, "[" + match + "]", "[[" + match + "]");
                }
            }
        }
        return value;
    }

    public static String replaceBaseUrl(String url) {
        return StringUtils.replace(url, String.valueOf(Application.getSettingsParam("base_url") + "/media/"), "http://[domain]/");
    }

    public static String replaceUrlImage(String body, String url) {
        return StringUtils.replace(body, String.valueOf(Application.getSettingsParam("base_url") + "/media/"), "http://" + url + "/");
    }

    public static boolean checkLinkRouting(String paramString) {
        boolean bool = false;
        switch (paramString) {
            case "routing-bitly":
            case "routing-gcloud":
            case "routing-tinyurl":
            case "attr-bitly":
            case "attr-gcloud":
            case "attr-tinyurl":
                bool = true;
                break;
        }
        return bool;
    }

    public static String checkContentTransferEncod(String value, String contentTransferEncoding) {
        if (!"".equals(value)){
            switch (contentTransferEncoding) {
                case "Quoted-Printable":
                    value = Crypto.QuotaPrintEncode(value);
                    break;
                case "base64":
                    value = Crypto.Base64Encode(value);
                    break;
            }
        }
        return value;
    }

    public static String getPtrRcrd(String paramString) {
        try {
            Name name = ReverseMap.fromAddress(InetAddress.getByName(paramString));
            Lookup lookup = new Lookup(name, 12);
            lookup.setResolver((Resolver)new SimpleResolver());
            lookup.setCache(null);
            Record[] arrayOfRecord = lookup.run();
            if (lookup.getResult() == 0){
                for (Record record : arrayOfRecord) {
                    if (record instanceof PTRRecord) {
                        PTRRecord pTRRecord = (PTRRecord)record;
                        return pTRRecord.getTarget().toString();
                    }
                }
            }
        } catch (UnknownHostException unknownHostException) {}
        return paramString;
    }

    public static synchronized void updateProgress(int processId, int progress, int mprocess) {
        try {
            if (mprocess == 0) {
                Database.get("system").executeUpdate("UPDATE production.mta_processes SET progress = progress + " + progress + "  WHERE id = ?", new Object[] {processId}, Connector.AFFECTED_ROWS);
                MtaProcesses.updateProgress(progress);
            } else {
                Database.get("system").executeUpdate("UPDATE production.smtp_processes SET progress = progress + " + progress + "  WHERE id = ?", new Object[] {processId}, Connector.AFFECTED_ROWS);
                SmtpProcesses.updateProgress(progress);
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    public static synchronized void finishProccess(ActiveRecord record, boolean error, int typeProcess) {
        try {
            if (typeProcess == 0) {
                ((MtaProcess)record).status = error ? "Error" : "Completed";
                ((MtaProcess)record).finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                ((MtaProcess)record).progress = MtaProcesses.TOTAL_PROGRESS;
                ((MtaProcess)record).update();
            } else {
                ((SmtpProcess)record).status = error ? "Error" : "Completed";
                ((SmtpProcess)record).finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                ((SmtpProcess)record).progress = SmtpProcesses.TOTAL_PROGRESS;
                ((SmtpProcess)record).update();
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
    }
}
