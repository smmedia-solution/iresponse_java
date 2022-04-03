package tech.iresponse.webservices;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.json.JSONArray;
import tech.iresponse.models.actions.Lead;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.Database;
import tech.iresponse.tools.proxy.Proxy;
import tech.iresponse.tools.services.DomainRecordsUpdater;
import tech.iresponse.tools.services.MailboxExtractor;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.DatesUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.http.ResponseData;
import tech.iresponse.core.Application;

public class Tools implements Controller {

    public static volatile LinkedHashMap<String, String> MAILBOXES_RESULTS = new LinkedHashMap<>();

    public Response updateDomainsRecords() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray domainsRecords = (app.getParameters().has("domains-records") && app.getParameters().get("domains-records") instanceof JSONArray) ? app.getParameters().getJSONArray("domains-records") : new JSONArray();
        if (domainsRecords == null || domainsRecords.length() == 0){
            throw new DatabaseException("No domains recrods passed !");
        }

        int nbthread = (domainsRecords.length() > 10) ? 10 : domainsRecords.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);

        for (int b = 0; b < domainsRecords.length(); b++){
            execService.submit((Runnable)new DomainRecordsUpdater(domainsRecords.getJSONObject(b)));
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)) {
            execService.shutdownNow();
        }
        return new Response("Domains records successfully !", 200);
    }

    public Response mailboxExtractor() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray mailboxes = (app.getParameters().has("mailboxes") && app.getParameters().get("mailboxes") instanceof JSONArray) ? app.getParameters().getJSONArray("mailboxes") : new JSONArray();
        if (mailboxes == null || mailboxes.length() == 0) {
            throw new DatabaseException("No mailboxes passed !");
        }

        JSONArray filters = (app.getParameters().has("filters") && app.getParameters().get("filters") instanceof JSONArray) ? app.getParameters().getJSONArray("filters") : new JSONArray();

        MailboxExtractor mboxExtract = null;
        int nbthread = (mailboxes.length() > 10) ? 10 : mailboxes.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);

        for (int b = 0; b < mailboxes.length(); b++) {
            String[] arrayOfString = String.valueOf(mailboxes.get(b)).replaceAll(" +", " ").trim().split(Pattern.quote(" "));
            if (arrayOfString.length > 0) {
                mboxExtract = new MailboxExtractor();
                mboxExtract.setEmail(arrayOfString[0].toLowerCase().trim());
                mboxExtract.setPassword(arrayOfString[1].trim());

                if (arrayOfString.length == 4){
                    mboxExtract.setProxy(new Proxy(arrayOfString[2].trim(), arrayOfString[3].trim()));
                }

                mboxExtract.setFolder(app.getParameters().getString("folder"));
                mboxExtract.setMaxEmailsNumber(TypesParser.safeParseInt(app.getParameters().getString("max-emails")));
                mboxExtract.setEmailsOrder(app.getParameters().getString("order"));
                mboxExtract.setFiltersType(app.getParameters().getString("filter-type"));
                mboxExtract.setMessageFilters(filters);
                mboxExtract.setSeparator(app.getParameters().getString("separator"));
                mboxExtract.setReturnType(app.getParameters().getString("return-type"));
                mboxExtract.setHeaderReturnKey(app.getParameters().getString("return-header-key"));

                if (app.getParameters().get("start-date") != null && !"".equals(String.valueOf(app.getParameters().get("start-date")))){
                    mboxExtract.setStartDate(DatesUtils.toDate(String.valueOf(app.getParameters().get("start-date")), "yyyy-MM-dd"));
                }

                if (app.getParameters().get("end-date") != null && !"".equals(String.valueOf(app.getParameters().get("end-date")))){
                    mboxExtract.setEndDate(DatesUtils.toDate(String.valueOf(app.getParameters().get("end-date")), "yyyy-MM-dd"));
                }

                execService.submit((Runnable)mboxExtract);
            }
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }

        if (MAILBOXES_RESULTS.isEmpty()){
            throw new DatabaseException("No results found !");
        }

        HashMap<Object, Object> results = new HashMap<>();
        MAILBOXES_RESULTS.entrySet().forEach(mailbox -> results.put(mailbox.getKey(), mailbox.getValue()));

        return (Response)new ResponseData("Mailbox extraction completed successfully !", results, 200);
    }

    public Response collectLeaders() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        String startDate = "";
        String endDate = "";

        if (app.getParameters().has("period")) {
            Calendar calendar;
            switch (app.getParameters().getString("period")) {
                case "today":{
                    startDate = (new SimpleDateFormat("yyyy-MM-dd 00:00:00")).format(new Date(System.currentTimeMillis()));
                    endDate = (new SimpleDateFormat("yyyy-MM-dd 23:59:59")).format(new Date(System.currentTimeMillis()));
                    break;
                }
                case "yesterday":{
                    calendar = Calendar.getInstance();
                    calendar.add(5, -1);
                    startDate = (new SimpleDateFormat("yyyy-MM-dd 00:00:00")).format(new Date(calendar.getTimeInMillis()));
                    endDate = (new SimpleDateFormat("yyyy-MM-dd 23:59:59")).format(new Date(calendar.getTimeInMillis()));
                    break;
                }
                case "this-week":{
                    calendar = Calendar.getInstance();
                    calendar.add(5, -7);
                    startDate = (new SimpleDateFormat("yyyy-MM-dd 00:00:00")).format(new Date(calendar.getTimeInMillis()));
                    endDate = (new SimpleDateFormat("yyyy-MM-dd 23:59:59")).format(new Date(System.currentTimeMillis()));
                    break;
                }
                case "this-month":{
                    startDate = (new SimpleDateFormat("yyyy-MM-01 00:00:00")).format(new Date(System.currentTimeMillis()));
                    endDate = (new SimpleDateFormat("yyyy-MM-dd 23:59:59")).format(new Date(System.currentTimeMillis()));
                    break;
                }
                case "last-month":{
                    calendar = Calendar.getInstance();
                    calendar.add(2, -1);
                    startDate = (new SimpleDateFormat("yyyy-MM-dd 00:00:00")).format(new Date(calendar.getTimeInMillis()));
                    endDate = (new SimpleDateFormat("yyyy-MM-dd 23:59:59")).format(new Date(System.currentTimeMillis()));
                    break;
                }
            }
        } else {
            startDate = app.getParameters().getString("start-date");
            endDate = app.getParameters().getString("end-date");
        }

        if ("".equals(startDate) || "".equals(endDate)){
            throw new DatabaseException("Invalid date range !");
        }

        List<Lead> leadList = (List)Lead.all(Lead.class, "TO_DATE(to_char(action_time,'YYYY-MM-DD'),'YYYY-MM-DD') between to_timestamp('" + startDate + "', 'YYYY-MM-DD') and to_timestamp('" + endDate + "', 'YYYY-MM-DD')", null);
        if (!leadList.isEmpty()){
            for (Lead conversion : leadList) {
                int listId = conversion.listId;
                int clientId = conversion.clientId;
                int vmtaId = conversion.vmtaId;

                if (listId > 0 && clientId > 0) {
                    DataList dtList = new DataList(Integer.valueOf(listId));
                    if (dtList.tableName != null && !"".equals(dtList.tableName) && dtList.tableSchema != null && !"".equals(dtList.tableSchema)){
                        Database.get("clients").executeUpdate("UPDATE " + dtList.tableSchema + "." + dtList.tableName + " SET is_leader = 't', is_fresh = 'f', is_clean = 'f', is_opener = 'f', is_clicker = 'f', is_optout = 'f' WHERE id = ?", new Object[] { Integer.valueOf(clientId) }, 0);
                    }
                }

                String mainTable = ("md".equals(conversion.processType) || "mt".equals(conversion.processType)) ? "production.mta_processes" : "production.smtp_processes";

                if (!conversion.processUpdated) {
                    if ("md".equals(conversion.processType)) {
                        Database.get("system").executeUpdate("UPDATE production.mta_processes_ips SET leads = leads + 1 WHERE drop_id = " + conversion.processId + " AND server_vmta_id = " + vmtaId, null, 0);
                    } else if ("sd".equals(conversion.processType)) {
                        Database.get("system").executeUpdate("UPDATE production.smtp_processes_users SET leads = leads + 1 WHERE drop_id = " + conversion.processId + " AND smtp_user_id = " + vmtaId, null, 0);
                    }
                    Database.get("system").executeUpdate("UPDATE " + mainTable + " SET leads = leads + 1 WHERE id = " + conversion.processId, null, 0);
                }

                conversion.processUpdated = true;
                conversion.update();
            }
        }
        return new Response("Leaders saved successfully !", 200);
    }

    public static synchronized void updateMailboxesResults(String mailbox, String result, String separator) {
        if (MAILBOXES_RESULTS.containsKey(mailbox)) {
            MAILBOXES_RESULTS.put(mailbox, (String)MAILBOXES_RESULTS.get(mailbox) + "\n" + separator + "\n" + result);
        } else {
            MAILBOXES_RESULTS.put(mailbox, result);
        }
    }

    public Response controller(String action) {
        try {
            switch (Crypto.Base64Encode(action)){
                case "dXBkYXRlRG9tYWluc1JlY29yZHM=": //updateDomainsRecords
                    return updateDomainsRecords();
                case "bWFpbGJveEV4dHJhY3Rvcg==": //mailboxExtractor
                    return mailboxExtractor() ;
                case "Y29sbGVjdExlYWRlcnM=": //collectLeaders
                    return collectLeaders() ;
            }
        }catch (Exception ex){
            new DatabaseException("Action not found !");
        }
        return null;
    }
}
