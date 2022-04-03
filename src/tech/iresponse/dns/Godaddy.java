package tech.iresponse.dns;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.GoDaddy;
import tech.iresponse.models.admin.ManagementServer;

public class Godaddy extends DnsApi {

    public String setDomainRecords(int accountId, String domainValue, Object mapRecords) {
        try {
            GoDaddy godaddy = new GoDaddy(Integer.valueOf(accountId));
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            JSONArray records = (getJsonRecords() != null && getJsonRecords().length() > 0) ? getJsonRecords() : (JSONArray)mapRecords;
            HttpPut httpPut = new HttpPut("https://api.godaddy.com/v1/domains/" + domainValue + "/records");
            httpPut.setEntity((HttpEntity)new StringEntity(records.toString()));
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-Type", "application/json");
            httpPut.setHeader("Authorization", "sso-key " + godaddy.accessKey + ":" + godaddy.secretKey);
            CloseableHttpResponse closeableHttpResponse = httpClient.execute((HttpUriRequest)httpPut);
            if (closeableHttpResponse.getStatusLine().getStatusCode() == 200){
                return "Records Updated Successfully !";
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return "Error while updating " + domainValue + " DNS records !";
    }

    public JSONArray getDomainRecords(int accountId, String domainValue) {
        JSONArray records = new JSONArray();
        try {
            String response = "";
            GoDaddy godaddy = new GoDaddy(Integer.valueOf(accountId));
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet url = new HttpGet("https://api.godaddy.com/v1/domains/" + domainValue + "/records");
            url.setHeader("Accept", "application/json");
            url.setHeader("Content-Type", "application/json");
            url.setHeader("Authorization", "sso-key " + godaddy.accessKey + ":" + godaddy.secretKey);
            CloseableHttpResponse closeableHttpResponse = httpClient.execute((HttpUriRequest)url);
            response = EntityUtils.toString(closeableHttpResponse.getEntity());
            JSONArray hosts = new JSONArray(response);
            if (hosts.length() > 0) {
                JSONObject rcd = null;
                for (int i = 0; i < hosts.length(); i++) {
                    rcd = new JSONObject();
                    rcd.put("host", hosts.getJSONObject(i).getString("name"));
                    rcd.put("type", hosts.getJSONObject(i).getString("type"));
                    rcd.put("value", hosts.getJSONObject(i).getString("data"));
                    if (hosts.getJSONObject(i).has("priority")){
                        rcd.put("mxpref", hosts.getJSONObject(i).getString("priority"));
                    }
                    rcd.put("ttl", hosts.getJSONObject(i).getString("ttl"));
                    records.put(rcd);
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return records;
    }

    public void setRecords(int accountId, String domainValue) {
        setJsonRecords(new JSONArray());
        JSONArray result = getDomainRecords(accountId, domainValue);
        try {
            JSONObject rcd = null;
            if (result != null && result.length() > 0){
                for (int b = 0; b < result.length(); b++) {
                    if (result.getJSONObject(b) != null && result.getJSONObject(b).has("type") && result.getJSONObject(b).getString("type").equals("NS")) {
                        rcd = new JSONObject();
                        rcd.put("name", result.getJSONObject(b).getString("host"));
                        rcd.put("data", result.getJSONObject(b).getString("value"));
                        rcd.put("type", result.getJSONObject(b).getString("type"));
                        rcd.put("ttl", result.getJSONObject(b).getInt("ttl"));
                        addRecord(rcd);
                    }
                }
            }
        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }

    public void addRecord(JSONObject records) {
        getJsonRecords().put(records);
    }

    public void setupRecords(String record, String mainIp, Domain domain, ManagementServer mangmentserver) {
        try {
            JSONObject rcd = new JSONObject();
            rcd.put("name", "@");
            rcd.put("type", record);
            rcd.put("data", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("name", "www");
            rcd.put("type", record);
            rcd.put("data", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("name", "mail");
            rcd.put("type", record);
            rcd.put("data", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("name", "@");
            rcd.put("type", "MX");

            if (mangmentserver != null && !mangmentserver.getEmpty()) {
                rcd.put("data", mangmentserver.hostName);
            } else {
                rcd.put("data", "mail." + domain.value);
            }

            rcd.put("priority", 10);
            rcd.put("ttl", 600);
            addRecord(rcd);

        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }

    public void addSPFDkim(int nbcount, String mainIp, String hostName, String record, String vspf, String dkim) {
        try {
            if (!"".equals(dkim)) {
                JSONObject rcd1 = new JSONObject();
                rcd1.put("name", "mail._domainkey." + hostName);
                rcd1.put("type", "TXT");
                //rcd1.put("data", dkim);
                rcd1.put("data", StringUtils.replace(dkim, "\"", ""));
                rcd1.put("ttl", 600);
                addRecord(rcd1);
            }

            JSONObject rcd = new JSONObject();
            rcd.put("name", hostName);
            rcd.put("type", record);
            rcd.put("data", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("name", hostName);
            rcd.put("type", "TXT");
            rcd.put("data", vspf);
            rcd.put("ttl", 600);
            addRecord(rcd);

        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }

    public void setupDkimDmarc(int nbcount, String dkim, String mainIp, boolean hasIpV6, String vspf, boolean dmarc, Domain domain){
        try {
            JSONObject rcd = new JSONObject();
            rcd.put("name", "*");
            if (hasIpV6) {
                rcd.put("type", "AAAA");
            } else {
                rcd.put("type", "A");
            }
            rcd.put("data", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("name", "@");
            rcd.put("type", "TXT");
            rcd.put("data", vspf);
            rcd.put("ttl", 600);
            addRecord(rcd);

            if (!"".equals(dkim)) {
                rcd = new JSONObject();
                rcd.put("name", "mail._domainkey");
                rcd.put("type", "TXT");
                rcd.put("data", StringUtils.replace(dkim, "\"", ""));
                rcd.put("ttl", 600);
                addRecord(rcd);
            }

            if (dmarc) {
                rcd = new JSONObject();
                rcd.put("name", "_dmarc");
                //rcd.put("RecordType", "TXT");
                rcd.put("type", "TXT");
                //rcd.put("Address", "v=DMARC1;p=reject;rua=mailto:reply@" + domain.value + ";ruf=mailto:reply@" + domain.value);
                rcd.put("data", "v=DMARC1;p=reject;rua=mailto:reply@" + domain.value + ";ruf=mailto:reply@" + domain.value);
                rcd.put("TTL", 600);
                addRecord(rcd);
            }

        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }
}