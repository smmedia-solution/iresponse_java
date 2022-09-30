package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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
import tech.iresponse.utils.Terminal;
import tech.iresponse.webservices.Affiliate;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Images;
import tech.iresponse.utils.Url;
import tech.iresponse.http.Agents;

public class HasOffersGrabber extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives;
    private JSONObject data;

    @Override
    public void run() {
        try {
            JSONObject params = new JSONObject();
            params.put("production-id", this.data.getJSONObject("offer").getInt("id"));
            params.put("campaign-id", this.data.getJSONObject("offer").getInt("id"));
            params.put("name", this.data.getJSONObject("offer").getString("name").trim());
            params.put("description", (this.data.getJSONObject("offer").get("description") != null) ? this.data.getJSONObject("offer").getString("description") : "No Description !");
            params.put("rules", (this.data.getJSONObject("offer").get("terms_and_conditions") != null) ? this.data.getJSONObject("offer").getString("terms_and_conditions") : "No Rules !");
            params.put("payout-type", this.data.getJSONObject("offer").getString("payout_type").trim().toLowerCase().contains("cpa") ? "CPA" : "CPC");
            params.put("payout-amount", this.data.getJSONObject("offer").getDouble("default_payout"));
            params.put("verticals", getVerticals());
            params.put("countries", getCountries());
            params.put("from-names", getFromNames());
            params.put("subjects", getSubject());
            getCreatives(params);
            Affiliate.updateOffers(params);
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    protected JSONArray getVerticals() {
        JSONArray verticals = new JSONArray();
        try {
            if (this.data.has("verticals") && this.data.get("verticals") instanceof JSONArray && this.data.getJSONObject("verticals").length() > 0) {
                Iterator it = this.data.getJSONObject("verticals").keys();
                while (it.hasNext()) {
                    JSONObject vertcl = this.data.getJSONObject("verticals").getJSONObject(String.valueOf(it.next()));
                    if (vertcl != null && vertcl instanceof JSONObject && vertcl.has("name")){
                        verticals.put(vertcl.getString("name"));
                    }
                }
            } else {
                LinkedHashMap<String,String> params = new LinkedHashMap<>();
                params.put("Format", "json");
                params.put("Target", "Affiliate_Offer");
                params.put("Version", "3");
                params.put("Method", "getCategories");
                params.put("NetworkId", this.affiliateNetwork.networkId);
                params.put("Service", "HasOffers");
                params.put("api_key", this.affiliateNetwork.apiKey);
                params.put("ids[]", String.valueOf(this.data.getJSONObject("offer").getInt("id")));

                String results = Agents.get(this.affiliateNetwork.apiUrl + "/Affiliate_Offer.json", params, 60);
                if (results != null && !"".equals(results)) {
                    JSONObject responses = new JSONObject(results);
                    if (responses.has("response") && responses.get("response") instanceof JSONObject) {
                        responses = responses.getJSONObject("response");
                        if (responses.has("data")) {
                            JSONObject data = responses.getJSONArray("data").getJSONObject(0);
                            if (data != null && data.has("categories") && data.get("categories") instanceof JSONObject) {
                                Iterator it = data.getJSONObject("categories").keys();
                                while (it.hasNext()) {
                                    JSONObject categories = data.getJSONObject("categories").getJSONObject(String.valueOf(it.next()));
                                    if (categories != null && categories instanceof JSONObject && categories.has("name")){
                                        verticals.put(categories.getString("name"));
                                    }
                                }
                            }
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

    protected String getCountries() {
        String countries = "";
        StringBuilder sb = new StringBuilder();
        try {
            if (this.data.getJSONObject("countries").length() > 0) {
                Iterator it = this.data.getJSONObject("countries").keys();
                while (it.hasNext()) {
                    JSONObject contry = this.data.getJSONObject("countries").getJSONObject(String.valueOf(it.next()));
                    if (contry != null && contry instanceof JSONObject && contry.has("code")){
                        sb.append(contry.getString("code")).append(",");
                    }
                }
            }
        } catch (Exception exception) {}
        countries = sb.toString();
        return "".contains(countries) ? "US" : (countries.endsWith(",") ? countries.substring(0, countries.length() - 1) : countries);
    }

    protected JSONArray getFromNames() {
        JSONArray fromNames = new JSONArray();
        try {
            String[] froms = this.data.getJSONObject("offer").has("email_instructions_from") ? this.data.getJSONObject("offer").getString("email_instructions_from").split(Pattern.quote("\r\n")) : null;
            if (froms != null && froms.length > 0){
                for (String result : froms) {
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
            String[] offerSubject = this.data.getJSONObject("offer").has("email_instructions_subject") ? this.data.getJSONObject("offer").getString("email_instructions_subject").split(Pattern.quote("\r\n")) : null;
            if (offerSubject != null && offerSubject.length > 0){
                for (String sbjct : offerSubject) {
                    if (sbjct != null && !"".equals(sbjct) && !sbjct.endsWith(":")){
                        subjects.put(PlaceHoldeNames.remplace(sbjct));
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return subjects;
    }

    protected String getTrackingLink() {
        String link = "";
        try {
            LinkedHashMap<String,String> params = new LinkedHashMap<>();
            params.put("Format", "json");
            params.put("Target", "Affiliate_Offer");
            params.put("Version", "3");
            params.put("Method", "generateTrackingLink");
            params.put("NetworkId", this.affiliateNetwork.networkId);
            params.put("Service", "HasOffers");
            params.put("offer_id", this.data.getJSONObject("offer").getString("id"));
            params.put("api_key", this.affiliateNetwork.apiKey);

            String results = Agents.get(this.affiliateNetwork.apiUrl + "/Affiliate_Offer.json", params, 60);
            if (results != null && !"".equals(results)) {
                JSONObject responses = new JSONObject(results);
                if (responses.has("response")) {
                    responses = responses.getJSONObject("response");
                    if (responses.has("data") && responses.get("data") instanceof JSONObject) {
                        responses = responses.getJSONObject("data");
                        if (responses.has("click_url")){
                            link = responses.getString("click_url").replaceAll(Pattern.quote("&aff_sub={transaction_id}"), "");
                        }
                    }
                }
            }
        } catch (JSONException jSONException) {}
        return link;
    }

    protected String getOptoutLink() {
        String link = "";
        try {
            String results = Agents.get("https://crm3-java." + Url.checkUrl(this.affiliateNetwork.website) + "/api/v1/proxy/links/offer/" + this.data.getJSONObject("offer").getString("id") + "/id/" + this.affiliateNetwork.affiliateId + "/name/" + URLEncoder.encode(this.affiliateNetwork.companyName, StandardCharsets.UTF_8.toString()), null, 60);
            if (results != null && !"".equals(results)) {
                JSONObject responses = new JSONObject(results);
                if (responses.has("optOutLink")){
                    link = responses.getString("optOutLink");
                }
            }
        } catch (UnsupportedEncodingException|JSONException unsupportedEncodingException) {}
        return link;
    }

    protected void getCreatives(JSONObject offers) throws Exception {
        try {
            offers.put("creatives", new JSONArray());
            File[] imagesFiles = new File[]{};
            String clickLink = getTrackingLink();
            String unsubLink = (this.data.getJSONObject("offer").has("dne_unsubscribe_url") && !"".equals(this.data.getJSONObject("offer").getString("dne_unsubscribe_url")) && this.data.getJSONObject("offer").getString("dne_unsubscribe_url").contains("http")) ? this.data.getJSONObject("offer").getString("dne_unsubscribe_url").trim() : getOptoutLink();
            unsubLink = unsubLink.toUpperCase().contains("USE_YOUR_OWN_UNSUB_LINK") ? "" : unsubLink;

            ManagementServer mngServ = Images.getMngemetServer();
            String baseUrl = Images.getBaseUrl(mngServ);

            LinkedHashMap<String,String> params = new LinkedHashMap<>();
            params.put("Format", "json");
            params.put("Target", "Affiliate_Offer");
            params.put("Version", "3");
            params.put("Method", "findAll");
            params.put("NetworkId", this.affiliateNetwork.networkId);
            params.put("Service", "HasOffers");
            params.put("filters[offer_id]", this.data.getJSONObject("offer").getString("id"));
            params.put("filters[type]", "email creative");
            params.put("limit", "100");
            params.put("api_key", this.affiliateNetwork.apiKey);

            String results = Agents.get(this.affiliateNetwork.apiUrl + "/Affiliate_OfferFile.json", params, 60);
            if (results != null && !"".equals(results)) {
                JSONObject responses = new JSONObject(results);
                if (responses.has("response") && responses.get("response") instanceof JSONObject) {
                    responses = responses.getJSONObject("response");
                    if (responses.has("data") && responses.get("data") instanceof JSONObject) {
                        responses = responses.getJSONObject("data");
                        if (responses.has("data") && responses.get("data") instanceof JSONObject) {
                            responses = responses.getJSONObject("data");
                            if (responses.length() > 0) {
                                Iterator iterator = responses.keys();
                                int count = 0;
                                while (iterator.hasNext()) {
                                    JSONObject creatives = responses.getJSONObject(String.valueOf(iterator.next()));
                                    if (creatives != null && (this.maxCreatives == 0 || count < this.maxCreatives) && creatives instanceof JSONObject && creatives.has("OfferFile")) {
                                        creatives = creatives.getJSONObject("OfferFile");
                                        if (creatives.has("code")) {
                                            JSONObject creativeRecord = new JSONObject();
                                            Document doc = Jsoup.parse(creatives.getString("code"), "utf-8");
                                            String code = PlaceHoldeNames.remplace(doc.toString());
                                            Elements anchors = doc.select("img");
                                            for (Element achr : anchors) {
                                                String src = String.valueOf(achr.attr("src")).trim();
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
                                            code = StringUtils.replace(code, "{tracking_link}", clickLink);
                                            code = StringUtils.replace(code, "{tracking}", clickLink);
                                            code = StringUtils.replace(code, "{" + clickLink + "}", clickLink);
                                            code = StringUtils.replace(code, "{unsubscribe_link}", unsubLink);
                                            code = StringUtils.replace(code, "{unsubscribe}", unsubLink);
                                            code = StringUtils.replace(code, "{" + unsubLink + "}", unsubLink);

                                            JSONArray links = new JSONArray();
                                            links.put(new JSONObject("{\"type\":\"preview\",\"link\":\"" + clickLink + "\"}"));
                                            if (!"null".equals(unsubLink) && !"".equals(unsubLink)){
                                                links.put(new JSONObject("{\"type\":\"unsub\",\"link\":\"" + unsubLink + "\"}"));
                                            }
                                            creativeRecord.put("code", code);
                                            creativeRecord.put("links", links);
                                            offers.getJSONArray("creatives").put(creativeRecord);
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
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"affiliateNetwork", "maxCreatives", "data"})
    public HasOffersGrabber(AffiliateNetwork affiliateNetwork, int maxCreatives, JSONObject data) {
        this.affiliateNetwork = affiliateNetwork;
        this.maxCreatives = maxCreatives;
        this.data = data;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof HasOffersGrabber))
            return false;
        HasOffersGrabber new1 = (HasOffersGrabber)paramObject;
        if (!new1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = new1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        if (getMaxCreatives() != new1.getMaxCreatives())
            return false;
        JSONObject jSONObject1 = getData();
        JSONObject jSONObject2 = new1.getData();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof HasOffersGrabber;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        n = n * 59 + getMaxCreatives();
        JSONObject jSONObject = getData();
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

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HasOffersGrabber(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ", data=" + getData() + ")";
    }
}
