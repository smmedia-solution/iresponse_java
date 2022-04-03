package tech.iresponse.dns;

import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.ManagementServer;

public interface DnsInterface {

    String setDomainRecords(int accountId, String domainValue, Object paramObject);

    JSONArray getDomainRecords(int accountId, String domainValue);

    boolean deleteRecords(int accountId, String domainValue, int id);

    void setRecords(int accountId, String domainValue);

    void addRecord(String record, String value);

    void addRecord(JSONObject records);

    void setupRecords(String record, String mainIp, Domain domain, ManagementServer managServ);

    void addSPFDkim(int nbcount, String mainIp, String hostName, String record, String vspf, String dkim);

    void setupDkimDmarc(int nbcount, String dkim, String mainIp, boolean hasIpV6, String vspf, boolean dmarc, Domain domain);
}