package tech.iresponse.affiliate;

import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.utils.Crypto;

public abstract class AffiliateApi implements AffiliateInterface {

    private AffiliateNetwork affiliateNetwork;
    private int maxCreatives = 1;

    public static AffiliateApi controller(String apiType) {
        switch (Crypto.Base64Encode(apiType)) {
            case "Y2FrZQ==":
                return new CakeApi();
            case "aGFzb2ZmZXJz":
                return new HasoffersApi();
            case "aGl0cGF0aA==":
                return new HitpathApi();
            case "cHVsbHN0YXQ=":
                return new PullstatApi();
            case "dzQ=":
                return new W4Api();
            case "ZXZlcmZsb3c=":
                return new EverflowApi();
        }
        return null;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AffiliateApi))
            return false;
        AffiliateApi do1 = (AffiliateApi)paramObject;
        if (!do1.exists(this))
            return false;
        AffiliateNetwork do2 = getAffiliateNetwork();
        AffiliateNetwork do3 = do1.getAffiliateNetwork();
        return ((do2 == null) ? (do3 != null) : !do2.equals(do3)) ? false : (!(getMaxCreatives() != do1.getMaxCreatives()));
    }

    protected boolean exists(Object instance) {
        return instance instanceof AffiliateApi;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        return n * 59 + getMaxCreatives();
    }

    public AffiliateNetwork getAffiliateNetwork() {
        return this.affiliateNetwork;
    }

    public int getMaxCreatives() {
        return this.maxCreatives;
    }

    public void setAffiliateNetwork(AffiliateNetwork affiliateNetwork) {
        this.affiliateNetwork = affiliateNetwork;
    }

    public void setMaxCreatives(int maxCreatives) {
        this.maxCreatives = maxCreatives;
    }

    @Override
    public String toString() {
        return "AffiliateApi(affiliateNetwork=" + getAffiliateNetwork() + ", maxCreatives=" + getMaxCreatives() + ")";
    }
}