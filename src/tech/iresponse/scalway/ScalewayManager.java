package tech.iresponse.scalway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import tech.iresponse.models.admin.ScalewayAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.Files;

public class ScalewayManager {

    private static final int TIMES = 60;
    private static final String API_LINK = "https://api.scaleway.com/";
    private ScalewayAccount account;

    public String createInstance(String hostName, String region, String os, String size) {
        try {
            HttpPost httpPost = new HttpPost("https://api.scaleway.com//instance/v1/zones/" + region + "/servers");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-Auth-Token", this.account.getApiToken());
            JSONObject params = new JSONObject();
            params.put("name", hostName);
            params.put("volumes", new JSONObject());
            params.put("image", getImageId(os, region));
            params.put("organization", this.account.getOrganization());
            params.put("commercial_type", size);
            httpPost.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("server") && response.get("server") instanceof JSONObject) {
                return response.getJSONObject("server").getString("id");
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getInstancesInfos(String region, String stats) {
        try {
            String str1 = "https://api.scaleway.com/" + ((stats != null) ? ("/instance/v1/zones/" + region + "/servers?state=" + stats) : ("/instance/v1/zones/" + region + "/servers"));
            HttpGet httpGet = new HttpGet(str1);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("X-Auth-Token", this.account.getApiToken());
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
                        JSONObject srver = servers.getJSONObject(i);
                        if (srver.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", srver.getString("id"));
                            params.put("name", srver.getString("name"));
                            params.put("status", srver.getString("state"));
                            params.put("image", srver.getJSONObject("image").getString("id"));
                            params.put("main_ip", srver.getJSONObject("public_ip").getString("address"));
                            params.put("private_ip", srver.getString("private_ip"));
                            params.put("server_type", srver.getString("commercial_type"));
                            params.put("region", region);
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

    public HashMap getInstance(String instanceId, String region, String stats) {
        try {
            List<HashMap<String, String>> instances = getInstancesInfos(region, stats);
            if (instances != null && !instances.isEmpty())
                for (HashMap server : instances) {
                    if (((String)server.get("id")).equals(instanceId)) {
                        return server;
                    }
                }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public boolean executeAction(String instanceId, String region, String action) {
        try {
            HttpPost httpPost = new HttpPost("https://api.scaleway.com//instance/v1/zones/" + region + "/servers/" + instanceId + "/action");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-Auth-Token", this.account.getApiToken());
            JSONObject params = new JSONObject();
            params.put("action", action);
            httpPost.setEntity((HttpEntity)new StringEntity(params.toString()));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.has("task") && response.get("task") instanceof JSONObject) {
                return true;
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return false;
    }

    public List gg(String region) {
        try {
            HttpGet httpGet = new HttpGet("https://api.scaleway.com//instance/v1/zones/" + region + "/products/servers?per_page=100");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("X-Auth-Token", this.account.getApiToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("servers") && response.get("servers") instanceof JSONObject) {
                JSONObject servers = response.getJSONObject("servers");
                if (servers.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    Iterator<String> iterator = servers.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        if (servers.get(next) instanceof JSONObject) {
                            JSONObject servr = servers.getJSONObject(next);
                            if (servr.length() > 0) {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("id", next);
                                params.put("name", next);
                                params.put("arch", servr.getString("arch"));
                                params.put("memory", Files.getMachine(TypesParser.safeParseLong(servr.getString("ram"))));
                                params.put("disk", Files.getMachine(TypesParser.safeParseLong(servr.getJSONObject("volumes_constraint").getString("max_size"))));
                                params.put("cpu", servr.getString("ncpus") + " CPU(s)");
                                instances.add(params);
                            }
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

    public List getInstanceInfosByRegion(String region) {
        try {
            HttpGet httpGet = new HttpGet("https://api.scaleway.com//instance/v1/zones/" + region + "/images?per_page=100");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("X-Auth-Token", this.account.getApiToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("images") && response.get("images") instanceof JSONArray) {
                JSONArray images = response.getJSONArray("images");
                if (images.length() > 0) {
                    ArrayList<HashMap<String, String>> arrayList = new ArrayList();
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject img = images.getJSONObject(i);
                        if (img.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", img.getString("id"));
                            params.put("name", img.getString("name"));
                            params.put("description", img.getString("name") + " " + img.getString("arch"));
                            params.put("organization", img.getString("organization"));
                            arrayList.add(params);
                        }
                    }
                    return arrayList;
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public String getImageId(String os, String region) {
        try {
            List<HashMap<String, String>> instances = getInstanceInfosByRegion(region);
            if (instances != null && !instances.isEmpty()) {
                for (HashMap instnce : instances) {
                    if (((String)instnce.get("name")).toLowerCase().contains(os.toLowerCase().replaceAll(Pattern.quote("-"), " "))) {
                        return (String)instnce.get("id");
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List ff(String region) {
        try {
            HttpGet httpGet = new HttpGet("https://api.scaleway.com//instance/v1/zones/" + region + "/placement_groups?per_page=100");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("X-Auth-Token", this.account.getApiToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("placement_groups") && response.get("placement_groups") instanceof JSONArray) {
                JSONArray placementGroups = response.getJSONArray("placement_groups");
                if (placementGroups.length() > 0) {
                    ArrayList<HashMap<String, String>> instances = new ArrayList();
                    for (int i = 0; i < placementGroups.length(); i++) {
                        JSONObject serv = placementGroups.getJSONObject(i);
                        if (serv.length() > 0) {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", serv.getString("id"));
                            hashMap.put("name", serv.getString("name"));
                            hashMap.put("organization", serv.getString("organization"));
                            instances.add(hashMap);
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

    protected CloseableHttpClient createBuilder() {
        RequestConfig.Builder buildr = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000);
        BasicCredentialsProvider bcp = null;
        if (this.account.proxyIp != null && !"".equals(this.account.proxyIp) && this.account.proxyPort != null && !"".equals(this.account.proxyPort)) {
            if (this.account.proxyUsername != null && !"".equals(this.account.proxyUsername) && this.account.proxyPassword != null && !"".equals(this.account.proxyPassword)) {
                bcp = new BasicCredentialsProvider();
                bcp.setCredentials(new AuthScope(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)), (Credentials)new UsernamePasswordCredentials(this.account.proxyUsername, this.account.proxyPassword));
            }
            buildr.setProxy(new HttpHost(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)));
        }
        return (bcp != null) ? HttpClientBuilder.create().setDefaultRequestConfig(buildr.build()).setDefaultCredentialsProvider((CredentialsProvider)bcp).build() : HttpClientBuilder.create().setDefaultRequestConfig(buildr.build()).build();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ScalewayManager))
            return false;
        ScalewayManager do1 = (ScalewayManager)paramObject;
        if (!do1.exists(this))
            return false;
        ScalewayAccount const1 = getAccount();
        ScalewayAccount const2 = do1.getAccount();
        return !((const1 == null) ? (const2 != null) : !const1.equals(const2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ScalewayManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        ScalewayAccount const1 = getAccount();
        return n * 59 + ((const1 == null) ? 43 : const1.hashCode());
    }

    public ScalewayAccount getAccount() {
        return account;
    }

    public void setAccount(ScalewayAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "ScalewayManager(account=" + getAccount() + ")";
    }
}
