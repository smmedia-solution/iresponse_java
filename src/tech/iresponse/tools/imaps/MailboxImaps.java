package tech.iresponse.tools.imaps;

import java.util.Arrays;
import java.util.Properties;

public class MailboxImaps {

    public static boolean checkIspSupported(String paramString) {
        return Arrays.asList(new String[] { "internal", "gmail", "yahoo", "ymail", "hotmail", "msn", "windowslive", "onmicrosoft", "outlook", "aol" }).contains(paramString);
    }

    public static Properties getImapProperties(String isp) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.store.protocol", "imaps");

        switch (isp) {
            case "yahoo":
            case "ymail":{
                properties.setProperty("mail.imaps.host", "imap.mail.yahoo.com");
                properties.setProperty("mail.imaps.port", "993");
                break;
            }
            case "aol":{
                properties.setProperty("mail.imaps.host", "imap.aol.com");
                properties.setProperty("mail.imaps.port", "993");
                break;
            }
            case "hotmail":
            case "outlook":
            case "msn":
            case "onmicrosoft":
            case "windowslive":{
                properties.setProperty("mail.imaps.host", "outlook.office365.com");
                properties.setProperty("mail.imaps.port", "993");
                break;
            }
            case "gmail":{
                properties.setProperty("mail.imaps.starttls.enable", "true");
                properties.setProperty("mail.imaps.starttls.required", "true");
                properties.setProperty("mail.imaps.host", "imap.gmail.com");
                properties.setProperty("mail.imaps.port", "993");
                break;
            }
        }

        properties.setProperty("mail.imaps.timeout", "10000");
        properties.setProperty("mail.imaps.connectiontimeout", "10000");
        return properties;
    }

    public static String getImapFolder(String isp, String folder) {
        String result = "";
        switch (isp) {
            case "internal":{
                switch (folder) {
                    case "inbox":{
                        result = "INBOX";
                        break;
                    }
                    case "spam":{
                        result = "Spam";
                        break;
                    }
                    case "draft":{
                        result = "Drafts";
                        break;
                    }
                    case "sent":{
                        result = "Sent";
                        break;
                    }
                    case "trash":{
                        result = "Trash";
                        break;
                    }
                    case "archive":{
                        result = "Archive";
                        break;
                    }
                    case "filtered":{
                        result = "Filtered";
                        break;
                    }
                }
                break;
            }
            case "yahoo":
            case "ymail":{
                switch (folder) {
                    case "inbox":{
                        result = "INBOX";
                        break;
                    }
                    case "spam":{
                        result = "Bulk Mail";
                        break;
                    }
                    case "draft":{
                        result = "Draft";
                        break;
                    }
                    case "sent":{
                        result = "Sent";
                        break;
                    }
                    case "trash":{
                        result = "Trash";
                        break;
                    }
                    case "archive":{
                        result = "Archive";
                        break;
                    }
                    case "filtered":{
                        result = "Filtered";
                        break;
                    }
                }
                break;
            }
            case "aol":{
                switch (folder) {
                    case "inbox": {
                        result = "INBOX";
                        break;
                    }
                    case "spam": {
                        result = "Spam";
                        break;
                    }
                    case "draft": {
                        result = "Drafts";
                        break;
                    }
                    case "sent": {
                        result = "Sent";
                        break;
                    }
                    case "trash": {
                        result = "Trash";
                        break;
                    }
                    case "archive": {
                        result = "Archive";
                        break;
                    }
                    case "filtered":{
                        result = "Filtered";
                        break;
                    }
                }
                break;
            }
            case "hotmail":
            case "outlook":
            case "msn":
            case "onmicrosoft":
            case "windowslive":{
                switch (folder) {
                    case "inbox": {
                        result = "INBOX";
                        break;
                    }
                    case "spam": {
                        result = "Junk";
                        break;
                    }
                    case "draft": {
                        result = "Drafts";
                        break;
                    }
                    case "sent": {
                        result = "Sent";
                        break;
                    }
                    case "trash": {
                        result = "Deleted";
                        break;
                    }
                    case "archive": {
                        result = "Archive";
                        break;
                    }
                    case "filtered":{
                        result = "Filtered";
                        break;
                    }
                }
                break;
            }
            case "gmail":{
                switch (folder) {
                    case "inbox": {
                        result = "INBOX";
                        break;
                    }
                    case "spam": {
                        result = "[Gmail]/Spam";
                        break;
                    }
                    case "draft": {
                        result = "Drafts";
                        break;
                    }
                    case "sent": {
                        result = "Sent";
                        break;
                    }
                    case "trash": {
                        result = "Trash";
                        break;
                    }
                    case "archive": {
                        result = "Archive";
                        break;
                    }
                    case "filtered":{
                        result = "Filtered";
                        break;
                    }
                }
                break;
            }
        }
        return result;
    }
}
