package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.Files;
import tech.iresponse.utils.Images;
import tech.iresponse.utils.Url;
import tech.iresponse.http.Agents;
import tech.iresponse.core.Application;
import tech.iresponse.webservices.Affiliate;

public class CakeOffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private int campaignId;
    private JSONObject offerData;

    @Override
    public void run() {
        try {
            LinkedHashMap<String,String> params = new LinkedHashMap<>();
            params.put("api_key", this.affiliateNetwork.apiKey);
            params.put("affiliate_id", String.valueOf(this.affiliateNetwork.affiliateId));
            params.put("campaign_id", String.valueOf(this.campaignId));

            String result = Agents.get(this.affiliateNetwork.apiUrl + "/Offers/Campaign", params, 60);
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (!response.has("success") || !response.getBoolean("success")){
                throw new DatabaseException(response.getString("message"));
            }

            int rowCount = response.has("row_count") ? response.getInt("row_count") : 0;

            if (rowCount == 0){
                throw new DatabaseException("No offer retreived !");
            }

            this.offerData = (response.has("data") && response.getJSONArray("data").length() > 0 && response.getJSONArray("data").get(0) instanceof JSONObject) ? response.getJSONArray("data").getJSONObject(0) : new JSONObject();
            if (this.offerData != null && this.offerData.length() == 0){
                throw new DatabaseException("No offer retreived !");
            }

            JSONObject offer = new JSONObject();
            offer.put("campaign-id", this.campaignId);
            offer.put("production-id", String.valueOf(this.offerData.getInt("offer_id")));
            offer.put("name", this.offerData.getString("offer_name").trim());
            offer.put("description", this.offerData.has("description") ? this.offerData.getString("description").trim() : "No Description !");
            offer.put("rules", this.offerData.has("restrictions") ? this.offerData.getString("restrictions").trim() : "No Rules !");
            offer.put("payout-type", this.offerData.getString("price_format").trim().split(Pattern.quote("_"))[0]);
            offer.put("payout-amount", this.offerData.getDouble("price"));
            offer.put("countries", getCountries());
            offer.put("from-names", getFromNames());
            offer.put("subjects", getSubject());
            offer.put("verticals", getVerticals());
            getCreatives(offer);
            Affiliate.updateOffers(offer);
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    protected String getCountriesOld() {
        String countries = "";
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray allowedCountries = (this.offerData.has("allowed_countries") && this.offerData.get("allowed_countries") instanceof JSONArray) ? this.offerData.getJSONArray("allowed_countries") : new JSONArray();
            if (allowedCountries.length() > 0){
                for (int b = 0; b < allowedCountries.length(); b++) {
                    if (allowedCountries.get(b) instanceof JSONObject && allowedCountries.getJSONObject(b).has("country_code")){
                        sb.append(allowedCountries.getJSONObject(b).getString("country_code")).append(",");
                    }
                }
            }
        } catch (JSONException jSONException) {}
        countries = sb.toString();
        return "".contains(countries) ? "US" : (countries.endsWith(",") ? countries.substring(0, countries.length() - 1) : countries);
    }

    protected String getCountries() {
        String[] countries = new String[]{};
        try {
            JSONArray allowedCountries = (this.offerData.has("allowed_countries") && this.offerData.get("allowed_countries") instanceof JSONArray) ? this.offerData.getJSONArray("allowed_countries") : new JSONArray();
            if (allowedCountries.length() > 0) {
                for (int b = 0; b < allowedCountries.length(); b++) {
                    if (allowedCountries.get(b) instanceof JSONObject && allowedCountries.getJSONObject(b).has("country_code")) {
                        String countryCode = allowedCountries.getJSONObject(b).getString("country_code").trim();
                        countryCode = "GB".equals(countryCode) ? "UK" : countryCode;
                        countries = (String[])ArrayUtils.add((Object[])countries, countryCode);
                    }
                }
            } else {
                String[] offerName = this.offerData.getString("offer_name").trim().split(Pattern.quote("-"));
                if (offerName != null && offerName.length > 1)
                    countries = StringUtils.replace(StringUtils.replace(offerName[0], "BEnl", "BE"), "BEfr", "BE").trim().split(Pattern.quote("/"));
            }
        } catch (JSONException jSONException) {}
        return (countries.length > 0) ? String.join(",", (CharSequence[])countries) : "US";
    }

    protected JSONArray getFromNames() {
        JSONArray fromNames = new JSONArray();
        try {
            JSONArray fromLines = (this.offerData.has("from_lines") && this.offerData.get("from_lines") instanceof JSONArray) ? this.offerData.getJSONArray("from_lines") : new JSONArray();
            if (fromLines.length() > 0){
                for (int b = 0; b < fromLines.length(); b++) {
                String result = fromLines.getString(b);
                    if (result != null && !"".equals(result) && !result.endsWith(":")){
                        fromNames.put(PlaceHoldeNames.remplace(result));
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return fromNames;
    }

    protected JSONArray getSubject() {
        JSONArray subjects = new JSONArray();
        try {
            JSONArray subjectLines = (this.offerData.has("subject_lines") && this.offerData.get("subject_lines") instanceof JSONArray) ? this.offerData.getJSONArray("subject_lines") : new JSONArray();
            if (subjectLines.length() > 0){
                for (int b = 0; b < subjectLines.length(); b++) {
                    String result = subjectLines.getString(b);
                    if (result != null && !"".equals(result) && !result.endsWith(":")){
                        subjects.put(PlaceHoldeNames.remplace(result));
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return subjects;
    }

    protected JSONArray getVerticals() {
        JSONArray verticals = new JSONArray();
        try {
            String verticl = "";
            if (this.offerData.getString("vertical_name").trim().contains("/")) {
                String[] verticalName = this.offerData.getString("vertical_name").split("/");
                for (String vrtname : verticalName){
                    verticl = verticl + "\"" + vrtname + "\",";
                }
            } else {
                verticl = "\"" + this.offerData.getString("vertical_name").trim() + "\"";
            }

            verticl = verticl.endsWith(",") ? verticl.substring(0, verticl.length() - 1) : verticl;
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

    protected void getCreatives(JSONObject offers) throws Exception {
        offers.put("creatives", new JSONArray());
        JSONArray creatives = (this.offerData.has("creatives") && this.offerData.get("creatives") instanceof JSONArray) ? this.offerData.getJSONArray("creatives") : new JSONArray();

        if (creatives.length() > 0) {
            ArrayList<JSONObject> creative = new ArrayList();
            int count = 0;
            for (int i = 0; i < creatives.length(); i++) {
                if ((this.maxCreatives == 0 || count < this.maxCreatives) && creatives.getJSONObject(i).toString().contains("\"creative_type_name\":\"Email\"")) {
                    creative.add(creatives.getJSONObject(i));
                    count++;
                }
            }

            if (!creative.isEmpty()) {
                File creativeFolder = new File(System.getProperty("trash.path") + File.separator + Strings.rndomSalt(20, false));
                creativeFolder.mkdirs();
                Application.add(creativeFolder);

                ManagementServer mngServ = Images.getMngemetServer();
                String str = Images.getBaseUrl(mngServ);

                File creativeFile = new File(creativeFolder.getAbsolutePath() + File.separator + Strings.rndomSalt(15, false));
                Agents.get(this.offerData.getString("creatives_download_url").trim(), null, 120, creativeFile);

                if (creativeFile.exists()) {
                    Terminal.executeCommand("unzip -d " + creativeFolder.getAbsolutePath() + " " + creativeFile.getAbsolutePath());
                    List<File> files = Files.listAllFiles(creativeFolder);

                    if (!files.isEmpty()){
                        for (JSONObject crtv : creative) {

                            File fileEmail = null;
                            int creativeId = crtv.has("creative_id") ? crtv.getInt("creative_id") : 0;
                            if (creativeId > 0){
                                for (File fl : files) {
                                    if (fl.getName().startsWith(creativeId + "_Email")){
                                        fileEmail = fl;
                                    }
                                }
                            }

                            if (fileEmail != null) {
                                JSONObject creativeRecord = new JSONObject();
                                File[] imagesFiles = new File[]{};
                                String code = "";
                                Document doc = null;
                                LinkedHashMap<String,String> params = new LinkedHashMap<>();

                                params.put("api_key", this.affiliateNetwork.apiKey);
                                params.put("affiliate_id", String.valueOf(this.affiliateNetwork.affiliateId));
                                params.put("campaign_id", String.valueOf(this.campaignId));
                                params.put("creative_id", String.valueOf(creativeId));
                                String results = Agents.get(this.affiliateNetwork.apiUrl + "/Creatives/GetCode", params, 60);

                                if (results != null && !"".equals(results)) {
                                    JSONObject response = new JSONObject(results);
                                    if (response.has("success") && response.getBoolean("success") == true) {
                                        code = PlaceHoldeNames.remplace(response.getJSONArray("data").getJSONObject(0).getString("file_content"));
                                        doc = Jsoup.parse(code);
                                    }
                                }

                                if (doc != null && !"".equals(code)) {
                                    Elements anchors = doc.select("img");

                                    for (Element anchr : anchors) {
                                        String src = String.valueOf(anchr.attr("src")).trim();
                                        if (src != null && !"".equals(src)) {
                                            String extension = src.substring(src.lastIndexOf('.') + 1);
                                            if (Images.isImage(extension)) {
                                                String fileName = Strings.rndomSalt(15, false) + "." + extension;
                                                File urlFileName = new File(System.getProperty("media.path") + File.separator + fileName);
                                                if (src.contains("http")) {
                                                    Agents.get(src, null, 120, urlFileName);
                                                } else if ((new File(fileEmail + File.separator + src)).exists()) {
                                                    FileUtils.copyFile(new File(fileEmail + File.separator + src), urlFileName);
                                                }
                                                code = StringUtils.replace(code, src, str + "/media/" + fileName);
                                                imagesFiles = (File[])ArrayUtils.add((Object[])imagesFiles, urlFileName);
                                            }
                                        }
                                    }

                                    if (imagesFiles.length > 0 && mngServ != null && !mngServ.getEmpty()){
                                        Images.uploadImages(mngServ, imagesFiles);
                                    }

                                    JSONArray creativeLinks = new JSONArray();
                                    ArrayList<String> checkLinks = new ArrayList();
                                    String uniqueLink = StringUtils.replace(StringUtils.replace(crtv.getString("unique_link"), "https://", ""), "http://", "");
                                    String str4 = Url.checkUrl(uniqueLink);

                                    if (uniqueLink != null) {
                                        Elements elsm = doc.select("a");
                                        for (Element element : elsm) {
                                            String href = String.valueOf(element.attr("href")).trim();
                                            if (href != null && !"".equals(href) && !checkLinks.contains(href)) {
                                                String urlLink = StringUtils.replace(StringUtils.replace(href, "https://", ""), "http://", "");
                                                if ((uniqueLink.trim().equals(urlLink.trim()) || urlLink.contains(str4)) && !urlLink.contains("unsub") && !urlLink.contains("opt-out") && !urlLink.contains("optout") && !urlLink.contains("remove") && !urlLink.contains("/oo/oo")) {
                                                    creativeLinks.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + href + "\"}"));
                                                } else {
                                                    creativeLinks.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + href + "\"}"));
                                                }
                                                checkLinks.add(href);
                                            }
                                        }
                                    }

                                    creativeRecord.put("code", code);
                                    creativeRecord.put("links", creativeLinks);
                                    offers.getJSONArray("creatives").put(creativeRecord);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "campaignId", "offerData"})
    public CakeOffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, int campaignId, JSONObject offerData) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.campaignId = campaignId;
        this.offerData = offerData;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof CakeOffersGrabber))
            return false;
        CakeOffersGrabber do1 = (CakeOffersGrabber)paramObject;
        if (!do1.exists(this))
            return false;
        AffiliateNetwork do2 = getAffiliateNetwork();
        AffiliateNetwork do3 = do1.getAffiliateNetwork();
        if ((do2 == null) ? (do3 != null) : !do2.equals(do3))
            return false;
        if (getMaxCreatives() != do1.getMaxCreatives())
            return false;
        if (getCampaignId() != do1.getCampaignId())
            return false;
        JSONObject jSONObject1 = getOfferData();
        JSONObject jSONObject2 = do1.getOfferData();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof CakeOffersGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        n = n * 59 + getMaxCreatives();
        n = n * 59 + getCampaignId();
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

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public JSONObject getOfferData() {
        return offerData;
    }

    public void setOfferData(JSONObject offerData) {
        this.offerData = offerData;
    }

    @Override
    public String toString() {
        return "CakeOffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", campaignId=" + getCampaignId() + ", offerData=" + getOfferData() + ")";
    }
}
