package tech.iresponse.affiliate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.affiliate.workers.HitpathOffersGrabber;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.Url;
import tech.iresponse.http.Agents;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.ThreadSleep;

public class HitpathApi extends AffiliateApi {

    @Override
    public void getOffers(List<Integer> offersIds) throws Exception {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_hitpath_result_" + (getAffiliateNetwork()).id + ".json");
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("Accept", "application/json");
        params.put("Authorization", "Bearer  " + (getAffiliateNetwork()).apiKey);

        if(!offersIds.isEmpty()){
            for (int i = 0; i < offersIds.size(); i++) {
                //System.out.println("link : " + i + " = " + (getAffiliateNetwork()).apiUrl + "/campaigns/" + offersIds.get(i));
                String results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + offersIds.get(i), null, 60, params);
                if (results == null || "".equals(results)) {
                    throw new DatabaseException("No result retreived !");
                }
                JSONObject responses = new JSONObject(results);
                if (!responses.has("data")){
                    throw new DatabaseException("No response retreived !");
                }
                JSONObject data = (responses.get("data") instanceof JSONObject) ? responses.getJSONObject("data") : null;
                //System.out.println("data lent = " + data);
                if (data == null){
                    throw new DatabaseException("No offers retreived !");
                }
                //int nbthread = offersIds.isEmpty() ? data.length() : offersIds.size();
                ExecutorService execService = Executors.newFixedThreadPool(1);
                execService.submit((Runnable)new HitpathOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), data));
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }

        }else{
            LinkedHashMap<String,String> records = new LinkedHashMap<>();
            records.put("records", "300");
            records.put("status", "Active");
            String results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns", records, 60, params);
            if (results == null || "".equals(results) || results.contains("Too Many Attempts.")) {
                if (lastResultFile.exists()) {
                    results = FileUtils.readFileToString(lastResultFile, "utf-8");
                } else {
                    throw new DatabaseException("No response retreived !");
                }
            } else {
                FileUtils.writeStringToFile(lastResultFile, results, "utf-8");
            }

            JSONObject responses = new JSONObject(results);
            if (!responses.has("data")){
                throw new DatabaseException("No response retreived !");
            }

            //#########################################################################################
            /*JSONObject meta = (responses.get("meta") instanceof JSONObject) ? responses.getJSONObject("meta") : null;
            System.out.println("meta 1 = " + meta.getInt("current_page"));
            System.out.println("meta 2 = " + meta.getInt("last_page"));*/
            //#########################################################################################

            JSONArray data = (responses.get("data") instanceof JSONArray) ? responses.getJSONArray("data") : null;
            //System.out.println("data = " + data.length());
            if (data == null || data.length() == 0){
                throw new DatabaseException("No offers retreived !");
            }
            //int nbthread = offersIds.isEmpty() ? data.length() : offersIds.size();
            int nbthread = data.length();
            ExecutorService execService = Executors.newFixedThreadPool((nbthread > 100) ? 100 : nbthread);

            if (data.length() > 0) {
                for (int b = 0; b < data.length(); b++) {
                    JSONObject offerData = data.getJSONObject(b);
                    //if (offerData != null && offerData.has("id") && (offersIds.isEmpty() || offersIds.contains(String.valueOf(offerData.get("id"))))){
                    if (offerData != null && offerData.has("id") && "approved".equalsIgnoreCase(offerData.getString("availability").trim()) && !"0".equalsIgnoreCase(offerData.getString("payout").trim()) && offerData.getInt("email_count") > 0){
                        execService.submit((Runnable)new HitpathOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), offerData));
                        if (b > 50){
                            //System.out.println("b = " + b);
                            ThreadSleep.sleep(3000L);
                        }
                    }

                    /*if (b == 50 || b == 100 || b == 150 || b == 200 || b == 250 || b == 300 || b == 350 || b == 400 || b == 450){
                        ThreadSleep.sleep(34000L);
                    }*/
                }
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception {
        //File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_hitpath_sup_result_" + (getAffiliateNetwork()).id + ".xml");
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Accept", "application/json");
        map.put("Authorization", "Bearer  " + (getAffiliateNetwork()).apiKey);

        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + offer.productionId + "/suppression", null, 60, map);
        if (results == null || "".equals(results)) {
            throw new DatabaseException("No result retreived Hitpath !");
        }

        JSONObject responses = new JSONObject(results);
        if (responses == null || !responses.has("data") || !responses.getJSONObject("data").has("download_url")){
            throw new DatabaseException("No suppression link retreived Hitpath !");
        }
        return responses.getJSONObject("data").getString("download_url");
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_hitpath_clicks_result_" + (getAffiliateNetwork()).id + ".xml");
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key", (getAffiliateNetwork()).apiKey);
        params.put("type", "clicks");
        params.put("start", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startDate)));
        params.put("end", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endDate)));
        params.put("format", "csv");

        String results = Agents.put("http://reporting." + Url.checkUrl((getAffiliateNetwork()).apiUrl) + "/api.php", params, 60);
        if (results == null || "".equals(results) || !results.contains("campaignid")) {
            if (lastResultFile.exists()) {
                results = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                throw new DatabaseException("No response retreived !");
            }
        } else {
            FileUtils.writeStringToFile(lastResultFile, results, "utf-8");
        }

        JSONArray response = JsonUtils.csvToJson(results, ',');
        if (response != null && response.length() > 0){
            for (int b = 0; b < response.length(); b++) {
                JSONObject row = response.getJSONObject(b);
                if (row != null && row.has("campaignid")) {
                    JSONObject clik = new JSONObject();
                    clik.put("token", StringUtils.replace(row.getString("c1") + row.getString("c2") + row.getString("c3"), "_", ""));
                    clik.put("offer_id", row.getInt("campaignid"));
                    clik.put("click_date", row.getString("date"));
                    clik.put("sub_1", row.getString("c1"));
                    clik.put("sub_2", row.getString("c2"));
                    clik.put("sub_3", row.getString("c3"));
                    clicks.put(clik);
                }
            }
        }
        return clicks;
    }

    public JSONArray getConversions(String startDate, String endDate) throws Exception {
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/last_hitpath_conversions_result_" + (getAffiliateNetwork()).id + ".json");
        JSONArray conversions = new JSONArray();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("Accept", "application/json");
        params.put("Authorization", "Bearer  " + (getAffiliateNetwork()).apiKey);
        LinkedHashMap<String, String> records = new LinkedHashMap<>();
        records.put("start", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startDate)));
        records.put("end", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endDate)));
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/reports/campaigns", records, 60, params);

        if (results == null || "".equals(results) || results.contains("Too Many Attempts.")) {
            if (lastResultFile.exists()) {
                results = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                throw new DatabaseException("No results retreived !");
            }
        } else {
            FileUtils.writeStringToFile(lastResultFile, results, "utf-8");
        }

        JSONObject responses = new JSONObject(results);
        if (responses != null && responses.has("data") && JsonUtils.isJSONArray(responses.get("data")) && responses.getJSONArray("data").length() > 0) {
            JSONArray data = responses.getJSONArray("data");
            List<Integer> compaingIds = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                if (!"0".equalsIgnoreCase(data.getJSONObject(i).getString("sales").trim())){
                //if (TypesParser.safeParseInt(data.getJSONObject(i).getString("sales")) > 0){
                    compaingIds.add(data.getJSONObject(i).getInt("id"));
                }
            }
            //System.out.println("compaingIds" + compaingIds);
            if (!compaingIds.isEmpty())
                for (int compId : compaingIds) {
                    records.put("records", "10000");
                    results = Agents.get((getAffiliateNetwork()).apiUrl + "/reports/sales-detail/" + compId, records, 60, params);
                    JSONObject rspSale = new JSONObject(results);
                    JSONArray dataSales = rspSale.getJSONArray("data");
                    if (dataSales != null && dataSales.length() > 0)
                        for (int j = 0; j < dataSales.length(); j++) {
                            JSONObject row = dataSales.getJSONObject(j);
                            if (row != null && (TypesParser.safeParseDouble(row.getString("earnings"))) > 0.0D) {
                                JSONObject cnvrsion = new JSONObject();
                                cnvrsion.put("token", StringUtils.replace(row.getString("c1") + row.getString("c2") + row.getString("c3"), "_", ""));
                                cnvrsion.put("offer_id", compId);
                                cnvrsion.put("conversion_date", row.getString("saledate"));
                                cnvrsion.put("payout", TypesParser.safeParseDouble(row.getString("earnings")));
                                cnvrsion.put("sub_1", row.getString("c1"));
                                cnvrsion.put("sub_2", row.getString("c2"));
                                cnvrsion.put("sub_3", row.getString("c3"));
                                conversions.put(cnvrsion);
                                //System.out.println("cnvrsion" + cnvrsion);
                            }
                        }
                }
        }
        return conversions;
    }
}
