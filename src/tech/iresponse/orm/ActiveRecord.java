package tech.iresponse.orm;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import tech.iresponse.utils.Inspector;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;

public abstract class ActiveRecord {
    private String database;
    private String schema;
    private String table;
    private LinkedHashMap<String,LinkedHashMap<String,Object>> columns = new LinkedHashMap<>();
    private LinkedHashMap<String,Object> primary = new LinkedHashMap<>();
    private boolean empty = true;
    public static final String INT = "integer";
    public static final String DECIMAL = "decimal";
    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String TIME_STAMP = "timestamp";
    public static final String BOOL = "boolean";
    public static final int CREATE_TABLE = 0;
    public static final int CREATE_CLASS = 1;
    public static final int FETCH_ARRAY = 2;
    public static final int FETCH_OBJECT = 3;
    public static final String key = "uId2ii4igAgdOoiApk81QB3BSjOZOKSeNzpn3YclnpI1Kb8cXrLoI3sMe//OsJZPeFnwp5fyylkja3LQBcICDgnAeNv2s3vAfqDwcTETMWA=";

    public void map(Map row) {
        if (!row.isEmpty()){
            Iterator it = row.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String c = (String)entry.getKey();
                if(this.columns.containsKey(c)) {
                    String str1 = (String)((LinkedHashMap)this.columns.get(c)).get("field");
                    this.setFieldValue(str1, entry.getValue());
                }
            }
        }
    }

    public int insert() throws Exception {
        boolean customPrimary = false;
        int index = 0;
        String[] columnsList = new String[this.columns.size()];
        Object[] data = new Object[this.columns.size()];

        if (!this.primary.isEmpty()) {
            columnsList[index] = (String)this.primary.get("name");
            Object value = this.getFieldValue((String)this.primary.get("field"));
            if (value == null || Integer.parseInt(String.valueOf(value)) == 0) {
                value = 0;
                List<Map> nextVal = Database.get(this.database).executeQuery("SELECT nextval('" + schemaPoint() + "seq_" + String.valueOf(this.primary.get("name")) + "_" + this.table + "')", null, Connector.FETCH_FIRST);
                if (!nextVal.isEmpty() && ((Map)nextVal.get(0)).containsKey("nextval")){
                    value = Integer.valueOf(Integer.parseInt(String.valueOf((nextVal.get(0)).get("nextval"))));
                }
                data[index] = value;
            } else {
                customPrimary = true;
                data[index] = value;
            }
            index++;
        }
        Iterator it = this.columns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String columnName = (String)pair.getKey();
            LinkedHashMap column = (LinkedHashMap)pair.getValue();
            if (false == ((Boolean)column.get("primary")).booleanValue()) {
                columnsList[index] = columnName;
                data[index] = this.getFieldValue((String)column.get("field"));
                index++;
            }
            it.remove();
        }

        int id = Database.get(this.database).query().from(schemaPoint() + this.table, columnsList).insert(data);
        if (customPrimary){
            Database.get(this.database).executeUpdate("ALTER SEQUENCE IF EXISTS " + schemaPoint() + "seq_" + String.valueOf(this.primary.get("name")) + "_" + this.table + " RESTART WITH " + (id + 1), null, Connector.AFFECTED_ROWS);
        }

        this.ini();
        this.empty = false;
        return id;
    }

    public int update(){
        int affectedRows = 0;
        try {
            String primaryColumn = (String)this.primary.get("name");
            Object primaryValue = this.getFieldValue((String)this.primary.get("field"));
            if (primaryValue == null || Integer.parseInt(String.valueOf(primaryValue)) == 0){
                throw new DatabaseException("Primary key must not be null !");
            }
            String[] columns = new String[]{};
            Object[] data = new Object[]{};
            Iterator it = this.columns.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                String columnName = (String)pair.getKey();
                Map column = (Map)pair.getValue();
                if(!((Boolean)column.get("primary")).booleanValue()) {
                    data = ArrayUtils.add(data, this.getFieldValue((String)column.get("field")));
                    columns = (String[])((String[])ArrayUtils.add(columns, columnName));
                }
            }
            affectedRows = Database.get(this.database).query().from(schemaPoint() + this.table, columns).where(primaryColumn + " = ?", new Object[] { primaryValue }, "").update(data);
            this.empty = false;
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return affectedRows;
    }

    public int delete() {
        int affectedRows = 0;
        try {
            String primaryColumn = (String) this.primary.get("name");
            Object primaryValue = this.getFieldValue((String) this.primary.get("field"));
            if (primaryValue == null || Integer.parseInt(String.valueOf(primaryValue)) == 0) {
                throw new DatabaseException("Primary key must not be null !");
            }
            affectedRows = Database.get(this.database).query().from(schemaPoint() + this.table, new String[]{}).where(primaryColumn + " = ?", new Object[]{primaryValue}, "").delete();
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return affectedRows;
    }

    public void load(String column, Object value) {
        try {
            if (column != null && !"".equals(column) && value != null && !"".equals(value)) {
                Map row = Database.get(this.database).query().from(schemaPoint() + this.table, new String[] {"*"}).where(column + " = ?", new Object[] { value }, "").first();
                if (row != null && !row.isEmpty()) {
                    Iterator it = row.entrySet().iterator();
                    while(it.hasNext()) {
                        Map.Entry entry = (Map.Entry)it.next();
                        String field = (String)((LinkedHashMap)this.columns.get(entry.getKey())).get("field");
                        this.setFieldValue(field, entry.getValue());
                    }
                    this.empty = false;
                }
            }
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
    }

    public void load() throws Exception {
        if (!this.primary.isEmpty()) {
            Object value = this.getFieldValue(String.valueOf(this.primary.get("field")));
            if (value != null){
                this.load(String.valueOf(this.primary.get("name")), value);
            }
        }
    }

    public void unLoad() {
        Iterator it = this.columns.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String columnName = (String)entry.getKey();
            Map column = (Map)entry.getValue();
            if(columnName != null && column != null && !column.isEmpty()) {
                Integer value = "integer".equalsIgnoreCase(String.valueOf(column.get("type")))?new Integer(0):null;
                this.setFieldValue(String.valueOf(column.get("field")), value);
            }
        }
        this.empty = true;
    }

    private void ini() throws Exception {
        if (this.columns.isEmpty()) {
            String[] fields = Inspector.classFields(this);
            if (fields != null && fields.length > 0){
                for (String field : fields) {
                    Column meta = Inspector.columnMeta(this, field);
                    if (meta != null) {
                        if (!isTypeSupported(meta.type())){
                            throw new DatabaseException(meta.type() + " is not a valid type !");
                        }

                        LinkedHashMap column = new LinkedHashMap();
                        column.put("field", field);
                        column.put("name", meta.name());
                        column.put("autoincrement", Boolean.valueOf(meta.autoincrement()));
                        column.put("primary", Boolean.valueOf(meta.primary()));
                        column.put("type", meta.type());
                        column.put("nullable", Boolean.valueOf(meta.nullable()));
                        column.put("indexed", Boolean.valueOf(meta.indexed()));
                        column.put("unique", Boolean.valueOf(meta.unique()));
                        column.put("length", Integer.valueOf(meta.length()));
                            if (meta.primary()){
                                this.primary = column;
                            }
                            this.columns.put(String.valueOf(column.get("name")), column);
                    }
                }
            }
        }
        if (this.primary.isEmpty()){
            throw new DatabaseException("This active record class must have one primary column !");
        }
    }

    public void sync() throws Exception {
        Iterator it = this.columns.entrySet().iterator();
        String lines = "";
        String indice = "";
        String sequence = "";
        String[] arrIndexed = new String[]{};
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            LinkedHashMap<String,Object> column = (LinkedHashMap<String,Object>)pair.getValue();
            String name = (String)pair.getKey();
            boolean primaryColumn = ((Boolean)column.get("primary")).booleanValue();
            boolean autoIncrement = ((Boolean)column.get("autoincrement")).booleanValue();
            String type = (String)column.get("type");
            boolean indexed = ((Boolean)column.get("indexed")).booleanValue();
            String unique = ((Boolean)column.get("unique")).booleanValue() ? " UNIQUE " : "";
            String nullable = ((Boolean)column.get("nullable")).booleanValue() ? " DEFAULT NULL " : " NOT NULL ";
            int length = ((Integer)column.get("length")).intValue();
            switch (type) {
                case "integer":{
                    if (primaryColumn){
                        indice = "CONSTRAINT c_pk_" + name + "_" + this.table + " PRIMARY KEY(" + name + ") \n";
                    }
                    if (autoIncrement){
                        sequence = "seq_" + name + "_" + this.table;
                    }
                    lines = lines + name + " integer " + nullable + " " + unique + ",\n";
                    break;
                }
                case "decimal":{
                    lines = lines + name + " decimal " + nullable + " " + unique + ",\n";
                    break;
                }
                case "text":{
                    if (length > 0 && length <= 255) {
                        lines = lines + name + " varchar(" + length + ") " + nullable + " " + unique + ",\n";
                        break;
                    }
                    lines = lines + name + " text " + nullable + " " + unique + ",\n";
                    break;
                }
                case "boolean":{
                    lines = lines + name + " boolean " + nullable + " " + unique + ",\n";
                    break;
                }
                case "timestamp":{
                    lines = lines + name + " timestamp " + nullable + " " + unique + ",\n";
                    break;
                }
                case "date":{
                    lines = lines + name + " timestamp " + nullable + " " + unique + ",\n";
                    break;
                }
            }
            if (indexed){
                arrIndexed = (String[])ArrayUtils.add((Object[])arrIndexed, name);
            }
            it.remove();
        }
        Database.get(this.database).executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (\n%s,\n%s);", new Object[] { schemaPoint() + this.table, lines.substring(0, lines.length() - 2), indice }), null, Connector.AFFECTED_ROWS);
        if (arrIndexed.length > 0){
            Database.get(this.database).executeUpdate("CREATE INDEX IF NOT EXISTS " + this.table + "_idx ON " + getSchema() + "." + this.table + " USING btree (" + String.join(",", (CharSequence[])arrIndexed) + ") TABLESPACE pg_default", null, Connector.AFFECTED_ROWS);
        }
        if (!"".equalsIgnoreCase(sequence)){
            Database.get(this.database).executeUpdate("CREATE SEQUENCE IF NOT EXISTS " + schemaPoint() + sequence + " START 1", null, Connector.AFFECTED_ROWS);
        }
    }

    public static Object all(Class subClass) {
        return all(subClass, 3, "", new Object[]{}, new String[] { "*" }, null, "ASC", 0, 0, null);
    }

    public static Object all(Class subClass, String paramString, Object[] paramArrayOfObject) {
        return all(subClass, 3, paramString, paramArrayOfObject, new String[] { "*" }, null, "ASC", 0, 0, null);
    }

    public static Object all(Class subClass, int paramInt) {
        return all(subClass, paramInt, "", new Object[]{}, new String[] { "*" }, null, "ASC", 0, 0, null);
    }

    public static Object all(Class subClass, int paramInt, String paramString, Object[] paramArrayOfObject) {
        return all(subClass, paramInt, paramString, paramArrayOfObject, new String[] { "*" }, null, "ASC", 0, 0, null);
    }

    public static Object all(Class subClass, int paramInt, String paramString, Object[] paramArrayOfObject, String[] paramArrayOfString) {
        return all(subClass, paramInt, paramString, paramArrayOfObject, paramArrayOfString, null, "ASC", 0, 0, null);
    }

    public static Object all(Class subClass, int paramInt, String paramString1, Object[] paramArrayOfObject, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString2) {
        return all(subClass, paramInt, paramString1, paramArrayOfObject, paramArrayOfString1, paramArrayOfString2, paramString2, 0, 0, null);
    }

    public static Object all(Class subClass, int paramInt1, String paramString1, Object[] paramArrayOfObject, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString2, int offset, int limit) {
        return all(subClass, paramInt1, paramString1, paramArrayOfObject, paramArrayOfString1, paramArrayOfString2, paramString2, offset, limit, null);
    }

    public static Object all(Class<ActiveRecord> subClass, int paramInt1, String paramString1, Object[] paramArrayOfObject, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString2, int offset, int limit, String[] paramArrayOfString3) {
        ArrayList records = new ArrayList();
        try {
            ActiveRecord record = subClass.getConstructor(new Class[0]).newInstance(new Object[]{});
            paramInt1 = (paramInt1 != 2 && paramInt1 != 3) ? 3 : paramInt1;
            paramArrayOfString1 = (paramArrayOfString1 == null) ? new String[]{"*"} : paramArrayOfString1;
            paramArrayOfString2 = (paramArrayOfString2 == null) ? new String[]{String.valueOf(record.primary.get("name"))} : paramArrayOfString2;
            paramString2 = (paramString2 == null) ? "ASC" : paramString2;
            Query queryBuilder = Database.get(record.database).query().from(record.schemaPoint() + record.table, paramArrayOfString1).order(paramArrayOfString2, paramString2);
            if (paramString1 != null && !"".equalsIgnoreCase(paramString1)){
                queryBuilder = queryBuilder.where(paramString1, paramArrayOfObject, "");
            }
            if (paramArrayOfString3 != null && paramArrayOfString3.length > 0){
                queryBuilder = queryBuilder.group(paramArrayOfString3);
            }
            List list = queryBuilder.limit(offset, limit).all();
            if (paramInt1 == 2){
                return list;
            }

            if (list != null && !list.isEmpty()){
                Iterator it = list.iterator();
                while(it.hasNext()) {
                    Map entry = (Map)it.next();
                    ActiveRecord record2 = subClass.getConstructor(new Class[0]).newInstance(new Object[]{});

                    for(Iterator it2 = entry.entrySet().iterator(); it2.hasNext(); it2.remove()) {
                        Map.Entry en = (Map.Entry)it2.next();
                        String c = (String)en.getKey();
                        Object var20 = en.getValue();
                        if(record2.columns.containsKey(c)) {
                            record2.setFieldValue((String)((LinkedHashMap)record2.columns.get(c)).get("field"), var20);
                         }
                    }
                    record2.empty = false;
                    records.add(subClass.cast(record2));
                }
            }
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return records;
    }

    public static List alls(Class<ActiveRecord> subClass, int paramInt1, String paramString1, Object[] paramArrayOfObject, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString2, int offset, int limit, String[] paramArrayOfString3) throws Exception {
        List records = new ArrayList();
        ActiveRecord outerObject = (ActiveRecord) subClass.getConstructor(new Class[0]).newInstance(new Object[]{});
        paramInt1 = (paramInt1 != 2 && paramInt1 != 3) ? 3 : paramInt1;
        (new String[1])[0] = "*";
        paramArrayOfString1 = (paramArrayOfString1 == null) ? new String[1] : paramArrayOfString1;
        (new String[1])[0] = String.valueOf(outerObject.primary.get("name"));
        paramArrayOfString2 = (paramArrayOfString2 == null) ? new String[1] : paramArrayOfString2;
        paramString2 = (paramString2 == null) ? "ASC" : paramString2;

        Query queryBuilder = Database.get(outerObject.database).query().from(outerObject.schemaPoint() + outerObject.table, paramArrayOfString1).order(paramArrayOfString2, paramString2);
        if (paramString1 != null && !"".equalsIgnoreCase(paramString1)){
            queryBuilder = queryBuilder.where(paramString1, paramArrayOfObject, "");
        }
        if (paramArrayOfString3 != null && paramArrayOfString3.length > 0){
            queryBuilder = queryBuilder.group(paramArrayOfString3);
        }

        try(Connection conn = (Database.get(outerObject.database).getDataSource()).dtsources.getConnection(); PreparedStatement ps = conn.prepareStatement(queryBuilder.limit(offset, limit).query(), ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
            if (paramArrayOfObject != null && paramArrayOfObject.length > 0) {
                int counter = 1;
                for (Object object : paramArrayOfObject) {
                    if (object != null) {
                        ps.setObject(counter, object, Connector.getType(object));
                        counter++;
                    }
                }
            }
            ps.setFetchSize(1);
            try (ResultSet result = ps.executeQuery()) {
                if (result.isBeforeFirst())
                    if (paramInt1 == 3) {
                        ActiveRecord record = null;
                        while (result.next()) {
                            record = subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
                            for (Map.Entry entry : record.columns.entrySet()){
                                record.setFieldValue((String)((Map)entry.getValue()).get("field"), result.getObject((String)((Map)entry.getValue()).get("name")));
                            }
                            record.empty = false;
                            records.add(subClass.cast(record));
                        }
                    } else {
                        ActiveRecord record = subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
                        LinkedHashMap<Object, Object> linkedHashMap = null;
                        while (result.next()) {
                            linkedHashMap = new LinkedHashMap<>();
                            for (Map.Entry entry : record.columns.entrySet()){
                                linkedHashMap.put(((Map)entry.getValue()).get("field"), result.getObject((String)((Map)entry.getValue()).get("name")));
                            }
                            records.add(linkedHashMap);
                        }
                    }
            }
        }
        return records;
    }

    public static Object first(Class subClass) {
        return first(subClass, 3, "", new Object[]{}, new String[] { "*" });
    }

    public static Object first(Class subClass, String paramString, Object[] paramArrayOfObject) {
        return first(subClass, 3, paramString, paramArrayOfObject, new String[] { "*" });
    }

    public static Object first(Class subClass, int paramInt) {
        return first(subClass, paramInt, "", new Object[]{}, new String[] { "*" });
    }

    public static Object first(Class subClass, int paramInt, String paramString, Object[] paramArrayOfObject) {
        return first(subClass, paramInt, paramString, paramArrayOfObject, new String[] { "*" });
    }

    public static Object first(Class<ActiveRecord> subClass, int paramInt, String condition, Object[] values, String[] conditions) {
        ActiveRecord record1 = null;
        try {
            ActiveRecord record2 = subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            paramInt = (paramInt != 2 && paramInt != 3) ? 3 : paramInt;
            conditions = (conditions == null) ? new String[]{"*"} : conditions;
            Query queryBuilder = Database.get(record2.database).query().from(record2.schemaPoint() + record2.table, conditions);
            if (condition != null && !"".equalsIgnoreCase(condition)){
                queryBuilder = queryBuilder.where(condition, values, "");
            }
            Map map = queryBuilder.limit(0, 1).first();
            if (paramInt == 2){
                return map;
            }
            if (map != null && !map.isEmpty()) {
                record1 = record2;
                Iterator<Map.Entry> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = it.next();
                    String c = (String)entry.getKey();
                    Object object = entry.getValue();
                    if (record1.columns.containsKey(c)){
                        record1.setFieldValue((String)((LinkedHashMap)record1.columns.get(c)).get("field"), object);
                    }
                    it.remove();
                }
                record1.empty = false;
            }
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return record1;
    }

    public static Object last(Class subClass) {
        return last(subClass, 3, "", new Object[]{}, new String[] { "*" });
    }

    public static Object last(Class subClass, String paramString, Object[] paramArrayOfObject) {
        return last(subClass, 3, paramString, paramArrayOfObject, new String[] { "*" });
    }

    public static Object last(Class subClass, int paramInt) {
        return last(subClass, paramInt, "", new Object[]{}, new String[] { "*" });
    }

    public static Object last(Class subClass, int paramInt, String condition, Object[] values) {
        return last(subClass, paramInt, condition, values, new String[] { "*" });
    }

    public static Object last(Class<ActiveRecord> subClass, int paramInt, String condition, Object[] values, String[] conditions) {
        ActiveRecord record1 = null;
        try {
            ActiveRecord record2 = subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            paramInt = (paramInt != 2 && paramInt != 3) ? 3 : paramInt;
            conditions = (conditions == null) ? new String[]{"*"} : conditions;
            Query queryBuilder = Database.get(record2.database).query().from(record2.schemaPoint() + record2.table, conditions);
            if (condition != null && !"".equalsIgnoreCase(condition)){
                queryBuilder = queryBuilder.where(condition, values, "");
            }
            Map map = queryBuilder.limit(0, 1).order(new String[]{String.valueOf(record2.primary.get("name"))}, "DESC").first();
            if (paramInt == 2){
                return map;
            }
            if (map != null && !map.isEmpty()) {
                record1 = record2;
                Iterator<Map.Entry> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = it.next();
                    String c = (String)entry.getKey();
                    Object object = entry.getValue();
                    if (record1.columns.containsKey(c)){
                        record1.setFieldValue((String)((LinkedHashMap)record1.columns.get(c)).get("field"), object);
                    }
                    it.remove();
                }
                record1.empty = false;
            }
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return record1;
    }

    public static int count(Class subClass) {
        return count(subClass, "", new Object[]{});
    }

    public static int count(Class<ActiveRecord> subClass, String condition, Object[] values) {
        int i = 0;
        try {
            ActiveRecord record = subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            Query queryBuilder = Database.get(record.database).query().from(record.schemaPoint() + record.table, (String[])null);
            if (condition != null && !"".equalsIgnoreCase(condition)){
                queryBuilder.where(condition, values, "");
            }
            i = queryBuilder.count();
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return i;

    }

    public static int[] insert(List records) throws Exception {
        int[] results = new int[0];
        if (records != null && records.size() > 0){
            for (Object record : records) {
                if (record != null){
                    results = ArrayUtils.add(results, ((ActiveRecord)record).insert());
                }
            }
        }
        return results;
    }

    public static int update(List records) throws Exception {
        int affectedRows = 0;
        if (records != null && records.size() > 0) {
            for (Object record : records) {
                if (record != null){
                    affectedRows = ((ActiveRecord)record).insert();
                }
            }
        }
        return affectedRows;
    }

    public static int delete(List records) {
        int i = 0;
        if (records != null && records.size() > 0){
            for (Object record : records) {
                if (record != null){
                    i = ((ActiveRecord)record).delete();
                }
            }
        }
        return i;
    }

    public static int delete(Class subClass) {
        return delete(subClass, "", new Object[]{});
    }

    public static int delete(Class subClass, String condition, Object[] values) {
        int affectedRows = 0;
        try {
            ActiveRecord record = (ActiveRecord)subClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            Query queryBuilder = Database.get(record.database).query().from(record.schemaPoint() + record.table, new String[]{});
            if (condition != null && !"".equalsIgnoreCase(condition)){
                queryBuilder.where(condition, values, "");
            }
            affectedRows = queryBuilder.delete();
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return affectedRows;
    }

    public static void sync(String model, String database, String schema, String table, String fileTemplate) throws Exception {
        String template = FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/backend/model.tpl"), "utf-8");
        template = StringUtils.replace(template, "$p_model", model);
        template = StringUtils.replace(template, "$p_database", database);
        template = StringUtils.replace(template, "$p_schema", schema);
        template = StringUtils.replace(template, "$p_table", table);
        String columns = "";
        String tab = "    ";
        String dataToImport = "";

        List<LinkedHashMap<String, Object>> rowscolumns = Database.get(database).executeQuery("SELECT * FROM information_schema.columns WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position ASC", new Object[] { schema, table }, Connector.FETCH_ALL);
        List<LinkedHashMap<String, Object>> rowsConstraint = Database.get(database).executeQuery("SELECT conname FROM pg_constraint WHERE conname LIKE 'c_pk_%'", null, Connector.FETCH_ALL);

        if (rowscolumns != null && !rowscolumns.isEmpty()){
            for (LinkedHashMap<String, Object> rowCol : rowscolumns) {
                if (rowCol != null && !rowCol.isEmpty()) {
                    String colName = String.valueOf(rowCol.get("column_name")).toLowerCase();
                    boolean primary = false;
                    if (rowsConstraint != null && !rowsConstraint.isEmpty()){
                        for (LinkedHashMap<String, Object> rowName : rowsConstraint) {
                            if (("c_pk_" + colName + "_" + table).equals(rowName.get("conname"))) {
                                primary = true;
                                break;
                            }
                        }
                    }
                    String type = "character varying".equalsIgnoreCase(String.valueOf(rowCol.get("data_type"))) ? "text" : String.valueOf(rowCol.get("data_type")).toLowerCase();
                    type = type.contains("timestamp") ? "timestamp" : type;
                    int length = (!rowCol.containsKey("character_maximum_length") || rowCol.get("character_maximum_length") == null) ? 0 : Integer.parseInt(String.valueOf(rowCol.get("character_maximum_length")));
                    boolean nullable = "YES".equalsIgnoreCase(String.valueOf(rowCol.get("is_nullable")));
                    String javaType = "String";

                    char[] field = WordUtils.capitalize(colName.replaceAll("_", " ").toLowerCase()).replaceAll(" ", "").toCharArray();
                    field[0] = Character.toLowerCase(field[0]);
                    switch (type) {
                        case "integer":{
                            javaType = "int";
                            break;
                        }
                        case "text":{
                            javaType = "String";
                            break;
                        }
                        case "decimal":{
                            javaType = "float";
                            break;
                        }
                        case "boolean":{
                            javaType = "boolean";
                            break;
                        }
                        case "date":{
                            javaType = "java.sql.Date";
                            break;
                        }
                        case "timestamp":{
                            javaType = "java.sql.Timestamp";
                            break;
                        }
                    }

                    columns = columns + "\n";
                    columns = columns + tab + "@Column( name = \"" + colName + "\", ";

                    if (primary){
                        columns = columns + "primary = true, autoincrement = true, ";
                    }

                    columns = columns + "type = \"" + type + "\", ";

                    if (length > 0) {
                        columns = columns + "nullable = " + String.valueOf(nullable) + ", length = " + length + " ";
                    } else {
                        columns = columns + "nullable = " + String.valueOf(nullable) + " ";
                    }

                    columns = columns + ")\n";
                    if ("date".equals(type)) {
                        columns = columns + tab + "@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = \"yyyy-MM-dd\")\n";
                        dataToImport = "\nimport com.fasterxml.jackson.annotation.JsonFormat;";
                    } else if ("timestamp".equals(type)) {
                        columns = columns + tab + "@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = \"yyyy-MM-dd HH:mm:ss\")\n";
                        dataToImport = "\nimport com.fasterxml.jackson.annotation.JsonFormat;";
                    }
                    columns = columns + tab + "public " + javaType + " " + new String(field) + ";\n";
                }
            }
        }
        template = StringUtils.replace(template, "$p_date_import", dataToImport);
        template = StringUtils.replace(template, "$p_colums", columns);
        if (!(new File(fileTemplate)).exists()){
            (new File(fileTemplate)).mkdirs();
        }
        FileUtils.writeStringToFile(new File(fileTemplate + File.separator + model + ".java"), template, "utf-8");
    }

    private Object getFieldValue(String field) throws Exception {
        return getClass().getDeclaredField(field).get(this);
    }

    private void setFieldValue(String field, Object value) {
        try {
            if (value == null) {
                switch (getClass().getDeclaredField(field).getGenericType().getTypeName()) {
                    case "int":
                        getClass().getDeclaredField(field).setInt(this, 0);
                        return;
                    case "float":
                        getClass().getDeclaredField(field).setFloat(this, 0.0F);
                        return;
                    case "boolean":
                        getClass().getDeclaredField(field).setBoolean(this, false);
                        return;
                }
                getClass().getDeclaredField(field).set(this, null);
            } else {
                switch (getClass().getDeclaredField(field).getGenericType().getTypeName()) {
                    case "int":
                        getClass().getDeclaredField(field).setInt(this, TypesParser.safeParseInt(value));
                        return;
                    case "float":
                        getClass().getDeclaredField(field).setFloat(this, TypesParser.safeParseFloat(value));
                        return;
                }
                getClass().getDeclaredField(field).set(this, value);
            }
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }

    }

    private boolean isTypeSupported(String type) {
        return ("integer".equalsIgnoreCase(type) || "decimal".equalsIgnoreCase(type) || "text".equalsIgnoreCase(type) || "date".equalsIgnoreCase(type) || "timestamp".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type));
    }

    public ActiveRecord() throws Exception {
        this.ini();
    }

    public ActiveRecord(Object primaryValue) throws Exception{
        this.ini();
        this.setFieldValue((String)this.primary.get("field"), primaryValue);
    }

    public String schemaPoint() {
        return (this.schema == null || "".equals(this.schema)) ? "" : (this.schema + ".");
    }

    public String getSchema() {
        return this.schema;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getTable() {
        return this.table;
    }

    public LinkedHashMap<String,LinkedHashMap<String,Object>> getColumns() {
        return this.columns;
    }

    public LinkedHashMap<String,Object> getPrimary() {
        return this.primary;
    }

    public boolean getEmpty() {
        return this.empty;
    }

    public void setDatabase(String paramString) {
        this.database = paramString;
    }

    public void setSchema(String paramString) {
        this.schema = paramString;
    }

    public void setTable(String paramString) {
        this.table = paramString;
    }

    public void setColumns(LinkedHashMap<String,LinkedHashMap<String,Object>> columns) {
        this.columns = columns;
    }

    public void setPrimary(LinkedHashMap<String,Object> primary) {
        this.primary = primary;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ActiveRecord))
            return false;
        ActiveRecord do1 = (ActiveRecord)paramObject;
        if (!do1.exists(this))
            return false;
        String str1 = getDatabase();
        String str2 = do1.getDatabase();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = schemaPoint();
        String str4 = do1.schemaPoint();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getTable();
        String str6 = do1.getTable();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        Map map1 = getColumns();
        Map map2 = do1.getColumns();
        if ((map1 == null) ? (map2 != null) : !map1.equals(map2))
            return false;
        Map map3 = getPrimary();
        Map map4 = do1.getPrimary();
        return ((map3 == null) ? (map4 != null) : !map3.equals(map4)) ? false : (!(getEmpty() != do1.getEmpty()));
    }

    protected boolean exists(Object instance) {
        return instance instanceof ActiveRecord;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getDatabase();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = schemaPoint();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getTable();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        Map map1 = getColumns();
        n = n * 59 + ((map1 == null) ? 43 : map1.hashCode());
        Map map2 = getPrimary();
        n = n * 59 + ((map2 == null) ? 43 : map2.hashCode());
        return n * 59 + (getEmpty() ? 79 : 97);
    }

    public String toString() {
        return "ActiveRecord(database=" + getDatabase() + ", schema=" + schemaPoint() + ", table=" + getTable() + ", columns=" + getColumns() + ", primary=" + getPrimary() + ", empty=" + getEmpty() + ")";
    }
}
