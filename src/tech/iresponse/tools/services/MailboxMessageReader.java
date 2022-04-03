package tech.iresponse.tools.services;

import java.beans.ConstructorProperties;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Tools;

public class MailboxMessageReader extends Thread {

    private String email;
    private Message message;
    private String separator;
    private String returnType;
    private String headerReturnKeys;

    @Override
    public void run() {
        StringBuilder source = null;
        try {
            String[] headeRtnKeys = (this.headerReturnKeys != null && !"".equals(this.headerReturnKeys)) ? String.valueOf(this.headerReturnKeys).split(Pattern.quote("|")) : new String[0];
            Enumeration<Header> headers = this.message.getAllHeaders();

            switch (this.returnType) {
                case "full-header":{
                    source = new StringBuilder();
                    while (headers.hasMoreElements()) {
                        Header next = headers.nextElement();
                        source.append(next.getName());
                        source.append(": ");
                        source.append(next.getValue());
                        source.append('\n');
                    }
                    Tools.updateMailboxesResults(this.email, source.toString(), this.separator);
                    break;
                }
                case "full-source":{
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    this.message.writeTo(output);
                    Tools.updateMailboxesResults(this.email, output.toString(), this.separator);
                    break;
                }
                case "full-body":{
                    Tools.updateMailboxesResults(this.email, getBody((Part)this.message), this.separator);
                    break;
                }
                case "header-value":{
                    source = new StringBuilder();
                    if (headeRtnKeys != null && headeRtnKeys.length > 0) {
                        while (headers.hasMoreElements()) {
                            Header next = headers.nextElement();
                            for (String str : headeRtnKeys) {
                                if (next.getName().toLowerCase().trim().equals(String.valueOf(str).toLowerCase().trim())){
                                    source.append(next.getName()).append(":").append(next.getValue()).append("\n");
                                }
                            }
                        }
                        Tools.updateMailboxesResults(this.email, source.toString(), this.separator);
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    private String getBody(Part pr) throws Exception {
        String content = "";
        if (pr.isMimeType("text/plain")) {
            content = (String)pr.getContent();
        } else if (pr.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)pr.getContent();
            int count = mp.getCount();
            for (int b = 0; b < count; b++){
                content = content + getBody((Part)mp.getBodyPart(b)) + "\n";
            }
        } else if (pr.isMimeType("message/rfc822")) {
            content = getBody((Part)pr.getContent());
        } else {
            Object obj = pr.getContent();
            if (obj instanceof String) {
            content = (String)obj;
            } else {
            content = obj.toString();
            }
        }
        return content;
    }

    @ConstructorProperties({"email", "message", "separator", "returnType", "headerReturnKeys"})
    public MailboxMessageReader(String email, Message message, String separator, String returnType, String headerReturnKeys) {
        this.email = email;
        this.message = message;
        this.separator = separator;
        this.returnType = returnType;
        this.headerReturnKeys = headerReturnKeys;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MailboxMessageReader))
        return false;
        MailboxMessageReader int1 = (MailboxMessageReader)paramObject;
        if (!int1.exists(this))
            return false;
        String str1 = getEmail();
        String str2 = int1.getEmail();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        Message message1 = getMessage();
        Message message2 = int1.getMessage();
        if ((message1 == null) ? (message2 != null) : !message1.equals(message2))
            return false;
        String str3 = getSeparator();
        String str4 = int1.getSeparator();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getReturnType();
        String str6 = int1.getReturnType();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getHeaderReturnKeys();
        String str8 = int1.getHeaderReturnKeys();
            return !((str7 == null) ? (str8 != null) : !str7.equals(str8));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MailboxMessageReader;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getEmail();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        Message message = getMessage();
        n = n * 59 + ((message == null) ? 43 : message.hashCode());
        String str2 = getSeparator();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getReturnType();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getHeaderReturnKeys();
        return n * 59 + ((str4 == null) ? 43 : str4.hashCode());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getHeaderReturnKeys() {
        return headerReturnKeys;
    }

    public void setHeaderReturnKeys(String headerReturnKeys) {
        this.headerReturnKeys = headerReturnKeys;
    }

    @Override
    public String toString() {
        return "MailboxMessageReader(email=" + getEmail() + ", message=" + getMessage() + ", separator=" + getSeparator() + ", returnType=" + getReturnType() + ", headerReturnKeys=" + getHeaderReturnKeys() + ")";
    }
}
