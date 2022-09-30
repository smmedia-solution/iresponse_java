package tech.iresponse.affiliate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.affiliate.workers.HasOffersGrabber;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.http.Agents;
import tech.iresponse.utils.Url;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.DatesUtils;

public class HasoffersApi extends AffiliateApi {

    public void getOffers(List offersIds) throws Exception {
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("Format", "json");
        params.put("Target", "Affiliate_Offer");
        params.put("Version", "3");
        params.put("Method", "findAll");
        params.put("NetworkId", (getAffiliateNetwork()).networkId);
        params.put("Service", "HasOffers");
        params.put("0|contain[]", "OfferVertical");
        params.put("1|contain[]", "GeoTargeting");
        params.put("api_key", (getAffiliateNetwork()).apiKey);

        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/Affiliate_Offer.json", params, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject responses = new JSONObject(results);
        if (!responses.has("response")){
            throw new DatabaseException("No response retreived !");
        }

        responses = responses.getJSONObject("response");
        if (!responses.has("status") || responses.getInt("status") == -1){
            throw new DatabaseException(responses.has("errorMessage") ? responses.getString("errorMessage") : "No offers retreived !");
        }

        JSONObject data = (responses.has("data") && responses.get("data") instanceof JSONObject) ? responses.getJSONObject("data") : null;
        if (data == null || data.length() == 0){
            throw new DatabaseException("No offers retreived !");
        }

        int lent = data.length();
        int nbthread = offersIds.isEmpty() ? lent : offersIds.size();
        ExecutorService execService = Executors.newFixedThreadPool((nbthread > 100) ? 100 : nbthread);

        Iterator<?> keys = data.keys();
        while (keys.hasNext()) {
            JSONObject next = data.getJSONObject(String.valueOf(keys.next()));
            if (next != null) {
                boolean dataExist = (next instanceof JSONObject && next.has("Offer") && next.get("Offer") instanceof JSONObject && next.getJSONObject("Offer").has("id") && (offersIds.isEmpty() || offersIds.contains(next.getJSONObject("Offer").getString("id"))) && ("approved".equalsIgnoreCase(next.getJSONObject("Offer").getString("approval_status")) || next.getJSONObject("Offer").getInt("require_approval") == 0) && next.getJSONObject("Offer").getInt("email_instructions") == 1) ? true : false;
                if (dataExist) {
                    JSONObject offerData = new JSONObject();
                    offerData.put("offer", next.getJSONObject("Offer"));
                    offerData.put("verticals", (next.has("OfferVertical") && next.get("OfferVertical") instanceof JSONObject) ? next.getJSONObject("OfferVertical") : null);
                    offerData.put("countries", (next.has("GeoTargeting") && next.get("GeoTargeting") instanceof JSONObject && next.getJSONObject("GeoTargeting").has("Countries") && next.getJSONObject("GeoTargeting").get("Countries") instanceof JSONObject) ? next.getJSONObject("GeoTargeting").getJSONObject("Countries") : null);
                    execService.submit((Runnable)new HasOffersGrabber(getAffiliateNetwork(), getMaxCreatives(), offerData));
                    if (lent > 1){
                        ThreadSleep.sleep(2000L);
                    }
                }
            }
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
    }

    public String getSuppressionLink(Offer offer) throws Exception {
        String str1 = Agents.get("https://crm3-java." + Url.checkUrl((getAffiliateNetwork()).website) + "/api/v1/proxy/links/offer/" + offer.productionId + "/id/" + (getAffiliateNetwork()).affiliateId + "/name/" + URLEncoder.encode((getAffiliateNetwork()).companyName, StandardCharsets.UTF_8.toString()), null, 60);
        if (str1 != null && !"".equals(str1)){
            try {
                JSONObject responses = new JSONObject(str1);
                if (responses.has("suppressionFile"))
                    return responses.getString("suppressionFile");
            } catch (Exception exception) {}
        }
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Format", "json");
        map.put("Target", "Affiliate_Offer");
        map.put("Version", "3");
        map.put("Method", "findById");
        map.put("NetworkId", (getAffiliateNetwork()).networkId);
        map.put("Service", "HasOffers");
        map.put("id", offer.productionId);
        map.put("api_key", (getAffiliateNetwork()).apiKey);
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/Affiliate_Offer.json", map, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived HasOffer !");
        }
        JSONObject jSONObject = new JSONObject(results);
        if (jSONObject.has("response")) {
            jSONObject = jSONObject.getJSONObject("response");
            if (jSONObject.has("status") && jSONObject.getInt("status") != -1) {
                jSONObject = (jSONObject.has("data") && jSONObject.get("data") instanceof JSONObject) ? jSONObject.getJSONObject("data") : null;
                if (jSONObject != null && jSONObject.length() > 0) {
                    jSONObject = (jSONObject.has("Offer") && jSONObject.get("Offer") instanceof JSONObject) ? jSONObject.getJSONObject("Offer") : null;
                    if (jSONObject != null && jSONObject.length() > 0 && jSONObject.has("dne_download_url") && jSONObject.getString("dne_download_url").contains("http"))
                        return jSONObject.getString("dne_download_url").trim();
                }
            }
        }
        throw new DatabaseException("No link retreived !");
    }

    public JSONArray getClicks(String startDate, String endDate) throws Exception {
        JSONArray clicks = new JSONArray();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("Target", "Affiliate_Report");
        params.put("Method", "getConversions");
        params.put("limit", "10000");
        params.put("dup8|fields[]", "Stat.ad_id");
        params.put("dup1|fields[]", "Stat.approved_payout");
        params.put("dup2|fields[]", "Stat.affiliate_info1");
        params.put("dup3|fields[]", "Stat.affiliate_info2");
        params.put("dup4|fields[]", "Stat.affiliate_info3");
        params.put("dup5|fields[]", "Stat.datetime");
        params.put("dup6|fields[]", "Stat.ip");
        params.put("dup7|fields[]", "Stat.offer_id");
        params.put("filters[Stat.datetime][conditional]", "BETWEEN");
        params.put("dup1|filters[Stat.datetime][values][]", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.substractHours(startDate, 9)));
        params.put("dup2|filters[Stat.datetime][values][]", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.substractHours(endDate, 9)));

        String results = Agents.get((getAffiliateNetwork()).apiUrl, params, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject responses = new JSONObject(results);
        if (!responses.has("response")){
            throw new DatabaseException("No response retreived !");
        }

        responses = responses.getJSONObject("response");
        if (!responses.has("status") || responses.getInt("status") == -1){
            throw new DatabaseException(responses.has("errorMessage") ? responses.getString("errorMessage") : "No conversions retreived !");
        }

        JSONObject data = responses.has("data") ? responses.getJSONObject("data") : null;
        if (data != null && data.length() > 0 && data.has("data")) {
            JSONArray stats = data.getJSONArray("data");
            JSONObject stat = null;
            if (stats.length() > 0){
                for (int b = 0; b < stats.length(); b++) {
                    JSONObject row = stats.getJSONObject(b);
                    if (row != null && row.has("Stat") && row.getJSONObject("Stat").has("affiliate_info1")) {
                        stat = row.getJSONObject("Stat");
                        JSONObject clik = new JSONObject();
                        clik.put("token", StringUtils.replace(stat.getString("affiliate_info1") + stat.getString("affiliate_info2") + stat.getString("affiliate_info3"), "_", ""));
                        clik.put("offer_id", stat.getInt("offer_id"));
                        clik.put("click_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.addHours(stat.getString("datetime").replaceAll("T", " ").split(Pattern.quote("."))[0], 9)));
                        clik.put("sub_1", stat.getString("affiliate_info1"));
                        clik.put("sub_2", stat.getString("affiliate_info2"));
                        clik.put("sub_3", stat.getString("affiliate_info3"));
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
        params.put("api_key", (getAffiliateNetwork()).apiKey);
        params.put("Target", "Affiliate_Report");
        params.put("Method", "getConversions");
        params.put("limit", "10000");
        params.put("dup8|fields[]", "Stat.ad_id");
        params.put("dup1|fields[]", "Stat.approved_payout");
        params.put("dup2|fields[]", "Stat.affiliate_info1");
        params.put("dup3|fields[]", "Stat.affiliate_info2");
        params.put("dup4|fields[]", "Stat.affiliate_info3");
        params.put("dup5|fields[]", "Stat.datetime");
        params.put("dup6|fields[]", "Stat.ip");
        params.put("dup7|fields[]", "Stat.offer_id");
        params.put("filters[Stat.datetime][conditional]", "BETWEEN");
        params.put("dup1|filters[Stat.datetime][values][]", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.substractHours(startDate, 9)));
        params.put("dup2|filters[Stat.datetime][values][]", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.substractHours(endDate, 9)));

        String results = Agents.get((getAffiliateNetwork()).apiUrl, params, 60);
        if (results == null || "".equals(results)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject responses = new JSONObject(results);
        if (!responses.has("response")){
            throw new DatabaseException("No response retreived !");
        }

        responses = responses.getJSONObject("response");
        if (!responses.has("status") || responses.getInt("status") == -1){
            throw new DatabaseException(responses.has("errorMessage") ? responses.getString("errorMessage") : "No conversions retreived !");
        }

        JSONObject data = responses.has("data") ? responses.getJSONObject("data") : null;
        if (data != null && data.length() > 0 && data.has("data")) {
            JSONArray stats = data.getJSONArray("data");
            JSONObject stat = null;
            if (stats.length() > 0){
                for (int b = 0; b < stats.length(); b++) {
                    JSONObject row = stats.getJSONObject(b);
                    if (row != null && row.has("Stat") && row.getJSONObject("Stat").has("affiliate_info1")) {
                        stat = row.getJSONObject("Stat");
                        if (stat.getDouble("approved_payout") > 0.0D) {
                            JSONObject cnvrsion = new JSONObject();
                            cnvrsion.put("token", StringUtils.replace(stat.getString("affiliate_info1") + stat.getString("affiliate_info2") + stat.getString("affiliate_info3"), "_", ""));
                            cnvrsion.put("offer_id", stat.getInt("offer_id"));
                            cnvrsion.put("click_date", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(DatesUtils.addHours(stat.getString("datetime").replaceAll("T", " ").split(Pattern.quote("."))[0], 9)));
                            cnvrsion.put("payout", stat.getDouble("approved_payout"));
                            cnvrsion.put("sub_1", stat.getString("affiliate_info1"));
                            cnvrsion.put("sub_2", stat.getString("affiliate_info2"));
                            cnvrsion.put("sub_3", stat.getString("affiliate_info3"));
                            conversions.put(cnvrsion);
                        }
                    }
                }
            }
        }
        return conversions;
    }
}
