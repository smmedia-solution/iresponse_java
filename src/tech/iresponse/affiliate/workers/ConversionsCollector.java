package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.actions.Click;
import tech.iresponse.models.actions.Lead;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.affiliate.AffiliateApi;
import tech.iresponse.utils.TypesParser;

public class ConversionsCollector extends Thread {

    private AffiliateNetwork affiliateNetwork;
    private String startDate;
    private String endDate;
    private LinkedHashMap mailers;

    @Override
    public void run() {
        try {
            if (this.affiliateNetwork.getEmpty()){
                throw new DatabaseException("No affiliate network passed to this process !");
            }

            AffiliateApi api = AffiliateApi.controller(this.affiliateNetwork.apiType);

            if (api != null) {
                api.setAffiliateNetwork(this.affiliateNetwork);
                JSONArray conversions = api.getConversions(this.startDate, this.endDate);

                if (conversions != null && conversions.length() > 0) {
                    Lead lead = null;
                    Click click = null;
                    JSONObject row = null;
                    String token = null;

                    for (int b = 0; b < conversions.length(); b++) {
                        boolean notExiste = false;
                        row = conversions.getJSONObject(b);

                        if (row != null && row.has("payout")) {
                            token = StringUtils.replace(StringUtils.replace(StringUtils.replace(row.getString("conversion_date") + row.getString("token"), "-", ""), " ", ""), ":", "");
                            if (token != null && token.length() > 0) {
                                lead = (Lead)Lead.first(Lead.class, 3, "unique_token = ?", new Object[] { token });
                                if (lead == null || lead.getEmpty()) {
                                    lead = new Lead();
                                    notExiste = true;
                                }


                                for (int index = 1; index < 4; index++) {
                                    //String[] subs = new String[0];
                                    String[] subs = {};
                                    switch (index) {
                                        case 1:{
                                            subs = (this.affiliateNetwork.subIdOne != null && !"".equals(this.affiliateNetwork.subIdOne)) ? this.affiliateNetwork.subIdOne.split(Pattern.quote("|")) : new String[]{};
                                            break;
                                        }
                                        case 2:{
                                            subs = (this.affiliateNetwork.subIdTwo != null && !"".equals(this.affiliateNetwork.subIdTwo)) ? this.affiliateNetwork.subIdTwo.split(Pattern.quote("|")) : new String[]{};
                                            break;
                                        }
                                        case 3:{
                                            subs = (this.affiliateNetwork.subIdThree != null && !"".equals(this.affiliateNetwork.subIdThree)) ? this.affiliateNetwork.subIdThree.split(Pattern.quote("|")) : new String[]{};
                                            break;
                                        }
                                    }

                                    if (row.has("sub_" + index) && !"".equals(row.getString("sub_" + index))) {
                                        String[] values = row.getString("sub_" + index).split(Pattern.quote("_"));
                                        int pointer = 0;
                                        for (String sub : subs) {
                                            if (pointer < values.length) {
                                                switch (sub) {
                                                    case "mailer_id":{
                                                        lead.userProductionId = TypesParser.safeParseInt(values[pointer]);
                                                        pointer++;
                                                        break;
                                                    }
                                                    case "process_id":{
                                                        lead.processId = TypesParser.safeParseInt(values[pointer]);
                                                        pointer++;
                                                        break;
                                                    }
                                                    case "vmta_id":{
                                                        lead.vmtaId = TypesParser.safeParseInt(values[pointer]);
                                                        pointer++;
                                                        break;
                                                    }
                                                    case "list_id":{
                                                        lead.listId = TypesParser.safeParseInt(values[pointer]);
                                                        pointer++;
                                                        break;
                                                    }
                                                    case "email_id":{
                                                        lead.clientId = TypesParser.safeParseInt(values[pointer]);
                                                        pointer++;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!"".equals(row.getString("sub_3"))) {
                                    String[] parts = row.getString("sub_3").split("_");
                                    if (parts.length >= 4) {
                                        click = new Click(Integer.valueOf(TypesParser.safeParseInt(parts[3])));
                                        if (!click.getEmpty()) {
                                            lead.agent = click.agent;
                                            lead.actionIp = click.actionIp;
                                            lead.countryCode = click.countryCode;
                                            lead.country = click.country;
                                            lead.region = click.region;
                                            lead.city = click.city;
                                            lead.deviceType = click.deviceType;
                                            lead.deviceName = click.deviceName;
                                            lead.operatingSystem = click.operatingSystem;
                                            lead.browserName = click.browserName;
                                            lead.browserVersion = click.browserVersion;
                                        }
                                        lead.processType = parts[4];
                                    }
                                } else {
                                    lead.processType = "md";
                                }

                                lead.uniqueToken = token;
                                lead.affiliateNetworkId = this.affiliateNetwork.id;
                                lead.offerProductionId = String.valueOf(row.get("offer_id"));
                                lead.userFullName = this.mailers.containsKey(Integer.valueOf(lead.userProductionId)) ? (String)this.mailers.get(Integer.valueOf(lead.userProductionId)) : "Unknown Mailer";
                                lead.actionTime = new Timestamp((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)).parse(row.getString("conversion_date")).getTime());
                                lead.payout = TypesParser.safeParseFloat(row.get("payout"));
                                lead.processType = (lead.processType == null) ? "md" : lead.processType;

                                lead.processType = (lead.processType == null) ? "md" : lead.processType;
                                if (lead.listId > 0 && lead.clientId > 0) {
                                    DataList dtList = new DataList(Integer.valueOf(lead.listId));
                                    if (!dtList.getEmpty() && dtList.tableSchema != null && !"".equals(dtList.tableSchema) && dtList.tableName != null && !"".equals(dtList.tableName) && lead.clientId > 0){
                                        try {
                                            Database.get("clients").executeUpdate("UPDATE " + dtList.tableSchema + "." + dtList.tableName + " SET is_leader = 't', is_fresh = 'f', is_clean = 'f', is_opener = 'f', is_clicker = 'f', is_optout = 'f' WHERE id = ?", new Object[] { Integer.valueOf(lead.clientId) }, 0);
                                        } catch (Exception e) {
                                            Loggers.error(e);
                                        }
                                    }
                                }

                                String tblUpdate = "production.mta_processes";
                                switch (lead.processType) {
                                    case "md":
                                    case "mt":{
                                        tblUpdate = "production.mta_processes";
                                        break;
                                    }
                                    case "sd":
                                    case "st":{
                                        tblUpdate = "production.smtp_processes";
                                        break;
                                    }
                                }

                                if (!lead.processUpdated){
                                    try {
                                        switch (lead.processType) {
                                            case "md":{
                                                Database.get("system").executeUpdate("UPDATE production.mta_processes_ips SET clicks = clicks + 1 WHERE process_id = " + lead.processId + " AND server_vmta_id = " + lead.vmtaId, null, 0);
                                                break;
                                            }
                                            case "sd":{
                                                Database.get("system").executeUpdate("UPDATE production.smtp_processes_users SET clicks = clicks + 1 WHERE process_id = " + lead.processId + " AND smtp_user_id = " + lead.smtpUserId, null, 0);
                                                break;
                                            }
                                        }
                                        Database.get("system").executeUpdate("UPDATE " + tblUpdate + " SET leads = leads + 1 WHERE id = " + lead.processId, null, 0);
                                        lead.processUpdated = true;
                                    } catch (Exception ex) {
                                        Loggers.error(ex);
                                    }
                                }

                                if (notExiste) {
                                    lead.insert();
                                } else {
                                    lead.update();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    @ConstructorProperties({"affiliateNetwork", "startDate", "endDate", "mailers"})
    public ConversionsCollector(AffiliateNetwork affiliateNetwork, String startDate, String endDate, LinkedHashMap mailers) {
        this.affiliateNetwork = affiliateNetwork;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mailers = mailers;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ConversionsCollector))
            return false;
        ConversionsCollector for1 = (ConversionsCollector)paramObject;
        if (!for1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = for1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        String str1 = getStartDate();
        String str2 = for1.getStartDate();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getEndDate();
        String str4 = for1.getEndDate();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        LinkedHashMap linkedHashMap1 = getMailers();
        LinkedHashMap linkedHashMap2 = for1.getMailers();
            return !((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ConversionsCollector;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AffiliateNetwork do1 = getAffiliateNetwork();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        String str1 = getStartDate();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getEndDate();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        LinkedHashMap linkedHashMap = getMailers();
        return n * 59 + ((linkedHashMap == null) ? 43 : linkedHashMap.hashCode());
    }

    public AffiliateNetwork getAffiliateNetwork() {
        return affiliateNetwork;
    }

    public void setAffiliateNetwork(AffiliateNetwork affiliateNetwork) {
        this.affiliateNetwork = affiliateNetwork;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public LinkedHashMap getMailers() {
        return mailers;
    }

    public void setMailers(LinkedHashMap mailers) {
        this.mailers = mailers;
    }

    @Override
    public String toString() {
        return "ConversionsCollector(affiliateNetwork=" + getAffiliateNetwork() + ", startDate=" + getStartDate() + ", endDate=" + getEndDate() + ", mailers=" + getMailers() + ")";
    }
}