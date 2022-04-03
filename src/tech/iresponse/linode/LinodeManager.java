package tech.iresponse.linode;

import java.io.IOException;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.LinodeAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.TypesParser;

public class LinodeManager {

    private static final int TIMES = 60;
    private static final String API_LINK = "https://api.linode.com/";
    private LinodeAccount account;

    public String createInstance(String hostName, String size, String region, String os, String rootPass) {
        JSONObject response = null;
        try {
            HttpPost httpPost = new HttpPost("https://api.linode.com//v4/linode/instances");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + this.account.getToken());

            JSONObject params = new JSONObject();
            params.put("backups_enabled", false);
            params.put("image", "centos-6".equals(os) ? "linode/centos6.8" : "linode/centos7");
            params.put("root_pass", rootPass);
            params.put("booted", true);
            params.put("label", hostName);
            params.put("type", size);
            params.put("region", region);

            httpPost.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse chr = this.createBuilder().execute((HttpUriRequest)httpPost);

            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }

            response = new JSONObject(result);
            if (response.length() > 0 && response.has("id")){
                return response.getString("id");
            }

        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return null;
    }

    public HashMap getInstanceInfos(String instanceId) {
        try {
            HttpGet httpGet = new HttpGet("https://api.linode.com//v4/linode/instances/" + instanceId);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());

            CloseableHttpResponse chr = this.createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());

            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("id")) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap = new HashMap<>();
                hashMap.put("id", response.getString("id"));
                hashMap.put("label", response.getString("label"));
                hashMap.put("status", response.getString("status"));
                hashMap.put("disk", response.getJSONObject("specs").getString("disk"));
                hashMap.put("memory", response.getJSONObject("specs").getString("memory"));
                hashMap.put("image", response.getString("image"));
                hashMap.put("main_ip", response.getJSONArray("ipv4").getString(0));
                hashMap.put("region", response.getString("region"));
                return hashMap;
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return null;
    }

    public List do3() {
        try {
            HttpGet httpGet = new HttpGet("https://api.linode.com//v4/linode/instances");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getToken());

            CloseableHttpResponse chr = this.createBuilder().execute((HttpUriRequest)httpGet);

            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("data") && response.get("data") instanceof JSONArray) {
                JSONArray data = response.getJSONArray("data");
                if (data.length() > 0) {
                    ArrayList instances = new ArrayList();
                    for (int b = 0; b < data.length(); b++) {
                        JSONObject instance = data.getJSONObject(b);
                        if (instance.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", instance.getString("id"));
                            params.put("label", instance.getString("label"));
                            params.put("status", instance.getString("status"));
                            params.put("disk", instance.getJSONObject("specs").getString("disk"));
                            params.put("memory", instance.getJSONObject("specs").getString("memory"));
                            params.put("image", instance.getString("image"));
                            params.put("main_ip", instance.getJSONArray("ipv4").getString(0));
                            params.put("region", instance.getString("region"));
                            instances.add(params);
                        }
                    }
                    return instances;
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return null;
    }

    public boolean deleteInstance(String instanceId) {
        try {
            HttpDelete httpDelete = new HttpDelete("https://api.linode.com//v4/linode/instances/" + instanceId);
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-Type", "application/json");
            httpDelete.setHeader("Authorization", "Bearer " + this.account.getToken());

            this.createBuilder().execute((HttpUriRequest)httpDelete);
            return true;

        } catch (IOException iOException) {
            Loggers.error(iOException);
            return false;
        }
    }

    public boolean executeAction(String instanceId, String action) {
        try {
        HttpPost httpPost = new HttpPost("https://api.linode.com//v4/linode/instances/" + instanceId + "/" + action);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + this.account.getToken());

        CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
        String result = EntityUtils.toString(chr.getEntity());
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        if (!"".equals(result)) {
            JSONObject response = new JSONObject(result);
            return (!response.has("errors") && response.length() == 0);
        }

        return true;

        } catch (Exception ex) {
            Loggers.error(ex);
            return false;
        }
    }

    public String updateInstanceRdns(String instanceId, String ips, String hostName) {
        try {
            HttpPut httpPut = new HttpPut("https://api.linode.com//v4/linode/instances/" + instanceId + "/ips/" + ips);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-Type", "application/json");
            httpPut.setHeader("Authorization", "Bearer " + this.account.getToken());

            JSONObject params = new JSONObject();
            params.put("rdns", hostName);

            httpPut.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse chr = this.createBuilder().execute((HttpUriRequest)httpPut);

            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("rdns") && response.get("address") instanceof JSONObject) {
                return response.getString("rdns");
            }

        } catch (Exception ex) {
            Loggers.error(ex);
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
        if (!(paramObject instanceof LinodeManager))
            return false;
        LinodeManager do1 = (LinodeManager)paramObject;
        if (!do1.exists(this))
            return false;
        LinodeAccount const1 = getAccount();
        LinodeAccount const2 = do1.getAccount();
        return !((const1 == null) ? (const2 != null) : !const1.equals(const2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof LinodeManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        LinodeAccount const1 = getAccount();
        return n * 59 + ((const1 == null) ? 43 : const1.hashCode());
    }

    public LinodeAccount getAccount() {
        return account;
    }

    public void setAccount(LinodeAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "LinodeManager(account=" + getAccount() + ")";
    }
}
