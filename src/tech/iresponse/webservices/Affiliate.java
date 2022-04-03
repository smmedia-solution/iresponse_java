package tech.iresponse.webservices;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.sql.Date;
import java.sql.Timestamp;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.models.affiliate.Suppression;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.admin.User;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.affiliate.AffiliateNetwork;
import tech.iresponse.models.lists.SuppressionEmail;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.Database;
import tech.iresponse.logging.Loggers;
import tech.iresponse.affiliate.workers.OffersInserter;
import tech.iresponse.affiliate.workers.SuppressionUpdater;
import tech.iresponse.affiliate.workers.ConversionsCollector;
import tech.iresponse.affiliate.workers.ClicksCollector;
import tech.iresponse.affiliate.affiliate.UpdateData;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.Files;
import tech.iresponse.utils.Terminal;
import tech.iresponse.http.Response;
import tech.iresponse.http.Agents;
import tech.iresponse.core.Application;
import tech.iresponse.utils.JsonUtils;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.affiliate.AffiliateApi;

public class Affiliate implements Controller {

    public static volatile List<JSONObject> CAMPAIGNS_OFFERS = new ArrayList();
    public static volatile boolean IS_ERROR_OCCURED = false;
    public static volatile Set SuppEmailList = null;
    public static volatile int nbrEmailFound = 1;
    public static volatile int maxSize = 50;

    public Response getOffers() throws Exception  {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        List offersIdsList = (app.getParameters().has("offers-ids") && app.getParameters().get("offers-ids") instanceof JSONArray) ? JsonUtils.jsonArrayToStringArrayList(app.getParameters().getJSONArray("offers-ids")) : new ArrayList();

        AffiliateNetwork affiliate = new AffiliateNetwork(app.getParameters().getInt("affiliate-network-id"));
        if (affiliate.getEmpty()){
            throw new DatabaseException("No affiliate network passed to this process !");
        }

        int maxCreatives = app.getParameters().getInt("max-creatives");

        AffiliateApi affApi = AffiliateApi.controller(affiliate.apiType);
        if (affApi == null){
            throw new DatabaseException("Affiliate network api not found !");
        }

        affApi.setAffiliateNetwork(affiliate);
        affApi.setMaxCreatives(maxCreatives);
        affApi.getOffers(offersIdsList);

        if (CAMPAIGNS_OFFERS.isEmpty()){
            throw new DatabaseException("No offers found !");
        }

        int j = (CAMPAIGNS_OFFERS.size() < maxSize) ? CAMPAIGNS_OFFERS.size() : maxSize;
        ExecutorService execService = Executors.newFixedThreadPool(j);

        CAMPAIGNS_OFFERS.forEach(dataJsn -> execService.submit((Runnable)new OffersInserter(dataJsn, affiliate)));
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Offers imported successfully !", 200);
    }

    public Response startSuppression() {
        Suppression supp = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            supp = new Suppression(Integer.valueOf(app.getParameters().getInt("process-id")));
            if (supp.getEmpty()){
                throw new DatabaseException("No process found !");
            }

            supp.processId = Application.checkAndgetInstance().getProcesssId() + "_" + Application.checkAndgetInstance().getSystemDatabaseProcessId() + "_" + Application.checkAndgetInstance().getClientsDatabaseProcessId();
            supp.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            supp.progress = "0%";
            supp.emailsFound = 0;
            supp.update();

            List<DataList> dataList = (List)DataList.all(DataList.class, "id IN (" + supp.listsIds + ")", null);
            if (dataList == null || dataList.isEmpty()){
                throw new DatabaseException("Lists not found !");
            }
            String trachPatch = System.getProperty("trash.path") + File.separator + Strings.rndomSalt(20, false);
            Application.add(new File(trachPatch));

            Offer offer = new Offer(Integer.valueOf(supp.offerId));
            if (offer.getEmpty()){
                throw new DatabaseException("No offer found for this proccess !");
            }

            AffiliateNetwork affNetwork = new AffiliateNetwork(Integer.valueOf(offer.affiliateNetworkId));
            if (affNetwork.getEmpty()){
                throw new DatabaseException("No affiliate network found for this proccess !");
            }

            String suppLink = offer.defaultSuppressionLink;
            if (Strings.isEmpty(suppLink)) {
                AffiliateApi affApi = AffiliateApi.controller(affNetwork.apiType);
                if (affApi == null){
                    throw new DatabaseException("Affiliate network api not found !");
                }
                affApi.setAffiliateNetwork(affNetwork);
                suppLink = affApi.getSuppressionLink(offer);
            }

            suppLink = suppLink.replaceAll(Pattern.quote(","), "").replaceAll(Pattern.quote(";"), "");
            if (Strings.isEmpty(suppLink)){
                throw new DatabaseException("No suppression link found !");
            }

            if (suppLink.contains("affiliateaccesskey")) {
                String newLnk = Agents.get(new URL(suppLink)).toString();
                if (newLnk != null && newLnk.contains("optizmo")){
                    suppLink = newLnk;
                }
            }

            if (suppLink.contains("optizmo") && !suppLink.contains("s=Download") && !suppLink.contains("getfile")) {

                String lnkSupp = Agents.get(suppLink.trim(), 60).split(Pattern.quote("?"))[1].replaceAll("mak=", "").trim();
                lnkSupp = lnkSupp.contains("&") ? lnkSupp.split(Pattern.quote("&"))[0] : lnkSupp;
                String token = String.valueOf(Application.getSettingsParam("optizmo_token"));

                String responseLnk = Agents.get("https://mailer-api.optizmo.net/accesskey/download/" + lnkSupp + "?token=" + token, null, 600);
                if (responseLnk == null || "".equals(responseLnk)){
                    throw new DatabaseException("No response retreived !");
                }

                JSONObject jSONObject = new JSONObject(responseLnk);
                if (jSONObject.has("result") && "success".equalsIgnoreCase(String.valueOf(jSONObject.get("result")))){
                    suppLink = jSONObject.getString("download_link");
                }
            }

            if (Strings.isEmpty(suppLink)){
                throw new DatabaseException("No suppression link found !");
            }

            if (suppLink.contains("google.com/spreadsheets")) {
                String[] arrayOfString = StringUtils.replace(StringUtils.replace(suppLink, "?", "/"), "https://docs.google.com/spreadsheets/d/", "").split(Pattern.quote("/"));
                if (arrayOfString.length > 0){
                    suppLink = "https://docs.google.com/spreadsheets/d/" + arrayOfString[0] + "/export?gid=0&format=csv&id=" + arrayOfString[0];
                }
            }

            if (suppLink.contains("dropbox.com")){
                suppLink = StringUtils.replace(suppLink, "dl=0", "dl=1");
            }

            File firstRndFile = new File(trachPatch + File.separator + Strings.rndomSalt(20, false));
            if (!firstRndFile.exists()){
                firstRndFile.mkdirs();
            }

            String rndSuppFile = "sup_" + Strings.rndomSalt(15, false);
            String ext = "text";
            File pathSuppFile = new File(firstRndFile.getAbsolutePath() + "/" + rndSuppFile);
            // new update trax
            String rsp = "";
            if (suppLink.contains("partner")) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Accept", "application/json");
                map.put("Authorization", "Bearer  " + (affNetwork.apiKey));
                rsp = Agents.get(suppLink.trim(), null, 60, map, (File)pathSuppFile);
            }else{
                rsp = Agents.get(suppLink.trim(), null, 60, (File)pathSuppFile);
            }

            //String rsp = Agents.get(suppLink.trim(), null, 60, (File)pathSuppFile);
            if (rsp == null){
                throw new DatabaseException("Unsupported content type !");
            }

            if (rsp.toLowerCase().contains("application/zip") || rsp.toLowerCase().contains("application/x-zip-compressed") || rsp.toLowerCase().contains("binary/octet-stream")){
                ext = "zip";
            }

            if (!pathSuppFile.exists()){
                throw new DatabaseException("Suppression file not found !");
            }

            if ("zip".equals(ext) || Files.read(pathSuppFile)) {
                File secondRndFile = new File(firstRndFile.getAbsolutePath() + "/" + Strings.rndomSalt(15, false));
                secondRndFile.mkdir();
                Terminal.executeCommand("unzip -d " + secondRndFile.getAbsolutePath() + " " + pathSuppFile.getAbsolutePath());
                List<File> listFile = Files.listAllFiles(secondRndFile, true);
                for (File file3 : listFile) {
                    if (file3.getName().contains("(") || file3.getName().contains(")")){
                        Terminal.executeCommand("rename '(' _ " + secondRndFile.getAbsolutePath() + "/*.txt");
                        Terminal.executeCommand("rename ')' _ " + secondRndFile.getAbsolutePath() + "/*.txt");
                        Terminal.executeCommand("rename '(' _ " + secondRndFile.getAbsolutePath() + "/*.txt");
                        Terminal.executeCommand("rename ')' _ " + secondRndFile.getAbsolutePath() + "/*.txt");
                    }
                }
                List<File> listFile2 = Files.listAllFiles(secondRndFile, true);
                for (File file3 : listFile2) {
                    if (file3.getName().toLowerCase().contains(".xlsx") || file3.getName().toLowerCase().contains(".xls")){
                        Workbook book = new Workbook(secondRndFile.getAbsolutePath() + "/" + file3.getName());
                        book.save(secondRndFile.getAbsolutePath() + "/" + file3.getName() + ".csv", SaveFormat.AUTO);
                        Terminal.executeCommand("unlink " + secondRndFile.getAbsolutePath() + "/" + file3.getName());
                    }
                }
                List<File> listFile3 = Files.listAllFiles(secondRndFile, true);
                for (File file3 : listFile3) {
                    if (!file3.getName().toLowerCase().contains("domain") && file3.length() > 0L){
                        pathSuppFile = file3;
                    }
                }

                File unikSupp = new File(secondRndFile.getAbsolutePath() + "/" + Strings.rndomSalt(15, false) + "_U.txt");
                Terminal.executeCommand("sort " + pathSuppFile + " | uniq > " + unikSupp);
                pathSuppFile = unikSupp;
            }

            Terminal.executeCommand("sed -i 's/\\r| //g' " + pathSuppFile);
            Terminal.executeCommand("sed -i 's/\\r//g' " + pathSuppFile);

            List<String> lines = FileUtils.readLines(pathSuppFile, "utf-8");
            if (lines == null || lines.isEmpty()) {
                throw new DatabaseException("Suppression file is empty !");
            }
            //Collections.sort(lines);

            boolean isMD5 = false;
            for (int i = 0; i < 5; i++) {
                isMD5 = (((String)lines.get(i)).length() == 32 && !((String)lines.get(i)).contains("@")) ? true : false;
                if (isMD5){
                    break;
                }
            }

            if (!isMD5) {
                boolean hasCsv = (rsp.toLowerCase().contains("application/csv") || rsp.toLowerCase().contains("text/csv")) ? true : false;
                for (int b = 0; b < lines.size(); b++) {
                    String str = ((String)lines.get(b)).trim().toLowerCase();
                    if (hasCsv){
                        if (str.contains(",")) {
                            String[] arrayOfString = str.split(",");
                            if (arrayOfString != null && arrayOfString.length > 0)
                                str = arrayOfString[0].trim();
                        } else if (str.contains(";")) {
                            String[] arrayOfString = str.split(";");
                            if (arrayOfString != null && arrayOfString.length > 0)
                                str = arrayOfString[0].trim();
                        }
                    }
                    str = StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(str.replaceAll("\n", "").replaceAll("\r", ""), "\"", ""), "'", ""), ";", ""), ",", ""), "#", "");
                    if (str == null || "".equals(str)) {
                        lines.set(b, "");
                    } else {
                        lines.set(b, Convertion.md5(str));
                    }
                }
            }

            //lines.removeAll(Collections.singleton(null));
            //lines.removeAll(Collections.singleton(""));
            //SuppEmailList = Collections.unmodifiableSet(new HashSet(lines));
            SuppEmailList = new HashSet(lines);

            if (SuppEmailList.isEmpty()) {
                UpdateData.finishUpdate(supp, IS_ERROR_OCCURED);
            } else {
                int size = dataList.size();
                ExecutorService execService = Executors.newFixedThreadPool((size > 100) ? 100 : size);
                for (DataList dtLst : dataList){
                    execService.submit((Runnable)new SuppressionUpdater(offer, size, supp, dtLst, firstRndFile.getAbsolutePath()));
                }
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }

            offer.lastSuppressionUpdatedDate = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            offer.update();

        } catch (Exception e) {
            IS_ERROR_OCCURED = true;
            Loggers.error(e);
        }
        if (supp != null){
            UpdateData.finishUpdate(supp, IS_ERROR_OCCURED);
        }
        return new Response("Process completed successfully !", 200);
    }

    public Response stopSupressionProcesses() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes found !");
        }

        Suppression supp = null;
        for (int b = 0; b < processesIds.length(); b++) {
            supp = new Suppression(Integer.valueOf(processesIds.getInt(b)));
            if (!supp.getEmpty()) {
                if (!"In Progress".equalsIgnoreCase(supp.status)){
                    throw new DatabaseException("This process with id : " + supp.id + " is not in progress !");
                }
                Terminal.killProcess(supp.processId);
                supp.status = "Interrupted";
                supp.finishTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
                supp.update();
            }
        }
        return new Response("Suppression processes stoped successfully !", 200);
    }

    public Response getClicks() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray affiliateNetworksIds = (app.getParameters().has("affiliate-networks-ids") && app.getParameters().get("affiliate-networks-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("affiliate-networks-ids") : new JSONArray();
        List<AffiliateNetwork> affiliate = new ArrayList<>();

        if (affiliateNetworksIds.length() == 0) {
            affiliate = (List)AffiliateNetwork.all(AffiliateNetwork.class, "status = ?", new Object[] { "Activated" });
        } else {
            String ids = "";
            for (int b = 0; b < affiliateNetworksIds.length(); b++){
                ids = ids + affiliateNetworksIds.getInt(b) + ",";
            }
            ids = ids + (ids.endsWith(",") ? ids.substring(0, ids.length() - 1) : ids);
            affiliate = (List)AffiliateNetwork.all(AffiliateNetwork.class, "status = ? AND id IN (" + ids + ")", new Object[] { "Activated" });
        }
        if (affiliate == null){
            throw new DatabaseException("Affiliate networks not found !");
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

        LinkedHashMap<Integer,String> mailers = new LinkedHashMap<>();
        ((List<User>)User.all(User.class)).forEach(
                usr -> mailers.put(usr.productionId, usr.firstName + " " + usr.lastName)
        );

        int nbThread = (affiliate.size() > 100) ? 100 : affiliate.size();
        ExecutorService execService = Executors.newFixedThreadPool(nbThread);

        for (AffiliateNetwork spnsor : affiliate){
            execService.submit((Runnable)new ClicksCollector(spnsor, startDate, endDate, mailers));
        }
        execService.shutdown();
        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Clicks saved successfully !", 200);
    }

    public Response getConversions() throws Exception {

        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        List<AffiliateNetwork> affiliate = new ArrayList<>();

        JSONArray affNetworkIds = (app.getParameters().has("affiliate-networks-ids") && app.getParameters().get("affiliate-networks-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("affiliate-networks-ids") : new JSONArray();
        if (affNetworkIds.length() == 0) {
            affiliate = (List)AffiliateNetwork.all(AffiliateNetwork.class, "status = ?", new Object[] { "Activated" });
        } else {
            String ids = "";
            for (int i = 0; i < affNetworkIds.length(); i++){
                ids = ids + affNetworkIds.getInt(i) + ",";
            }
            ids = ids + (ids.endsWith(",") ? ids.substring(0, ids.length() - 1) : ids);
            affiliate = (List)AffiliateNetwork.all(AffiliateNetwork.class, "status = ? AND id IN (" + ids + ")", new Object[] { "Activated" });
        }

        if (affiliate == null){
            throw new DatabaseException("Affiliate networks not found !");
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

        LinkedHashMap<Integer,String> mailers = new LinkedHashMap<>();
        ((List<User>)User.all(User.class)).forEach(
                entry -> mailers.put(entry.productionId, entry.firstName + " " + entry.lastName)
        );

        int nbThread = (affiliate.size() > 100) ? 100 : affiliate.size();
        ExecutorService execService = Executors.newFixedThreadPool(nbThread);

        for (AffiliateNetwork spnsor : affiliate){
            execService.submit((Runnable)new ConversionsCollector(spnsor, startDate, endDate, mailers));
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Conversions saved successfully !", 200);
    }

    public static synchronized int getNbrEmailFound() {
        return Affiliate.nbrEmailFound;
    }

    public static synchronized void countEmailFound() {
        Affiliate.nbrEmailFound++;
    }

    public static synchronized void updateOffers(JSONObject offers) {
        Affiliate.CAMPAIGNS_OFFERS.add(offers);
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "Z2V0T2ZmZXJz": {
                return getOffers();
            }
            case "c3RhcnRTdXBwcmVzc2lvbg==": {
                return startSuppression() ;
            }
            case "c3RvcFN1cHJlc3Npb25Qcm9jZXNzZXM=": {
                return stopSupressionProcesses();
            }
            case "Z2V0Q29udmVyc2lvbnM=": {
                return getConversions();
            }
        }
        throw new DatabaseException("Action not found !");

    }
}
