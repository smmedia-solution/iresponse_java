package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.actions.Click;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.affiliate.AffiliateApi;
import tech.iresponse.utils.TypesParser;

public class ClicksCollector extends Thread {

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
            if (api == null){
                throw new DatabaseException("Affiliate network api not found !");
            }

            api.setAffiliateNetwork(this.affiliateNetwork);

            //JSONArray clicks = api.getClicks(this.startDate, this.endDate);
            JSONArray clicks = null;
            if (clicks == null || clicks.length() == 0){
                throw new DatabaseException("No clicks found !");
            }

            Click click = null;
            JSONObject row = null;
            String token = null;

            for (int b = 0; b < clicks.length(); b++) {
                boolean notExiste = false;
                row = clicks.getJSONObject(b);
                if (row != null) {
                    token = StringUtils.replace(StringUtils.replace(StringUtils.replace(row.getString("click_date") + row.getString("token"), "-", ""), " ", ""), ":", "");
                    click = (Click)Click.first(Click.class, 3, "unique_token = ?", new Object[] { token });
                    if (click == null || click.getEmpty()) {
                        click = new Click();
                        notExiste = true;
                    }

                    click.uniqueToken = token;
                    click.affiliateNetworkId = this.affiliateNetwork.id;
                    click.offerProductionId = String.valueOf(row.get("offer_id"));
                    click.userFullName = this.mailers.containsKey(Integer.valueOf(click.userProductionId)) ? (String)this.mailers.get(Integer.valueOf(click.userProductionId)) : "Unknown Mailer";
                    click.actionTime = new Timestamp((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)).parse(row.getString("click_date")).getTime());
                    click.processType = "md";

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
                            String[] subsIndex = row.getString("sub_" + index).split(Pattern.quote("_"));
                            int pointer = 0;
                            for (String str2 : subs) {
                                if (pointer < subsIndex.length){
                                    switch (str2) {
                                        case "mailer_id":{
                                            click.userProductionId = TypesParser.safeParseInt(subsIndex[pointer]);
                                            pointer++;
                                            break;
                                        }
                                        case "process_id":{
                                            click.processId = TypesParser.safeParseInt(subsIndex[pointer]);
                                            pointer++;
                                            break;
                                        }
                                        case "vmta_id":{
                                            click.vmtaId = TypesParser.safeParseInt(subsIndex[pointer]);
                                            pointer++;
                                            break;
                                        }
                                        case "list_id":{
                                            click.listId = TypesParser.safeParseInt(subsIndex[pointer]);
                                            pointer++;
                                            break;
                                        }
                                        case "email_id":{
                                            click.clientId = TypesParser.safeParseInt(subsIndex[pointer]);
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
                        if (parts.length >= 4){
                            click.processType = parts[4];
                        }
                    }

                    String tblUpdate = "production.mta_processes";
                    switch (click.processType) {
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

                    if (!click.processUpdated){
                        try {
                            switch (click.processType) {
                                case "md":{
                                    Database.get("system").executeUpdate("UPDATE production.mta_processes_ips SET clicks = clicks + 1 WHERE process_id = " + click.processId + " AND server_vmta_id = " + click.vmtaId, null, 0);
                                    break;
                                }
                                case "sd":{
                                    Database.get("system").executeUpdate("UPDATE production.smtp_processes_users SET clicks = clicks + 1 WHERE process_id = " + click.processId + " AND smtp_user_id = " + click.smtpUserId, null, 0);
                                    break;
                                }
                            }
                            Database.get("system").executeUpdate("UPDATE " + tblUpdate + " SET clicks = clicks + 1 WHERE id = " + click.processId, null, 0);
                            click.processUpdated = true;
                        } catch (Exception ex) {
                            Loggers.error(ex);
                        }
                    }

                    if (notExiste) {
                        click.insert();
                    } else {
                        click.update();
                    }
                }
            }
        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    @ConstructorProperties({"affiliateNetwork", "startDate", "endDate", "mailers"})
    public ClicksCollector(AffiliateNetwork affiliateNetwork, String startDate, String endDate, LinkedHashMap mailers) {
        this.affiliateNetwork = affiliateNetwork;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mailers = mailers;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ClicksCollector))
            return false;
        ClicksCollector if1 = (ClicksCollector)paramObject;
        if (!if1.exists(this))
            return false;
        AffiliateNetwork do1 = getAffiliateNetwork();
        AffiliateNetwork do2 = if1.getAffiliateNetwork();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        String str1 = getStartDate();
        String str2 = if1.getStartDate();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getEndDate();
        String str4 = if1.getEndDate();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        LinkedHashMap linkedHashMap1 = getMailers();
        LinkedHashMap linkedHashMap2 = if1.getMailers();
            return !((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ClicksCollector;
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
        return "ClicksCollector(affiliateNetwork=" + getAffiliateNetwork() + ", startDate=" + getStartDate() + ", endDate=" + getEndDate() + ", mailers=" + getMailers() + ")";
    }
}
