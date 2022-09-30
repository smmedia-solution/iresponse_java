package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Images;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Agents;

public class W4OffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private JSONObject offerData;

    @Override
    public void run() {
        try {
            JSONObject params = new JSONObject();
            params.put("production-id", this.offerData.getInt("campaign_id"));
            params.put("campaign-id", this.offerData.getInt("campaign_id"));
            params.put("name", this.offerData.getString("campaign_name").trim());
            params.put("description", this.offerData.has("campaign_description") ? this.offerData.getString("campaign_description").trim() : "No Description !");
            params.put("rules", (this.offerData.has("restrictions") && !"".equals(this.offerData.getString("restrictions").trim())) ? this.offerData.getString("restrictions").trim() : "No Rules !");
            params.put("payout-type", (this.offerData.getInt("payout_type") != 1) ? "CPC" : "CPA");
            params.put("payout-amount", TypesParser.safeParseDouble(String.valueOf(this.offerData.get("payout"))));
            params.put("countries", getCountries());
            params.put("verticals", getVerticals());
            getCreatives(params);
            Affiliate.updateOffers(params);
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    protected String getCountries() {
        String countries = "";
        StringBuilder sb = new StringBuilder();
        try {
            if (this.offerData.has("country_ids") && !"".equals(this.offerData.getString("country_ids")) && this.offerData.getString("country_ids").startsWith("[")) {
                JSONArray countryIds = new JSONArray(this.offerData.getString("country_ids"));
                for (int b = 0; b < countryIds.length(); b++){
                    sb.append(countryIds.getString(b)).append(",");
                }
            }
        } catch (JSONException jSONException) {}
        countries = sb.toString();
        return "".contains(countries) ? "US" : (countries.endsWith(",") ? countries.substring(0, countries.length() - 1) : countries);
    }

    protected JSONArray getVerticals() {
        JSONArray verticals = new JSONArray();
        try {
            if (this.offerData.has("categories") && JsonUtils.isJSONArray(this.offerData.get("categories"))) {
                JSONArray categories = this.offerData.getJSONArray("categories");
                for (int b = 0; b < categories.length(); b++) {
                    if (categories.getString(b) != null && !"".equals(categories.getString(b).trim())){
                        verticals.put(categories.getString(b));
                    }
                }
            }
        } catch (JSONException jSONException) {}

        if (verticals.length() == 0){
            verticals.put("Uncategorized");
        }
        return verticals;
    }

    protected void getCreatives(JSONObject params) throws Exception {
        params.put("from-names", new JSONArray());
        params.put("subjects", new JSONArray());
        params.put("creatives", new JSONArray());

        LinkedHashMap<String,String> prms = new LinkedHashMap<>();
        prms.put("key_id", this.affiliateNetwork.apiKey);
        prms.put("campaign_id", this.offerData.getString("campaign_id"));
        String result = Agents.get(this.affiliateNetwork.apiUrl + "/pubs_creative_email/get/", prms, 60);

        if (result != null && !"".equals(result)) {
            JSONObject responses = new JSONObject(result);
            if (responses.has("success") && responses.getBoolean("success") == true) {
                JSONObject data = responses.has("data") ? new JSONObject(responses.getString("data")) : null;
                if (data != null && data.length() > 0 && data.has("results")) {
                    JSONObject results = data.getJSONObject("results");
                    if (results.has("all_email_assets")) {
                        JSONArray creativesData = new JSONArray();
                        boolean sbjctFrmNameFound = false;
                        int count = 0;

                        if (results.get("all_email_assets") instanceof String && !"".equals(results.getString("all_email_assets"))) {
                            creativesData = new JSONArray(results.getString("all_email_assets"));
                        } else if (results.get("all_email_assets") instanceof JSONArray) {
                            creativesData = results.getJSONArray("all_email_assets");
                        }

                        if (creativesData != null && creativesData.length() > 0) {
                            File[] imagesFiles = new File[]{};
                            ManagementServer mngServ = Images.getMngemetServer();
                            String baseUrl = Images.getBaseUrl(mngServ);

                            for (int b1 = 0; b1 < creativesData.length() && (this.maxCreatives <= 0 || count < this.maxCreatives); b1++) {
                                JSONObject creative = creativesData.getJSONObject(b1);
                                if (creative != null && creative.length() > 0) {
                                    if (!sbjctFrmNameFound) {
                                        if (creative.has("all_from_lines")) {
                                            JSONArray allFromLines = new JSONArray(creative.getString("all_from_lines"));
                                            if (allFromLines.length() > 0){
                                                for (int b2 = 0; b2 < allFromLines.length(); b2++) {
                                                    String content = allFromLines.getJSONObject(b2).getString("content").trim();
                                                    if (content != null && !"".equals(content) && !content.endsWith(":")){
                                                        params.getJSONArray("from-names").put(PlaceHoldeNames.remplace(content));
                                                    }
                                                }
                                            }
                                        }
                                        if (creative.has("all_subject_lines")) {
                                            JSONArray allSubjectLines = new JSONArray(creative.getString("all_subject_lines"));
                                            if (allSubjectLines.length() > 0){
                                                for (int b2 = 0; b2 < allSubjectLines.length(); b2++) {
                                                    String content2 = allSubjectLines.getJSONObject(b2).getString("content").trim();
                                                    if (content2 != null && !"".equals(content2) && !content2.endsWith(":")){
                                                        params.getJSONArray("subjects").put(PlaceHoldeNames.remplace(content2));
                                                    }
                                                }
                                            }
                                        }
                                        if (params.getJSONArray("subjects").length() > 0 && params.getJSONArray("from-names").length() > 0){
                                            sbjctFrmNameFound = true;
                                        }
                                    }

                                    if (creative.has("content") && creative.get("content") instanceof String && !"".equals(creative.get("content"))) {
                                        JSONObject creativeRecord = new JSONObject();
                                        Document doc = Jsoup.parse(creative.getString("content"), "utf-8");
                                        String code = PlaceHoldeNames.remplace(doc.toString());
                                        Elements imgAnchors = doc.select("img");

                                        for (Element anchr : imgAnchors) {
                                            String src = String.valueOf(anchr.attr("src")).trim();
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

                                        JSONArray creativeLinks = new JSONArray();
                                        ArrayList<String> checkLinks = new ArrayList();
                                        Elements anchors = doc.select("a");
                                        boolean unsubFound = false;

                                        for (Element anch : anchors) {
                                            String href = String.valueOf(anch.attr("href")).trim();
                                            if (!checkLinks.contains(href)) {
                                                boolean unubCondition = (href.contains("unsub") || href.contains("opt-out") || href.contains("optout") || href.contains("remove") || href.contains("/oo/oo")) ? true : false;
                                                if (unubCondition && !href.contains("cid=" + this.affiliateNetwork.affiliateId)) {
                                                    creativeLinks.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + href + "\"}"));
                                                    unsubFound = true;
                                                } else {
                                                    if (href.contains("?")) {
                                                        code = StringUtils.replace(code, href, href.split(Pattern.quote("?"))[0]);
                                                        href = href.split(Pattern.quote("?"))[0];
                                                    }
                                                    creativeLinks.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + href + "\"}"));
                                                }
                                                checkLinks.add(href);
                                            }
                                        }

                                        if (creativeLinks.length() > 1 && !unsubFound){
                                            creativeLinks.getJSONObject(creativeLinks.length() - 1).put("type", "unsub");
                                        }
                                        creativeRecord.put("code", code);
                                        creativeRecord.put("links", creativeLinks);
                                        params.getJSONArray("creatives").put(creativeRecord);
                                        count++;
                                    }
                                }
                            }

                            if (imagesFiles.length > 0 && mngServ != null && !mngServ.getEmpty()){
                                Images.uploadImages(mngServ, imagesFiles);
                            }
                        }
                    }
                }
            }
        }
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "offerData"})
    public W4OffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, JSONObject offerData) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.offerData = offerData;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof W4OffersGrabber))
            return false;
        W4OffersGrabber goto1 = (W4OffersGrabber)paramObject;
        if (!goto1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = goto1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        if (getMaxCreatives() != goto1.getMaxCreatives())
            return false;
        JSONObject jSONObject1 = getOfferData();
        JSONObject jSONObject2 = goto1.getOfferData();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof W4OffersGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        n = n * 59 + getMaxCreatives();
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

    public JSONObject getOfferData() {
        return offerData;
    }

    public void setOfferData(JSONObject offerData) {
        this.offerData = offerData;
    }

    @Override
    public String toString() {
        return "W4OffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", offerData=" + getOfferData() + ")";
    }
}
