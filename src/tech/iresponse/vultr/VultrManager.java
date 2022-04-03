package tech.iresponse.vultr;

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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import tech.iresponse.models.admin.VultrAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.TypesParser;

public class VultrManager {

    private static final int TIMES = 60;
    private static final String API_LINK = "https://api.vultr.com/v1/";
    private VultrAccount account;

    public String createInstance(String hostName, String region, String os, String size) {
        try {
            HttpPost httpPost = new HttpPost("https://api.vultr.com/v1//v1/server/create");
            httpPost.setHeader("API-Key", this.account.getToken());
            ArrayList<BasicNameValuePair> arrayList = new ArrayList();
            arrayList.add(new BasicNameValuePair("DCID", region));
            arrayList.add(new BasicNameValuePair("VPSPLANID", size));
            arrayList.add(new BasicNameValuePair("OSID", getImageId(os)));
            arrayList.add(new BasicNameValuePair("hostname", hostName));
            arrayList.add(new BasicNameValuePair("label", hostName));
            httpPost.setEntity((HttpEntity)new UrlEncodedFormEntity(arrayList));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            System.out.println(response);
            if (response.length() > 0 && response.has("SUBID")) {
                return response.getString("SUBID");
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getInstances() {
        try {
            HttpGet httpGet = new HttpGet("https://api.vultr.com/v1//v1/server/list");
            httpGet.setHeader("API-Key", this.account.getToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0) {
                ArrayList<HashMap<String, String>> instances = new ArrayList();
                Iterator<String> it = response.keys();
                while (it.hasNext()) {
                    String nextValue = it.next();
                    if (response.get(nextValue) instanceof JSONObject) {
                        JSONObject server = response.getJSONObject(nextValue);
                        if (server.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", server.getString("SUBID"));
                            params.put("name", server.getString("label"));
                            params.put("status", server.getString("status"));
                            params.put("image", server.getString("VPSPLANID"));
                            params.put("main_ip", server.getString("main_ip"));
                            params.put("password", server.getString("default_password"));
                            params.put("server_type", server.getString("OSID"));
                            params.put("location", server.getString("DCID"));
                            instances.add(params);
                        }
                    }
                }
                return instances;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public HashMap getInstanceById(String id) {
        try {
            List<HashMap<String, String>> instances = getInstances();
            if (instances != null && !instances.isEmpty()) {
                for (HashMap instance : instances) {
                    if (id.equals(instance.get("id"))) {
                        return instance;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public boolean executeAction(String instanceId, String action) {
        try {
            HttpPost httpPost = new HttpPost("https://api.vultr.com/v1//v1/server/" + action);
            httpPost.setHeader("API-Key", this.account.getToken());
            ArrayList<BasicNameValuePair> arrayList = new ArrayList();
            arrayList.add(new BasicNameValuePair("SUBID", instanceId));
            httpPost.setEntity((HttpEntity)new UrlEncodedFormEntity(arrayList));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpPost);
            if (chr.getStatusLine().getStatusCode() == 200){
                return true;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return false;
    }

    public List getRegions() {
        try {
            HttpGet httpGet = new HttpGet("https://api.vultr.com/v1//v1/regions/list");
            httpGet.setHeader("API-Key", this.account.getToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0) {
                ArrayList<HashMap<String, String>> regions = new ArrayList();
                Iterator<String> it = response.keys();
                while (it.hasNext()) {
                    String nextValue = it.next();
                    if (response.get(nextValue) instanceof JSONObject) {
                        JSONObject region = response.getJSONObject(nextValue);
                        if (region.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", region.getString("DCID"));
                            params.put("name", region.getString("name") + "( " + region.getString("continent") + " - " + region.getString("country") + ")");
                            params.put("state", region.getString("state"));
                            params.put("ddos_protection", String.valueOf(region.get("ddos_protection")));
                            params.put("block_storage", String.valueOf(region.get("block_storage")));
                            regions.add(params);
                        }
                    }
                }
                return regions;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getPlans() {
        try {
            HttpGet httpGet = new HttpGet("https://api.vultr.com/v1//v1/plans/list");
            httpGet.setHeader("API-Key", this.account.getToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0) {
                ArrayList<HashMap<String, String>> plans = new ArrayList();
                Iterator<String> it = response.keys();
                while (it.hasNext()) {
                    String nextValue = it.next();
                    if (response.get(nextValue) instanceof JSONObject) {
                        JSONObject plan = response.getJSONObject(nextValue);
                        if (plan.length() > 0) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", plan.getString("VPSPLANID"));
                            params.put("name", plan.getString("name"));
                            params.put("type", plan.getString("plan_type"));
                            params.put("memory", plan.getString("ram") + " Mb");
                            params.put("disk", plan.getString("disk") + " Gb");
                            params.put("cpu", plan.getString("vcpu_count") + " CPU(s)");
                            plans.add(params);
                        }
                    }
                }
                return plans;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getImages() {
        try {
            HttpGet httpGet = new HttpGet("https://api.vultr.com/v1//v1/os/list");
            httpGet.setHeader("API-Key", this.account.getToken());
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0) {
                ArrayList<HashMap<String, String>> images = new ArrayList();
                Iterator<String> it = response.keys();
                while (it.hasNext()) {
                    String nextValue = it.next();
                    if (response.get(nextValue) instanceof JSONObject) {
                        JSONObject image = response.getJSONObject(nextValue);
                        if (image.length() > 0 && image.getString("arch").contains("x64")) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id", image.getString("OSID"));
                            params.put("name", image.getString("name"));
                            params.put("description", image.getString("name") + " " + image.getString("arch"));
                            images.add(params);
                        }
                    }
                }
                return images;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public String getImageId(String os) {
        try {
            List<HashMap<String, String>>  list = getImages();
            if (list != null && !list.isEmpty())
                for (HashMap hashMap : list) {
                    if (((String)hashMap.get("name")).toLowerCase().contains(os.toLowerCase().replaceAll(Pattern.quote("-"), " "))){
                        return (String)hashMap.get("id");
                    }
                }
        } catch (Exception exception) {
            Loggers.error(exception);
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
        if (!(paramObject instanceof VultrManager))
            return false;
        VultrManager do1 = (VultrManager)paramObject;
        if (!do1.exists(this))
            return false;
        VultrAccount const1 = getAccount();
        VultrAccount const2 = do1.getAccount();
        return !((const1 == null) ? (const2 != null) : !const1.equals(const2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof VultrManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        VultrAccount const1 = getAccount();
        return n * 59 + ((const1 == null) ? 43 : const1.hashCode());
    }

    public VultrAccount getAccount() {
        return account;
    }

    public void setAccount(VultrAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "VultrManager(account=" + getAccount() + ")";
    }
}
