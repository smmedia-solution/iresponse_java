package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Affiliate;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Images;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.http.Agents;

public class HitpathOffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private JSONObject offerData;

    @Override
    public void run() {
        try {
            JSONObject params = new JSONObject();
            params.put("production-id", this.offerData.getInt("id"));
            params.put("campaign-id", this.offerData.getInt("id"));
            params.put("name", this.offerData.getString("name").trim());
            params.put("description", this.offerData.has("description") ? this.offerData.getString("description").trim() : "No Description !");
            params.put("rules", this.offerData.has("geo") ? this.offerData.getString("geo").trim() : "No Rules !");
            params.put("payout-type", this.offerData.getString("unit").trim().toLowerCase().contains("lead") ? "CPA" : "CPC");
            params.put("payout-amount", TypesParser.safeParseDouble(this.offerData.getString("payout").replaceAll(Pattern.quote("$"), "").trim()));
            params.put("countries", getCountries());
            params.put("verticals", getVerticals());
            getCreatives(params);
            Affiliate.updateOffers(params);
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    protected String getCountries() {
        String countries = "";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (!"".equals(this.offerData.getString("geo"))){
                if (this.offerData.getString("geo").trim().toLowerCase().contains("us only") || this.offerData.getString("description").trim().toLowerCase().contains("us only")) {
                    countries = "US";
                } else if (this.offerData.getString("geo").trim().toLowerCase().contains("uk only") || this.offerData.getString("description").trim().toLowerCase().contains("uk only")) {
                    countries = "UK";
                } else if (this.offerData.getString("geo").trim().toLowerCase().contains("all traffic accepted")) {
                    countries = "ALL";
                }
            }
        } catch (JSONException jSONException) {}
        countries = stringBuilder.toString();
        return "".contains(countries) ? "US" : (countries.endsWith(",") ? countries.substring(0, countries.length() - 1) : countries);
    }

    protected JSONArray getVerticals() {
        JSONArray verticals = new JSONArray();
        try {
            String verticl = "";
            if (this.offerData.getString("category").trim().contains(",")) {
                String[] arrayOfString = this.offerData.getString("category").split(",");
                for (String str1 : arrayOfString){
                    verticl = verticl + "\"" + str1 + "\",";
                }
            } else {
                verticl = "\"" + this.offerData.getString("category").trim() + "\"";
            }

            if (!"".equals(verticl)) {
                verticals = new JSONArray("[" + verticl + "]");
            } else {
                verticals = new JSONArray();
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
        File lastResultFile = new File(System.getProperty("storage.path") + "/affiliate/" + (getAffiliateNetwork()).id + "/last_hitpath_result_offerId_" + this.offerData.getInt("id") + ".json");

        LinkedHashMap<String,String> offers = new LinkedHashMap<>();
        offers.put("Accept", "application/json");
        offers.put("Authorization", "Bearer  " + (getAffiliateNetwork()).apiKey);
        String results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + this.offerData.getInt("id") + "/emails", null, 60, offers);

        if (results.contains("Too Many Attempts.")) {
            ThreadSleep.sleep(34000L);
            results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + this.offerData.getInt("id") + "/emails", null, 60, offers);
        } else {
            FileUtils.writeStringToFile(lastResultFile, results, "utf-8");
        }

        if (results == null || "".equals(results) || results.contains("Too Many Attempts.")) {
            if (lastResultFile.exists()) {
                results = FileUtils.readFileToString(lastResultFile, "utf-8");
            } else {
                ThreadSleep.sleep(34000L);
                results = Agents.get((getAffiliateNetwork()).apiUrl + "/campaigns/" + this.offerData.getInt("id") + "/emails", null, 60, offers);
            }
        } else {
            FileUtils.writeStringToFile(lastResultFile, results, "utf-8");
        }

        JSONObject responses = new JSONObject(results);
        if(responses != null && responses.has("data") && JsonUtils.isJSONArray(responses.get("data")) && responses.getJSONArray("data").length() > 0 )
        {
            JSONArray data = responses.getJSONArray("data");
            int count = 0;
            for (int i = 0; i < data.length(); i++) {
                if ((this.maxCreatives == 0 || count < this.maxCreatives) && "html".equalsIgnoreCase(data.getJSONObject(i).getString("type").trim())) {
                    ManagementServer uploadCenter = Images.getMngemetServer();
                    String baseUrl = Images.getBaseUrl(uploadCenter);
                    File[] imagesFiles = new File[]{};

                    if (data.getJSONObject(i).has("body")) {
                        JSONObject creativeRecord = new JSONObject();
                        JSONObject creativeData = data.getJSONObject(i);
                        Document doc = Jsoup.parse(creativeData.getString("body"), "utf-8");
                        String code = PlaceHoldeNames.remplace(doc.toString());
                        JSONArray creativeLinks = new JSONArray();
                        //ArrayList<String> checkLinks = new ArrayList();
                        ArrayList checkLinks = new ArrayList();
                        Elements anchors = doc.select("a");
                        boolean unsubFound = false;
                        //System.out.println("creativeData = " + creativeData);
                        for (Element element : anchors) {
                            String href = String.valueOf(element.attr("href")).trim();
                            if (!checkLinks.contains(href)) {
                                if (href.contains("http")) {
                                    boolean unubCondition = (href.contains("unsub") || href.contains("opt-out") || href.contains("optout") || href.contains("remove") || href.contains("/oo/oo")) ? true : false;
                                    if (href.contains("/0/2/") || href.contains("/0/0/0/")) {
                                        if (href.contains("/0/2/")) {
                                            creativeLinks.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + href + "\"}"));
                                            } else if (href.contains("/0/0/0/") || href.contains("/u") || unubCondition) {
                                                creativeLinks.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + href + "\"}"));
                                                unsubFound = true;
                                            } else {
                                                creativeLinks.put(new JSONObject("{\"type\":\"other\",\"link\":\"" + href + "\"}"));
                                            }
                                        } else if (unubCondition) {
                                            creativeLinks.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + href + "\"}"));
                                        } else {
                                            creativeLinks.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + href + "\"}"));
                                        }
                                    }
                                    checkLinks.add(href);
                                }
                            }
                            if (creativeLinks.length() > 1 && unsubFound == false){
                                creativeLinks.getJSONObject(creativeLinks.length() - 1).put("type", "unsub");
                            }

                            //if (!fromNameFound) {
                            if (creativeData.has("from_lines") && creativeData.get("from_lines") instanceof JSONArray && creativeData.getJSONArray("from_lines").length() > 0){
                                for (int b = 0; b < creativeData.getJSONArray("from_lines").length(); b++) {
                                    if (creativeData.getJSONArray("from_lines").get(b) != null && !"".equals(creativeData.getJSONArray("from_lines").getString(b))){
                                        if(!params.getJSONArray("from-names").toString().contains(creativeData.getJSONArray("from_lines").getString(b))){
                                            params.getJSONArray("from-names").put(PlaceHoldeNames.remplace(creativeData.getJSONArray("from_lines").getString(b)));
                                        }
                                        //System.out.println("from: " + b + " = " + creativeData.getJSONArray("from_lines").getString(b));
                                    }
                                }
                            }
                            //System.out.println("from_name = " + params.getJSONArray("from-names"));
                            if (creativeData.has("subjects") && creativeData.get("subjects") instanceof JSONArray && creativeData.getJSONArray("subjects").length() > 0){
                                for (int b = 0; b < creativeData.getJSONArray("subjects").length(); b++) {
                                    if (creativeData.getJSONArray("subjects").get(b) != null && !"".equals(creativeData.getJSONArray("subjects").getString(b))){
                                        if(!params.getJSONArray("subjects").toString().contains(creativeData.getJSONArray("subjects").getString(b))){
                                            params.getJSONArray("subjects").put(PlaceHoldeNames.remplace(creativeData.getJSONArray("subjects").getString(b)));
                                        }
                                        //System.out.println("subjct: " + b + " = " + creativeData.getJSONArray("subjects").getString(b));
                                    }
                                }
                            }
                            //}
                            //System.out.println("subjects = " + params.getJSONArray("subjects"));

                            /*Elements anchorsImg = doc.select("img");
                            for (Element anchrimg : anchorsImg) {
                                String src = String.valueOf(anchrimg.attr("src")).trim();
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
                            }*/
                            if (creativeData.has("images") && JsonUtils.isJSONArray(creativeData.get("images"))) {
                                JSONArray imagesData = creativeData.getJSONArray("images");
                                if (imagesData != null && imagesData.length() > 0) {
                                    for (int b = 0; b < imagesData.length(); b++) {
                                        String name = imagesData.getJSONObject(b).getString("name").trim();
                                        String extension2 = name.substring(name.lastIndexOf('.') + 1);
                                        String fileName2 = Strings.rndomSalt(15, false) + "." + extension2;
                                        File urlFileName2 = new File(System.getProperty("media.path") + File.separator + fileName2);
                                        if (Images.isImage(extension2)) {
                                            String urlImage = imagesData.getJSONObject(b).getString("url").trim();
                                            //System.out.println("url = " + imagesData.getJSONObject(b).getString("url").trim());
                                            /*String resImage = Agents.get(urlImage, null, 120, offers, urlFileName2);
                                            if (resImage.contains("text/html")) {
                                                ThreadSleep.sleep(34000L);
                                                //System.out.println("results = " + resImage);
                                                Agents.get(urlImage, null, 120, offers, urlFileName2);
                                                //break;
                                            }*/
                                            Agents.get(urlImage, null, 120, offers, urlFileName2);
                                            if (urlFileName2.exists()) {
                                                code = StringUtils.replace(code, name, baseUrl + "/media/" + fileName2);
                                                imagesFiles = (File[])ArrayUtils.add((Object[])imagesFiles, urlFileName2);
                                            }
                                            //ThreadSleep.sleep(2280L);
                                        }

                                    }
                                }
                            }

                            creativeRecord.put("code", code);
                            creativeRecord.put("links", creativeLinks);
                            params.getJSONArray("creatives").put(creativeRecord);
                        }

                        if (imagesFiles.length > 0 && uploadCenter != null && !uploadCenter.getEmpty()){
                            Images.uploadImages(uploadCenter, imagesFiles);
                        }
                        count++;
                    }
                }
            }
        //}
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "offerData"})
    public HitpathOffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, JSONObject offerData) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.offerData = offerData;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof HitpathOffersGrabber))
            return false;
        HitpathOffersGrabber try1 = (HitpathOffersGrabber)paramObject;
        if (!try1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = try1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        if (getMaxCreatives() != try1.getMaxCreatives())
            return false;
        JSONObject jSONObject1 = getOfferData();
        JSONObject jSONObject2 = try1.getOfferData();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof HitpathOffersGrabber;
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
        return "HitpathOffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", offerData=" + getOfferData() + ")";
    }
}
