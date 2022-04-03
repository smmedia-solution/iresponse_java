package tech.iresponse.webservices;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.interfaces.Controller;
import tech.iresponse.production.workers.AutoResponderExecuter;
import tech.iresponse.utils.Crypto;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class AutoResponders implements Controller {

    public Response proceed() throws Exception {
        Application app = Application.checkAndgetInstance();
        if (app == null) {
            throw new DatabaseException("Application not found !");
        }

        JSONArray autoRespondersIds = (app.getParameters().has("auto-responders-ids") && app.getParameters().get("auto-responders-ids") instanceof JSONArray) ? app.getParameters().getJSONArray("auto-responders-ids") : new JSONArray();
        if (autoRespondersIds.length() == 0){
            throw new DatabaseException("No auto responders passed to this process !");
        }

        int originalProcessId = app.getParameters().getInt("original-process-id");
        if (originalProcessId == 0){
            throw new DatabaseException("No process passed to this process !");
        }

        String originalProcessType = app.getParameters().getString("original-process-type");
        if ("".equals(originalProcessType)){
            throw new DatabaseException("No process type passed to this process !");
        }

        DataList dtList = new DataList(Integer.valueOf(app.getParameters().getInt("list-id")));
        if (dtList.getEmpty()){
            throw new DatabaseException("No list passed to this process !");
        }

        int clientId = app.getParameters().getInt("client-id");
        if (clientId == 0){
            throw new DatabaseException("No client passed to this process !");
        }

        int nbthread = (autoRespondersIds.length() > 100) ? 100 : autoRespondersIds.length();
        ExecutorService execService = Executors.newFixedThreadPool(nbthread);

        for (int b = 0; b < autoRespondersIds.length(); b++){
            execService.submit((Runnable)new AutoResponderExecuter(autoRespondersIds.getInt(b), dtList, clientId, originalProcessId, originalProcessType));
        }
        execService.shutdown();

        if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
            execService.shutdownNow();
        }
        return new Response("Process completed successfully !", 200);
    }

    public Response controller(String action) {
        try {
            switch (Crypto.Base64Encode(action)){
                case "cHJvY2VlZA==":  //proceed
                    return proceed();
            }
        }catch (Exception ex){
            new DatabaseException("Action not found !");
        }
        return null;
    }
}
