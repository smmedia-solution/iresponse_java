package tech.iresponse.orm;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;

public class Database {

    private static final Map PAKAGER = new HashMap<>();

    public static void init() throws Exception {
        List list = (List)FileUtils.listFilesAndDirs(new File(System.getProperty("datasources.path")), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        if (!list.isEmpty()){
            Iterator file = list.iterator();
            while(file.hasNext()) {
                File file2 = (File)file.next();
                if(!file2.isDirectory() && file2.getAbsolutePath().endsWith("json")) {
                    JSONObject jSONObject = new JSONObject(FileUtils.readFileToString(file2, "utf-8"));
                    if(jSONObject.has("key")) {
                        Connector connector = new Connector();
                        connector.setDriver(jSONObject.getString("driver"));
                        connector.setKey(jSONObject.getString("key"));
                        connector.setHost(jSONObject.getString("host"));
                        connector.setPort(Integer.parseInt(jSONObject.getString("port")));
                        connector.setUsername(jSONObject.getString("username"));
                        connector.setPassword(jSONObject.getString("password"));
                        connector.setDatabaseName(jSONObject.getString("database"));
                        connector.iniDataSource();
                        Database.PAKAGER.put(connector.getKey(), connector);
                    }

                }

            }
        }
    }

    public static Connector get(String paramString) {
        return (Database.PAKAGER.containsKey(paramString) && Database.PAKAGER.get(paramString) != null) ? (Connector)Database.PAKAGER.get(paramString) : null;
    }

    public static String getBackendPid(String paramString) {
        if (Database.PAKAGER.containsKey(paramString) && Database.PAKAGER.get(paramString) != null){
            try {
                List<String> list = ((Connector)Database.PAKAGER.get(paramString)).availableTables("select pg_backend_pid();", null, 1, "pg_backend_pid");
                if (list != null && !list.isEmpty())
                    return list.get(0);
            } catch (Exception exception) {
                Loggers.error(exception);
            }
        }
        return null;
    }
}