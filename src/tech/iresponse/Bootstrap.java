package tech.iresponse;

import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.MapError;
import tech.iresponse.http.Response;
import tech.iresponse.core.Application;

public class Bootstrap {
    public static void main(String[] params) {
        try {
            if (params.length != 1){
                throw new DatabaseException("No arguments passed to our service !");
            }

            //System.out.println(message);
            //System.exit(0);
            Application app = new Application();
            app.ini();
            Database.init();
            app.setSystemDatabaseProcessId(Integer.parseInt(Database.getBackendPid("system")));
            app.setClientsDatabaseProcessId(Integer.parseInt(Database.getBackendPid("clients")));
            app.checkEndpoint(params[0]);
            app.getInstanceApplication();
            MapError.ini(app.controller());
        } catch (Throwable t) {
            String str = (t instanceof DatabaseException) ? t.getMessage() : "Internal server error !";
            MapError.ini(new Response(str, 500));
            Loggers.error(t);
        } finally {
            Application.deleteFille();
        }
    }
}
