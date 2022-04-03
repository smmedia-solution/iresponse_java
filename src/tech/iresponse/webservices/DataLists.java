package tech.iresponse.webservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import tech.iresponse.models.admin.Isp;
import tech.iresponse.models.lists.BlackList;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.lists.BlackListEmail;
import tech.iresponse.models.lists.DataProvider;
import tech.iresponse.models.lists.Email;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.Matcher;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.DatesUtils;
import tech.iresponse.data.scripts.BlackListManager;
import tech.iresponse.data.scripts.DataDuplicateFilter;
import tech.iresponse.data.update.UpdateData;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class DataLists implements Controller {

    public static volatile List listEmail = new ArrayList();
    public static volatile int count = 1;

    public Response createLists() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        String name = app.getParameters().getString("name");
        String table = app.getParameters().getString("table");
        String fileType = app.getParameters().getString("file-type");
        String countryCode = app.getParameters().getString("country-code").toUpperCase();
        String emailsType = app.getParameters().getString("emails-type");
        String verticalsIds = app.getParameters().getString("verticals-ids");
        int maxPerList = app.getParameters().getInt("max-per-list");
        boolean allowDuplicates = "enabled".equalsIgnoreCase(app.getParameters().getString("allow-duplicates"));
        boolean encryptEmails = "enabled".equalsIgnoreCase(app.getParameters().getString("encrypt-emails"));
        boolean filterData = "enabled".equalsIgnoreCase(app.getParameters().getString("filter-data"));

        String pathTmpDir = System.getProperty("trash.path") + File.separator + app.getParameters().getString("tmp-dir");
        String fileName = app.getParameters().getString("file-name");

        int sizeNewList = 0;
        int sizeOldList = 0;

        DataProvider dtProvider = new DataProvider(app.getParameters().getInt("data-provider-id"));
        DataList oldList = new DataList(app.getParameters().getInt("old-list-id"));

        boolean oldListExist = (!oldList.getEmpty()) ? true : false;

        Isp isp = new Isp(app.getParameters().getInt("isp-id"));
        Application.add(new File(pathTmpDir));

        ArrayList<String> newList = "email-by-line".equalsIgnoreCase(fileType) ? new ArrayList(Arrays.asList((Object[])FileUtils.readFileToString(new File(pathTmpDir + File.separator + fileName), "UTF-8").replaceAll("\"", "").replaceAll("'", "").replaceAll(";", "").replaceAll(" ", "").toLowerCase().split("\n"))) : new ArrayList(Arrays.asList((Object[])FileUtils.readFileToString(new File(pathTmpDir + File.separator + fileName), "UTF-8").replaceAll("\"", "").replaceAll("'", "").replaceAll(" ", "").toLowerCase().split("\n")));
        Collections.sort(newList);

        if (!allowDuplicates) {
            HashSet<String> hashSet = new HashSet<>(newList);
            newList.clear();
            newList.addAll(hashSet);
            hashSet = null;
        }

        sizeNewList = newList.size();

        if (filterData) {
            DataLists.listEmail.addAll(new HashSet<>(newList));
            if (DataLists.listEmail.isEmpty()){
                throw new DatabaseException("No emails found !");
            }

            for (int b1 = 0; b1 < DataLists.listEmail.size(); b1++) {
                DataLists.listEmail.set(b1, ((String)DataLists.listEmail.get(b1)).replace("\n", "").replace("\r", ""));
            }

            List<DataList> listData = (List)DataList.all(DataList.class, "isp_id = ? AND status = ?", new Object[] { isp.id, "Activated" });
            if (listData == null){
                listData = new ArrayList();
            }

            List<String> listSpecial = Database.get("clients").availableTables("specials");
            if (listSpecial != null && !listSpecial.isEmpty()){
                for (String tbl : listSpecial) {
                    DataList dtlst = new DataList();
                    dtlst.setId(TypesParser.safeParseInt(Strings.getSaltString(5, false, false, true, false)));
                    dtlst.setTableName(tbl);
                    dtlst.setTableSchema("specials");
                    dtlst.setEncryptEmails("md5");
                    listData.add(dtlst);
                }
            }

            if (!listData.isEmpty()) {
                ExecutorService execService = Executors.newFixedThreadPool((listData.size() > 100) ? 100 : listData.size());
                listData.parallelStream().forEachOrdered(dtlist -> execService.submit((Runnable)new DataDuplicateFilter(dtlist)));
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }

            if (DataLists.listEmail.isEmpty()){
                throw new DatabaseException("All emails are either duplicates or bounced or blacklists !");
            }

            newList.clear();
            newList.addAll(DataLists.listEmail);
            sizeOldList = newList.size();
            DataLists.listEmail = null;
        }


        int m = 0;
        int firstCount = 0;
        int idDtList = 0;
        int sizeNewList2 = newList.size();
        int countList = (oldList.getEmpty() && maxPerList > 0) ? maxPerList : sizeNewList2;
        int diviCntList = sizeNewList2 % countList;
        int lineList = Math.round((sizeNewList2 / countList));
        lineList = (diviCntList > 0) ? (lineList + 1) : lineList;

        String ispSchemaNme = isp.schemaName;
        if (oldListExist) {
            idDtList = oldList.id;
            name = oldList.name;
            table = oldList.tableName;
            ispSchemaNme = oldList.tableSchema;
            m = (TypesParser.safeParseInt(table.split("_")[(table.split("_")).length - 1]) > 0) ? TypesParser.safeParseInt(table.split("_")[(table.split("_")).length - 1]) : 0;
            encryptEmails = "enabled".equalsIgnoreCase(oldList.encryptEmails);
            lineList = 1;
        }

        if (!oldListExist && Database.get("clients").existeTable(ispSchemaNme, table)) {
            boolean bool5 = false;
            List<String> list = Database.get("clients").availableTables(ispSchemaNme);
            if (list != null && !list.isEmpty()){
                for (String str : list) {
                    if (table.toLowerCase().equals(str) || str.contains(table.toLowerCase())) {
                        bool5 = true;
                        int i6 = TypesParser.safeParseInt(str.split("_")[(str.split("_")).length - 1]);
                        if (m < i6){
                            m = i6;
                        }
                    }
                }
            }
            if (bool5){
                m++;
            }
        }

        String[] emails = new String[]{};
        String emailMd5 = "";
        String email = "";
        String firstName = "";
        String lastName = "";
        String seeds = "seeds".equals(emailsType) ? "TRUE" : "FALSE";
        String fresh = "fresh".equals(emailsType) ? "TRUE" : "FALSE";
        String clean = "clean".equals(emailsType) ? "TRUE" : "FALSE";
        String openers = "openers".equals(emailsType) ? "TRUE" : "FALSE";
        String clickers = "clickers".equals(emailsType) ? "TRUE" : "FALSE";
        String leads = "leads".equals(emailsType) ? "TRUE" : "FALSE";
        String unsubs = "unsubs".equals(emailsType) ? "TRUE" : "FALSE";
        String country = (new Locale("", countryCode)).getDisplayCountry();
        boolean emailByLine = "email-by-line".equalsIgnoreCase(fileType);

        for (int b = 0; b < lineList; b++) {
            int j = 0;
            String pathNewCsv = pathTmpDir + File.separator + Strings.rndomSalt(15, false) + ".csv";
            String tableList = (m == 0) ? table : (table + "_" + m);
            String nameList = (m == 0) ? name : (name + "_" + m);

            if (!oldListExist) {
                oldList = new DataList();
                oldList.setStatus("Activated");
                oldList.setDataProviderId(dtProvider.id);
                oldList.setDataProviderName(dtProvider.name);
                oldList.setIspId(isp.id);
                oldList.setIspName(isp.name);
                oldList.setName(nameList);
                oldList.setTableName(tableList.toLowerCase());
                oldList.setTableSchema(ispSchemaNme.toLowerCase());
                oldList.setEncryptEmails(encryptEmails ? "Enabled" : "Disabled");
                oldList.setCreatedBby((Application.checkAndgetInstance().getUser()).email);
                oldList.setCreatedDate(new Date(System.currentTimeMillis()));
                oldList.setLastUpdatedBy((Application.checkAndgetInstance().getUser()).email);
                oldList.setLastUpdatedDate(new Date(System.currentTimeMillis()));
                idDtList = oldList.insert();
                oldList.setId(idDtList);
            }

            Email email1 = new Email();
            email1.setSchema(ispSchemaNme);
            email1.setTable(tableList);
            email1.sync();

            int countList2 = oldListExist ? (oldList.totalCount + 1) : 1;

            try(FileWriter fw = new FileWriter(pathNewCsv, true); BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write("id;list_id;email;email_md5;first_name;last_name;verticals;is_seed;is_fresh;is_clean;is_opener;is_clicker;is_leader;is_unsub;is_optout;is_blacklisted;is_hard_bounced;last_action_time;last_action_type;agent;ip;country_code;country;region;city;language;device_type;device_name;os;browser_name;browser_version\n");
                for (int i = firstCount; i < firstCount + countList; i++) {
                    boolean isWriters = false;
                    if (emailByLine) {
                        email = String.valueOf(newList.get(i)).trim();
                        if (Matcher.pat1(email)) {
                            emailMd5 = Convertion.md5(email);
                            firstName = encryptEmails ? Convertion.crypt(email.split(Pattern.quote("@"))[0].trim()) : email.split(Pattern.quote("@"))[0].trim();
                            lastName = firstName;
                            if (encryptEmails){
                                email = Convertion.crypt(email);
                            }
                            isWriters = true;
                        }
                    } else {
                        emails = String.valueOf(newList.get(i)).trim().split(Pattern.quote(";"));
                        if (emails.length == 3) {
                            email = emails[0].trim();
                            if (Matcher.pat1(email)) {
                                emailMd5 = Convertion.md5(email);
                                firstName = encryptEmails ? Convertion.crypt(emails[1].trim()) : emails[1].trim();
                                lastName = encryptEmails ? Convertion.crypt(emails[2].trim()) : emails[2].trim();
                                if (encryptEmails){
                                    email = Convertion.crypt(email);
                                }
                                isWriters = true;
                            }
                        }
                    }

                    if (isWriters) {
                        bw.write(countList2 + ";" + idDtList + ";" + email + ";" + emailMd5 + ";" + firstName + ";" + lastName + ";" + verticalsIds + ";" + seeds + ";" + fresh + ";" + clean + ";" + openers + ";" + clickers + ";" + leads + ";" + unsubs + ";FALSE;FALSE;FALSE;;;;;" + countryCode + ";" + country + ";;;;;;;;\n");
                        countList2++;
                        j++;
                    }

                    if (i == sizeNewList2 - 1){
                        break;
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
            }

            Database.get("clients").executeUpdate("COPY " + ispSchemaNme + "." + tableList + " FROM '" + pathNewCsv + "' WITH CSV HEADER DELIMITER AS ';' NULL AS ''", null, 0);
            oldList.setTotalCount(oldList.getTotalCount() + j);
            oldList.update();
            firstCount += countList;
            m++;
        }

        String response = "Emails inserted successfully !";
        if (filterData && sizeOldList != sizeNewList){
            response = response + " ( Duplicated Feilterd emails count is " + (sizeNewList - sizeOldList) + " ) ";
        }
        return new Response(response, 200);
    }

    public Response manageBlacklists() throws Exception {
        BlackList blckList = null;
        try {
            Application app = Application.checkAndgetInstance();
            if (app == null){
                throw new DatabaseException("Application not found !");
            }

            blckList = new BlackList(Integer.valueOf(app.getParameters().getInt("id")));
            if (blckList.getEmpty()){
                throw new DatabaseException("Process not found !");
            }

            blckList.processId = Application.checkAndgetInstance().getProcesssId();
            blckList.startTime = new Timestamp(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            blckList.progress = "0%";
            blckList.emailsFound = 0;
            blckList.update();

            String fileName = app.getParameters().getString("file-name");
            String tmpDir = app.getParameters().getString("tmp-dir");

            Application.add(new File(System.getProperty("trash.path") + File.separator + tmpDir));

            List<String> listBlck = FileUtils.readLines(new File(System.getProperty("trash.path") + File.separator + tmpDir + File.separator + fileName), "utf-8");
            Collections.sort(listBlck);

            boolean hasMd5 = false;
            if (listBlck.isEmpty()){
                throw new DatabaseException("No emails found !");
            }

            for (int b1 = 0; b1 < 5; b1++) {
                if (((String)listBlck.get(0)).length() == 32 && !((String)listBlck.get(0)).contains("@")) {
                    hasMd5 = true;
                    break;
                }
            }

            Set<String> setBlckList = Collections.unmodifiableSet(new HashSet(listBlck));
            listBlck.clear();

            int  id = 1;
            String tblBlackList = "blacklists_" + DatesUtils.format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime(), "yyyy_MM_dd") + "_" + Strings.rndomSalt(5, false);
            String newTblRndom = System.getProperty("trash.path") + File.separator + tmpDir + File.separator + Strings.rndomSalt(15, false) + ".txt";

            BlackListEmail blckListEmail = new BlackListEmail();
            blckListEmail.setTable(tblBlackList);
            blckListEmail.sync();

            try(FileWriter fw = new FileWriter(newTblRndom, true); BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write("id;email_md5\n");
                for (String lineEmail : setBlckList) {
                    if (hasMd5) {
                        bw.write(id + ";" + lineEmail.trim() + "\n");
                    } else {
                        bw.write(id + ";" + Convertion.md5(lineEmail.trim().toLowerCase()) + "\n");
                    }
                    id++;
                }
            } catch (Exception exception) {
                throw new DatabaseException(exception);
            }

            Database.get("clients").executeUpdate("COPY " + blckListEmail.getSchema() + "." + blckListEmail.getTable() + " FROM '" + newTblRndom + "' WITH CSV HEADER DELIMITER AS ';' NULL AS ''", null, 0);

            List<DataList> dtList = (List)DataList.all(DataList.class, "status = ?", new Object[] { "Activated" });
            if (dtList == null || dtList.isEmpty()){
                throw new DatabaseException("Data lists not found !");
            }

            int sizeDtList = dtList.size();
            ExecutorService execService = Executors.newFixedThreadPool((sizeDtList > 100) ? 100 : sizeDtList);

            for (DataList dtlst : dtList){
                execService.submit((Runnable)new BlackListManager(dtlst, blckList, blckListEmail, sizeDtList));
            }
            execService.shutdown();

            if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
            }
        } catch (Exception ex) {
            if (blckList != null){
                UpdateData.finishUpdate(blckList, true);
            }
            throw ex;
        } finally {
            if (blckList != null){
                UpdateData.finishUpdate(blckList, false);
            }
        }
        return new Response("Process completed successfully !", 200);
    }

    public static synchronized int getCount() {
        return count;
    }

    public static synchronized void updateCount() {
        count++;
    }

    public static synchronized void add(String list) {
        DataLists.listEmail.add(list);
    }

    public static synchronized void filtersMd5(Set<?> paramSet, boolean encryptEmails) throws NoSuchAlgorithmException {
        if (!DataLists.listEmail.isEmpty()) {
            boolean bool = ((String)DataLists.listEmail.get(0)).contains(";");

            if (encryptEmails) {
                ArrayList<String> arrayList = new ArrayList();

                if (bool) {
                    for (int b1 = 0; b1 < DataLists.listEmail.size(); b1++){
                        arrayList.add(b1, Convertion.md5(((String)DataLists.listEmail.get(b1)).split(Pattern.quote(";"))[0]));
                    }
                } else {
                    for (int b1 = 0; b1 < DataLists.listEmail.size(); b1++){
                        arrayList.add(b1, Convertion.md5((String)DataLists.listEmail.get(b1)));
                    }
                }

                arrayList.retainAll(paramSet);
                for (int b = 0; b < arrayList.size(); b++){
                    DataLists.listEmail.remove(b);
                }
                arrayList.clear();

            } else if (bool) {
                ArrayList<String> arrayList = new ArrayList();
                for (int i = 0; i < DataLists.listEmail.size(); i++){
                    arrayList.add(i, ((String)DataLists.listEmail.get(i)).split(Pattern.quote(";"))[0].toLowerCase());
                }
                arrayList.retainAll(paramSet);
                for (int i = 0; i < arrayList.size(); i++){
                    DataLists.listEmail.remove(i);
                }
                arrayList.clear();
            } else {
                DataLists.listEmail.removeAll(paramSet);
            }
        }
    }

    public Response controller(String action) throws Exception {
        switch (Crypto.Base64Encode(action)) {
            case "Y3JlYXRlTGlzdHM=": {
                return createLists();
            }
            case "bWFuYWdlQmxhY2tsaXN0cw==": {
                return manageBlacklists();
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
