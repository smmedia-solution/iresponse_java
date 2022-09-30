package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Affiliate;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Images;
import tech.iresponse.http.Agents;

public class PullStatOffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private String campaignId;
    private String payoutType;
    private JSONObject offerData;

    @Override
    public void run() {
        try {
            LinkedHashMap<String,String> params = new LinkedHashMap<>();
            params.put("key", this.affiliateNetwork.apiKey);
            String result = Agents.get(this.affiliateNetwork.apiUrl + "/campaigns/" + this.campaignId + "/", params, 60);
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (!response.has("status") || !"ok".equals(response.getString("status"))){
                throw new DatabaseException(response.getString("message"));
            }

            this.offerData = (response.has("data") && response.get("data") instanceof JSONObject && response.getJSONObject("data").length() > 0) ? response.getJSONObject("data") : new JSONObject();
            if (this.offerData != null && this.offerData.length() == 0){
                throw new DatabaseException("No offer retreived !");
            }

            JSONObject offer = new JSONObject();
            offer.put("campaign-id", this.campaignId);
            offer.put("production-id", String.valueOf(this.offerData.getString("offer_ref")));
            offer.put("name", this.offerData.getString("offer_name").trim());
            offer.put("description", this.offerData.has("offer_description") ? this.offerData.getString("offer_description").trim() : "No Description !");
            offer.put("rules", this.offerData.has("offer_restrictions") ? this.offerData.getString("offer_restrictions").trim() : "No Rules !");
            offer.put("payout-type", this.payoutType);
            offer.put("payout-amount", this.offerData.has("payout") ? this.offerData.getString("payout") : "%");

            String[] names = offer.getString("name").replaceAll(Pattern.quote("/"), "-").split("-");
            String[] countries = new String[]{};
            List<String> countryList = Arrays.asList(Agents.COUNTRIES);

            if (names.length > 0){
                for (String nm : names) {
                    if (countryList.contains(nm.replaceAll(" ", ""))){
                        countries = (String[])ArrayUtils.add((Object[])countries, nm.replaceAll(" ", ""));
                    }
                }
            }

            offer.put("countries", (countries.length > 0) ? String.join(",", (CharSequence[])countries) : "US");
            offer.put("verticals", getVerticals());
            getCreatives(offer);
            Affiliate.updateOffers(offer);
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    protected JSONArray getVerticals() {
        JSONArray verticals = new JSONArray();
        try {
            String verticl = "";
            if (this.offerData.has("categories")) {
                JSONArray categories = this.offerData.getJSONArray("categories");
                if (categories.length() > 0){
                    for (int b = 0; b < categories.length(); b++) {
                        if (categories.getJSONObject(b).getString("category_name").trim().contains(",")) {
                            String[] categoryName = categories.getJSONObject(b).getString("category_name").split(",");
                            for (String name : categoryName){
                                verticl = verticl + "\"" + name + "\",";
                            }
                        } else {
                            verticl = "\"" + categories.getJSONObject(b).getString("category_name").trim() + "\"";
                        }

                        verticl = verticl.trim().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");

                        if (!"".equals(verticl.trim()) && !"\"\"".equals(verticl.trim())) {
                            verticals = new JSONArray("[" + verticl + "]");
                        } else {
                            verticals = new JSONArray();
                        }
                    }
                }
            }
        } catch (JSONException jSONException) {}

        if (verticals.length() == 0){
            verticals.put("Uncategorized");
        }
        return verticals;
    }

    protected void getCreatives(JSONObject offers) throws Exception {
        offers.put("from-names", new JSONArray());
        offers.put("subjects", new JSONArray());
        offers.put("creatives", new JSONArray());

        File[] imagesFiles = new File[]{};
        String trackLink = "";

        if (this.offerData.has("tracking_urls")) {
            JSONArray trackingUrls = this.offerData.getJSONArray("tracking_urls");
            if (trackingUrls.length() > 0){
                for (int b = 0; b < trackingUrls.length(); b++) {
                    if (!trackingUrls.getJSONObject(b).has("landing_page")) {
                        trackLink = trackingUrls.getJSONObject(b).getString("link");
                        break;
                    }
                }
            }
        }

        String unsubLink = (this.offerData.has("unsub_link") && !"null".equals(String.valueOf(this.offerData.get("unsub_link")))) ? String.valueOf(this.offerData.get("unsub_link")) : "";

        ManagementServer mngServ = Images.getMngemetServer();
        String baseUrl = Images.getBaseUrl(mngServ);

        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("key", this.affiliateNetwork.apiKey);

        String result = Agents.get(this.affiliateNetwork.apiUrl + "/campaigns/" + this.campaignId + "/creatives/", params, 60);
        if (result == null || "".equals(result)){
            throw new DatabaseException("No response retreived !");
        }

        JSONObject response = new JSONObject(result);
        if (!response.has("status") || !"ok".equals(response.getString("status"))){
            throw new DatabaseException(response.getString("message"));
        }

        if (response.has("data")) {
            JSONObject data = response.getJSONObject("data");
            if (data.length() > 0) {

                if (data.has("from_lines") && data.get("from_lines") instanceof JSONArray && data.getJSONArray("from_lines").length() > 0){
                    for (int b = 0; b < data.getJSONArray("from_lines").length(); b++) {
                        if (data.getJSONArray("from_lines").get(b) != null && !"".equals(data.getJSONArray("from_lines").getString(b))){
                            offers.getJSONArray("from-names").put(PlaceHoldeNames.remplace(data.getJSONArray("from_lines").getString(b)));
                        }
                    }
                }

                if (data.has("subject_lines") && data.get("subject_lines") instanceof JSONArray && data.getJSONArray("subject_lines").length() > 0){
                    for (int b = 0; b < data.getJSONArray("subject_lines").length(); b++) {
                        if (data.getJSONArray("subject_lines").get(b) != null && !"".equals(data.getJSONArray("subject_lines").getString(b))){
                            offers.getJSONArray("subjects").put(PlaceHoldeNames.remplace(data.getJSONArray("subject_lines").getString(b)));
                        }
                    }
                }

                JSONArray creatives = data.getJSONArray("creatives");
                if (creatives.length() > 0){

                    for (int b = 0; b < creatives.length(); b++) {
                        if (creatives.getJSONObject(b).has("id") && !"".equals(creatives.getJSONObject(b).getString("id")) && creatives.getJSONObject(b).has("type") && "email".equals(creatives.getJSONObject(b).getString("type")) && (this.maxCreatives == 0 || b < this.maxCreatives)) {
                            params = new LinkedHashMap<>();
                            params.put("key", this.affiliateNetwork.apiKey);
                            result = Agents.get(this.affiliateNetwork.apiUrl + "/campaigns/" + this.campaignId + "/creatives/" + creatives.getJSONObject(b).getString("id") + "/", params, 60);
                            if (result == null || "".equals(result)){
                                throw new DatabaseException("No response retreived !");
                            }

                            JSONObject response2 = new JSONObject(result);
                            if (!response2.has("status") || !"ok".equals(response2.getString("status"))){
                                throw new DatabaseException(response2.getString("message"));
                            }

                            if (response2.has("data")) {
                                JSONObject ofrData = response2.getJSONObject("data");
                                if (ofrData.length() > 0) {
                                    JSONObject creativeRecord = new JSONObject();
                                    Document doc = Jsoup.parse(ofrData.getString("html_body"), "utf-8");
                                    String code = PlaceHoldeNames.remplace(doc.toString());
                                    Elements anchors = doc.select("img");

                                    for (Element elm : anchors) {
                                        String src = String.valueOf(elm.attr("src")).trim();
                                        if (src.contains("http")) {
                                            String extension = src.substring(src.lastIndexOf('.') + 1);
                                            if (Images.isImage(extension)) {
                                                String fileName = Strings.rndomSalt(15, false) + "." + extension;
                                                File urlFileName = new File(System.getProperty("media.path") + File.separator + fileName);
                                                Agents.get(src, null, 120, urlFileName);
                                                code = StringUtils.replace(code, src, baseUrl + "/media/" + fileName);
                                                imagesFiles = (File[])ArrayUtils.add((Object[])imagesFiles, urlFileName);
                                            }
                                        }
                                    }

                                    code = StringUtils.replace(code, "[tracking_link]", trackLink);
                                    code = StringUtils.replace(code, "[unsub_link]", unsubLink);

                                    JSONArray creativeLinks = new JSONArray();
                                    creativeLinks.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + trackLink + "\"}"));

                                    if (!"null".equals(unsubLink)){
                                        creativeLinks.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + unsubLink + "\"}"));
                                    }

                                    creativeRecord.put("code", code);
                                    creativeRecord.put("links", creativeLinks);
                                    offers.getJSONArray("creatives").put(creativeRecord);
                                    this.maxCreatives++;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (imagesFiles.length > 0 && mngServ != null && !mngServ.getEmpty()){
            Images.uploadImages(mngServ, imagesFiles);
        }
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "campaignId", "payoutType", "offerData"})
    public PullStatOffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, String campaignId, String payoutType, JSONObject offerData) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.campaignId = campaignId;
        this.payoutType = payoutType;
        this.offerData = offerData;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof PullStatOffersGrabber))
            return false;
        PullStatOffersGrabber case1 = (PullStatOffersGrabber)paramObject;
        if (!case1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = case1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        if (getMaxCreatives() != case1.getMaxCreatives())
            return false;
        String str1 = getCampaignId();
        String str2 = case1.getCampaignId();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getPayoutType();
        String str4 = case1.getPayoutType();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        JSONObject jSONObject1 = getOfferData();
        JSONObject jSONObject2 = case1.getOfferData();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof PullStatOffersGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        n = n * 59 + getMaxCreatives();
        String str1 = getCampaignId();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getPayoutType();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        JSONObject jSONObject = getOfferData();
        return n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
    }

    public AffiliateNetwork getAffiliateNetwork() {
        return affiliateNetwork;
    }

    public void setAffiliateNetwork(AffiliateNetwork affiliateNetwork) {
        this.affiliateNetwork = affiliateNetwork;
    }

    public int getMaxCreatives() {
        return maxCreatives;
    }

    public void setMaxCreatives(int maxCreatives) {
        this.maxCreatives = maxCreatives;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getPayoutType() {
        return payoutType;
    }

    public void setPayoutType(String payoutType) {
        this.payoutType = payoutType;
    }

    public JSONObject getOfferData() {
        return offerData;
    }

    public void setOfferData(JSONObject offerData) {
        this.offerData = offerData;
    }

    @Override
    public String toString() {
        return "PullStatOffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", campaignId=" + getCampaignId() + ", payoutType=" + getPayoutType() + ", offerData=" + getOfferData() + ")";
    }
}