package tech.iresponse.affiliate;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.http.Agents;
import tech.iresponse.affiliate.workers.PullStatOffersGrabber;
import tech.iresponse.utils.TypesParser;

public class PullstatApi extends AffiliateApi {

    public void getOffers(List offersIds) throws Exception {
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key", (getAffiliateNetwork()).apiKey);

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("status") || !"ok".equals(response.getString("status"))){
            throw new DatabaseException(response.getString("message"));
        }

        int total = response.has("total") ? response.getInt("total") : 0;
        if (total == 0){
            throw new DatabaseException("No offers retreived !");
        }

        if (response.has("data")) {
            JSONArray data = response.getJSONArray("data");
            if (data.length() > 0) {
                int nbthread = offersIds.isEmpty() ? data.length() : offersIds.size();
                ExecutorService execService = Executors.newFixedThreadPool((nbthread > 100) ? 100 : nbthread);
                for (int b = 0; b < data.length(); b++) {
                    if (offersIds.isEmpty() || offersIds.contains(data.getJSONObject(b).getString("offer_ref"))){
                        execService.submit((Runnable)new PullStatOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), data.getJSONObject(b).getString("ref"), data.getJSONObject(b).getString("conversion").trim(), null));
                    }
                }
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("key", (getAffiliateNetwork()).apiKey);
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + offer.campaignId + "/", map, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived Pullstat !");
        }
        JSONObject responses = new JSONObject(results);
        if (!responses.has("status") || !"ok".equals(responses.getString("status"))){
            throw new DatabaseException(responses.getString("message"));
        }
        if (responses.has("data")) {
            JSONObject jSONObject1 = responses.getJSONObject("data");
            if (jSONObject1.length() > 0 && jSONObject1.has("suppression_url")){
                return jSONObject1.getString("suppression_url");
            }
        }
        return "";
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_pullstat_clicks_result_" + (getAffiliateNetwork()).id + ".xml");
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key", (getAffiliateNetwork()).apiKey);
        params.put("type", "clicks");
        params.put("start", startDate);
        params.put("end", endDate);
        params.put("format", "json");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/reports", params, 60);
        if (result == null || "".equals(result) || !result.contains("data")) {
            if (lastResultFile.exists()) {
                result = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                throw new DatabaseException("No response retreived !");
            }
        } else {
            FileUtils.writeStringToFile(lastResultFile, result, "utf-8");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("status") || !"ok".equals(response.getString("status"))){
            throw new DatabaseException(response.getString("error_msg"));
        }

        if (response.has("data") && response.has("total") && response.getInt("total") > 0) {
            JSONArray data = response.getJSONArray("data");
            if (data.length() > 0){
                for (int b = 0; b < data.length(); b++) {
                    JSONObject row = data.getJSONObject(b);
                    if (row != null && row.has("campaignid") && TypesParser.safeParseDouble(row.get("amount")) == 0.0D && row.has("subid1") && row.has("subid2") && row.has("subid3")) {
                        JSONObject clik = new JSONObject();
                        clik.put("token", StringUtils.replace(row.getString("subid1") + row.getString("subid2") + row.getString("subid3"), "_", ""));
                        clik.put("offer_id", row.getString("campaignid"));
                        clik.put("conversion_date", row.getString("tstamp"));
                        clik.put("payout", TypesParser.safeParseDouble(row.getString("amount")));
                        clik.put("sub_1", row.getString("subid1"));
                        clik.put("sub_2", row.getString("subid2"));
                        clik.put("sub_3", row.getString("subid3"));
                        clicks.put(clik);
                    }
                }
            }
        }
        return clicks;
    }

    public JSONArray getConversions(String startDate, String endDate) throws Exception {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_pullstat_conversions_result_" + (getAffiliateNetwork()).id + ".xml");
        JSONArray conversions = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key", (getAffiliateNetwork()).apiKey);
        params.put("type", "sales");
        params.put("start", startDate);
        params.put("end", endDate);
        params.put("format", "json");

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/reports", params, 60);
        if (result == null || "".equals(result) || !result.contains("data")) {
            if (lastResultFile.exists()) {
                result = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                throw new DatabaseException("No response retreived !");
            }
        } else {
            FileUtils.writeStringToFile(lastResultFile, result, "utf-8");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("status") || !"ok".equals(response.getString("status"))){
            throw new DatabaseException(response.getString("error_msg"));
        }

        if (response.has("data") && response.has("total") && response.getInt("total") > 0) {
            JSONArray data = response.getJSONArray("data");
            if (data.length() > 0)
                for (int b = 0; b < data.length(); b++) {
                    JSONObject row = data.getJSONObject(b);
                    if (row != null && row.has("campaignid") && TypesParser.safeParseDouble(row.get("amount")) > 0.0D && row.has("subid1") && row.has("subid2") && row.has("subid3")) {
                        JSONObject cnvrsion = new JSONObject();
                        cnvrsion.put("token", StringUtils.replace(row.getString("subid1") + row.getString("subid2") + row.getString("subid3"), "_", ""));
                        cnvrsion.put("offer_id", row.getString("campaignid"));
                        cnvrsion.put("conversion_date", row.getString("tstamp"));
                        cnvrsion.put("payout", TypesParser.safeParseDouble(row.getString("amount")));
                        cnvrsion.put("sub_1", row.getString("subid1"));
                        cnvrsion.put("sub_2", row.getString("subid2"));
                        cnvrsion.put("sub_3", row.getString("subid3"));
                        conversions.put(cnvrsion);
                    }
                }
        }
        return conversions;
    }
}