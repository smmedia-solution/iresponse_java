package tech.iresponse.helpers.scripts;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;
import tech.iresponse.logging.Loggers;

public class ManageServerWebmail {

    public static ManagementServer getCurrentWebMailServer() throws Exception {
        ManagementServer manageserver = null;
        try {
            manageserver = new ManagementServer(Integer.valueOf(TypesParser.safeParseInt(Application.getSettingsParam("webmail_server_id"))));
            manageserver = manageserver.getEmpty() ? null : manageserver;
        }
        catch (DatabaseException e){
            Loggers.error(e);
        }
        return manageserver;

    }
}