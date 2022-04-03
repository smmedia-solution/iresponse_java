package tech.iresponse.orm;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import tech.iresponse.exceptions.DatabaseException;

public class Connector {

    private String driver;
    private String key;
    private String databaseName;
    private String host;
    private int port;
    private String username;
    private String password;
    private DataSource dataSource;
    public static final int FETCH_FIRST = 0;
    public static final int FETCH_ALL = 1;
    public static final int FETCH_LAST = 3;
    public static final int AFFECTED_ROWS = 0;
    public static final int LAST_INSERTED_ID = 1;
    public static final int BEGIN_TRANSACTION = 0;
    public static final int COMMIT_TRANSACTION = 1;
    public static final int ROLLBACK_TRANSACTION = 2;
    public static final int VAR_INCONNU = 1;

    public List executeQuery(String query, Object[] data, int returnType) throws Exception {
        ArrayList<LinkedHashMap> results = new ArrayList();
        try(Connection conn = this.dataSource.dtsources.getConnection(); PreparedStatement ps = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
            if (data != null && data.length > 0) {
                int index = 1;
                for (Object object : data) {
                    if (object != null) {
                        ps.setObject(index, object, getType(object));
                        index++;
                    }
                }
            }
            ps.setFetchSize(1);
            try (ResultSet result = ps.executeQuery()) {
                if (result.isBeforeFirst()) {
                    ResultSetMetaData meta = result.getMetaData();
                    switch (returnType) {
                        case Connector.FETCH_ALL: //1
                            while (result.next()){
                                results.add(putRow(result, meta));
                            }
                            break;
                        case Connector.FETCH_FIRST:  //0
                            result.first();
                            results.add(putRow(result, meta));
                            break;
                        case Connector.FETCH_LAST: //3
                            result.last();
                            results.add(putRow(result, meta));
                            break;
                    }
                }
            }
        }
        return results;
    }

    public HashSet executeQuery(String query, Object[] data, int returnType, String column) throws Exception {
        HashSet<String> tables = new HashSet();
        try(Connection conn = this.dataSource.dtsources.getConnection(); PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
            if (data != null && data.length > 0) {
                int index = 1;
                for (Object object : data) {
                    if (object != null) {
                        ps.setObject(index, object, getType(object));
                        index++;
                    }
                }
            }
            ps.setFetchSize(1);
            try (ResultSet result = ps.executeQuery()) {
                if (result.isBeforeFirst())
                    switch (returnType) {
                        case Connector.FETCH_ALL: //1
                            while (result.next())
                                tables.add(String.valueOf(result.getObject(column)));
                            break;
                        case Connector.FETCH_FIRST: //0
                            result.first();
                            tables.add(String.valueOf(result.getObject(column)));
                            break;
                        case Connector.FETCH_LAST:  //3
                            result.last();
                            tables.add(String.valueOf(result.getObject(column)));
                            break;
                    }
            }
        }
        return tables;
    }

    public List availableTables(String query, Object[] data, int returnType, String column) throws Exception {
        return new ArrayList(executeQuery(query, data, returnType, column));
    }

    public int executeUpdate(String query, Object[] data, int returnType) throws Exception {
        try(Connection conn = this.dataSource.dtsources.getConnection(); PreparedStatement ps = (returnType == Connector.LAST_INSERTED_ID) ? conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(query)) {
            if (data != null && data.length > 0) {
                int index = 1;
                for (Object object : data) {
                    if (object != null) {
                        ps.setObject(index, object, getType(object));
                        index++;
                    }
                }
            }
            int result = ps.executeUpdate();
            if (returnType == Connector.LAST_INSERTED_ID){
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()){
                        result = rs.getInt(1);
                    }
                }
            }
            return result;
        }
    }

    public boolean existeTable(String nspname, String relname) throws Exception {
        List<String> tables = availableTables("SELECT EXISTS (SELECT 1 FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE  n.nspname = '" + nspname + "' AND c.relname = '" + relname + "' AND c.relkind = 'r')", null, 1, "exists");
        return (tables != null && !tables.isEmpty() && "true".equalsIgnoreCase(tables.get(0)));
    }

    public List availableTables(String schema) throws Exception {
        return availableTables("SELECT relname AS name FROM pg_stat_user_tables WHERE schemaname = '" + schema + "' ORDER BY name ASC", null, 1, "name");
    }

    public Query query() {
        return new Query(this.key);
    }

    public void iniDataSource() throws Exception {
        switch (this.driver) {
            case "pgsql":
                this.dataSource = new DataSource("org.postgresql.Driver", "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.databaseName, this.username, this.password);
                return;
            case "mysql":
                this.dataSource = new DataSource("com.mysql.jdbc.Driver", "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.databaseName, this.username, this.password);
                return;
        }
        throw new DatabaseException("Unsupported database driver !");
    }

    public static int getType(Object data) {
        if (data == null){
            return Types.NULL;
        }
        switch (data.getClass().getName()) {
            case "java.lang.String":
                return Types.VARCHAR; //12;
            case "java.lang.Float":
                return Types.DECIMAL; //3;
            case "java.lang.Integer":
                return Types.INTEGER; //4;
            case "java.sql.Date":
                return Types.DATE; //91;
            case "java.sql.Timestamp":
                return Types.TIMESTAMP; //93;
            case "java.lang.Boolean":
                return Types.BOOLEAN; //16;
        }
        return Types.VARCHAR; //12;
    }

    private LinkedHashMap putRow(ResultSet result, ResultSetMetaData meta) throws Exception {
        LinkedHashMap<Object, Object> row = new LinkedHashMap<>();
        for (int b = 1; b <= meta.getColumnCount(); ++b){
            row.put(meta.getColumnName(b), result.getObject(b));
        }
        return row;
    }

    public String getDriver() {
        return this.driver;
    }

    public String getKey() {
        return this.key;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDriver(String paramString) {
        this.driver = paramString;
    }

    public void setKey(String paramString) {
        this.key = paramString;
    }

    public void setDatabaseName(String paramString) {
        this.databaseName = paramString;
    }

    public void setHost(String paramString) {
        this.host = paramString;
    }

    public void setPort(int paramInt) {
        this.port = paramInt;
    }

    public void setUsername(String paramString) {
        this.username = paramString;
    }

    public void setPassword(String paramString) {
        this.password = paramString;
    }

    public void setDataSource(DataSource paramnew) {
        this.dataSource = paramnew;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Connector))
            return false;
        Connector con1 = (Connector)paramObject;
        if (!con1.checkbool(this))
            return false;
        String str1 = getDriver();
        String str2 = con1.getDriver();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getKey();
        String str4 = con1.getKey();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getDatabaseName();
        String str6 = con1.getDatabaseName();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getHost();
        String str8 = con1.getHost();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        if (getPort() != con1.getPort())
            return false;
        String str9 = getUsername();
        String str10 = con1.getUsername();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getPassword();
        String str12 = con1.getPassword();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        DataSource new1 = getDataSource();
        DataSource new2 = con1.getDataSource();
        return !((new1 == null) ? (new2 != null) : !new1.equals(new2));
    }

    protected boolean checkbool(Object paramObject) {
        return paramObject instanceof Connector;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getDriver();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getKey();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getDatabaseName();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getHost();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getPort();
        String str5 = getUsername();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getPassword();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        DataSource new1 = getDataSource();
        return n * 59 + ((new1 == null) ? 43 : new1.hashCode());
    }

    public String toString() {
        return "Connector(driver=" + getDriver() + ", key=" + getKey() + ", databaseName=" + getDatabaseName() + ", host=" + getHost() + ", port=" + getPort() + ", username=" + getUsername() + ", password=" + getPassword() + ", datasource=" + getDataSource() + ")";
    }
}
