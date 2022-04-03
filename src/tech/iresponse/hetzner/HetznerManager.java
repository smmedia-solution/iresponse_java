package tech.iresponse.hetzner;

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
import tech.iresponse.models.admin.HetznerAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.TypesParser;

public class HetznerManager {

    private static final int TIMES = 60;
    private static final String API_LINK = "https://api.hetzner.cloud/";
    private HetznerAccount account;

    public HashMap createInstance(String hostName, String region, String os, String size, String rootPass) {
        try {
            HttpPost httpPost = new HttpPost("https://api.hetzner.cloud//v1/servers");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            JSONObject jSONObject1 = new JSONObject();
            jSONObject1.put("name", hostName);
            jSONObject1.put("location", region);
            jSONObject1.put("image", getImageId(os));
            jSONObject1.put("start_after_create", "true");
            jSONObject1.put("server_type", size);
            jSONObject1.put("user_data", "#!/bin/bash -xe\necho \"Started...\"\npwd\n\nsed -i 's|PermitRootLogin without-password|PermitRootLogin yes|' /etc/ssh/sshd_config\nsed -i 's|PasswordAuthentication no|PasswordAuthentication yes|' /etc/ssh/sshd_config\n\necho 'root:" + rootPass + "' | chpasswd\nservice sshd reload");
            httpPost.setEntity((HttpEntity)new StringEntity(jSONObject1.toString()));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("server") && response.get("server") instanceof JSONObject) {
                HashMap<String, String> instances = new HashMap<>();
                instances.put("id", response.getJSONObject("server").getString("id"));
                instances.put("password", response.getString("root_password"));
                instances.put("name", response.getJSONObject("server").getString("name"));
                instances.put("status", response.getJSONObject("server").getString("status"));
                instances.put("disk", response.getJSONObject("server").getJSONObject("server_type").getString("disk"));
                instances.put("memory", response.getJSONObject("server").getJSONObject("server_type").getString("memory"));
                instances.put("image", response.getJSONObject("server").getJSONObject("image").getString("id"));
                instances.put("main_ip", response.getJSONObject("server").getJSONObject("public_net").getJSONObject("ipv4").getString("ip"));
                instances.put("region", response.getJSONObject("server").getJSONObject("datacenter").getString("name"));
                return instances;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getInstances(String status) {
        try {
            String link = "https://api.hetzner.cloud/" + ((status != null) ? ("/v1/servers?sort=id:asc&status=" + status) : "/v1/servers?sort=id:asc");
            HttpGet httpGet = new HttpGet(link);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("servers") && response.get("servers") instanceof JSONArray) {
                JSONArray servers = response.getJSONArray("servers");
                if (servers.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    for (int i = 0; i < servers.length(); i++) {
                        JSONObject servr = servers.getJSONObject(i);
                        if (servr.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", servr.getString("id"));
                            params.put("name", servr.getString("name"));
                            params.put("status", servr.getString("status"));
                            params.put("disk", servr.getJSONObject("server_type").getString("disk"));
                            params.put("memory", servr.getJSONObject("server_type").getString("memory"));
                            params.put("image", servr.getJSONObject("image").getString("id"));
                            params.put("main_ip", servr.getJSONObject("public_net").getJSONObject("ipv4").getString("ip"));
                            params.put("region", servr.getJSONObject("datacenter").getString("name"));
                            instances.add(params);
                        }
                    }
                    return instances;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public HashMap getInstanceById(String instnceId) {
        try {
            HttpGet httpGet = new HttpGet("https://api.hetzner.cloud//v1/servers/" + instnceId);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("server") && response.get("server") instanceof JSONObject) {
                JSONObject server = response.getJSONObject("server");
                if (server.length() > 0) {
                    HashMap<String, String> instances = new HashMap<>();
                    instances.put("id", server.getString("id"));
                    instances.put("name", server.getString("name"));
                    instances.put("status", server.getString("status"));
                    instances.put("disk", server.getJSONObject("server_type").getString("disk"));
                    instances.put("memory", server.getJSONObject("server_type").getString("memory"));
                    instances.put("image", server.getJSONObject("image").getString("id"));
                    instances.put("main_ip", server.getJSONObject("public_net").getJSONObject("ipv4").getString("ip"));
                    instances.put("region", server.getJSONObject("datacenter").getString("name"));
                    return instances;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public boolean deleteInstance(String instanceId) {
        try {
            HttpDelete httpDelete = new HttpDelete("https://api.hetzner.cloud//v1/servers/" + instanceId);
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-Type", "application/json");
            httpDelete.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpDelete);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("error") && response.get("error") instanceof JSONObject) {
                return false;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return true;
    }

    public boolean executeAction(String instanceId, String action) {
        try {
            HttpPost httpPost = new HttpPost("https://api.hetzner.cloud//v1/servers/" + instanceId + "/actions/" + action);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("error") && response.get("error") instanceof JSONObject) {
                return false;
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return true;
    }

    public List getLocations() {
        try {
            HttpGet httpGet = new HttpGet("https://api.hetzner.cloud//v1/locations");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("locations") && response.get("locations") instanceof JSONArray) {
                JSONArray locations = response.getJSONArray("locations");
                if (locations.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject location = locations.getJSONObject(i);
                        if (location.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", location.getString("id"));
                            params.put("description", location.getString("description"));
                            params.put("name", location.getString("name"));
                            instances.add(params);
                        }
                    }
                    return instances;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getServerTypes() {
        try {
            HttpGet httpGet = new HttpGet("https://api.hetzner.cloud//v1/server_types");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("server_types") && response.get("server_types") instanceof JSONArray) {
                JSONArray serverTypes = response.getJSONArray("server_types");
                if (serverTypes.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    for (int i = 0; i < serverTypes.length(); i++) {
                        JSONObject server = serverTypes.getJSONObject(i);
                        if (server.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", server.getString("id"));
                            params.put("name", server.getString("name"));
                            params.put("description", server.getString("description"));
                            params.put("memory", server.getString("memory") + " GB");
                            params.put("disk", server.getString("disk") + " GB");
                            params.put("cpu", server.getString("cores") + " CPU");
                            instances.add(params);
                        }
                    }
                    return instances;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getImages() {
        try {
            HttpGet httpGet = new HttpGet("https://api.hetzner.cloud//v1/images?type=system&status=available&sort=name:asc");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + this.account.getApiKey());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("images") && response.get("images") instanceof JSONArray) {
                JSONArray images = response.getJSONArray("images");
                if (images.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject image = images.getJSONObject(i);
                        if (image.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", image.getString("id"));
                            params.put("name", image.getString("name"));
                            params.put("description", image.getString("description"));
                            instances.add(params);
                        }
                    }
                    return instances;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public String getImageId(String os) {
        try {
            List<HashMap<String, String>>  instances = getImages();
            if (instances != null && !instances.isEmpty())
                for (HashMap instnce : instances) {
                    if (os.equalsIgnoreCase((String)instnce.get("name")))
                        return (String)instnce.get("id");
                }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    protected CloseableHttpClient createBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000);
        BasicCredentialsProvider basicCredentialsProvider = null;
        if (this.account.proxyIp != null && !"".equals(this.account.proxyIp) && this.account.proxyPort != null && !"".equals(this.account.proxyPort)) {
            if (this.account.proxyUsername != null && !"".equals(this.account.proxyUsername) && this.account.proxyPassword != null && !"".equals(this.account.proxyPassword)) {
                basicCredentialsProvider = new BasicCredentialsProvider();
                basicCredentialsProvider.setCredentials(new AuthScope(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)), (Credentials)new UsernamePasswordCredentials(this.account.proxyUsername, this.account.proxyPassword));
            }
            builder.setProxy(new HttpHost(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)));
        }
        return (basicCredentialsProvider != null) ? HttpClientBuilder.create().setDefaultRequestConfig(builder.build()).setDefaultCredentialsProvider((CredentialsProvider)basicCredentialsProvider).build() : HttpClientBuilder.create().setDefaultRequestConfig(builder.build()).build();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof HetznerManager))
            return false;
        HetznerManager do1 = (HetznerManager)paramObject;
        if (!do1.exists(this))
            return false;
        HetznerAccount const1 = getAccount();
        HetznerAccount const2 = do1.getAccount();
        return !((const1 == null) ? (const2 != null) : !const1.equals(const2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof HetznerManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        HetznerAccount const1 = getAccount();
        return n * 59 + ((const1 == null) ? 43 : const1.hashCode());
    }

    public HetznerAccount getAccount() {
        return account;
    }

    public void setAccount(HetznerAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "HetznerManager(account=" + getAccount() + ")";
    }
}
