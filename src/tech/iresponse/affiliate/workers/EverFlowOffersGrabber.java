package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.Files;
import tech.iresponse.utils.Images;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Agents;
import tech.iresponse.core.Application;

public class EverFlowOffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private int offerId;

    @Override
    public void run() {
        try {
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("x-eflow-api-key", this.affiliateNetwork.apiKey);
            params.put("content-type", "application/json");
            String result = Agents.get(this.affiliateNetwork.apiUrl + "/v1/affiliates/offers/" + this.offerId, null, 60, params);
            if (result == null || "".equals(result)){
                throw new DatabaseException("No response retreived !");
            }

            JSONObject response = new JSONObject(result);
            if (response.has("error")){
                throw new DatabaseException(response.getString("error"));
            }

            //System.out.println("response : " + response);

            JSONObject offer = new JSONObject();
            offer.put("campaign-id", String.valueOf(response.get("network_id")));
            offer.put("production-id", String.valueOf(response.get("network_offer_id")));
            offer.put("name", response.getString("name").trim());
            offer.put("description", response.has("html_description") ? response.getString("html_description").trim() : "No Description !");
            offer.put("rules", response.has("terms_and_conditions") ? response.getString("terms_and_conditions").trim() : "No Rules !");
            offer.put("payout-type", "CPA");
            offer.put("payout-amount", 0.0D);

            /*if (response.has("payouts") && response.get("payouts") instanceof JSONObject && response.getJSONObject("payouts").length() > 0 && response.getJSONObject("payouts").has("entries") && response.getJSONObject("payouts").get("entries") instanceof JSONArray && response.getJSONObject("payouts").getJSONArray("entries").length() > 0) {
                offer.put("payout-type", response.getJSONObject("payouts").getJSONArray("entries").getJSONObject(0).getString("payout_type").toUpperCase());
                offer.put("payout-amount", TypesParser.safeParseDouble(response.getJSONObject("payouts").getJSONArray("entries").getJSONObject(0).get("payout_amount")));
            }*/
            if (response.getJSONObject("relationship").has("payouts") && response.getJSONObject("relationship").get("payouts") instanceof JSONObject && response.getJSONObject("relationship").getJSONObject("payouts").length() > 0 && response.getJSONObject("relationship").getJSONObject("payouts").has("entries") && response.getJSONObject("relationship").getJSONObject("payouts").get("entries") instanceof JSONArray && response.getJSONObject("relationship").getJSONObject("payouts").getJSONArray("entries").length() > 0) {
                offer.put("payout-type", response.getJSONObject("relationship").getJSONObject("payouts").getJSONArray("entries").getJSONObject(0).getString("payout_type").toUpperCase());
                offer.put("payout-amount", TypesParser.safeParseDouble(response.getJSONObject("relationship").getJSONObject("payouts").getJSONArray("entries").getJSONObject(0).get("payout_amount")));
            }

            offer.put("countries", getCountries(response.getJSONObject("relationship")));
            offer.put("from-names", getFromNames(response.getJSONObject("relationship")));
            offer.put("subjects", getSubject(response.getJSONObject("relationship")));
            offer.put("verticals", getVerticals(response.getJSONObject("relationship")));

            offer = getCreatives(offer, response);
            Affiliate.updateOffers(offer);

        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    protected String getCountries(JSONObject paramJSONObject) {
        String countries = "";
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray jSONArray = (paramJSONObject.has("ruleset") && paramJSONObject.get("ruleset") instanceof JSONObject && paramJSONObject.getJSONObject("ruleset").has("countries") && paramJSONObject.getJSONObject("ruleset").get("countries") instanceof JSONArray) ? paramJSONObject.getJSONObject("ruleset").getJSONArray("countries") : new JSONArray();
            if (jSONArray.length() > 0){
                for (int b = 0; b < jSONArray.length(); b++) {
                    if (jSONArray.get(b) instanceof JSONObject && jSONArray.getJSONObject(b).has("country_code")){
                        sb.append(jSONArray.getJSONObject(b).getString("country_code")).append(",");
                    }
                }
            }
        } catch (JSONException jSONException) {}
        countries = sb.toString();
        return "".contains(countries) ? "US" : (countries.endsWith(",") ? countries.substring(0, countries.length() - 1) : countries);
    }

    protected JSONArray getFromNames(JSONObject paramJSONObject) {
        JSONArray fromNames = new JSONArray();
        try {
            String fromLines = (paramJSONObject.has("email") && paramJSONObject.get("email") instanceof JSONObject) ? paramJSONObject.getJSONObject("email").getString("from_lines") : "";
            if (!"".equals(fromLines)) {
                String[] results = fromLines.split("\\n");
                for (String fromnm : results) {
                    if (fromnm != null && !"".equals(fromnm) && !fromnm.endsWith(":")){
                        fromNames.put(PlaceHoldeNames.remplace(fromnm));
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return fromNames;
    }

    protected JSONArray getSubject(JSONObject paramJSONObject) {
        JSONArray subjects = new JSONArray();
        try {
            String subjectLines = (paramJSONObject.has("email") && paramJSONObject.get("email") instanceof JSONObject) ? paramJSONObject.getJSONObject("email").getString("subject_lines") : "";
            if (!"".equals(subjectLines)) {
                String[] results = subjectLines.split("\\n");
                for (String sbj : results) {
                    if (sbj != null && !"".equals(sbj) && !sbj.endsWith(":")){
                        subjects.put(PlaceHoldeNames.remplace(sbj));
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return subjects;
    }

    protected JSONArray getVerticals(JSONObject paramJSONObject) {
        JSONArray verticals = new JSONArray();
        try {
            String name = paramJSONObject.getJSONObject("category").getString("name");
            if (!"".equals(name)) {
                verticals = new JSONArray("[" + name + "]");
            } else {
                verticals = new JSONArray();
            }
        } catch (JSONException jSONException) {}

        if (verticals.length() == 0){
            verticals.put("Uncategorized");
        }
        return verticals;
    }

    protected JSONObject getCreatives(JSONObject offers, JSONObject paramJSONObject2) throws Exception {
        offers.put("creatives", new JSONArray());
        String trackingUrl = paramJSONObject2.getString("tracking_url");
        String unsubLink = paramJSONObject2.getJSONObject("relationship").getJSONObject("email_optout").getString("unsub_link");
        File creativeFolder = new File(System.getProperty("trash.path") + File.separator + Strings.rndomSalt(20, false));
        creativeFolder.mkdirs();
        Application.add(creativeFolder);

        ManagementServer mngServ = Images.getMngemetServer();
        String str3 = Images.getBaseUrl(mngServ);

        File creativeFile = new File(creativeFolder.getAbsolutePath() + File.separator + Strings.rndomSalt(15, false) + ".zip");
        Agents.get(paramJSONObject2.getJSONObject("relationship").getJSONObject("creative_bundle").getString("url").trim(), null, 120, creativeFile);

        if (creativeFile.exists()) {
            Terminal.executeCommand("unzip -d " + creativeFolder.getAbsolutePath() + " " + creativeFile.getAbsolutePath());
            List<File> files = Files.listAllFiles(creativeFolder);

            if (!files.isEmpty()) {
                int count = 1;
                for (File file : files) {
                    if (this.maxCreatives > 0 && count > this.maxCreatives){
                        break;
                    }

                    JSONObject jSONObject = new JSONObject();
                    File[] imagesFiles = new File[]{};
                    String code = null;
                    Document document = null;
                    int i = TypesParser.safeParseInt(file.getName().split("_")[0]);
                    List<File> creatives = Files.listAllFiles(file, true);

                    if (creatives != null && !creatives.isEmpty()) {
                        boolean extracted = false;
                        for (File file3 : creatives) {
                            if (file3.getAbsolutePath().toLowerCase().endsWith("zip")) {
                                Terminal.executeCommand("unzip -d " + creativeFolder.getAbsolutePath() + " " + file3.getAbsolutePath());
                                extracted = true;
                            }
                        }

                        if (extracted){
                            creatives = Files.listAllFiles(file, true);
                        }

                        for (File file3 : creatives) {
                            if (FilenameUtils.getExtension(file3.getAbsolutePath()).toLowerCase().contains("txt") || FilenameUtils.getExtension(file3.getAbsolutePath()).toLowerCase().contains("html")) {
                                code = FileUtils.readFileToString(file3, "UTF-8");
                                code = StringUtils.replace(code, "?creative_id=" + i, "");
                                code = StringUtils.replace(code, "#url#", trackingUrl);
                                code = StringUtils.replace(code, "#unsub#", unsubLink);
                                document = Jsoup.parse(code);

                                if (document != null && !"".equals(code)) {
                                    Elements img = document.select("img");
                                    for (Element elm : img) {
                                        String src = String.valueOf(elm.attr("src")).trim();
                                        if (src != null && !"".equals(src)) {
                                            String extension = src.substring(src.lastIndexOf('.') + 1);
                                            if (Images.isImage(extension)) {
                                                String fileName = Strings.rndomSalt(15, false) + "." + extension;
                                                File urlFileName = new File(System.getProperty("media.path") + File.separator + fileName);
                                                if (src.contains("http")) {
                                                    Agents.get(src, null, 120, urlFileName);
                                                } else if ((new File(file + File.separator + src)).exists()) {
                                                    FileUtils.copyFile(new File(file + File.separator + src), urlFileName);
                                                }
                                                code = StringUtils.replace(code, src, str3 + "/media/" + fileName);
                                                imagesFiles = (File[])ArrayUtils.add((Object[])imagesFiles, urlFileName);
                                            }
                                        }
                                    }
                                    if (imagesFiles.length > 0 && mngServ != null && !mngServ.getEmpty()){
                                        Images.uploadImages(mngServ, imagesFiles);
                                    }
                                    JSONArray creativeRecord = new JSONArray();
                                    creativeRecord.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + trackingUrl + "\"}"));
                                    creativeRecord.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + unsubLink + "\"}"));
                                    jSONObject.put("code", Jsoup.parse(code).toString());
                                    jSONObject.put("links", creativeRecord);
                                    offers.getJSONArray("creatives").put(jSONObject);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return offers;
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "offerId"})
    public EverFlowOffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, int offerId) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.offerId = offerId;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof EverFlowOffersGrabber))
            return false;
        EverFlowOffersGrabber int1 = (EverFlowOffersGrabber)paramObject;
        if (!int1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = int1.getAffiliateNetwork();
            return ((do1 == null) ? (do2 != null) : !do1.equals(do2)) ? false : ((getMaxCreatives() != int1.getMaxCreatives()) ? false : (!(getOfferId() != int1.getOfferId())));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof EverFlowOffersGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        n = n * 59 + getMaxCreatives();
        return n * 59 + getOfferId();
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

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    @Override
    public String toString() {
        return "EverFlowOffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", offerId=" + getOfferId() + ")";
    }
}
