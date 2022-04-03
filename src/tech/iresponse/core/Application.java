package tech.iresponse.core;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;
import tech.iresponse.webservices.*;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.models.admin.User;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Response;

public class Application {

    private int processsId;
    private int systemDatabaseProcessId;
    private int clientsDatabaseProcessId;
    private User user;
    private JSONObject settings;
    private String endpoint;
    private String action;
    private JSONObject parameters;
    public static final String key = "qjnjP//FpUstbkdhPtQfVwZ9mWS/Gl8FNwrVLCM4FqYK1FNfO51vaRn3Y+/WDeaeeg08sBDrEmpQaZRvIzQ82hMLWFeZdD47wULYPyQ2q4M=";
    public static volatile List list = new ArrayList();

    public void ini() throws Exception {
        this.processsId = TypesParser.safeParseInt(ManagementFactory.getRuntimeMXBean().getName().split(Pattern.quote("@"))[0]);
        System.setProperty("backend.path", (new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getAbsolutePath());
        System.setProperty("frontend.path", (new File(System.getProperty("backend.path"))).getParentFile().getParentFile().getAbsolutePath());
        System.setProperty("app.path", System.getProperty("frontend.path") + File.separator + "app");
        System.setProperty("storage.path", System.getProperty("frontend.path") + File.separator + "storage");
        System.setProperty("assets.path", System.getProperty("frontend.path") + File.separator + "assets");
        System.setProperty("public.path", System.getProperty("frontend.path") + File.separator + "public");
        System.setProperty("media.path", System.getProperty("public.path") + File.separator + "media");
        System.setProperty("configs.path", System.getProperty("frontend.path") + File.separator + "config");
        System.setProperty("logs.path", System.getProperty("storage.path") + File.separator + "logs");
        System.setProperty("trash.path", System.getProperty("storage.path") + File.separator + "trash");
        System.setProperty("webdrivers.path", (new File(System.getProperty("backend.path"))).getParentFile().getAbsolutePath() + File.separator + "webdrivers");
        System.setProperty("datasources.path", new File(System.getProperty("frontend.path")) + File.separator + "datasources");
        File file = new File(System.getProperty("configs.path") + File.separator + "application.json");
        if (file.exists()) {
            String str = FileUtils.readFileToString(file, "UTF-8");
            this.settings = (new JSONObject(str)).getJSONObject("application");
        } else {
            this.settings = new JSONObject();
        }
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        System.setProperty("org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY", "error");
        System.setProperty("org.slf4j.simpleLogger.logFile", "/dev/null");
        PropertyConfigurator.configure(Application.class.getResource("/tech/iresponse/props/log4j.properties"));
    }

    public void checkEndpoint(String endpoint) throws Exception {
        JSONObject jsonObj = new JSONObject(Crypto.Base64Decode(endpoint));
        if (jsonObj.length() > 0 && jsonObj.has("endpoint")) {
            this.user = new User(Integer.valueOf(jsonObj.getInt("user-id")));
            if (this.user.getEmpty()){
                this.user = (User)User.first(User.class, "master_access = ?", new Object[] { "Enabled" });
            }
            this.endpoint = jsonObj.getString("endpoint");
            this.action = jsonObj.getString("action");
            this.parameters = (jsonObj.has("parameters") && jsonObj.get("parameters") instanceof JSONObject) ? jsonObj.getJSONObject("parameters") : new JSONObject();
        } else {
            throw new tech.iresponse.exceptions.DatabaseException("Endpoint not found !");
        }
    }

    public void getInstanceApplication() {
        tech.iresponse.registry.Packager.getInstance().setRegistry("application", this);
    }

    public Response controller() throws Exception {
        switch (Crypto.Base64Encode(this.endpoint)) {
            case "QWZmaWxpYXRl": {
                return (new Affiliate()).controller(this.action);
            }
            case "UHJvZHVjdGlvbg==": {
                return (new Production()).controller(this.action);
            }
            case "TXRhUHJvY2Vzc2Vz": {
                return (new MtaProcesses()).controller(this.action);
            }
            case "U2VydmVycw==": {
                return (new Servers()).controller(this.action);
            }
            case "RGF0YUxpc3Rz": {
                return (new DataLists()).controller(this.action);
            }
            case "VG9vbHM=": {
                return (new Tools()).controller(this.action);
            }
            case "U210cFByb2Nlc3Nlcw==": {
                return (new SmtpProcesses()).controller(this.action);
            }
            case "TWFpbGJveGVz": {
                return (new Mailboxes()).controller(this.action);
            }
            case "QXV0b1Jlc3BvbmRlcnM=": {
                return (new AutoResponders()).controller(this.action);
            }
            case "UG10YQ==": {
                return (new Pmta()).controller(this.action);
            }
            case "QW1hem9u": {
                return (new Amazon()).controller(this.action);
            }
            case "RGlnaXRhbE9jZWFu": {
                return (new DigitalOcean()).controller(this.action);
            }
            case "TGlub2Rl": {
                return (new Linode()).controller(this.action);
            }
            case "SGV0em5lcg==": {
                return (new Hetzner()).controller(this.action);
            }
            case "QXRsYW50aWM=": {
                return (new Atlantic()).controller(this.action);
            }
            case "U2NhbGV3YXk=": {
                return (new Scaleway()).controller(this.action);
            }
            case "VnVsdHI=": {
                return (new Vultr()).controller(this.action);
            }
        }
        throw new DatabaseException("Endpoint not found !");
    }

    public static Application checkAndgetInstance() {
        return tech.iresponse.registry.Packager.getInstance().checkInstance("application") ? (Application)tech.iresponse.registry.Packager.getInstance().getRegistry("application") : null;
    }

    public static boolean checkBoolInstance() {
        return (tech.iresponse.registry.Packager.getInstance().checkInstance("application") && tech.iresponse.registry.Packager.getInstance().getRegistry("application") instanceof Application);
    }

    public static Object getSettingsParam(String paramString) {
        if (checkBoolInstance()){
            try {
                return (checkAndgetInstance()).settings.has(paramString) ? (checkAndgetInstance()).settings.get(paramString) : null;
            } catch (JSONException jSONException) {}
        }
        return null;
    }

    public static String checkUser() {
        return ((checkAndgetInstance()).user == null || (checkAndgetInstance()).user.getEmpty()) ? "Unknown User" : (checkAndgetInstance()).user.email;
    }

    public static void deleteFille() {
        if (!list.isEmpty()){
            list.forEach(paramFile -> {
                try {
                    FileDeleteStrategy.FORCE.delete((File) paramFile);
                } catch (IOException iOException) {
                    tech.iresponse.logging.Loggers.error(iOException);
                }
            });
        }
        System.exit(0);
    }

    public static synchronized void add(File paramFile) {
        list.add(paramFile);
    }

    public int getProcesssId() {
        return this.processsId;
    }

    public void setProcesssId(int processsId) {
        this.processsId = processsId;
    }

    public int getSystemDatabaseProcessId() {
        return systemDatabaseProcessId;
    }

    public void setSystemDatabaseProcessId(int systemDatabaseProcessId) {
        this.systemDatabaseProcessId = systemDatabaseProcessId;
    }

    public int getClientsDatabaseProcessId() {
        return clientsDatabaseProcessId;
    }

    public void setClientsDatabaseProcessId(int clientsDatabaseProcessId) {
        this.clientsDatabaseProcessId = clientsDatabaseProcessId;
    }

    public User getUser() {
        return this.user;
    }

    public JSONObject getSettings() {
        return this.settings;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getAction() {
        return this.action;
    }

    public JSONObject getParameters() {
        return this.parameters;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSettings(JSONObject settings) {
        this.settings = settings;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setParameters(JSONObject parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Application))
            return false;
        Application do1 = (Application)paramObject;
        if (!do1.checkbool(this))
            return false;
        if (getProcesssId() != do1.getProcesssId())
            return false;
        if (getSystemDatabaseProcessId() != do1.getSystemDatabaseProcessId())
            return false;
        if (getClientsDatabaseProcessId() != do1.getClientsDatabaseProcessId())
            return false;
        User default1 = getUser();
        User default2 = do1.getUser();
        if ((default1 == null) ? (default2 != null) : !default1.equals(default2))
            return false;
        JSONObject jSONObject1 = getSettings();
        JSONObject jSONObject2 = do1.getSettings();
        if ((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2))
            return false;
        String str1 = getEndpoint();
        String str2 = do1.getEndpoint();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getAction();
        String str4 = do1.getAction();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        JSONObject jSONObject3 = getParameters();
        JSONObject jSONObject4 = do1.getParameters();
        return !((jSONObject3 == null) ? (jSONObject4 != null) : !jSONObject3.equals(jSONObject4));
    }

    protected boolean checkbool(Object paramObject) {
        return paramObject instanceof Application;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getProcesssId();
        n = n * 59 + getSystemDatabaseProcessId();
        n = n * 59 + getClientsDatabaseProcessId();
        User default1 = getUser();
        n = n * 59 + ((default1 == null) ? 43 : default1.hashCode());
        JSONObject jSONObject1 = getSettings();
        n = n * 59 + ((jSONObject1 == null) ? 43 : jSONObject1.hashCode());
        String str1 = getEndpoint();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getAction();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        JSONObject jSONObject2 = getParameters();
        return n * 59 + ((jSONObject2 == null) ? 43 : jSONObject2.hashCode());
    }

    @Override
    public String toString() {
        return "Application(processId=" + getProcesssId() + ", systemDatabaseProcessId=" + getSystemDatabaseProcessId() + ", clientsDatabaseProcessId=" + getClientsDatabaseProcessId() + ", user=" + getUser() + ", settings=" + getSettings() + ", endpoint=" + getEndpoint() + ", action=" + getAction() + ", parameters=" + getParameters() + ")";
    }
}
