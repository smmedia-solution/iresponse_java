package tech.iresponse.digitalocean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.DigitalOceanAccount;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;

public class DigitalOceanManager {

    private static final int TIMES = 60;
    private static final String API_LINK = "https://api.digitalocean.com/v2";
    private DigitalOceanAccount account;

    public String getDropletInfos(String hostName, String region, String os, String size, String sshKeyId) {
        try {
            HttpPost post = new HttpPost("https://api.digitalocean.com/v2/droplets?page=1&per_page=1000");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + this.account.getToken());

            JSONObject params = new JSONObject();
            params.put("name", hostName);
            params.put("region", region);
            params.put("image", "centos-6".equalsIgnoreCase(os) ? "centos-6-x64" : "centos-7-x64");
            params.put("size", size);
            params.put("ssh_keys", new JSONArray("[" + sshKeyId + "]"));

            post.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)post);
            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("droplet") && response.get("droplet") instanceof JSONObject){
                return response.getJSONObject("droplet").getString("id");
            }

        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public HashMap getDropletInfos(String dropletId) {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/droplets/" + dropletId);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());
            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("droplet") && response.get("droplet") instanceof JSONObject) {
                JSONObject droplet = response.getJSONObject("droplet");
                if (droplet.length() > 0) {
                    HashMap<String, String> params = new HashMap<>();
                    params = new HashMap<>();
                    params.put("id", droplet.getString("id"));
                    params.put("name", droplet.getString("name"));
                    params.put("status", droplet.getString("status"));
                    params.put("disk", droplet.getString("disk"));
                    params.put("memory", droplet.getString("memory"));
                    params.put("image", droplet.getJSONObject("image").getString("id"));

                    if (droplet.has("networks") && droplet.get("networks") instanceof JSONObject && droplet.getJSONObject("networks").has("v4") && droplet.getJSONObject("networks").get("v4") instanceof JSONArray){
                        for (int b = 0; b < droplet.getJSONObject("networks").getJSONArray("v4").length(); b++) {
                            String ipAddress = droplet.getJSONObject("networks").getJSONArray("v4").getJSONObject(b).getString("ip_address");
                            if (!ipAddress.startsWith("10.")){
                                params.put("main_ip", ipAddress);
                            }
                        }
                    }
                    params.put("region", droplet.getJSONObject("region").getString("name"));
                    return params;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public List g3() {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/droplets?page=1&per_page=1000");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());

            CloseableHttpResponse chr = this.createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("droplets") && response.get("droplets") instanceof JSONArray) {
                JSONArray droplets = response.getJSONArray("droplets");
                if (droplets.length() > 0) {
                    ArrayList arrayList = new ArrayList();
                    for (int b = 0; b < droplets.length(); b++) {
                        JSONObject droplet = droplets.getJSONObject(b);
                        if (droplet.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", droplet.getString("id"));
                            params.put("name", droplet.getString("name"));
                            params.put("status", droplet.getString("status"));
                            params.put("disk", droplet.getString("disk"));
                            params.put("memory", droplet.getString("memory"));
                            params.put("image", droplet.getJSONObject("image").getString("id"));
                            params.put("main_ip", droplet.getJSONObject("networks").getJSONArray("v4").getJSONObject(0).getString("ip_address"));
                            params.put("region", droplet.getJSONObject("region").getString("name"));
                            arrayList.add(params);
                        }
                    }
                    return arrayList;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public boolean deleteDroplet(String dropletId) {
        try {
            HttpDelete httpDelete = new HttpDelete("https://api.digitalocean.com/v2/droplets/" + dropletId);
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-Type", "application/json");
            httpDelete.setHeader("Authorization", "Bearer " + this.account.getToken());
            this.createBuilder().execute((HttpUriRequest)httpDelete);
            return true;
        } catch (Exception e) {
            Loggers.error(e);
            return false;
        }
    }

    public boolean executeAction(String dropletId, String action) {
        try {
            HttpPost post = new HttpPost("https://api.digitalocean.com/v2/droplets/" + dropletId + "/actions");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + this.account.getToken());
            JSONObject params = new JSONObject();
            params.put("type", action);
            post.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse chresponse = this.createBuilder().execute((HttpUriRequest)post);
            String result = EntityUtils.toString(chresponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("action") && response.get("action") instanceof JSONObject){
                return true;
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return false;
    }

    public String SshKeys(String name, String publicKey) {
        try {
            HttpPost post = new HttpPost("https://api.digitalocean.com/v2/account/keys");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + this.account.getToken());

            JSONObject params = new JSONObject();
            params.put("name", name);
            params.put("public_key", publicKey);

            post.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)post);

            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("ssh_key") && response.get("ssh_key") instanceof JSONObject){
                return response.getJSONObject("ssh_key").getString("id");
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public List getSshKeys() {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/account/keys");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());
            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)httpGet);

            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("ssh_keys") && response.get("ssh_keys") instanceof JSONArray) {
                JSONArray sshKeys = response.getJSONArray("ssh_keys");
                if (sshKeys.length() > 0) {
                    ArrayList keys = new ArrayList();
                    for (int b = 0; b < sshKeys.length(); b++) {
                        JSONObject values = sshKeys.getJSONObject(b);
                        if (values.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", values.getString("id"));
                            params.put("name", values.getString("name"));
                            params.put("fingerprint", values.getString("fingerprint"));
                            params.put("public_key", values.getString("public_key"));
                            keys.add(params);
                        }
                    }
                    return keys;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public List for8() {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/regions");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());

            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("regions") && response.get("regions") instanceof JSONArray) {
                JSONArray jSONArray = response.getJSONArray("regions");
                if (jSONArray.length() > 0) {
                    ArrayList names = new ArrayList();
                    for (int b = 0; b < jSONArray.length(); b++) {
                        JSONObject values = jSONArray.getJSONObject(b);
                        if (values.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("slug", values.getString("slug"));
                            params.put("name", values.getString("name"));
                            names.add(params);
                        }
                    }
                    return names;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public List int9() {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/sizes");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());

            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("sizes") && response.get("sizes") instanceof JSONArray) {
                JSONArray jSONArray = response.getJSONArray("sizes");
                if (jSONArray.length() > 0) {
                    ArrayList prices = new ArrayList();
                    for (int b = 0; b < jSONArray.length(); b++) {
                        JSONObject values = jSONArray.getJSONObject(b);
                        if (values.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("slug", values.getString("slug"));
                            params.put("memory", values.getString("memory") + " MB");
                            params.put("disk", values.getString("disk") + " GB");
                            params.put("cpu", values.getString("vcpus") + " CPU");
                            params.put("transfer", values.getString("transfer") + " TB");
                            params.put("price_monthly", "$" + values.getString("price_monthly") + "/mo");
                            params.put("price_hourly", "$" + values.getString("price_hourly").substring(0, 5) + "/hour");
                            prices.add(params);
                        }
                    }
                    return prices;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    public List new10() {
        try {
            HttpGet httpGet = new HttpGet("https://api.digitalocean.com/v2/images?page=1&per_page=1000");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());
            CloseableHttpResponse closeableHttpResponse = this.createBuilder().execute((HttpUriRequest)httpGet);

            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("images") && response.get("images") instanceof JSONArray) {
                JSONArray jSONArray = response.getJSONArray("images");
                if (jSONArray.length() > 0) {
                    ArrayList infos = new ArrayList();
                    for (int b = 0; b < jSONArray.length(); b++) {
                        JSONObject values = jSONArray.getJSONObject(b);
                        if (values.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", values.getString("id"));
                            params.put("name", values.getString("name"));
                            params.put("slug", values.getString("slug"));
                            params.put("description", values.getString("description"));
                            infos.add(params);
                        }
                    }
                    return infos;
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return null;
    }

    protected CloseableHttpClient createBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000);
        BasicCredentialsProvider bcp = null;

        if (this.account.proxyIp != null && !"".equals(this.account.proxyIp) && this.account.proxyPort != null && !"".equals(this.account.proxyPort)) {
            if (this.account.proxyUsername != null && !"".equals(this.account.proxyUsername) && this.account.proxyPassword != null && !"".equals(this.account.proxyPassword)) {
                bcp = new BasicCredentialsProvider();
                bcp.setCredentials(new AuthScope(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)), (Credentials)new UsernamePasswordCredentials(this.account.proxyUsername, this.account.proxyPassword));
            }
            builder.setProxy(new HttpHost(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)));
        }
        return (bcp != null) ? HttpClientBuilder.create().setDefaultRequestConfig(builder.build()).setDefaultCredentialsProvider((CredentialsProvider)bcp).build() : HttpClientBuilder.create().setDefaultRequestConfig(builder.build()).build();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DigitalOceanManager))
            return false;
        DigitalOceanManager do1 = (DigitalOceanManager)paramObject;
        if (!do1.exists(this))
            return false;
        DigitalOceanAccount char1 = getAccount();
        DigitalOceanAccount char2 = do1.getAccount();
        return !((char1 == null) ? (char2 != null) : !char1.equals(char2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DigitalOceanManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        DigitalOceanAccount char1 = getAccount();
        return n * 59 + ((char1 == null) ? 43 : char1.hashCode());
    }

    public DigitalOceanAccount getAccount() {
        return account;
    }

    public void setAccount(DigitalOceanAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "DigitalOceanManager(account=" + getAccount() + ")";
    }
}
