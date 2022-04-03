package tech.iresponse.production.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.utils.ThreadSleep;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmtpComponent implements Serializable {

    private int id = 0;
    private String status;
    private String processType;
    private String testThreads;
    private String content;
    private String query;
    private String[] smtpServersIds;
    private String[] smtpUsersIds;
    private int smtpUsersRotation;
    private String emailsProcessType;
    private int numberOfEmails;
    private int emailsPeriodValue;
    private String emailsPeriodUnit;
    private int batch;
    private long xdelay;
    private int offerId;
    private String fromName;
    private String subject;
    private String[] headers;
    private int headersRotation;
    private String returnPath;
    private boolean trackOpens;
    private String charset;
    private String contentTransferEncoding;
    private String contentType;
    private String staticDomain;
    private String shortDomain;
    private String linkType;
    private String body;
    private String negativeFilePath;
    private List placeholdersRotations;
    private List placeholdersCombinations;
    private List<String[]> placeholders;
    private int testAfter;
    private int testRotation;
    private boolean testEmailsCombination;
    private String[] testEmails;
    private boolean autoReplyActivated;
    private String[] autoReplyMailboxes;
    private int autoReplyRotation;
    private String splitEmailsType;
    private int ispId;
    private int dataStart;
    private int dataCount;
    private int dataDuplicate;
    private int totalEmails;
    private boolean first = true;
    private volatile Rotator smtpUsersRotator;
    private volatile LinkedHashMap<Integer,Integer> smtpUsersTotals = new LinkedHashMap<>();

    public SmtpUser getCurrentSmtpUser() {
        return (SmtpUser) this.smtpUsersRotator.getCurrentThenRotate();
    }

    public synchronized void updateSmtpUsersTotals(int smtpUsrId) {
        if (this.smtpUsersTotals.containsKey(smtpUsrId)) {
            this.smtpUsersTotals.put(smtpUsrId, this.smtpUsersTotals.get(smtpUsrId) + 1);
        } else {
            this.smtpUsersTotals.put(smtpUsrId, 0);
        }
    }

    public synchronized void await() {
        if (!this.first){
            ThreadSleep.sleep(this.xdelay);
        }
        this.first = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getTestThreads() {
        return testThreads;
    }

    public void setTestThreads(String testThreads) {
        this.testThreads = testThreads;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getSmtpServersIds() {
        return smtpServersIds;
    }

    public void setSmtpServersIds(String[] smtpServersIds) {
        this.smtpServersIds = smtpServersIds;
    }

    public String[] getSmtpUsersIds() {
        return smtpUsersIds;
    }

    public void setSmtpUsersIds(String[] smtpUsersIds) {
        this.smtpUsersIds = smtpUsersIds;
    }

    public int getSmtpUsersRotation() {
        return smtpUsersRotation;
    }

    public void setSmtpUsersRotation(int smtpUsersRotation) {
        this.smtpUsersRotation = smtpUsersRotation;
    }

    public String getEmailsProcessType() {
        return emailsProcessType;
    }

    public void setEmailsProcessType(String emailsProcessType) {
        this.emailsProcessType = emailsProcessType;
    }

    public int getNumberOfEmails() {
        return numberOfEmails;
    }

    public void setNumberOfEmails(int numberOfEmails) {
        this.numberOfEmails = numberOfEmails;
    }

    public int getEmailsPeriodValue() {
        return emailsPeriodValue;
    }

    public void setEmailsPeriodValue(int emailsPeriodValue) {
        this.emailsPeriodValue = emailsPeriodValue;
    }

    public String getEmailsPeriodUnit() {
        return emailsPeriodUnit;
    }

    public void setEmailsPeriodUnit(String emailsPeriodUnit) {
        this.emailsPeriodUnit = emailsPeriodUnit;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public long getXdelay() {
        return xdelay;
    }

    public void setXdelay(long xdelay) {
        this.xdelay = xdelay;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public int getHeadersRotation() {
        return headersRotation;
    }

    public void setHeadersRotation(int headersRotation) {
        this.headersRotation = headersRotation;
    }

    public String getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    public boolean isTrackOpens() {
        return trackOpens;
    }

    public void setTrackOpens(boolean trackOpens) {
        this.trackOpens = trackOpens;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentTransferEncoding() {
        return contentTransferEncoding;
    }

    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStaticDomain() {
        return staticDomain;
    }

    public void setStaticDomain(String staticDomain) {
        this.staticDomain = staticDomain;
    }

    public String getShortDomain() {
        return shortDomain;
    }

    public void setShortDomain(String shortDomain) {
        this.shortDomain = shortDomain;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getNegativeFilePath() {
        return negativeFilePath;
    }

    public void setNegativeFilePath(String negativeFilePath) {
        this.negativeFilePath = negativeFilePath;
    }

    public List getPlaceholdersRotations() {
        return placeholdersRotations;
    }

    public void setPlaceholdersRotations(List placeholdersRotations) {
        this.placeholdersRotations = placeholdersRotations;
    }

    public List getPlaceholdersCombinations() {
        return placeholdersCombinations;
    }

    public void setPlaceholdersCombinations(List placeholdersCombinations) {
        this.placeholdersCombinations = placeholdersCombinations;
    }

    public List<String[]> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<String[]> placeholders) {
        this.placeholders = placeholders;
    }

    public int getTestAfter() {
        return testAfter;
    }

    public void setTestAfter(int testAfter) {
        this.testAfter = testAfter;
    }

    public int getTestRotation() {
        return testRotation;
    }

    public void setTestRotation(int testRotation) {
        this.testRotation = testRotation;
    }

    public boolean isTestEmailsCombination() {
        return testEmailsCombination;
    }

    public void setTestEmailsCombination(boolean testEmailsCombination) {
        this.testEmailsCombination = testEmailsCombination;
    }

    public String[] getTestEmails() {
        return testEmails;
    }

    public void setTestEmails(String[] testEmails) {
        this.testEmails = testEmails;
    }

    public boolean isAutoReplyActivated() {
        return autoReplyActivated;
    }

    public void setAutoReplyActivated(boolean autoReplyActivated) {
        this.autoReplyActivated = autoReplyActivated;
    }

    public String[] getAutoReplyMailboxes() {
        return autoReplyMailboxes;
    }

    public void setAutoReplyMailboxes(String[] autoReplyMailboxes) {
        this.autoReplyMailboxes = autoReplyMailboxes;
    }

    public int getAutoReplyRotation() {
        return autoReplyRotation;
    }

    public void setAutoReplyRotation(int autoReplyRotation) {
        this.autoReplyRotation = autoReplyRotation;
    }

    public String getSplitEmailsType() {
        return splitEmailsType;
    }

    public void setSplitEmailsType(String splitEmailsType) {
        this.splitEmailsType = splitEmailsType;
    }

    public int getIspId() {
        return ispId;
    }

    public void setIspId(int ispId) {
        this.ispId = ispId;
    }

    public int getDataStart() {
        return dataStart;
    }

    public void setDataStart(int dataStart) {
        this.dataStart = dataStart;
    }

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public int getDataDuplicate() {
        return dataDuplicate;
    }

    public void setDataDuplicate(int dataDuplicate) {
        this.dataDuplicate = dataDuplicate;
    }

    public int getTotalEmails() {
        return totalEmails;
    }

    public void setTotalEmails(int totalEmails) {
        this.totalEmails = totalEmails;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public Rotator getSmtpUsersRotator() {
        return smtpUsersRotator;
    }

    public void setSmtpUsersRotator(Rotator smtpUsersRotator) {
        this.smtpUsersRotator = smtpUsersRotator;
    }

    public LinkedHashMap getSmtpUsersTotals() {
        return smtpUsersTotals;
    }

    public void setSmtpUsersTotals(LinkedHashMap smtpUsersTotals) {
        this.smtpUsersTotals = smtpUsersTotals;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpComponent))
            return false;
        SmtpComponent int1 = (SmtpComponent)paramObject;
        if (!int1.exists(this))
            return false;
        if (getId() != int1.getId())
            return false;
        String str1 = getStatus();
        String str2 = int1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessType();
        String str4 = int1.getProcessType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getTestThreads();
        String str6 = int1.getTestThreads();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getContent();
        String str8 = int1.getContent();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getQuery();
        String str10 = int1.getQuery();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (!Arrays.deepEquals((Object[])getSmtpServersIds(), (Object[])int1.getSmtpServersIds()))
            return false;
        if (!Arrays.deepEquals((Object[])getSmtpUsersIds(), (Object[])int1.getSmtpUsersIds()))
            return false;
        if (getSmtpUsersRotation() != int1.getSmtpUsersRotation())
            return false;
        String str11 = getEmailsProcessType();
        String str12 = int1.getEmailsProcessType();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        if (getNumberOfEmails() != int1.getNumberOfEmails())
            return false;
        if (getEmailsPeriodValue() != int1.getEmailsPeriodValue())
            return false;
        String str13 = getEmailsPeriodUnit();
        String str14 = int1.getEmailsPeriodUnit();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        if (getBatch() != int1.getBatch())
            return false;
        if (getXdelay() != int1.getXdelay())
            return false;
        if (getOfferId() != int1.getOfferId())
            return false;
        String str15 = getFromName();
        String str16 = int1.getFromName();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getSubject();
        String str18 = int1.getSubject();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        if (!Arrays.deepEquals((Object[])getHeaders(), (Object[])int1.getHeaders()))
            return false;
        if (getHeadersRotation() != int1.getHeadersRotation())
            return false;
        String str19 = getReturnPath();
        String str20 = int1.getReturnPath();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        if (isTrackOpens() != int1.isTrackOpens())
            return false;
        String str21 = getCharset();
        String str22 = int1.getCharset();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getContentTransferEncoding();
        String str24 = int1.getContentTransferEncoding();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getContentType();
        String str26 = int1.getContentType();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getStaticDomain();
        String str28 = int1.getStaticDomain();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getShortDomain();
        String str30 = int1.getShortDomain();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        String str31 = getLinkType();
        String str32 = int1.getLinkType();
        if ((str31 == null) ? (str32 != null) : !str31.equals(str32))
            return false;
        String str33 = getBody();
        String str34 = int1.getBody();
        if ((str33 == null) ? (str34 != null) : !str33.equals(str34))
            return false;
        String str35 = getNegativeFilePath();
        String str36 = int1.getNegativeFilePath();
        if ((str35 == null) ? (str36 != null) : !str35.equals(str36))
            return false;
        List list1 = getPlaceholdersRotations();
        List list2 = int1.getPlaceholdersRotations();
        if ((list1 == null) ? (list2 != null) : !list1.equals(list2))
            return false;
        List list3 = getPlaceholdersCombinations();
        List list4 = int1.getPlaceholdersCombinations();
        if ((list3 == null) ? (list4 != null) : !list3.equals(list4))
            return false;
        List list5 = getPlaceholders();
        List list6 = int1.getPlaceholders();
        if ((list5 == null) ? (list6 != null) : !list5.equals(list6))
            return false;
        if (getTestAfter() != int1.getTestAfter())
            return false;
        if (getTestRotation() != int1.getTestRotation())
            return false;
        if (isTestEmailsCombination() != int1.isTestEmailsCombination())
            return false;
        if (!Arrays.deepEquals((Object[])getTestEmails(), (Object[])int1.getTestEmails()))
            return false;
        if (isAutoReplyActivated() != int1.isAutoReplyActivated())
            return false;
        if (!Arrays.deepEquals((Object[])getAutoReplyMailboxes(), (Object[])int1.getAutoReplyMailboxes()))
            return false;
        if (getAutoReplyRotation() != int1.getAutoReplyRotation())
            return false;
        String str37 = getSplitEmailsType();
        String str38 = int1.getSplitEmailsType();
        if ((str37 == null) ? (str38 != null) : !str37.equals(str38))
            return false;
        if (getIspId() != int1.getIspId())
            return false;
        if (getDataStart() != int1.getDataStart())
            return false;
        if (getDataCount() != int1.getDataCount())
            return false;
        if (getDataDuplicate() != int1.getDataDuplicate())
            return false;
        if (getTotalEmails() != int1.getTotalEmails())
            return false;
        if (isFirst() != int1.isFirst())
            return false;
        Rotator for1 = getSmtpUsersRotator();
        Rotator for2 = int1.getSmtpUsersRotator();
        if ((for1 == null) ? (for2 != null) : !for1.equals(for2))
            return false;
        LinkedHashMap linkedHashMap1 = getSmtpUsersTotals();
        LinkedHashMap linkedHashMap2 = int1.getSmtpUsersTotals();
        return !((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpComponent;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getStatus();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getProcessType();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getTestThreads();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getContent();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getQuery();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + Arrays.deepHashCode((Object[])getSmtpServersIds());
        n = n * 59 + Arrays.deepHashCode((Object[])getSmtpUsersIds());
        n = n * 59 + getSmtpUsersRotation();
        String str6 = getEmailsProcessType();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        n = n * 59 + getNumberOfEmails();
        n = n * 59 + getEmailsPeriodValue();
        String str7 = getEmailsPeriodUnit();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        n = n * 59 + getBatch();
        long l = getXdelay();
        n = n * 59 + (int)(l >>> 32L ^ l);
        n = n * 59 + getOfferId();
        String str8 = getFromName();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getSubject();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        n = n * 59 + Arrays.deepHashCode((Object[])getHeaders());
        n = n * 59 + getHeadersRotation();
        String str10 = getReturnPath();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        n = n * 59 + (isTrackOpens() ? 79 : 97);
        String str11 = getCharset();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getContentTransferEncoding();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getContentType();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getStaticDomain();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getShortDomain();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        String str16 = getLinkType();
        n = n * 59 + ((str16 == null) ? 43 : str16.hashCode());
        String str17 = getBody();
        n = n * 59 + ((str17 == null) ? 43 : str17.hashCode());
        String str18 = getNegativeFilePath();
        n = n * 59 + ((str18 == null) ? 43 : str18.hashCode());
        List list1 = getPlaceholdersRotations();
        n = n * 59 + ((list1 == null) ? 43 : list1.hashCode());
        List list2 = getPlaceholdersCombinations();
        n = n * 59 + ((list2 == null) ? 43 : list2.hashCode());
        List list3 = getPlaceholders();
        n = n * 59 + ((list3 == null) ? 43 : list3.hashCode());
        n = n * 59 + getTestAfter();
        n = n * 59 + getTestRotation();
        n = n * 59 + (isTestEmailsCombination() ? 79 : 97);
        n = n * 59 + Arrays.deepHashCode((Object[])getTestEmails());
        n = n * 59 + (isAutoReplyActivated() ? 79 : 97);
        n = n * 59 + Arrays.deepHashCode((Object[])getAutoReplyMailboxes());
        n = n * 59 + getAutoReplyRotation();
        String str19 = getSplitEmailsType();
        n = n * 59 + ((str19 == null) ? 43 : str19.hashCode());
        n = n * 59 + getIspId();
        n = n * 59 + getDataStart();
        n = n * 59 + getDataCount();
        n = n * 59 + getDataDuplicate();
        n = n * 59 + getTotalEmails();
        n = n * 59 + (isFirst() ? 79 : 97);
        Rotator for1 = getSmtpUsersRotator();
        n = n * 59 + ((for1 == null) ? 43 : for1.hashCode());
        LinkedHashMap linkedHashMap = getSmtpUsersTotals();
        return n * 59 + ((linkedHashMap == null) ? 43 : linkedHashMap.hashCode());
    }

    @Override
    public String toString() {
        return "SmtpComponent(id=" + getId() + ", status=" + getStatus() + ", processType=" + getProcessType() + ", testThreads=" + getTestThreads() + ", content=" + getContent() + ", query=" + getQuery() + ", smtpServersIds=" + Arrays.deepToString((Object[])getSmtpServersIds()) + ", smtpUsersIds=" + Arrays.deepToString((Object[])getSmtpUsersIds()) + ", smtpUsersRotation=" + getSmtpUsersRotation() + ", emailsProcessType=" + getEmailsProcessType() + ", numberOfEmails=" + getNumberOfEmails() + ", emailsPeriodValue=" + getEmailsPeriodValue() + ", emailsPeriodUnit=" + getEmailsPeriodUnit() + ", batch=" + getBatch() + ", xdelay=" + getXdelay() + ", offerId=" + getOfferId() + ", fromName=" + getFromName() + ", subject=" + getSubject() + ", headers=" + Arrays.deepToString((Object[])getHeaders()) + ", headersRotation=" + getHeadersRotation() + ", returnPath=" + getReturnPath() + ", trackOpens=" + isTrackOpens() + ", charset=" + getCharset() + ", contentTransferEncoding=" + getContentTransferEncoding() + ", contentType=" + getContentType() + ", staticDomain=" + getStaticDomain() + ", shortDomain=" + getShortDomain() + ", linkType=" + getLinkType() + ", body=" + getBody() + ", negativeFilePath=" + getNegativeFilePath() + ", placeholdersRotations=" + getPlaceholdersRotations() + ", placeholdersCombinations=" + getPlaceholdersCombinations() + ", placeholders=" + getPlaceholders() + ", testAfter=" + getTestAfter() + ", testRotation=" + getTestRotation() + ", testEmailsCombination=" + isTestEmailsCombination() + ", testEmails=" + Arrays.deepToString((Object[])getTestEmails()) + ", autoReplyActivated=" + isAutoReplyActivated() + ", autoReplyMailboxes=" + Arrays.deepToString((Object[])getAutoReplyMailboxes()) + ", autoReplyRotation=" + getAutoReplyRotation() + ", splitEmailsType=" + getSplitEmailsType() + ", ispId=" + getIspId() + ", dataStart=" + getDataStart() + ", dataCount=" + getDataCount() + ", dataDuplicate=" + getDataDuplicate() + ", totalEmails=" + getTotalEmails() + ", first=" + isFirst() + ", smtpUsersRotator=" + getSmtpUsersRotator() + ", smtpUsersTotals=" + getSmtpUsersTotals() + ")";
    }
}
