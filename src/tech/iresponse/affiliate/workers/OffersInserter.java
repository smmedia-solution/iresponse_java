package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.affiliate.Vertical;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.affiliate.FromName;
import tech.iresponse.models.affiliate.Creative;
import tech.iresponse.models.affiliate.Link;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.affiliate.Subject;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.Crypto;
import tech.iresponse.core.Application;

public class OffersInserter extends Thread {

    private JSONObject data;
    private AffiliateNetwork affiliateNetwork;

    @Override
    public void run() {
        try {
            if (this.data != null && this.data.has("campaign-id")) {

                Offer ofer = (Offer)Offer.first(Offer.class, "affiliate_network_id = ? AND production_id = ?", new Object[] { Integer.valueOf(this.affiliateNetwork.id), String.valueOf(this.data.get("production-id")) });
                if (ofer != null && ofer.id > 0) {
                    FromName.delete(FromName.class, "offer_id = ?", new Object[] { Integer.valueOf(ofer.id) });
                    Subject.delete(Subject.class, "offer_id = ?", new Object[] { Integer.valueOf(ofer.id) });
                    Creative.delete(Creative.class, "offer_id = ?", new Object[] { Integer.valueOf(ofer.id) });
                    Link.delete(Link.class, "offer_id = ?", new Object[] { Integer.valueOf(ofer.id) });
                } else {
                    ofer = new Offer();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.add(1, 1);
                ofer.status = "Activated";
                ofer.affiliateNetworkId = this.affiliateNetwork.id;
                ofer.affiliateNetworkName = this.affiliateNetwork.name;
                ofer.productionId = String.valueOf(this.data.get("production-id"));
                ofer.campaignId = String.valueOf(this.data.get("campaign-id"));
                ofer.name = this.data.getString("name");
                ofer.description = this.data.getString("description");
                ofer.rules = this.data.getString("rules");
                ofer.expirationDate = new Date(calendar.getTimeInMillis());
                ofer.type = String.valueOf(this.data.getString("payout-type")).toUpperCase();
                ofer.autoSup = false;
                ofer.payout = this.data.getString("payout-amount");
                ofer.payout = (new DecimalFormat("#.00")).format(TypesParser.safeParseDouble(ofer.payout.replace("&pound;", "").replace("$", "").replace("£","")));
                ofer.availableDays = "mon,tue,wed,thu,fri,sat,sun";
                ofer.createdBby = (Application.checkAndgetInstance().getUser()).email;
                ofer.createdDate = new Date(System.currentTimeMillis());
                ofer.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                ofer.lastUpdatedDate = new Date(System.currentTimeMillis());
                ofer.countries = this.data.getString("countries");

                // Verticals
                if (this.data.has("verticals")) {
                    String[] verticalIds = new String[0];
                    Vertical vertical = null;
                    for (int b = 0; b < this.data.getJSONArray("verticals").length(); b++) {
                        vertical = (Vertical)Vertical.first(Vertical.class, "name LIKE ?", new Object[] { this.data.getJSONArray("verticals").getString(b) });
                        if (vertical != null && vertical.id > 0) {
                            verticalIds = (String[])ArrayUtils.add(verticalIds, String.valueOf(vertical.id));
                        } else {
                            vertical = new Vertical();
                            vertical.status = "Activated";
                            vertical.name = this.data.getJSONArray("verticals").getString(b);
                            vertical.createdBby = (Application.checkAndgetInstance().getUser()).email;
                            vertical.createdDate = new Date(System.currentTimeMillis());
                            vertical.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                            vertical.lastUpdatedDate = new Date(System.currentTimeMillis());
                            int veticalId = vertical.insert();
                            verticalIds = (String[])ArrayUtils.add(verticalIds, String.valueOf(veticalId));
                        }
                    }
                    if (verticalIds.length > 0){
                        ofer.verticalsIds = String.join(",", (CharSequence[])verticalIds);
                    }
                }

                int offerId = ofer.id;
                if (offerId == 0) {
                    offerId = ofer.insert();
                } else {
                    ofer.update();
                }

                // FromNames
                if (this.data.has("from-names") && this.data.getJSONArray("from-names").length() > 0) {
                    FromName frmName = null;
                    for (int b = 0; b < this.data.getJSONArray("from-names").length(); b++) {
                        frmName = new FromName();
                        frmName.status = "Activated";
                        frmName.affiliateNetworkId = this.affiliateNetwork.id;
                        frmName.offerId = offerId;
                        frmName.name = "from_name_" + (b + 1);
                        frmName.value = this.data.getJSONArray("from-names").getString(b);
                        frmName.createdBby = (Application.checkAndgetInstance().getUser()).email;
                        frmName.createdDate = new Date(System.currentTimeMillis());
                        frmName.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        frmName.lastUpdatedDate = new Date(System.currentTimeMillis());
                        if (frmName.value != null && frmName.value.length() > 0 && frmName.value.length() < 200){
                            frmName.insert();
                        }
                    }
                }

                // Subjects
                if (this.data.has("subjects") && this.data.getJSONArray("subjects").length() > 0) {
                    Subject subject = null;
                    for (int b = 0; b < this.data.getJSONArray("subjects").length(); b++) {
                        subject = new Subject();
                        subject.status = "Activated";
                        subject.offerId = offerId;
                        subject.affiliateNetworkId = this.affiliateNetwork.id;
                        subject.name = "subject_" + (b + 1);
                        subject.value = this.data.getJSONArray("subjects").getString(b);
                        subject.createdBby = (Application.checkAndgetInstance().getUser()).email;
                        subject.createdDate = new Date(System.currentTimeMillis());
                        subject.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        subject.lastUpdatedDate = new Date(System.currentTimeMillis());
                        if (subject.value != null && subject.value.length() > 0 && subject.value.length() < 200){
                            subject.insert();
                        }
                    }
                }

                // Creatives
                if (this.data.has("creatives") && this.data.getJSONArray("creatives").length() > 0) {
                    Creative creative = null;
                    int creativeId = 0;
                    for (int b = 0; b < this.data.getJSONArray("creatives").length(); b++) {
                        creative = new Creative();
                        creative.status = "Activated";
                        creative.offerId = offerId;
                        creative.affiliateNetworkId = this.affiliateNetwork.id;
                        //creative.name = "creative_" + (b + 1);
                        creative.name = this.data.getJSONArray("creatives").getJSONObject(b).has("name") ? this.data.getJSONArray("creatives").getJSONObject(b).getString("name") : ("creative_" + (b + 1));
                        creative.value = Crypto.Base64Encode(this.data.getJSONArray("creatives").getJSONObject(b).getString("code"));
                        creative.createdBby = (Application.checkAndgetInstance().getUser()).email;
                        creative.createdDate = new Date(System.currentTimeMillis());
                        creative.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                        creative.lastUpdatedDate = new Date(System.currentTimeMillis());
                        creativeId = creative.insert();

                        // Links
                        if (this.data.getJSONArray("creatives").getJSONObject(b).has("links") && this.data.getJSONArray("creatives").getJSONObject(b).getJSONArray("links").length() > 0) {
                            Link link = null;
                            for (int b1 = 0; b1 < this.data.getJSONArray("creatives").getJSONObject(b).getJSONArray("links").length(); b1++) {
                                link = new Link();
                                link.status = "Activated";
                                link.offerId = offerId;
                                link.affiliateNetworkId = this.affiliateNetwork.id;
                                link.creativeId = creativeId;
                                link.type = this.data.getJSONArray("creatives").getJSONObject(b).getJSONArray("links").getJSONObject(b1).getString("type");
                                link.value = this.data.getJSONArray("creatives").getJSONObject(b).getJSONArray("links").getJSONObject(b1).getString("link");
                                link.createdBby = (Application.checkAndgetInstance().getUser()).email;
                                link.createdDate = new Date(System.currentTimeMillis());
                                link.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
                                link.lastUpdatedDate = new Date(System.currentTimeMillis());
                                link.insert();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"data", "affiliateNetwork"})
    public OffersInserter(JSONObject data, AffiliateNetwork affiliateNetwork) {
        this.data = data;
        this.affiliateNetwork = affiliateNetwork;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof OffersInserter))
            return false;
        OffersInserter byte1 = (OffersInserter)paramObject;
        if (!byte1.exists(this))
            return false;
        JSONObject jSONObject1 = getData();
        JSONObject jSONObject2 = byte1.getData();
        if ((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = byte1.getAffiliateNetwork();
        return !((do1 == null) ? (do2 != null) : !do1.equals(do2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof OffersInserter;
    }

    @Override
    public int hashCode() {
        int n = 1;
        JSONObject jSONObject = getData();
        n = n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
        AffiliateNetwork do1 = getAffiliateNetwork();
        return n * 59 + ((do1 == null) ? 43 : do1.hashCode());
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public AffiliateNetwork getAffiliateNetwork() {
        return affiliateNetwork;
    }

    public void setAffiliateNetwork(AffiliateNetwork affiliateNetwork) {
        this.affiliateNetwork = affiliateNetwork;
    }

    @Override
    public String toString() {
        return "OffersInserter(data=" + getData() + ", affiliateNetwork=" + getAffiliateNetwork() + ")";
    }
}