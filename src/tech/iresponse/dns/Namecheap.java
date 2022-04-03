package tech.iresponse.dns;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.NameCheap;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.http.Agents;

public class Namecheap extends DnsApi {

    public String setDomainRecords(int accountId, String domainValue, Object mapRecords) {
        try {
            String url = "https://api.namecheap.com/xml.response";
            NameCheap namecheap = new NameCheap(Integer.valueOf(accountId));
            LinkedHashMap<String, String> records = (getMapRecords() != null && !getMapRecords().isEmpty()) ? getMapRecords() : (LinkedHashMap)mapRecords;
            records.put("ApiUser", namecheap.username);
            records.put("ApiKey", namecheap.apiKey);
            records.put("UserName", namecheap.username);
            records.put("ClientIP", namecheap.whiteListedIp);
            records.put("Command", "namecheap.domains.dns.setHosts");
            String result = Agents.put(url, records, 30);

            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = XML.toJSONObject(result);
            if (response != null && response.has("ApiResponse") && response.getJSONObject("ApiResponse") != null && response.getJSONObject("ApiResponse").has("Status")) {
                String status = response.getJSONObject("ApiResponse").getString("Status");
                if ("OK".equalsIgnoreCase(status)){
                    return domainValue + " DNS records updated successfully !";
                }
                String error = response.getJSONObject("ApiResponse").getJSONObject("Errors").getJSONObject("Error").getString("content");
                return "Error while updating " + domainValue + " DNS records : " + error;
            }

        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return "Error while updating " + domainValue + " DNS records !";
    }

    public JSONArray getDomainRecords(int accountId, String domain) {
        JSONArray records = new JSONArray();
        try {
            String url = "https://api.namecheap.com/xml.response";
            NameCheap namecheap = new NameCheap(Integer.valueOf(accountId));
            LinkedHashMap<String,String> recrd = new LinkedHashMap<>();
            recrd.put("ApiUser", namecheap.username);
            recrd.put("ApiKey", namecheap.apiKey);
            recrd.put("UserName", namecheap.username);
            recrd.put("ClientIP", namecheap.whiteListedIp);
            recrd.put("Command", "namecheap.domains.dns.getHosts");

            String[] parts = domain.split(Pattern.quote("."));
            if (parts.length < 2){
                throw new DatabaseException("Invalid domain name !");
            }

            recrd.put("SLD", parts[0]);

            if (parts.length > 2) {
                recrd.put("TLD", parts[1] + "." + parts[2]);
            } else {
                recrd.put("TLD", parts[1]);
            }

            String result = Agents.get(url, recrd, 30);
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = XML.toJSONObject(result);
            if (response != null && response.has("ApiResponse") && response.get("ApiResponse") instanceof JSONObject && response.getJSONObject("ApiResponse").has("CommandResponse") && response.getJSONObject("ApiResponse").get("CommandResponse") instanceof JSONObject && response.getJSONObject("ApiResponse").getJSONObject("CommandResponse").has("DomainDNSGetHostsResult") && response.getJSONObject("ApiResponse").getJSONObject("CommandResponse").get("DomainDNSGetHostsResult") instanceof JSONObject && response.getJSONObject("ApiResponse").getJSONObject("CommandResponse").getJSONObject("DomainDNSGetHostsResult").has("host")) {

                JSONObject rsp = response.getJSONObject("ApiResponse").getJSONObject("CommandResponse").getJSONObject("DomainDNSGetHostsResult");

                if (rsp.has("host")) {

                    JSONArray hosts = new JSONArray();
                    if (rsp.get("host") instanceof JSONObject) {
                        hosts.put(rsp.getJSONObject("host"));
                    } else {
                        hosts = rsp.getJSONArray("host");
                    }
                    if (hosts != null && hosts.length() > 0) {
                        JSONObject rcd = null;
                        for (int i = 0; i < hosts.length(); i++) {
                            rcd = new JSONObject();
                            rcd.put("host", String.valueOf(hosts.getJSONObject(i).get("Name")));
                            rcd.put("type", String.valueOf(hosts.getJSONObject(i).get("Type")));
                            rcd.put("value", String.valueOf(hosts.getJSONObject(i).get("Address")));
                            rcd.put("mxpref", String.valueOf(hosts.getJSONObject(i).get("MXPref")));
                            rcd.put("ttl", String.valueOf(hosts.getJSONObject(i).get("TTL")));
                            records.put(rcd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return records;
    }

    public void setRecords(int accountId, String domainValue) {
        setMapRecords(new LinkedHashMap<>());
    }

    public void addRecord(String record, String value) {
        getMapRecords().put(record, value);
    }

    public void setupRecords(String record, String mainIp, Domain domain, ManagementServer mangmentserver) {
        addRecord("HostName1", "@");
        addRecord("RecordType1", record);
        addRecord("Address1", mainIp);
        addRecord("TTL1", "60");
        addRecord("HostName2", "www");
        addRecord("RecordType2", record);
        addRecord("Address2", mainIp);
        addRecord("TTL2", "60");
        addRecord("HostName3", "mail");
        addRecord("RecordType3", record);
        addRecord("Address3", mainIp);
        addRecord("TTL3", "60");
        addRecord("HostName4", "ns1");
        addRecord("RecordType4", record);
        addRecord("Address4", mainIp);
        addRecord("TTL4", "60");
        addRecord("HostName5", domain.value);
        addRecord("RecordType5", "NS");
        addRecord("Address5", "ns1." + domain.value);
        addRecord("TTL5", "60");
        addRecord("EmailType", "MX");
        addRecord("HostName6", "@");
        addRecord("RecordType6", "MX");
        if (mangmentserver == null || mangmentserver.getEmpty()) {
            addRecord("Address6", "mail." + domain.value);
        } else if (!"".equals(mangmentserver.hostName)) {
            addRecord("Address6", mangmentserver.hostName);
        } else {
            addRecord("Address6", mangmentserver.mainIp);
        }
        addRecord("MXPre6", "10");
        addRecord("TTL6", "60");
    }

    public void addSPFDkim(int nbcount, String mainIp, String hostName, String record, String vspf, String dkim) {
        if (!"".equals(dkim)) {
            addRecord("HostName" + nbcount, "mail._domainkey." + hostName);
            addRecord("RecordType" + nbcount, "TXT");
            //addRecord("Address" + nbcount, dkim);
            addRecord("Address" + nbcount, StringUtils.replace(dkim, "\"", ""));
            addRecord("TTL" + nbcount, "60");
            nbcount++;
        }
        addRecord("HostName" + nbcount, hostName);
        addRecord("RecordType" + nbcount, record);
        addRecord("Address" + nbcount, mainIp);
        addRecord("TTL" + nbcount, "60");
        addRecord("HostName" + ++nbcount, hostName);
        addRecord("RecordType" + nbcount, "TXT");
        addRecord("Address" + nbcount, vspf);
        addRecord("TTL" + nbcount, "60");
    }

    public void setupDkimDmarc(int nbcount, String dkim, String mainIp, boolean hasIpV6, String vspf, boolean dmarc, Domain domain) {
        addRecord("HostName" + nbcount, "*");
        if (hasIpV6) {
            addRecord("RecordType" + nbcount, "AAAA");
        } else {
            addRecord("RecordType" + nbcount, "A");
        }
        addRecord("Address" + nbcount, mainIp);
        addRecord("TTL" + nbcount, "60");
        addRecord("HostName" + ++nbcount, "@");
        addRecord("RecordType" + nbcount, "TXT");
        addRecord("Address" + nbcount, vspf);
        addRecord("TTL" + nbcount, "60");
        nbcount++;
        if (!"".equals(dkim)) {
            addRecord("HostName" + nbcount, "mail._domainkey");
            addRecord("RecordType" + nbcount, "TXT");
            addRecord("Address" + nbcount, StringUtils.replace(dkim, "\"", ""));
            addRecord("TTL" + nbcount, "60");
            nbcount++;
        }
        if (dmarc) {
            addRecord("HostName" + nbcount, "_dmarc");
            addRecord("RecordType" + nbcount, "TXT");
            addRecord("Address" + nbcount, "v=DMARC1;p=reject;rua=mailto:reply@" + domain.value + ";ruf=mailto:reply@" + domain.value);
            addRecord("TTL" + nbcount, "60");
            nbcount++;
        }
        String[] arrayOfString = domain.value.split(Pattern.quote("."));
        addRecord("SLD", arrayOfString[0]);
        if (arrayOfString.length > 2) {
            addRecord("TLD", arrayOfString[1] + "." + arrayOfString[2]);
        } else {
            addRecord("TLD", arrayOfString[1]);
        }
    }
}