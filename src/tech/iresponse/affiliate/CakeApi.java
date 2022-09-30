package tech.iresponse.affiliate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.affiliate.workers.CakeOffersGrabber;
import tech.iresponse.http.Agents;

public class CakeApi extends AffiliateApi {

    public void getOffers(List offersIds) throws Exception {
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("affiliate_id", String.valueOf((getAffiliateNetwork()).affiliateId));
        params.put("offer_status_id", "1");
        params.put("start_at_row", "1");
        params.put("row_limit", "0");
        params.put("dup1|fields", "offer_id");
        params.put("dup2|fields", "campaign_id");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/Offers/Feed", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("success") || !response.getBoolean("success")){
            throw new DatabaseException(response.getString("message"));
        }

        int rowCount = response.has("row_count") ? response.getInt("row_count") : 0;
        if (rowCount == 0){
            throw new DatabaseException("No offers retreived !");
        }

        if (response.has("data")) {
            JSONArray data = response.getJSONArray("data");
            if (data.length() > 0) {
                int nbthread = offersIds.isEmpty() ? data.length() : offersIds.size();
                ExecutorService execService = Executors.newFixedThreadPool((nbthread > 100) ? 100 : nbthread);
                for (int i = 0; i < data.length(); i++) {
                    if (offersIds.isEmpty() || offersIds.contains(String.valueOf(data.getJSONObject(i).get("offer_id")))){
                        execService.submit((Runnable)new CakeOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), data.getJSONObject(i).getInt("campaign_id"), null));
                    }
                }
                execService.shutdown();

                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception{
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("affiliate_id", String.valueOf((getAffiliateNetwork()).affiliateId));
        params.put("offer_id", String.valueOf(offer.productionId));
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/Offers/SuppressionList", params, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived Cake !");
        }
        JSONObject response = new JSONObject(results);
        if (!response.has("success") || !response.getBoolean("success")){
            throw new DatabaseException(response.getString("message"));
        }
        return response.has("download_url") ? response.getString("download_url") : "";
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception {
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("affiliate_id", String.valueOf((getAffiliateNetwork()).affiliateId));
        params.put("start_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startDate)));
        params.put("end_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endDate)));
        params.put("start_at_row", "1");
        params.put("row_limit", "0");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/Reports/Clicks", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("success") || !response.getBoolean("success")){
            throw new DatabaseException(response.getString("message"));
        }

        int rowCount = response.has("row_count") ? response.getInt("row_count") : 0;
        if (rowCount == 0 || !response.has("data")){
            throw new DatabaseException("No clicks retreived !");
        }

        JSONArray data = (response.get("data") instanceof JSONArray) ? response.getJSONArray("data") : new JSONArray();
        if (data.length() > 0) {
            JSONObject clik = null;
            for (int b = 0; b < data.length(); b++) {
                JSONObject row = data.getJSONObject(b);
                if (row != null && row.has("unique_click_id")) {
                    clik = new JSONObject();
                    clik.put("token", StringUtils.replace(row.getString("subid_1") + row.getString("subid_2") + row.getString("subid_3"), "_", ""));
                    clik.put("offer_id", (row.has("offer") && row.getJSONObject("offer").has("offer_id")) ? row.getJSONObject("offer").getInt("offer_id") : 0);
                    clik.put("click_date", row.getString("click_date").replaceAll("T", " ").split(Pattern.quote("."))[0]);
                    clik.put("sub_1", row.getString("subid_1"));
                    clik.put("sub_2", row.getString("subid_2"));
                    clik.put("sub_3", row.getString("subid_3"));
                    clicks.put(clik);
                }
            }
        }
        return clicks;
    }

    public JSONArray getConversions(String startDate, String endDate) throws Exception {
        //File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_cake_conversions_result_" + (getAffiliateNetwork()).id + ".json");
        JSONArray conversions = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("affiliate_id", String.valueOf((getAffiliateNetwork()).affiliateId));
        params.put("start_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startDate)));
        params.put("end_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endDate)));
        params.put("start_at_row", "1");
        params.put("row_limit", "0");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/Reports/Conversions", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("success") || !response.getBoolean("success")){
            throw new DatabaseException(response.getString("message"));
        }

        int rowCount = response.has("row_count") ? response.getInt("row_count") : 0;
        if (rowCount == 0 || !response.has("data")){
            throw new DatabaseException("No conversions retreived !");
        }

        JSONArray data = (response.get("data") instanceof JSONArray) ? response.getJSONArray("data") : new JSONArray();
        if (data.length() > 0) {
            JSONObject cnvrsion = null;
            for (int b = 0; b < data.length(); b++) {
                JSONObject row = data.getJSONObject(b);
                if (row != null && row.has("conversion_id")) {
                    cnvrsion = new JSONObject();
                    cnvrsion.put("offer_id", row.getInt("offer_id"));
                    cnvrsion.put("conversion_date", row.getString("conversion_date").replaceAll("T", " ").split(Pattern.quote("."))[0]);
                    cnvrsion.put("payout", row.getDouble("price"));
                    cnvrsion.put("sub_1", row.getString("subid_1"));
                    cnvrsion.put("sub_2", row.getString("subid_2"));
                    cnvrsion.put("sub_3", row.getString("subid_3"));
                    //cnvrsion.put("token", row.getString("conversion_date").replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + StringUtils.replace(row.getString("subid_1") + row.getString("subid_2") + row.getString("subid_3"), "_", "") + row.getString("conversion_id"));
                    cnvrsion.put("token", cnvrsion.getString("conversion_date").replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + StringUtils.replace(row.getString("subid_1") + row.getString("subid_2") + row.getString("subid_3"), "_", "") + row.getString("conversion_id"));
                    conversions.put(cnvrsion);
                }
            }
        }
        return conversions;
    }

}

