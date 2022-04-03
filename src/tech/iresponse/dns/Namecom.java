package tech.iresponse.dns;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.NameCom;

public class Namecom extends DnsApi {

    public String setDomainRecords(int accountId, String domainValue, Object paramObject) {
        try {
            NameCom nmcom = new NameCom(Integer.valueOf(accountId));
            JSONArray records = (getJsonRecords() != null && getJsonRecords().length() > 0) ? getJsonRecords() : (JSONArray)paramObject;
            int response = 500;

            if (getJsonRecords() != null && getJsonRecords().length() > 0) {

                JSONArray record = getDomainRecords(accountId, domainValue);

                if (record != null && record.length() > 0){
                    for (int i = 0; i < record.length(); i++) {
                        deleteRecords(accountId, domainValue, record.getJSONObject(i).getInt("id"));
                    }
                }
            }

            if (records != null && records.length() > 0){
                for (int b = 0; b < records.length(); b++) {
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost httpPost = new HttpPost("https://api.name.com/v4/domains/" + domainValue + "/records");

                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64((nmcom.username + ":" + nmcom.apiKey).getBytes(StandardCharsets.ISO_8859_1))));
                    httpPost.setEntity((HttpEntity)new StringEntity(records.getJSONObject(b).toString()));

                    CloseableHttpResponse httpRsponse = httpClient.execute((HttpUriRequest)httpPost);
                    response = httpRsponse.getStatusLine().getStatusCode();
                }
            }
            if (response == 200){
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
            NameCom nmcom = new NameCom(Integer.valueOf(accountId));

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet url = new HttpGet("https://api.name.com/v4/domains/" + domainValue + "/records");
            url.setHeader("Content-Type", "application/json");
            url.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64((nmcom.username + ":" + nmcom.apiKey).getBytes(StandardCharsets.ISO_8859_1))));

            CloseableHttpResponse httpRsponse = httpClient.execute((HttpUriRequest)url);
            response = EntityUtils.toString(httpRsponse.getEntity());
            JSONObject hosts = new JSONObject(response);

            if (hosts.length() > 0 && hosts.has("records") && hosts.get("records") instanceof JSONArray && hosts.getJSONArray("records").length() > 0) {
                JSONObject rcd = null;
                for (int b = 0; b < hosts.getJSONArray("records").length(); b++) {

                    rcd = new JSONObject();
                    rcd.put("id", hosts.getJSONArray("records").getJSONObject(b).getInt("id"));

                    if (hosts.getJSONArray("records").getJSONObject(b).has("host")) {
                        rcd.put("host", hosts.getJSONArray("records").getJSONObject(b).getString("host"));
                    } else {
                        rcd.put("host", "@");
                    }

                    rcd.put("type", hosts.getJSONArray("records").getJSONObject(b).getString("type"));
                    rcd.put("value", hosts.getJSONArray("records").getJSONObject(b).getString("answer"));

                    if (hosts.getJSONArray("records").getJSONObject(b).has("priority")){
                        rcd.put("mxpref", hosts.getJSONArray("records").getJSONObject(b).getString("priority"));
                    }

                    rcd.put("ttl", hosts.getJSONArray("records").getJSONObject(b).getString("ttl"));
                    records.put(rcd);
                }
            }

        } catch (Exception e) {
            Loggers.error(e);
        }
        return records;
    }

    public boolean deleteRecords(int accountId, String domainValue, int id) {
        try {
            NameCom nmcom = new NameCom(Integer.valueOf(accountId));
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            HttpDelete httpDelete = new HttpDelete("https://api.name.com/v4/domains/" + domainValue + "/records/" + id);
            httpDelete.setHeader("Content-Type", "application/json");
            httpDelete.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64((nmcom.username + ":" + nmcom.apiKey).getBytes(StandardCharsets.ISO_8859_1))));

            CloseableHttpResponse httpRsponse = httpClient.execute((HttpUriRequest)httpDelete);
            if (httpRsponse.getStatusLine().getStatusCode() == 200){
                return true;
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return false;
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
                        rcd.put("data", result.getJSONObject(b).getString("answer"));
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

    public void setupRecords(String record, String ip, Domain domain, ManagementServer managServ) {
        try {
            JSONObject rcd = new JSONObject();
            rcd.put("host", "@");
            rcd.put("type", record);
            rcd.put("answer", ip);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("host", "www");
            rcd.put("type", record);
            rcd.put("answer", ip);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("host", "mail");
            rcd.put("type", record);
            rcd.put("answer", ip);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("host", "@");
            rcd.put("type", "MX");
            if (managServ != null && !managServ.getEmpty()) {
                rcd.put("answer", managServ.hostName);
            } else {
                rcd.put("answer", "mail." + domain.value);
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
                rcd1.put("host", "mail._domainkey." + hostName);
                rcd1.put("type", "TXT");
                rcd1.put("answer", StringUtils.replace(dkim, "\"", ""));
                rcd1.put("ttl", 600);
                addRecord(rcd1);
            }

            JSONObject rcd = new JSONObject();
            rcd.put("host", hostName);
            rcd.put("type", record);
            rcd.put("answer", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("host", hostName);
            rcd.put("type", "TXT");
            rcd.put("answer", vspf);
            rcd.put("ttl", 600);
            addRecord(rcd);

        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }

    public void setupDkimDmarc(int nbcount, String dkim, String mainIp, boolean hasIpV6, String vspf, boolean dmarc, Domain domain) {
        try {
            JSONObject rcd = new JSONObject();
            rcd.put("host", "*");
            if (hasIpV6) {
                rcd.put("type", "AAAA");
            } else {
                rcd.put("type", "A");
            }
            rcd.put("answer", mainIp);
            rcd.put("ttl", 600);
            addRecord(rcd);

            rcd = new JSONObject();
            rcd.put("host", "@");
            rcd.put("type", "TXT");
            rcd.put("answer", vspf);
            rcd.put("ttl", 600);
            addRecord(rcd);

            if (!"".equals(dkim)) {
                rcd = new JSONObject();
                rcd.put("host", "mail._domainkey");
                rcd.put("type", "TXT");
                rcd.put("answer", StringUtils.replace(dkim, "\"", ""));
                rcd.put("ttl", 600);
                addRecord(rcd);
            }

            if (dmarc) {
                rcd = new JSONObject();
                rcd.put("host", "_dmarc");
                rcd.put("RecordType", "TXT");
                rcd.put("Address", "v=DMARC1;p=reject;rua=mailto:reply@" + domain.value + ";ruf=mailto:reply@" + domain.value);
                rcd.put("TTL", 600);
                addRecord(rcd);
            }

        } catch (JSONException e) {
            Loggers.error((Throwable)e);
        }
    }
}