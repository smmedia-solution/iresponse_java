package tech.iresponse.production.shorlink;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import tech.iresponse.exceptions.DatabaseException;

public class Bitly {

    public static String shortBitly(String paramString) throws Exception {
        try {
            String str1 = tech.iresponse.core.Application.checkAndgetInstance().getSettings().getString("bit_shortlinks_token");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000).build();
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpPost httpPost = new HttpPost("https://api-ssl.bitly.com/v4/shorten");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + str1);
            JSONObject jSONObject1 = new JSONObject();
            jSONObject1.put("long_url", paramString);
            httpPost.setEntity((HttpEntity)new StringEntity(jSONObject1.toString()));
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute((HttpUriRequest)httpPost);
            String response = EntityUtils.toString(closeableHttpResponse.getEntity());
            if (response == null || "".equals(response)){
                throw new DatabaseException("No response retreived !");
            }
            JSONObject jSONObject2 = new JSONObject(response);
            if (jSONObject2.length() > 0 && jSONObject2.has("link") && !"".equals(jSONObject2.getString("link"))){
                return jSONObject2.getString("link");
            }
        } catch (Exception e) {
            throw new DatabaseException("Could not shorten link !", e);
        }
        return paramString;
    }
}
