package tech.iresponse.tools.services;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import org.json.JSONArray;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.tools.proxy.Proxy;
import tech.iresponse.tools.imaps.MailboxImaps;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Url;

public class MailboxExtractor extends Thread {

    private Proxy proxy;
    private String email;
    private String password;
    private String folder;
    private int maxEmailsNumber;
    private String emailsOrder;
    private Date startDate;
    private Date endDate;
    private JSONArray messageFilters;
    private String filtersType;
    private String separator;
    private String returnType;
    private String headerReturnKey;

    @Override
    public void run() {
        try {
            if (Strings.isEmpty(this.email) || Strings.isEmpty(this.password)){
                throw new DatabaseException("No email or password found !");
            }

            String ispEmail = Url.checkUrl(this.email.split(Pattern.quote("@"))[1].toLowerCase().trim()).split(Pattern.quote("."))[0];
            if (!MailboxImaps.checkIspSupported(ispEmail)){
                throw new DatabaseException("Unsupported isp !");
            }

            this.folder = MailboxImaps.getImapFolder(ispEmail, this.folder);
            if (Strings.isEmpty(this.folder)){
                throw new DatabaseException("Folder not found !");
            }

            Properties properties = MailboxImaps.getImapProperties(ispEmail);
            if (this.proxy != null && !Strings.isEmpty(this.proxy.getHost())) {
                properties.setProperty("proxySet", "true");
                properties.setProperty("socksProxyHost", this.proxy.getHost());
                properties.setProperty("socksProxyPort", this.proxy.getPort());
            }

            Session session = Session.getInstance(properties, new PassAuth(this));
            Store store = session.getStore("imaps");
            store.connect();
            if (!store.isConnected()){
                throw new DatabaseException("Could not connect to mailbox !");
            }

            Folder folder = store.getFolder(this.folder);
            folder.open(1);
            Message[] messagesByDate = null;

            if (this.startDate != null && this.endDate != null) {
                if (this.startDate.compareTo(this.endDate) != 0) {
                messagesByDate = folder.search((SearchTerm)new AndTerm((SearchTerm)new ReceivedDateTerm(2, this.endDate), (SearchTerm)new ReceivedDateTerm(5, this.startDate)));
                } else {
                messagesByDate = folder.search((SearchTerm)new ReceivedDateTerm(3, this.startDate));
                }
            } else {
                messagesByDate = folder.getMessages();
            }

            if (messagesByDate != null && messagesByDate.length > 0) {
                int nbthread = (messagesByDate.length > 500) ? 500 : messagesByDate.length;
                ExecutorService execService = Executors.newFixedThreadPool(nbthread);

                int counter = 1;
                if ("new-to-old".equals(this.emailsOrder)) {
                    for (int i = messagesByDate.length - 1; i >= 0; i--) {
                        execService.submit(new MailboxMessageReader(this.email, messagesByDate[i], this.separator, this.returnType, this.headerReturnKey));
                        if (counter++ == this.maxEmailsNumber){
                            break;
                        }
                    }
                } else {
                    for (Message message : messagesByDate) {
                        execService.submit(new MailboxMessageReader(this.email, message, this.separator, this.returnType, this.headerReturnKey));
                        if (counter++ == this.maxEmailsNumber){
                            break;
                        }
                    }
                }
                execService.shutdown();

                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }
        } catch (Exception ex) {
            Loggers.error((Throwable)ex);
        }
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MailboxExtractor))
            return false;
        MailboxExtractor if1 = (MailboxExtractor)paramObject;
        if (!if1.exists(this))
            return false;
        Proxy do1 = getProxy();
        Proxy do2 = if1.getProxy();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        String str1 = getEmail();
        String str2 = if1.getEmail();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getPassword();
        String str4 = if1.getPassword();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getFolder();
        String str6 = if1.getFolder();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getMaxEmailsNumber() != if1.getMaxEmailsNumber())
            return false;
        String str7 = getEmailsOrder();
        String str8 = if1.getEmailsOrder();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        Date date1 = getStartDate();
        Date date2 = if1.getStartDate();
        if ((date1 == null) ? (date2 != null) : !date1.equals(date2))
            return false;
        Date date3 = getEndDate();
        Date date4 = if1.getEndDate();
        if ((date3 == null) ? (date4 != null) : !date3.equals(date4))
            return false;
        JSONArray jSONArray1 = getMessageFilters();
        JSONArray jSONArray2 = if1.getMessageFilters();
        if ((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2))
            return false;
        String str9 = getFiltersType();
        String str10 = if1.getFiltersType();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getSeparator();
        String str12 = if1.getSeparator();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getReturnType();
        String str14 = if1.getReturnType();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getHeaderReturnKey();
        String str16 = if1.getHeaderReturnKey();
            return !((str15 == null) ? (str16 != null) : !str15.equals(str16));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MailboxExtractor;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Proxy do1 = getProxy();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        String str1 = getEmail();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getPassword();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getFolder();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getMaxEmailsNumber();
        String str4 = getEmailsOrder();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        Date date1 = getStartDate();
        n = n * 59 + ((date1 == null) ? 43 : date1.hashCode());
        Date date2 = getEndDate();
        n = n * 59 + ((date2 == null) ? 43 : date2.hashCode());
        JSONArray jSONArray = getMessageFilters();
        n = n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
        String str5 = getFiltersType();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getSeparator();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getReturnType();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getHeaderReturnKey();
        return n * 59 + ((str8 == null) ? 43 : str8.hashCode());
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public int getMaxEmailsNumber() {
        return maxEmailsNumber;
    }

    public void setMaxEmailsNumber(int maxEmailsNumber) {
        this.maxEmailsNumber = maxEmailsNumber;
    }

    public String getEmailsOrder() {
        return emailsOrder;
    }

    public void setEmailsOrder(String emailsOrder) {
        this.emailsOrder = emailsOrder;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public JSONArray getMessageFilters() {
        return messageFilters;
    }

    public void setMessageFilters(JSONArray messageFilters) {
        this.messageFilters = messageFilters;
    }

    public String getFiltersType() {
        return filtersType;
    }

    public void setFiltersType(String filtersType) {
        this.filtersType = filtersType;
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

    public String getHeaderReturnKey() {
        return headerReturnKey;
    }

    public void setHeaderReturnKey(String headerReturnKey) {
        this.headerReturnKey = headerReturnKey;
    }

    @Override
    public String toString() {
        return "MailboxExtractor(proxy=" + getProxy() + ", email=" + getEmail() + ", password=" + getPassword() + ", folder=" + getFolder() + ", maxEmailsNumber=" + getMaxEmailsNumber() + ", emailsOrder=" + getEmailsOrder() + ", startDate=" + getStartDate() + ", endDate=" + getEndDate() + ", messageFilters=" + getMessageFilters() + ", filtersType=" + getFiltersType() + ", separator=" + getSeparator() + ", returnType=" + getReturnType() + ", headerReturnKey=" + getHeaderReturnKey() + ")";
    }
}
