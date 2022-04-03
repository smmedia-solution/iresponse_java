package tech.iresponse.atlantic;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import tech.iresponse.models.admin.AtlanticAccount;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.TypesParser;

public class AtlanticManager {

    private static final int TIMES = 60;
    private AtlanticAccount account;

    public HashMap createInstance(String hostName, String region, String os, String size) {
        try {
            String link = executeAction("run-instance") + "&servername=" + hostName + "&planname=" + size + "&imageid=" + getImageId(os) + "&server_qty=1&vm_location" + region;
            HttpGet httpGet = new HttpGet(link);
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("run-instanceresponse") && response.get("run-instanceresponse") instanceof JSONObject) {
                JSONObject runResponse = response.getJSONObject("run-instanceresponse");
                if (runResponse.length() > 0 && runResponse.has("instancesSet") && runResponse.get("instancesSet") instanceof JSONObject) {
                    JSONObject instances = runResponse.getJSONObject("instancesSet");
                    if (instances.length() > 0) {
                        HashMap<String, String> instance = new HashMap<>();
                        instance.put("id", instances.getJSONObject("item").getString("instanceid"));
                        instance.put("main_ip", instances.getJSONObject("item").getString("ip_address"));
                        instance.put("password", instances.getJSONObject("item").getString("password"));
                        return instance;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getInstances() {
        try {
            HttpGet httpGet = new HttpGet(executeAction("list-instances"));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("list-instancesresponse") && response.get("list-instancesresponse") instanceof JSONObject) {
                JSONObject listInstances = response.getJSONObject("list-instancesresponse");
                if (listInstances.length() > 0 && listInstances.has("instancesSet") && listInstances.get("instancesSet") instanceof JSONObject) {
                    JSONObject instances = listInstances.getJSONObject("instancesSet");
                    if (instances.length() > 0) {
                        ArrayList<HashMap<String, String>> instance = new ArrayList();
                        Iterator<String> it = instances.keys();
                        while (it.hasNext()) {
                            String nextValue = it.next();
                            if (instances.get(nextValue) instanceof JSONObject) {
                                JSONObject instnce = instances.getJSONObject(nextValue);
                                if (instnce.length() > 0) {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("id", instnce.getString("InstanceId"));
                                    params.put("name", instnce.getString("vm_description"));
                                    params.put("status", instnce.getString("vm_status").toLowerCase());
                                    params.put("main_ip", instnce.getString("vm_ip_address"));
                                    params.put("memory", instnce.getString("vm_ram_req") + " GB");
                                    params.put("disk", instnce.getString("vm_disk_req") + " GB");
                                    params.put("cpu", instnce.getString("vm_cpu_req") + " CPU");
                                    params.put("image", instnce.getString("vm_image"));
                                    params.put("plan", instnce.getString("vm_plan_name"));
                                    instance.add(params);
                                }
                            }
                        }
                        return instance;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public HashMap getInstanceById(String id) {
        List<HashMap<String, String>> instances = getInstances();
        if (instances != null && !instances.isEmpty())
            for (HashMap instance : instances) {
                if (id.equals(instance.get("id")))
                    return instance;
            }
        return null;
    }

    public boolean terminateInstance(String instanceId) {
        try {
            HttpGet httpGet = new HttpGet(executeAction("terminate-instance") + "&instanceid=" + instanceId);
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("terminate-instanceresponse") && response.get("terminate-instanceresponse") instanceof JSONObject) {
                JSONObject responses = response.getJSONObject("terminate-instanceresponse");
                if (responses.length() > 0 && responses.has("instancesSet") && responses.get("instancesSet") instanceof JSONObject) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return false;
    }

    public boolean executeAction(String instanceId, String action) {
        try {
            HttpGet httpGet = new HttpGet(executeAction(action + "-instance") + "&instanceid=" + instanceId);
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has(action + "-instanceresponse") && response.get(action + "-instanceresponse") instanceof JSONObject) {
                JSONObject responses = response.getJSONObject(action + "-instanceresponse");
                if (responses.length() > 0 && responses.has("instancesSet") && responses.get("instancesSet") instanceof JSONObject) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return true;
    }

    public List getlocations() {
        try {
            HttpGet httpGet = new HttpGet(executeAction("list-locations"));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("list-locationsresponse") && response.get("list-locationsresponse") instanceof JSONObject) {
                JSONObject listLocations = response.getJSONObject("list-locationsresponse");
                if (listLocations.length() > 0 && listLocations.has("KeysSet") && listLocations.get("KeysSet") instanceof JSONObject) {
                    JSONObject keysSet = listLocations.getJSONObject("KeysSet");
                    if (keysSet.length() > 0) {
                        ArrayList<HashMap<String, String>> locations = new ArrayList();
                        Iterator<String> it = keysSet.keys();
                        while (it.hasNext()) {
                            String nextValue = it.next();
                            if (keysSet.get(nextValue) instanceof JSONObject) {
                                JSONObject location = keysSet.getJSONObject(nextValue);
                                if (location.length() > 0) {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("id", location.getString("location_code"));
                                    params.put("name", location.getString("location_name"));
                                    params.put("description", location.getString("description"));
                                    locations.add(params);
                                }
                            }
                        }
                        return locations;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getPlans() {
        try {
            HttpGet httpGet = new HttpGet(executeAction("describe-plan"));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("describe-planresponse") && response.get("describe-planresponse") instanceof JSONObject) {
                JSONObject describePlan = response.getJSONObject("describe-planresponse");
                if (describePlan.length() > 0 && describePlan.has("plans") && describePlan.get("plans") instanceof JSONObject) {
                    JSONObject plans = describePlan.getJSONObject("plans");
                    if (plans.length() > 0) {
                        ArrayList<HashMap<String, String>> plan = new ArrayList();
                        Iterator<String> it = plans.keys();
                        while (it.hasNext()) {
                            String nextValue = it.next();
                            if (plans.get(nextValue) instanceof JSONObject) {
                                JSONObject values = plans.getJSONObject(nextValue);
                                if (values.length() > 0) {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("id", values.getString("plan_name"));
                                    params.put("name", values.getString("plan_name"));
                                    params.put("memory", values.getString("display_ram"));
                                    params.put("disk", values.getString("display_disk"));
                                    params.put("cpu", values.getString("num_cpu") + " CPUs");
                                    plan.add(params);
                                }
                            }
                        }
                        return plan;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getImages() {
        try {
            HttpGet httpGet = new HttpGet(executeAction("describe-image"));
            CloseableHttpResponse chr = createBuilder().execute((HttpUriRequest)httpGet);
            String result = EntityUtils.toString(chr.getEntity());
            if (result == null || "".equals(result)) {
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.length() > 0 && response.has("describe-imageresponse") && response.get("describe-imageresponse") instanceof JSONObject) {
                JSONObject describeImage = response.getJSONObject("describe-imageresponse");
                if (describeImage.length() > 0 && describeImage.has("imagesset") && describeImage.get("imagesset") instanceof JSONObject) {
                    JSONObject images = describeImage.getJSONObject("imagesset");
                    if (images.length() > 0) {
                        ArrayList<HashMap<String, String>> image = new ArrayList();
                        Iterator<String> it = images.keys();
                        while (it.hasNext()) {
                            String nextValue = it.next();
                            if (images.get(nextValue) instanceof JSONObject) {
                                JSONObject img = images.getJSONObject(nextValue);
                                if (img.length() > 0) {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("id", img.getString("imageid"));
                                    params.put("name", img.getString("displayname"));
                                    params.put("description", img.getString("displayname"));
                                    image.add(params);
                                }
                            }
                        }
                        return image;
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public String getImageId(String os) {
        try {
            List<HashMap<String, String>> images = getImages();
            if (images != null && !images.isEmpty()) {
                for (HashMap image : images) {
                    if (os.equalsIgnoreCase("centos-6")) {
                        if ("CentOS-6.9_64bit".equalsIgnoreCase((String)image.get("id"))) {
                            return (String)image.get("id");
                        }
                        continue;
                    }
                    if ("CentOS-7.7_64bit".equalsIgnoreCase((String)image.get("id"))) {
                        return (String)image.get("id");
                    }
                }
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    protected String executeAction(String action) {
        try {
            String rndmId = UUID.randomUUID().toString();
            long l = Instant.now().toEpochMilli();
            SecretKeySpec secretKey= new SecretKeySpec(this.account.getPrivateKey().getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] arrayOfByte = mac.doFinal((l + "" + rndmId).getBytes("UTF-8"));
            String signture = URLEncoder.encode(new String(Base64.encodeBase64(arrayOfByte)), StandardCharsets.UTF_8.toString());
            return "https://cloudapi.atlantic.net/?&Action=" + action + "&Format=json&Version=2010-12-30&ACSAccessKeyId=" + this.account.getAccessKey() + "&Timestamp=" + l + "&Rndguid=" + rndmId + "&Signature=" + signture;
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
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
        if (!(paramObject instanceof AtlanticManager))
            return false;
        AtlanticManager do1 = (AtlanticManager)paramObject;
        if (!do1.exists(this))
            return false;
        AtlanticAccount const1 = getAccount();
        AtlanticAccount const2 = do1.getAccount();
        return !((const1 == null) ? (const2 != null) : !const1.equals(const2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AtlanticManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AtlanticAccount const1 = getAccount();
        return n * 59 + ((const1 == null) ? 43 : const1.hashCode());
    }

    public AtlanticAccount getAccount() {
        return account;
    }

    public void setAccount(AtlanticAccount account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "AtlanticManager(account=" + getAccount() + ")";
    }
}
