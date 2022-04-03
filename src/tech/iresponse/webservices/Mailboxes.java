package tech.iresponse.webservices;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.mailboxes.extractors.MailboxesCreator;
import tech.iresponse.mailboxes.extractors.MailboxesRemover;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.utils.Crypto ;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;
import tech.iresponse.helpers.scripts.ManageServerWebmail;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class Mailboxes implements Controller {

    public Response createMailboxes() throws Exception {
        SSHConnector ssh = null;
        try {
             Application app = Application.checkAndgetInstance();
             if (app == null){
                 throw new DatabaseException("Application not found !");
             }

             String status = String.valueOf(app.getParameters().get("status"));
             JSONArray domainsIds = (app.getParameters().has("domains-ids") && app.getParameters().get("domains-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("domains-ids") : new JSONArray();
             if (domainsIds == null || domainsIds.length() == 0){
                 throw new DatabaseException("No domains passed !");
             }

             JSONArray prefixes = (app.getParameters().has("prefixes") && app.getParameters().get("prefixes") instanceof JSONArray) ? app.getParameters().getJSONArray("prefixes") : new JSONArray();
             if (prefixes == null || prefixes.length() == 0){
                 throw new DatabaseException("No prefixes passed !");
             }

             ManagementServer mngmentServ = ManageServerWebmail.getCurrentWebMailServer();
             if (mngmentServ == null || mngmentServ.getEmpty()){
                 throw new DatabaseException("No webmail server found !");
             }

             ssh = Authentification.connectToServer(mngmentServ);
             if (ssh == null || !ssh.isConnected()){
                 throw new DatabaseException("Could not connect to webmail server !");
             }

             int nbthread = (domainsIds.length() > 100) ? 100 : domainsIds.length();
             ExecutorService execService = Executors.newFixedThreadPool(nbthread);

             for (int b = 0; b < domainsIds.length(); b++){
                 execService.submit((Runnable)new MailboxesCreator(ssh, domainsIds.getInt(b), prefixes, status));
             }
             execService.shutdown();

             if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
             }
        } finally {
             if (ssh != null && ssh.isConnected()){
                 ssh.disconnect();
             }
        }
        return new Response("Mailboxes created successfully !", 200);
    }

    public Response removeMailboxes() throws Exception {
        SSHConnector ssh = null;
        try {
             Application app = Application.checkAndgetInstance();
             if (app == null){
                 throw new DatabaseException("Application not found !");
             }

             JSONArray mailboxesIds = (app.getParameters().has("mailboxes-ids") && app.getParameters().get("mailboxes-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("mailboxes-ids") : new JSONArray();
             if (mailboxesIds == null || mailboxesIds.length() == 0){
                throw new DatabaseException("No mailboxes passed !");
             }

             ManagementServer mngmentServ = ManageServerWebmail.getCurrentWebMailServer();
             if (mngmentServ == null || mngmentServ.getEmpty()){
                throw new DatabaseException("No webmail server found !");
             }

             ssh = Authentification.connectToServer(mngmentServ);
             if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to webmail server !");
             }

             int nbthred = (mailboxesIds.length() > 100) ? 100 : mailboxesIds.length();
             ExecutorService execService = Executors.newFixedThreadPool(nbthred);

             for (int b = 0; b < mailboxesIds.length(); b++){
                execService.submit((Runnable)new MailboxesRemover(ssh, TypesParser.safeParseInt(mailboxesIds.get(b))));
             }
             execService.shutdown();

             if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                execService.shutdownNow();
             }
        } finally {
             if (ssh != null && ssh.isConnected()){
                 ssh.disconnect();
             }
        }
        return new Response("Mailboxes removed successfully !", 200);
    }

    public Response controller(String action) {
        try {
            switch (Crypto.Base64Encode(action)){
                case "Y3JlYXRlTWFpbGJveGVz": //createMailboxes
                    return createMailboxes();
                case "cmVtb3ZlTWFpbGJveGVz": //removeMailboxes
                    return removeMailboxes() ;
            }
        }catch (Exception ex){
            new DatabaseException("Action not found !");
        }
        return null;
    }

}
