package tech.iresponse.dns;

import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.utils.Crypto;

public abstract class DnsApi implements DnsInterface {

    private LinkedHashMap mapRecords;
    private JSONArray jsonRecords;

    public static DnsApi controller(String accountType) {
        switch (Crypto.Base64Encode(accountType)) {
            case "bmFtZWNoZWFw": //namecheap
                return new Namecheap();
            case "Z29kYWRkeQ==": //godaddy
                return new Godaddy();
            case "bmFtZWNvbQ==": //Namecom
                return new Namecom();
        }
        return null;
    }

    public String setDomainRecords(int accountId, String domainValue, Object paramObject) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public boolean deleteRecords(int accountId, String domainValue, int id) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public JSONArray getDomainRecords(int accountId, String domainValue) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void setRecords(int accountId, String domainValue) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void addRecord(String record, String value) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void addRecord(JSONObject records) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void setupRecords(String record, String ip, Domain domin, ManagementServer managServ) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void addSPFDkim(int nbcount, String mainIp, String hostName, String record, String vspf, String dkim) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    public void setupDkimDmarc(int nbcount, String dkim, String mainIp, boolean hasIpV6, String vspf, boolean dmarc, Domain domain) {
        throw new UnsupportedOperationException("Not supported here!");
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
        return true;
        if (!(paramObject instanceof DnsApi))
        return false;
        DnsApi do1 = (DnsApi)paramObject;
        if (!do1.exists(this))
            return false;
        LinkedHashMap linkedHashMap1 = getMapRecords();
        LinkedHashMap linkedHashMap2 = do1.getMapRecords();
        if ((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2))
            return false;
        JSONArray jSONArray1 = getJsonRecords();
        JSONArray jSONArray2 = do1.getJsonRecords();
        return !((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2));
    }

    protected boolean exists(Object instance) {
        return instance instanceof DnsApi;
    }

    @Override
    public int hashCode() {
        int n = 1;
        LinkedHashMap linkedHashMap = getMapRecords();
        n = n * 59 + ((linkedHashMap == null) ? 43 : linkedHashMap.hashCode());
        JSONArray jSONArray = getJsonRecords();
        return n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
    }

    public LinkedHashMap getMapRecords() {
        return this.mapRecords;
    }

    public JSONArray getJsonRecords() {
        return this.jsonRecords;
    }

    public void setMapRecords(LinkedHashMap mapRecords) {
        this.mapRecords = mapRecords;
    }

    public void setJsonRecords(JSONArray jsonRecords) {
        this.jsonRecords = jsonRecords;
    }

    @Override
    public String toString() {
        return "DnsApi(mapRecords=" + getMapRecords() + ", jsonRecords=" + getJsonRecords() + ")";
    }
}