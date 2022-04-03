package tech.iresponse.affiliate;

import com.google.common.base.Joiner;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.http.Agents;
import tech.iresponse.affiliate.workers.W4OffersGrabber;

public class W4Api extends AffiliateApi {

    public void getOffers(List<Integer> offersIds) throws Exception {
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key_id", (getAffiliateNetwork()).apiKey);
        if (!offersIds.isEmpty()){
            params.put("campaign_ids", "[" + Joiner.on(',').join(offersIds) + "]");
        }

        params.put("status", "approved");
        params.put("limit", "500");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/pubs_campaign_available/get/", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject responses = new JSONObject(result);
        if (!responses.has("success") || !responses.getBoolean("success")){
            throw new DatabaseException(responses.getString("message"));
        }

        JSONObject data = responses.has("data") ? responses.getJSONObject("data") : null;
        if (data == null || data.length() == 0){
            throw new DatabaseException("No offers retreived !");
        }

        int rowCount = data.has("count") ? data.getInt("count") : 0;
        if (rowCount == 0){
            throw new DatabaseException("No offers retreived !");
        }

        if (!data.has("results")){
            throw new DatabaseException("No offers retreived !");
        }

        JSONArray results = data.getJSONArray("results");
        int i = offersIds.isEmpty() ? data.length() : offersIds.size();

        ExecutorService executorService = Executors.newFixedThreadPool((i > 100) ? 100 : i);
        for (int b = 0; b < results.length(); b++) {
            JSONObject offerData = results.getJSONObject(b);
            if (offerData != null && offerData.has("campaign_id") && (offersIds.isEmpty() || offersIds.contains(String.valueOf(offerData.get("campaign_id"))))){
                executorService.submit((Runnable)new W4OffersGrabber(getAffiliateNetwork(), getMaxCreatives(), offerData));
            }
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(1L, TimeUnit.DAYS)){
            executorService.shutdownNow();
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("key_id", (getAffiliateNetwork()).apiKey);
        map.put("campaign_id", String.valueOf(offer.campaignId));
        map.put("md5", "true");
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/pubs_campaign_suppression/get/", map, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived W4 !");
        }
        JSONObject responses = new JSONObject(results);
        if (!responses.has("success") || !responses.getBoolean("success")){
            throw new DatabaseException(responses.getString("message"));
        }
        JSONObject data = responses.has("data") ? new JSONObject(responses.getString("data")) : null;
        if (data == null || data.length() == 0){
            throw new DatabaseException("No link retreived !");
        }
        if (!data.has("download_link") || "".equalsIgnoreCase(data.getString("download_link"))){
            throw new DatabaseException("No link retreived !");
        }
        return data.getString("download_link");
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception {
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key_id", (getAffiliateNetwork()).apiKey);
        params.put("min_date", (new SimpleDateFormat("MM/dd/yyyy")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(startDate)));
        params.put("max_date", (new SimpleDateFormat("MM/dd/yyyy")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(endDate)));

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/pubs_click_log/get/", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject responses = new JSONObject(result);
        if (!responses.has("success") || !responses.getBoolean("success")){
            throw new DatabaseException(responses.getString("message"));
        }

        JSONObject data = responses.has("data") ? responses.getJSONObject("data") : null;
        if (data != null && data.length() > 0) {
            int rowCount = data.has("count") ? data.getInt("count") : 0;
            if ((rowCount > 0) && data.has("results")) {
                JSONArray results = data.getJSONArray("results");
                JSONObject clik = null;
                for (int b = 0; b < results.length(); b++) {
                    JSONObject row = results.getJSONObject(b);
                    if (row != null && row.has("click_id") && "unique".equalsIgnoreCase(row.getString("type"))) {
                        clik = new JSONObject();
                        clik.put("token", StringUtils.replace(row.getString("sid1") + row.getString("sid2") + row.getString("sid3"), "_", ""));
                        clik.put("offer_id", row.getInt("campaign_id"));
                        clik.put("click_date", row.getString("created_date").replaceAll("T", " ").split(Pattern.quote("."))[0]);
                        clik.put("sub_1", row.getString("sid1"));
                        clik.put("sub_2", row.getString("sid2"));
                        clik.put("sub_3", row.getString("sid3"));
                        clicks.put(clik);
                    }
                }
            }
        }
        return clicks;
    }

    public JSONArray getConversions(String startDate, String endDate) throws Exception {
        JSONArray conversions = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key_id", (getAffiliateNetwork()).apiKey);
        params.put("min_date", (new SimpleDateFormat("MM/dd/yyyy")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(startDate)));
        params.put("max_date", (new SimpleDateFormat("MM/dd/yyyy")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(endDate)));

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/pubs_conversion_log/get/", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }
        JSONObject responses = new JSONObject(result);
        if (!responses.has("success") || !responses.getBoolean("success")){
            throw new DatabaseException(responses.getString("message"));
        }

        JSONObject data = responses.has("data") ? responses.getJSONObject("data") : null;
        if (data != null && data.length() > 0) {
            int rowCount = data.has("count") ? data.getInt("count") : 0;
            if ((rowCount > 0) && data.has("results")) {
                JSONArray results = data.getJSONArray("results");
                JSONObject cnvrsion = null;
                for (int b = 0; b < results.length(); b++) {
                    JSONObject row = results.getJSONObject(b);
                    if (row != null && row.has("click_id") && row.getDouble("payout") > 0.0D) {
                        cnvrsion = new JSONObject();
                        cnvrsion.put("token", StringUtils.replace(row.getString("sid1") + row.getString("sid2") + row.getString("sid3"), "_", ""));
                        cnvrsion.put("offer_id", row.getInt("campaign_id"));
                        cnvrsion.put("conversion_date", row.getString("conversion_date").replaceAll("T", " ").split(Pattern.quote("."))[0]);
                        cnvrsion.put("payout", row.getDouble("payout"));
                        cnvrsion.put("sub_1", row.getString("sid1"));
                        cnvrsion.put("sub_2", row.getString("sid2"));
                        cnvrsion.put("sub_3", row.getString("sid3"));
                        conversions.put(cnvrsion);
                    }
                }
            }
        }
        return conversions;
    }
}
