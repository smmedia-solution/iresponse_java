package tech.iresponse.production.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.SmtpServer;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.admin.MtaServer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutoResponderComponent implements Serializable {

    private int id = 0;  //do
    private String processType; //if
    private int originalProcessId; //for;
    private String originalProcessType; //int;
    private String content; //new;
    private int serverId;  //try;
    private int componentId;  //byte;
    private MtaServer mtaServer;  //case;
    private SmtpServer smtpServer;  //char;
    private ServerVmta vmta;  //else;
    private SmtpUser smtpUser;  //goto;
    private int batch = 1; //long
    private long xdelay = 0L;    //this
    private int offerId;   //void;
    private String fromName;  //break;
    private String subject;  //catch;
    private String returnPath;  //class;
    private String header;  //const;
    private String charset;  //final;
    private String contentTransferEncoding;  //float;
    private String contentType;  //short;
    private String staticDomain;   //super;
    private String shortDomain;  //throw;
    private String linkType;  //while;
    private String body;  //double;
    private boolean hasPtrTag;  //import;

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AutoResponderComponent))
            return false;
        AutoResponderComponent do1 = (AutoResponderComponent)paramObject;
        if (!do1.exists(this))
            return false;
        if (getId() != do1.getId())
            return false;
        String str1 = getProcessType();
        String str2 = do1.getProcessType();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getOriginalProcessId() != do1.getOriginalProcessId())
            return false;
        String str3 = getOriginalProcessType();
        String str4 = do1.getOriginalProcessType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getContent();
        String str6 = do1.getContent();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (getServerId() != do1.getServerId())
            return false;
        if (getComponentId() != do1.getComponentId())
            return false;
        MtaServer throw1 = getMtaServer();
        MtaServer throw2 = do1.getMtaServer();
        if ((throw1 == null) ? (throw2 != null) : !throw1.equals(throw2))
            return false;
        SmtpServer finally1 = getSmtpServer();
        SmtpServer finally2 = do1.getSmtpServer();
        if ((finally1 == null) ? (finally2 != null) : !finally1.equals(finally2))
            return false;
        ServerVmta extends1 = getVmta();
        ServerVmta extends2 = do1.getVmta();
        if ((extends1 == null) ? (extends2 != null) : !extends1.equals(extends2))
            return false;
        SmtpUser package1 = getSmtpUser();
        SmtpUser package2 = do1.getSmtpUser();
        if ((package1 == null) ? (package2 != null) : !package1.equals(package2))
            return false;
        if (getBatch() != do1.getBatch())
            return false;
        if (getXdelay() != do1.getXdelay())
            return false;
        if (getOfferId() != do1.getOfferId())
            return false;
        String str7 = getFromName();
        String str8 = do1.getFromName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getSubject();
        String str10 = do1.getSubject();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getReturnPath();
        String str12 = do1.getReturnPath();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getHeader();
        String str14 = do1.getHeader();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getCharset();
        String str16 = do1.getCharset();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getContentTransferEncoding();
        String str18 = do1.getContentTransferEncoding();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getContentType();
        String str20 = do1.getContentType();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getStaticDomain();
        String str22 = do1.getStaticDomain();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getShortDomain();
        String str24 = do1.getShortDomain();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getLinkType();
        String str26 = do1.getLinkType();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getBody();
        String str28 = do1.getBody();
            return ((str27 == null) ? (str28 != null) : !str27.equals(str28)) ? false : (!(isHasPtrTag() != do1.isHasPtrTag()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AutoResponderComponent;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getProcessType();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getOriginalProcessId();
        String str2 = getOriginalProcessType();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getContent();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getServerId();
        n = n * 59 + getComponentId();
        MtaServer throw1 = getMtaServer();
        n = n * 59 + ((throw1 == null) ? 43 : throw1.hashCode());
        SmtpServer finally1 = getSmtpServer();
        n = n * 59 + ((finally1 == null) ? 43 : finally1.hashCode());
        ServerVmta extends1 = getVmta();
        n = n * 59 + ((extends1 == null) ? 43 : extends1.hashCode());
        SmtpUser package1 = getSmtpUser();
        n = n * 59 + ((package1 == null) ? 43 : package1.hashCode());
        n = n * 59 + getBatch();
        long l = getXdelay();
        n = n * 59 + (int)(l >>> 32L ^ l);
        n = n * 59 + getOfferId();
        String str4 = getFromName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getSubject();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getReturnPath();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getHeader();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getCharset();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getContentTransferEncoding();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getContentType();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getStaticDomain();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getShortDomain();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getLinkType();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getBody();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        return n * 59 + (isHasPtrTag() ? 79 : 97);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public MtaServer getMtaServer() {
        return mtaServer;
    }

    public void setMtaServer(MtaServer mtaServer) {
        this.mtaServer = mtaServer;
    }

    public SmtpServer getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public ServerVmta getVmta() {
        return vmta;
    }

    public void setVmta(ServerVmta vmta) {
        this.vmta = vmta;
    }

    public SmtpUser getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(SmtpUser smtpUser) {
        this.smtpUser = smtpUser;
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

    public String getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
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

    public boolean isHasPtrTag() {
        return hasPtrTag;
    }

    public void setHasPtrTag(boolean hasPtrTag) {
        this.hasPtrTag = hasPtrTag;
    }

    @Override
    public String toString() {
        return "AutoResponderComponent(id=" + getId() + ", processType=" + getProcessType() + ", originalProcessId=" + getOriginalProcessId() + ", originalProcessType=" + getOriginalProcessType() + ", content=" + getContent() + ", serverId=" + getServerId() + ", componentId=" + getComponentId() + ", mtaServer=" + getMtaServer() + ", smtpServer=" + getSmtpServer() + ", vmta=" + getVmta() + ", smtpUser=" + getSmtpUser() + ", batch=" + getBatch() + ", xdelay=" + getXdelay() + ", offerId=" + getOfferId() + ", fromName=" + getFromName() + ", subject=" + getSubject() + ", returnPath=" + getReturnPath() + ", header=" + getHeader() + ", charset=" + getCharset() + ", contentTransferEncoding=" + getContentTransferEncoding() + ", contentType=" + getContentType() + ", staticDomain=" + getStaticDomain() + ", shortDomain=" + getShortDomain() + ", linkType=" + getLinkType() + ", body=" + getBody() + ", hasPtrTag=" + isHasPtrTag() + ")";
    }
}
