package tech.iresponse.webservices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.orm.Database;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.drops.MtaLinkRouting;
import tech.iresponse.production.workers.ActionExecuter;
import tech.iresponse.production.workers.DataListCounter;
import tech.iresponse.production.workers.MtaDropsUpdater;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.http.ResponseData;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class Production implements Controller {

    public static volatile List listCount = new ArrayList();

    public Response generateLinks() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        String sendType = String.valueOf(app.getParameters().get("send-type"));
        int offerId = TypesParser.safeParseInt(app.getParameters().get("offer-id"));
        String linkType = String.valueOf(app.getParameters().get("link-type"));
        String staticDomain = String.valueOf(app.getParameters().get("static-domain"));
        int id = 0;

        if (offerId == 0){
            throw new DatabaseException("Offer id is incorrect !");
        }

        if ("".equals(sendType) || "null".equals(sendType)){
            throw new DatabaseException("Send type is incorrect !");
        }

        if ("".equals(linkType) || "null".equals(linkType)){
            throw new DatabaseException("Link type is incorrect !");
        }

        if ("mta".equalsIgnoreCase(sendType)) {
            ServerVmta servVmta = new ServerVmta(Integer.valueOf(TypesParser.safeParseInt(String.valueOf(app.getParameters().get("vmta-id")).split(Pattern.quote("|"))[1])));
            if (servVmta.getEmpty()){
                throw new DatabaseException("Vmta not found !");
            }
            if ("".equals(staticDomain) || "null".equals(staticDomain) || "[domain]".equals(staticDomain)){
                staticDomain = servVmta.domain;
            }
            id = servVmta.id;
            sendType = "mt";
        } else if ("smtp".equalsIgnoreCase(sendType)) {
            SmtpUser smtpUsr = new SmtpUser(Integer.valueOf(TypesParser.safeParseInt(String.valueOf(app.getParameters().get("smtp-user-id")).split(Pattern.quote("|"))[1])));
            if (smtpUsr.getEmpty()){
                throw new DatabaseException("Smtp user not found !");
            }

            if ("".equals(staticDomain) || "null".equals(staticDomain) || "[domain]".equals(staticDomain)) {
                ServerVmta servVmta = (ServerVmta)ServerVmta.last(ServerVmta.class, 3);
                if (servVmta.getEmpty()){
                    throw new DatabaseException("Vmta not found !");
                }
                staticDomain = servVmta.domain;
            }
            id = smtpUsr.id;
            sendType = "st";
        }

        if (id == 0){
            throw new DatabaseException("No vmta or smtp user selected !");
        }

        if ("".equals(staticDomain) || "null".equals(staticDomain) || "[domain]".equals(staticDomain)){
            throw new DatabaseException("No vmta or smtp user selected !");
        }

        HashMap<Object, Object> links = new HashMap<>(4);
        links.put("open-link", MtaLinkRouting.createLinkRouting(linkType, "op", 0, sendType, offerId, id, 0, 0));
        links.put("click-link", MtaLinkRouting.createLinkRouting(linkType, "cl", 0, sendType, offerId, id, 0, 0));
        links.put("unsub-link", MtaLinkRouting.createLinkRouting(linkType, "un", 0, sendType, offerId, id, 0, 0));
        links.put("optout-link", MtaLinkRouting.createLinkRouting(linkType, "oop", 0, sendType, offerId, id, 0, 0));
        staticDomain = staticDomain.contains("http") ? staticDomain : ("http://" + staticDomain);

        switch (linkType) {
            case "routing-bitly":
            case "attr-bitly":
                staticDomain = Bitly.shortBitly(staticDomain) + "#";
                links.put("open-link", staticDomain + (String)links.get("open-link"));
                links.put("click-link", staticDomain + (String)links.get("click-link"));
                links.put("unsub-link", staticDomain + (String)links.get("unsub-link"));
                links.put("optout-link", staticDomain + (String)links.get("optout-link"));
                return (Response)new ResponseData("Links generated successfully !", links, 200);
            case "routing-gcloud":
            case "attr-gcloud":
                staticDomain = Google.shortGoogle(staticDomain) + "#";
                links.put("open-link", staticDomain + (String)links.get("open-link"));
                links.put("click-link", staticDomain + (String)links.get("click-link"));
                links.put("unsub-link", staticDomain + (String)links.get("unsub-link"));
                links.put("optout-link", staticDomain + (String)links.get("optout-link"));
                return (Response)new ResponseData("Links generated successfully !", links, 200);
            case "routing-tinyurl":
            case "attr-tinyurl":
                staticDomain = Tinyurl.shortTinyurl(staticDomain) + "#";
                links.put("open-link", staticDomain + (String)links.get("open-link"));
                links.put("click-link", staticDomain + (String)links.get("click-link"));
                links.put("unsub-link", staticDomain + (String)links.get("unsub-link"));
                links.put("optout-link", staticDomain + (String)links.get("optout-link"));
                return (Response)new ResponseData("Links generated successfully !", links, 200);
        }
        staticDomain = staticDomain + "/";
        links.put("open-link", staticDomain + (String)links.get("open-link"));
        links.put("click-link", staticDomain + (String)links.get("click-link"));
        links.put("unsub-link", staticDomain + (String)links.get("unsub-link"));
        links.put("optout-link", staticDomain + (String)links.get("optout-link"));
        return (Response)new ResponseData("Links generated successfully !", links, 200);
    }

    public Response getEmailsLists() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        int offerId = TypesParser.safeParseInt(app.getParameters().get("offer-id"));

        JSONArray dataListIds = (app.getParameters().has("data-lists-ids") && app.getParameters().get("data-lists-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("data-lists-ids") : new JSONArray();
        if (dataListIds.length() == 0){
            throw new DatabaseException("No data lists passed to this process !");
        }

        String verticalsIds = String.valueOf(app.getParameters().get("verticals-ids"));
        String countries = String.valueOf(app.getParameters().get("countries"));
        JSONObject filters = (app.getParameters().has("filters") && app.getParameters().get("filters") instanceof JSONObject) ? app.getParameters().getJSONObject("filters") : new JSONObject();

        ExecutorService execServ = Executors.newFixedThreadPool((dataListIds.length() > 500) ? 500 : dataListIds.length());

        for (int i = 0; i < dataListIds.length(); i++) {
            execServ.submit((Runnable)new DataListCounter(dataListIds.getInt(i), offerId, verticalsIds, countries, filters));
        }

        execServ.shutdown();

        if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
            execServ.shutdownNow();
        }

        if (Production.listCount.isEmpty()){
            throw new DatabaseException("No data lists found !");
        }

        return (Response)new ResponseData("Emails lists collected successfully !", Production.listCount, 200);
    }

    public Response executeProcessAction() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray processesIds = (app.getParameters().has("processes-ids") && app.getParameters().get("processes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("processes-ids") : new JSONArray();
        if (processesIds.length() == 0){
            throw new DatabaseException("No processes found !");
        }

        String action = String.valueOf(app.getParameters().get("action"));
        if ("".equals(action)){
            throw new DatabaseException("No action found !");
        }

        String type = String.valueOf(app.getParameters().get("type"));
        if ("".equals(type)){
            throw new DatabaseException("No type found !");
        }

        ExecutorService execServ = Executors.newFixedThreadPool((processesIds.length() > 100) ? 100 : processesIds.length());
        for (int i = 0; i < processesIds.length(); i++){
            execServ.submit((Runnable)new ActionExecuter(processesIds.getInt(i), action, type));
        }

        execServ.shutdown();

        if (!execServ.awaitTermination(1L, TimeUnit.DAYS)){
            execServ.shutdownNow();
        }
        return new Response("Actions executed successfully !", 200);
    }

    public Response uploadImages() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null){
            throw new DatabaseException("Application not found !");
        }

        JSONArray imagesPaths = (app.getParameters().has("images-paths") && app.getParameters().get("images-paths") instanceof JSONArray) ? app.getParameters().getJSONArray("images-paths") : new JSONArray();
        if (imagesPaths.length() == 0){
            throw new DatabaseException("No images found !");
        }

        ManagementServer mngServ = new ManagementServer(Integer.valueOf(TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id"))));
        if (mngServ.getEmpty()){
            throw new DatabaseException("No upload center found !");
        }

        SSHConnector ssh = null;
        try {
            ssh = Authentification.connectToServer(mngServ);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to our upload center !");
            }

            ssh.cmd("mkdir -p /var/www/html/media/");
            File file = null;
            for (int b = 0; b < imagesPaths.length(); b++) {
                file = new File(imagesPaths.getString(b));
                if (file.exists()){
                    ssh.upload(file.getCanonicalPath(), "/var/www/html/media/" + file.getName());
                }
            }

        } finally {
            if (ssh != null && ssh.isConnected()){
                ssh.disconnect();
            }
        }
        return new Response("Images uploaded succesfully !", 200);
    }

    public static synchronized void addCount(HashMap<String, Object> result) {
        Production.listCount.add(result);
    }

    public Response controller(String action) throws Exception{
        switch (Crypto.Base64Encode(action)) {
            case "Z2VuZXJhdGVMaW5rcw==": {
                return generateLinks();
            }
            case "Z2V0RW1haWxzTGlzdHM=": {
                return getEmailsLists();
            }
            case "ZXhlY3V0ZVByb2Nlc3NBY3Rpb24=": {
                return executeProcessAction();
            }
            case "dXBsb2FkSW1hZ2Vz": {
                return uploadImages();
            }
        }
        throw new DatabaseException("Action not found !");
    }
}
