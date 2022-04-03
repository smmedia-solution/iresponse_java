package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Production;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.TypesParser;

public class DataListCounter extends Thread {

    private int dataListId;
    private int offerId;
    private String verticalsIds;
    private String countries;
    private JSONObject filters;

    public void run() {
        try {
            DataList dtList = new DataList(Integer.valueOf(this.dataListId));
            if (dtList.getEmpty()){
                throw new DatabaseException("Datalist not found !");
            }

            boolean bool = false;
            StringBuilder query1 = new StringBuilder();
            StringBuilder query2 = new StringBuilder();

            query1.append("SELECT COUNT(1) AS count FROM ").append(dtList.tableSchema).append(".").append(dtList.tableName).append(" t WHERE (is_hard_bounced = 'f' OR is_hard_bounced IS NULL) AND (is_blacklisted = 'f' OR is_blacklisted IS NULL)");

            if ("enabled".equalsIgnoreCase(this.filters.getString("seeds"))) {
                query2.append("is_seed = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("fresh"))) {
                query2.append("is_fresh = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("clean"))) {
                query2.append("is_clean = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("openers"))) {
                query2.append("is_opener = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("clickers"))) {
                query2.append("is_clicker = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("leaders"))) {
                query2.append("is_leader = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("unsubs"))) {
                query2.append("is_unsub = 't' OR ");
                bool = true;
            }

            if ("enabled".equalsIgnoreCase(this.filters.getString("optouts"))) {
                query2.append("is_optout = 't' OR ");
                bool = true;
            }

            if (bool == true){
                query1.append(" AND (").append(query2.toString().trim().substring(0, query2.length() - 4)).append(") ");
            }

            if (this.verticalsIds != null && !"".equals(this.verticalsIds)){
                query1.append(" AND string_to_array(verticals,',') @> ARRAY[").append(this.verticalsIds).append("]");
            }

            if (this.countries != null && !"".equals(this.countries)){
                query1.append(" AND country_code = ANY(ARRAY[").append(this.countries).append("])");
            }

            Offer ofer = null;
            if (this.offerId > 0){
                ofer = new Offer(this.offerId);
            }

            if (ofer != null && !ofer.getEmpty()) {
                String supList = "sup_list_" + ofer.affiliateNetworkId + "_" + ofer.productionId + "_" + this.dataListId;
                if (Database.get("clients").existeTable("suppressions", supList)){
                    query1.append(" AND NOT EXISTS ( SELECT FROM suppressions.").append(supList).append(" WHERE email_md5 = t.email_md5 )");
                }
            }

            int id = TypesParser.safeParseInt(Database.get("clients").availableTables(query1.toString(), null, 0, "count").get(0));
            if (id > 0) {
                HashMap<String, Object> result = new HashMap<>(4);
                result.put("id", dtList.id);
                result.put("name", dtList.name);
                result.put("total-count", dtList.totalCount);
                result.put("available-count", id);
                Production.addCount(result);
            }

        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"dataListId", "offerId", "verticalsIds", "countries", "filters"})
    public DataListCounter(int dataListId, int offerId, String verticalsIds, String countries, JSONObject filters) {
        this.dataListId = dataListId;
        this.offerId = offerId;
        this.verticalsIds = verticalsIds;
        this.countries = countries;
        this.filters = filters;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DataListCounter))
            return false;
        DataListCounter for1 = (DataListCounter)paramObject;
        if (!for1.exists(this))
            return false;
        if (getDataListId() != for1.getDataListId())
            return false;
        if (getOfferId() != for1.getOfferId())
            return false;
        String str1 = getVerticalsIds();
        String str2 = for1.getVerticalsIds();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getCountries();
        String str4 = for1.getCountries();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        JSONObject jSONObject1 = getFilters();
        JSONObject jSONObject2 = for1.getFilters();
        return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DataListCounter;
    }

    @Override
    public int hashCode() {
        int n  = 1;
        n = n * 59 + getDataListId();
        n = n * 59 + getOfferId();
        String str1 = getVerticalsIds();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getCountries();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        JSONObject jSONObject = getFilters();
        return n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
    }

    public int getDataListId() {
        return dataListId;
    }

    public void setDataListId(int dataListId) {
        this.dataListId = dataListId;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public String getVerticalsIds() {
        return verticalsIds;
    }

    public void setVerticalsIds(String verticalsIds) {
        this.verticalsIds = verticalsIds;
    }

    public String getCountries() {
        return countries;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public JSONObject getFilters() {
        return filters;
    }

    public void setFilters(JSONObject filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "DataListCounter(dataListId=" + getDataListId() + ", offerId=" + getOfferId() + ", verticalsIds=" + getVerticalsIds() + ", countries=" + getCountries() + ", filters=" + getFilters() + ")";
    }
}
