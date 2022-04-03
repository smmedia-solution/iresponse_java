package tech.iresponse.production.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.utils.ThreadSleep;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MtaComponent implements Serializable {

    private int id = 0;
    private String status;
    private String processType;
    private String testThreads;
    private String content;
    private String query;
    private String[] mtaServersIds;
    private String[] vmtasIds;
    private int vmtasRotation;
    private String vmtasType;
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
    private boolean hasPtrTag;
    private boolean hasNegative;
    private volatile Rotator vmtasRotator;
    public volatile LinkedHashMap<Integer,Integer> vmtasTotals = new LinkedHashMap<>();

    public ServerVmta getCurrentVmta() {
        return (ServerVmta) this.vmtasRotator.getCurrentThenRotate();
    }

    public synchronized void updateVmtasTotals(int vmtaId) {
        if(this.vmtasTotals.containsKey(vmtaId))  {
            this.vmtasTotals.put(vmtaId, this.vmtasTotals.get(vmtaId) + 1);
        }else {
            this.vmtasTotals.put(vmtaId, 0);
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

    public String[] getMtaServersIds() {
        return mtaServersIds;
    }

    public void setMtaServersIds(String[] mtaServersIds) {
        this.mtaServersIds = mtaServersIds;
    }

    public String[] getVmtasIds() {
        return vmtasIds;
    }

    public void setVmtasIds(String[] vmtasIds) {
        this.vmtasIds = vmtasIds;
    }

    public int getVmtasRotation() {
        return vmtasRotation;
    }

    public void setVmtasRotation(int vmtasRotation) {
        this.vmtasRotation = vmtasRotation;
    }

    public String getVmtasType() {
        return vmtasType;
    }

    public void setVmtasType(String vmtasType) {
        this.vmtasType = vmtasType;
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

    public boolean getTrackOpens() {
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

    public boolean getTestEmailsCombination() {
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

    public boolean getAutoReplyActivated() {
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

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean getHasPtrTag() {
        return hasPtrTag;
    }

    public void setHasPtrTag(boolean hasPtrTag) {
        this.hasPtrTag = hasPtrTag;
    }

    public boolean getHasNegative() {
        return hasNegative;
    }

    public void setHasNegative(boolean hasNegative) {
        this.hasNegative = hasNegative;
    }

    public Rotator getVmtasRotator() {
        return vmtasRotator;
    }

    public void setVmtasRotator(Rotator vmtasRotator) {
        this.vmtasRotator = vmtasRotator;
    }

    public LinkedHashMap getVmtasTotals() {
        return vmtasTotals;
    }

    public void setVmtasTotals(LinkedHashMap vmtasTotals) {
        this.vmtasTotals = vmtasTotals;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaComponent))
            return false;
        MtaComponent if1 = (MtaComponent)paramObject;
        if (!if1.exists(this))
            return false;
        if (getId() != if1.getId())
            return false;
        String str1 = getStatus();
        String str2 = if1.getStatus();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getProcessType();
        String str4 = if1.getProcessType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getTestThreads();
        String str6 = if1.getTestThreads();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getContent();
        String str8 = if1.getContent();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getQuery();
        String str10 = if1.getQuery();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        if (!Arrays.deepEquals((Object[])getMtaServersIds(), (Object[])if1.getMtaServersIds()))
            return false;
        if (!Arrays.deepEquals((Object[])getVmtasIds(), (Object[])if1.getVmtasIds()))
            return false;
        if (getVmtasRotation() != if1.getVmtasRotation())
            return false;
        String str11 = getVmtasType();
        String str12 = if1.getVmtasType();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getEmailsProcessType();
        String str14 = if1.getEmailsProcessType();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        if (getNumberOfEmails() != if1.getNumberOfEmails())
            return false;
        if (getEmailsPeriodValue() != if1.getEmailsPeriodValue())
            return false;
        String str15 = getEmailsPeriodUnit();
        String str16 = if1.getEmailsPeriodUnit();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        if (getBatch() != if1.getBatch())
            return false;
        if (getXdelay() != if1.getXdelay())
            return false;
        if (getOfferId() != if1.getOfferId())
            return false;
        String str17 = getFromName();
        String str18 = if1.getFromName();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getSubject();
        String str20 = if1.getSubject();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        if (!Arrays.deepEquals((Object[])getHeaders(), (Object[])if1.getHeaders()))
            return false;
        if (getHeadersRotation() != if1.getHeadersRotation())
            return false;
        String str21 = getReturnPath();
        String str22 = if1.getReturnPath();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        if (getTrackOpens() != if1.getTrackOpens())
            return false;
        String str23 = getCharset();
        String str24 = if1.getCharset();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getContentTransferEncoding();
        String str26 = if1.getContentTransferEncoding();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getContentType();
        String str28 = if1.getContentType();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getStaticDomain();
        String str30 = if1.getStaticDomain();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        String str31 = getShortDomain();
        String str32 = if1.getShortDomain();
        if ((str31 == null) ? (str32 != null) : !str31.equals(str32))
            return false;
        String str33 = getLinkType();
        String str34 = if1.getLinkType();
        if ((str33 == null) ? (str34 != null) : !str33.equals(str34))
            return false;
        String str35 = getBody();
        String str36 = if1.getBody();
        if ((str35 == null) ? (str36 != null) : !str35.equals(str36))
            return false;
        String str37 = getNegativeFilePath();
        String str38 = if1.getNegativeFilePath();
        if ((str37 == null) ? (str38 != null) : !str37.equals(str38))
            return false;
        List list1 = getPlaceholdersRotations();
        List list2 = if1.getPlaceholdersRotations();
        if ((list1 == null) ? (list2 != null) : !list1.equals(list2))
            return false;
        List list3 = getPlaceholdersCombinations();
        List list4 = if1.getPlaceholdersCombinations();
        if ((list3 == null) ? (list4 != null) : !list3.equals(list4))
            return false;
        List list5 = getPlaceholders();
        List list6 = if1.getPlaceholders();
        if ((list5 == null) ? (list6 != null) : !list5.equals(list6))
            return false;
        if (getTestAfter() != if1.getTestAfter())
            return false;
        if (getTestRotation() != if1.getTestRotation())
            return false;
        if (getTestEmailsCombination() != if1.getTestEmailsCombination())
            return false;
        if (!Arrays.deepEquals((Object[])getTestEmails(), (Object[])if1.getTestEmails()))
            return false;
        if (getAutoReplyActivated() != if1.getAutoReplyActivated())
            return false;
        if (!Arrays.deepEquals((Object[])getAutoReplyMailboxes(), (Object[])if1.getAutoReplyMailboxes()))
            return false;
        if (getAutoReplyRotation() != if1.getAutoReplyRotation())
            return false;
        String str39 = getSplitEmailsType();
        String str40 = if1.getSplitEmailsType();
        if ((str39 == null) ? (str40 != null) : !str39.equals(str40))
            return false;
        if (getIspId() != if1.getIspId())
            return false;
        if (getDataStart() != if1.getDataStart())
            return false;
        if (getDataCount() != if1.getDataCount())
            return false;
        if (getDataDuplicate() != if1.getDataDuplicate())
            return false;
        if (getTotalEmails() != if1.getTotalEmails())
            return false;
        if (getFirst() != if1.getFirst())
            return false;
        if (getHasPtrTag() != if1.getHasPtrTag())
            return false;
        if (getHasNegative() != if1.getHasNegative())
            return false;
        Rotator for1 = getVmtasRotator();
        Rotator for2 = if1.getVmtasRotator();
        if ((for1 == null) ? (for2 != null) : !for1.equals(for2))
            return false;
        LinkedHashMap linkedHashMap1 = getVmtasTotals();
        LinkedHashMap linkedHashMap2 = if1.getVmtasTotals();
        return !((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaComponent;
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
        n = n * 59 + Arrays.deepHashCode((Object[])getMtaServersIds());
        n = n * 59 + Arrays.deepHashCode((Object[])getVmtasIds());
        n = n * 59 + getVmtasRotation();
        String str6 = getVmtasType();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getEmailsProcessType();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        n = n * 59 + getNumberOfEmails();
        n = n * 59 + getEmailsPeriodValue();
        String str8 = getEmailsPeriodUnit();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        n = n * 59 + getBatch();
        long l = getXdelay();
        n = n * 59 + (int)(l >>> 32L ^ l);
        n = n * 59 + getOfferId();
        String str9 = getFromName();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getSubject();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        n = n * 59 + Arrays.deepHashCode((Object[])getHeaders());
        n = n * 59 + getHeadersRotation();
        String str11 = getReturnPath();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        n = n * 59 + (getTrackOpens() ? 79 : 97);
        String str12 = getCharset();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getContentTransferEncoding();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getContentType();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getStaticDomain();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        String str16 = getShortDomain();
        n = n * 59 + ((str16 == null) ? 43 : str16.hashCode());
        String str17 = getLinkType();
        n = n * 59 + ((str17 == null) ? 43 : str17.hashCode());
        String str18 = getBody();
        n = n * 59 + ((str18 == null) ? 43 : str18.hashCode());
        String str19 = getNegativeFilePath();
        n = n * 59 + ((str19 == null) ? 43 : str19.hashCode());
        List list1 = getPlaceholdersRotations();
        n = n * 59 + ((list1 == null) ? 43 : list1.hashCode());
        List list2 = getPlaceholdersCombinations();
        n = n * 59 + ((list2 == null) ? 43 : list2.hashCode());
        List list3 = getPlaceholders();
        n = n * 59 + ((list3 == null) ? 43 : list3.hashCode());
        n = n * 59 + getTestAfter();
        n = n * 59 + getTestRotation();
        n = n * 59 + (getTestEmailsCombination() ? 79 : 97);
        n = n * 59 + Arrays.deepHashCode((Object[])getTestEmails());
        n = n * 59 + (getAutoReplyActivated() ? 79 : 97);
        n = n * 59 + Arrays.deepHashCode((Object[])getAutoReplyMailboxes());
        n = n * 59 + getAutoReplyRotation();
        String str20 = getSplitEmailsType();
        n = n * 59 + ((str20 == null) ? 43 : str20.hashCode());
        n = n * 59 + getIspId();
        n = n * 59 + getDataStart();
        n = n * 59 + getDataCount();
        n = n * 59 + getDataDuplicate();
        n = n * 59 + getTotalEmails();
        n = n * 59 + (getFirst() ? 79 : 97);
        n = n * 59 + (getHasPtrTag() ? 79 : 97);
        n = n * 59 + (getHasNegative() ? 79 : 97);
        Rotator for1 = getVmtasRotator();
        n = n * 59 + ((for1 == null) ? 43 : for1.hashCode());
        LinkedHashMap linkedHashMap = getVmtasTotals();
        return n * 59 + ((linkedHashMap == null) ? 43 : linkedHashMap.hashCode());
    }

    @Override
    public String toString() {
        return "MtaComponent(id=" + getId() + ", status=" + getStatus() +  ", testThreads=" + getTestThreads() + ", content=" + getContent() + ", query=" + getQuery() + ", mtaServersIds=" + Arrays.deepToString((Object[])getMtaServersIds()) + ", vmtasIds=" + Arrays.deepToString((Object[])getVmtasIds()) + ", vmtasRotation=" + getVmtasRotation() + ", vmtasType=" + getVmtasType() + ", emailsProcessType=" + getEmailsProcessType() + ", numberOfEmails=" + getNumberOfEmails() + ", emailsPeriodValue=" + getEmailsPeriodValue() + ", emailsPeriodUnit=" + getEmailsPeriodUnit() + ", batch=" + getBatch() + ", xdelay=" + getXdelay() + ", offerId=" + getOfferId() + ", fromName=" + getFromName() + ", subject=" + getSubject() + ", headers=" + Arrays.deepToString((Object[])getHeaders()) + ", headersRotation=" + getHeadersRotation() + ", returnPath=" + getReturnPath() + ", trackOpens=" + getTrackOpens() + ", charset=" + getCharset() + ", contentTransferEncoding=" + getContentTransferEncoding() + ", contentType=" + getContentType() + ", staticDomain=" + getStaticDomain() + ", shortDomain=" + getShortDomain() + ", linkType=" + getLinkType() + ", body=" + getBody() + ", negativeFilePath=" + getNegativeFilePath() + ", placeholdersRotations=" + getPlaceholdersRotations() + ", placeholdersCombinations=" + getPlaceholdersCombinations() + ", placeholders=" + getPlaceholders() + ", testAfter=" + getTestAfter() + ", testRotation=" + getTestRotation() + ", testEmailsCombination=" + getTestEmailsCombination() + ", testEmails=" + Arrays.deepToString((Object[])getTestEmails()) + ", autoReplyActivated=" + getAutoReplyActivated() + ", autoReplyMailboxes=" + Arrays.deepToString((Object[])getAutoReplyMailboxes()) + ", autoReplyRotation=" + getAutoReplyRotation() + ", splitEmailsType=" + getSplitEmailsType() + ", ispId=" + getIspId() + ", dataStart=" + getDataStart() + ", dataCount=" + getDataCount() + ", dataDuplicate=" + getDataDuplicate() + ", totalEmails=" + getTotalEmails() + ", first=" + getFirst() + ", hasPtrTag=" + getHasPtrTag() + ", hasNegative=" + getHasNegative() + ", vmtasRotator=" + getVmtasRotator() + ", vmtasTotals=" + getVmtasTotals() + ")";
    }

}