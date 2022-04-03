package tech.iresponse.affiliate;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.http.Agents;
import tech.iresponse.affiliate.workers.EverFlowOffersGrabber;
import tech.iresponse.utils.TypesParser;

public class EverflowApi extends AffiliateApi {

    public void getOffers(List offersIds) throws Exception {
        if (offersIds.isEmpty()) {
            LinkedHashMap<Object, Object> params = new LinkedHashMap<>();
            params.put("x-eflow-api-key", (getAffiliateNetwork()).apiKey);
            params.put("content-type", "application/json");
            String result = Agents.get((getAffiliateNetwork()).apiUrl + "/v1/affiliates/alloffers/?page_size=-1", null, 60, params);
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }
            JSONObject response = new JSONObject(result);
            if (response.has("error")){
                throw new DatabaseException(response.getString("error"));
            }
            int rowCount = response.getJSONObject("paging").has("total_count") ? response.getJSONObject("paging").getInt("total_count") : 0;
            if (rowCount == 0){
                throw new DatabaseException("No offers retreived !");
            }

            if (response.has("offers")) {
                JSONArray offers = response.getJSONArray("offers");
                if (offers.length() > 0){
                    for (int b = 0; b < offers.length(); b++) {
                        if ("approved".equalsIgnoreCase(offers.getJSONObject(b).getJSONObject("relationship").getString("offer_affiliate_status")) || "public".equalsIgnoreCase(offers.getJSONObject(b).getJSONObject("relationship").getString("offer_affiliate_status"))){
                            offersIds.add(String.valueOf(offers.getJSONObject(b).get("network_offer_id")));
                        }
                    }
                }
            }
        }
        int size = offersIds.size();
        ExecutorService execService = Executors.newFixedThreadPool((size > 100) ? 100 : size);
        offersIds.forEach(offerId -> execService.submit((Runnable)new EverFlowOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), TypesParser.safeParseInt(offerId))));
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("x-eflow-api-key", (getAffiliateNetwork()).apiKey);
        map.put("content-type", "application/json");
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/v1/affiliates/offers/" + offer.productionId, null, 60, map);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived Everflow !");
        }
        JSONObject response = new JSONObject(results);
        if (response.has("error")){
            throw new DatabaseException(response.getString("error"));
        }
        //System.out.println("everflow : " + response.getJSONObject("relationship").getJSONObject("email_optout").getString("suppression_file_link"));
        return response.getJSONObject("relationship").getJSONObject("email_optout").getString("suppression_file_link");
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception  {
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("x-eflow-api-key", (getAffiliateNetwork()).apiKey);
        params.put("content-type", "application/json");
        JSONObject jSONObject1 = new JSONObject();
        jSONObject1.put("from", (new SimpleDateFormat("yyyy-MM-dd")).format((new SimpleDateFormat("yyyy-MM-dd")).parse(startDate)));
        jSONObject1.put("to", (new SimpleDateFormat("yyyy-MM-dd")).format((new SimpleDateFormat("yyyy-MM-dd")).parse(endDate)));
        jSONObject1.put("timezone_id", 67);

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/v1/affiliates/reporting/clicks", null, 60, params, jSONObject1.toString());
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (response.has("error")){
            throw new DatabaseException(response.getString("error"));
        }

        if (response.has("clicks")) {
            JSONArray data = response.getJSONArray("clicks");
            if (data.length() > 0) {
                JSONObject clik = null;
                for (int b = 0; b < data.length(); b++) {
                    JSONObject row = data.getJSONObject(b);
                    if (row != null && row.has("transaction_id")) {
                        clik = new JSONObject();
                        clik.put("token", StringUtils.replace(row.getString("sub1") + row.getString("sub2") + row.getString("sub3"), "_", ""));
                        clik.put("offer_id", row.getJSONObject("relationship").getJSONObject("offer").getInt("network_offer_id"));
                        clik.put("conversion_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(TypesParser.safeParseLong(row.getString("unix_timestamp")) * 1000L)));
                        clik.put("sub_1", row.getString("sub1"));
                        clik.put("sub_2", row.getString("sub2"));
                        clik.put("sub_3", row.getString("sub3"));
                        clicks.put(clik);
                    }
                }
            }
        }
        return clicks;
    }

    public JSONArray getConversions(String startDate, String endDate) throws Exception  {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_eflow_conversions_result_" + (getAffiliateNetwork()).id + ".json");
        JSONArray conversions = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("x-eflow-api-key", (getAffiliateNetwork()).apiKey);
        params.put("content-type", "application/json");
        JSONObject jSONObject1 = new JSONObject();
        jSONObject1.put("from", (new SimpleDateFormat("yyyy-MM-dd")).format((new SimpleDateFormat("yyyy-MM-dd")).parse(startDate)));
        jSONObject1.put("to", (new SimpleDateFormat("yyyy-MM-dd")).format((new SimpleDateFormat("yyyy-MM-dd")).parse(endDate)));
        jSONObject1.put("show_conversions", true);
        jSONObject1.put("show_events", true);
        jSONObject1.put("timezone_id", 67);

        String result = Agents.get((getAffiliateNetwork()).apiUrl + "/v1/affiliates/reporting/conversions", null, 60, params, jSONObject1.toString());

        if (result == null || "".equals(result)) {
            if (lastResultFile.exists()) {
                result = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                throw new DatabaseException("No results retreived !");
            }
        } else {
            //FileUtils.writeStringToFile(lastResultFile, result, "utf-8");
            FileUtils.writeStringToFile(lastResultFile, "", "utf-8");
        }

        JSONObject response = new JSONObject(result);
        if (response.has("error")){
            throw new DatabaseException(response.getString("error"));
        }

        /*int totalCount = response.getJSONObject("paging").has("total_count") ? response.getJSONObject("paging").getInt("total_count") : 0;
        if (totalCount == 0){
            throw new DatabaseException("No conversions retreived !");
        }*/

        if (response.has("conversions")) {
            JSONArray data = response.getJSONArray("conversions");
            if (data.length() > 0) {
                JSONObject cnvrsion = null;
                for (int b = 0; b < data.length(); b++) {
                    JSONObject row = data.getJSONObject(b);
                    if (row != null && row.has("conversion_id")) {
                        cnvrsion = new JSONObject();
                        cnvrsion.put("token", StringUtils.replace(row.getString("sub1") + row.getString("sub2") + row.getString("sub3"), "_", ""));
                        cnvrsion.put("offer_id", row.getJSONObject("relationship").getJSONObject("offer").getInt("network_offer_id"));
                        cnvrsion.put("conversion_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(TypesParser.safeParseLong(row.getString("conversion_unix_timestamp")) * 1000L)));
                        cnvrsion.put("conversion_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(TypesParser.safeParseLong(row.getString("conversion_unix_timestamp")) * 1000L)));
                        cnvrsion.put("payout", row.getDouble("revenue"));
                        cnvrsion.put("sub_1", row.getString("sub1"));
                        cnvrsion.put("sub_2", row.getString("sub2"));
                        cnvrsion.put("sub_3", row.getString("sub3"));
                        conversions.put(cnvrsion);
                    }
                }
            }
        }
        return conversions;
    }
}
