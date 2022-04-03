package tech.iresponse.tools.services;

import com.google.common.net.InternetDomainName;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.dns.DnsApi;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;

public class DomainRecordsUpdater extends Thread {

    private JSONObject map;

    @Override
    public void run() {
        try {
            if (this.map == null || this.map.length() == 0){
                throw new DatabaseException("No domain map found !");
            }

            Domain domin = new Domain(Integer.valueOf(this.map.getInt("domain-id")));
            if (domin.getEmpty()){
                throw new DatabaseException("No domain found !");
            }

            domin.value = domin.value.replaceAll("\r", "").replaceAll("\n", "");

            DnsApi api = DnsApi.controller(domin.accountType);
            if (api == null){
                throw new DatabaseException("No domain api found !");
            }

            switch (domin.accountType) {
                case "namecheap": {
                    LinkedHashMap<Object, Object> nmeCheapRecord = new LinkedHashMap<>();
                    nmeCheapRecord.put("EmailType", "MX");
                    int b1 = 1;
                    for (int b2 = 0; b2 < this.map.getJSONArray("records").length(); b2++) {
                        nmeCheapRecord.put("HostName" + b1, this.map.getJSONArray("records").getJSONObject(b2).getString("host"));
                        nmeCheapRecord.put("RecordType" + b1, this.map.getJSONArray("records").getJSONObject(b2).getString("type").toUpperCase());
                        nmeCheapRecord.put("Address" + b1, this.map.getJSONArray("records").getJSONObject(b2).getString("value"));
                        nmeCheapRecord.put("TTL" + b1, this.map.getJSONArray("records").getJSONObject(b2).getString("ttl"));
                        if ("mx".equalsIgnoreCase(this.map.getJSONArray("records").getJSONObject(b2).getString("type"))){
                            nmeCheapRecord.put("MXPre" + b1, this.map.getJSONArray("records").getJSONObject(b2).getString("priority"));
                        }
                        b1++;
                    }
                    nmeCheapRecord.put("TLD", InternetDomainName.from(domin.value).publicSuffix().toString());
                    nmeCheapRecord.put("SLD", StringUtils.replace(domin.value, "." + (String)nmeCheapRecord.get("TLD"), ""));
                    api.setDomainRecords(domin.accountId, domin.value, nmeCheapRecord);
                    return;
                }
                case "godaddy": {
                    JSONArray godaddyRecord = new JSONArray();
                    for (int b2 = 0; b2 < this.map.getJSONArray("records").length(); b2++) {
                        JSONObject rcord = new JSONObject();
                        rcord.put("name", this.map.getJSONArray("records").getJSONObject(b2).getString("host"));
                        rcord.put("data", this.map.getJSONArray("records").getJSONObject(b2).getString("value"));
                        rcord.put("ttl", TypesParser.safeParseInt(this.map.getJSONArray("records").getJSONObject(b2).getString("ttl")));
                        rcord.put("type", this.map.getJSONArray("records").getJSONObject(b2).getString("type").toUpperCase());
                        if ("mx".equalsIgnoreCase(rcord.getString("type"))){
                            rcord.put("priority", TypesParser.safeParseInt(this.map.getJSONArray("records").getJSONObject(b2).getString("priority")));
                        }
                        godaddyRecord.put(rcord);
                    }
                    api.setDomainRecords(domin.accountId, domin.value, godaddyRecord);
                    return;
                }
                case "namecom": {
                    JSONArray nmComRecord = api.getDomainRecords(domin.accountId, domin.value);
                    if (nmComRecord != null && nmComRecord.length() > 0){
                        for (int b1 = 0; b1 < nmComRecord.length(); b1++){
                            api.deleteRecords(domin.accountId, domin.value, nmComRecord.getJSONObject(b1).getInt("id"));
                        }
                    }
                    nmComRecord = new JSONArray();
                    for (int b2 = 0; b2 < this.map.getJSONArray("records").length(); b2++) {
                        JSONObject rcordNmcom = new JSONObject();
                        rcordNmcom.put("host", this.map.getJSONArray("records").getJSONObject(b2).getString("host"));
                        rcordNmcom.put("answer", this.map.getJSONArray("records").getJSONObject(b2).getString("value"));
                        rcordNmcom.put("ttl", TypesParser.safeParseInt(this.map.getJSONArray("records").getJSONObject(b2).getString("ttl")));
                        rcordNmcom.put("type", this.map.getJSONArray("records").getJSONObject(b2).getString("type").toUpperCase());
                        if ("mx".equalsIgnoreCase(rcordNmcom.getString("type"))){
                            rcordNmcom.put("priority", TypesParser.safeParseInt(this.map.getJSONArray("records").getJSONObject(b2).getString("priority")));
                        }
                        nmComRecord.put(rcordNmcom);
                    }
                    api.setDomainRecords(domin.accountId, domin.value, nmComRecord);
                    return;
                }
            }

            throw new DatabaseException("Unsupported dns api !");

        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    @ConstructorProperties({"map"})
    public DomainRecordsUpdater(JSONObject map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DomainRecordsUpdater))
            return false;
        DomainRecordsUpdater do1 = (DomainRecordsUpdater)paramObject;
        if (!do1.exists(this))
            return false;
        JSONObject jSONObject1 = getMap();
        JSONObject jSONObject2 = do1.getMap();
            return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DomainRecordsUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        JSONObject jSONObject = getMap();
        return n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
    }

    public JSONObject getMap() {
        return map;
    }

    public void setMap(JSONObject map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "DomainRecordsUpdater(map=" + getMap() + ")";
    }
}
