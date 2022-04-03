package tech.iresponse.orm;

import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.logging.Loggers;

public class Query {

    private String database;
    private String from = "";
    private String[] fields = new String[]{};
    private int offset = 0;
    private int limit = 0;
    private String[] order = new String[]{};
    private String direction = "ASC";
    private String[] group = new String[]{};
    private String[] join = new String[]{};
    private String[] where = new String[]{};
    private Object[] whereParameters = new Object[]{};
    private Object[] parameters = new Object[]{};
    private String query = "";
    public static final int SELECT = 0;
    public static final int INSERT = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;
    public static final int ONLY_BUILD_QUERY = 0;
    public static final int EXECUTE_QUERY = 1;
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String LEFT_JOIN = "LEFT JOIN";
    public static final String RIGHT_JOIN = "RIGHT JOIN";
    public static final String INNER_JOIN = "INNER JOIN";
    public static final String FULL_OUTER_JOIN = "FULL OUTER JOIN";
    public static final String KEY = "68U5HBfK-&r!gNF559@qJzbxP4aVHT7em#";

    public String query() throws Exception{
        if (this.whereParameters != null && this.whereParameters.length > 0){
            this.parameters = ArrayUtils.addAll(this.parameters, this.whereParameters);
        }
        if ("".equalsIgnoreCase(this.query)){
            build(0);
        }
        return this.query;
    }

    public List all() {
        List<LinkedHashMap<String, Object>> results = new ArrayList<LinkedHashMap<String, Object>>();
        try{
            if (this.whereParameters != null && this.whereParameters.length > 0){
                this.parameters = ArrayUtils.addAll(this.parameters, this.whereParameters);
            }
            if ("".equalsIgnoreCase(this.query)){
                build(0);
            }
            results = Database.get(this.database).executeQuery(this.query, this.parameters, 1);
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return results;

    }

    public Map first() {
        LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
        try{
            if (this.whereParameters != null && this.whereParameters.length > 0){
                this.parameters = ArrayUtils.addAll(this.parameters, this.whereParameters);
            }
            if ("".equalsIgnoreCase(this.query)){
                build(0);
            }
            List<LinkedHashMap<String, Object>> results = Database.get(this.database).executeQuery(this.query, this.parameters, 0);
            //row = !(results = Database.get(this.database).executeQuery(this.query, this.parameters, 0)).isEmpty() ? results.get(0) : row;
            row = !results.isEmpty() ? results.get(0) : row;
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return row;
    }

    public int count() throws Exception {
        this.fields = new String[] { "COUNT(1) AS count" };
        if (this.whereParameters != null && this.whereParameters.length > 0)
            this.parameters = ArrayUtils.addAll(this.parameters, this.whereParameters);
        if ("".equalsIgnoreCase(this.query)){
            build(0);
        }
        List<Map> list = Database.get(this.database).executeQuery(this.query, this.parameters, 0);
        return (list == null || list.isEmpty()) ? 0 : TypesParser.safeParseInt(((Map)list.get(0)).get("count"));
    }

    public int insert(Object[] parameters) throws Exception {
        this.parameters = parameters;
        if ("".equalsIgnoreCase(this.query)){
            build(Query.INSERT);
        }
        return Database.get(this.database).executeUpdate(this.query, this.parameters, Connector.LAST_INSERTED_ID);
    }

    public int update(Object[] paramArrayOfObject) {
        int result = 0;
        try {
            this.parameters = paramArrayOfObject;
            if (this.whereParameters != null && this.whereParameters.length > 0)
                this.parameters = ArrayUtils.addAll(this.parameters, this.whereParameters);
            if ("".equalsIgnoreCase(this.query)) {
                build(2);
            }
            result = Database.get(this.database).executeUpdate(this.query, this.parameters, 0);
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return result;
    }

    public int delete() {
        int result = 0;
        try {
            if (this.whereParameters != null && this.whereParameters.length > 0)
                this.parameters = this.whereParameters;
            if ("".equalsIgnoreCase(this.query)) {
                build(3);
            }
            result = Database.get(this.database).executeUpdate(this.query, this.parameters, 0);
        }
        catch (Exception e) {
            Loggers.error(new DatabaseException(e));
        }
        return result;
    }

    public Query from(String from, String[] fieldsValue) {
        this.from = from;
        if (fieldsValue == null || 0 == fieldsValue.length){
            fieldsValue = new String[] { "*" };
        }
        this.fields = (String[])ArrayUtils.addAll((Object[])this.fields, (Object[])fieldsValue);
        return this;
    }

    public Query where(String condition, Object[] parameters, String concat) {
        concat = ("and".equalsIgnoreCase(concat) || "or".equalsIgnoreCase(concat) || "nand".equalsIgnoreCase(concat) || "nor".equalsIgnoreCase(concat)) ? (concat + " ") : "";
        this.where = (String[])ArrayUtils.add((Object[])this.where, concat + condition);
        this.whereParameters = ArrayUtils.addAll(this.whereParameters, parameters);
        return this;
    }

    public Query order(String[] columns, String direction) {
        this.order = (String[])ArrayUtils.addAll((Object[])this.order, (Object[])columns);
        this.direction = direction;
        return this;
    }

    public Query limit(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public Query group(String[] columns) {
        this.group = (String[])ArrayUtils.addAll((Object[])this.group, (Object[])columns);
        return this;
    }

    public Query join(String join, String on, String[] fieldsValue, String type) {
        type = (type == null || "".equalsIgnoreCase(type)) ? "LEFT JOIN" : type;
        if (fieldsValue == null){
            fieldsValue = new String[]{};
        }
        if (0 == fieldsValue.length){
            fieldsValue[0] = "*";
        }
        this.fields = (String[])ArrayUtils.addAll((Object[])this.fields, (Object[])fieldsValue);
        this.join = (String[])ArrayUtils.add((Object[])this.join, type + " " + join + " ON " + on);
        return this;
    }

    private Query build(int type) throws Exception {
        switch (type) {
            case Query.SELECT: {
                String template = "SELECT %s FROM %s %s %s %s %s %s";
                String fieldsValue = "";
                String wheres = "";
                String orders = "";
                String limit = "";
                String joins = "";
                String groups = "";
                int i;
                for (i = 0; i < this.fields.length; i++) {
                    fieldsValue = fieldsValue + this.fields[i];
                    if (i != this.fields.length - 1){
                        fieldsValue = fieldsValue + ",";
                    }
                }
                if (this.join != null && this.join.length > 0) {
                    for (i = 0; i < this.join.length; i++) {
                        joins = joins + this.join[i];
                        if (i != this.join.length - 1){
                            joins = joins + this.join[i] + " ";
                        }
                    }
                }
                if (this.where != null && this.where.length > 0) {
                    wheres = "WHERE ";
                    for (i = 0; i < this.where.length; i++) {
                        wheres = wheres + this.where[i];
                        if (i != this.where.length - 1){
                            wheres = wheres + " ";
                        }
                    }
                }
                if (this.group != null && this.group.length > 0) {
                    groups = "GROUP BY ";
                    for (i = 0; i < this.group.length; i++) {
                        groups = groups + this.group[i];
                        if (i != this.group.length - 1){
                            groups = groups + ",";
                        }
                    }
                }
                if (this.order != null && this.order.length > 0) {
                    orders = "ORDER BY ";
                    for (i = 0; i < this.order.length; i++) {
                        orders = orders + this.order[i];
                        if (i != this.order.length - 1){
                            orders = orders + ",";
                        }
                    }
                    orders = orders + " " + this.direction;
                }
                if (this.limit > 0){
                    if (this.offset > 0) {
                        limit = "OFFSET " + this.offset + " LIMIT " + this.limit;
                    } else {
                        limit = "LIMIT " + this.limit;
                    }
                }
                this.query = String.format(template, fieldsValue, this.from, joins, wheres, groups, orders, limit);
                break;
            }
            case Query.INSERT: {
                String template = "INSERT INTO %s (%s) VALUES (%s)";
                String fieldsValue = "";
                String values = "";
                int i;
                ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
                for (i = 0; i < this.fields.length; i++) {
                    fieldsValue = fieldsValue + this.fields[i];
                    if (i != this.fields.length - 1) {
                        fieldsValue = fieldsValue + ",";
                    }
                }
                for (i = 0; i < this.fields.length; i++) {
                    if (this.parameters[i] == null) {
                        values = values + "NULL";
                        removeIndexes.add(Integer.valueOf(i));
                    } else {
                        values = values + "?";
                    }
                    if (i != this.fields.length - 1){
                        values = values + ",";
                    }
                }
                Object[] listadd = new Object[]{};
                for (int b3 = 0; b3 < this.parameters.length; b3++) {
                    if (!removeIndexes.contains(Integer.valueOf(b3))){
                        listadd = ArrayUtils.add(listadd, this.parameters[b3]);
                    }
                }
                this.parameters = listadd;
                this.query = String.format(template, this.from, fieldsValue, values);
                break;
            }
            case Query.UPDATE: {
                String template = "UPDATE %s SET %s %s";
                String fieldsValue = "";
                String wheres = "";
                ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
                int i;
                for (i = 0; i < this.fields.length; i++) {
                    if (this.parameters[i] == null) {
                        fieldsValue = fieldsValue + this.fields[i] + " = NULL";
                        removeIndexes.add(Integer.valueOf(i));
                    } else {
                        fieldsValue = fieldsValue + this.fields[i] + " = ?";
                    }
                    if (i != this.fields.length - 1){
                        fieldsValue = fieldsValue + ",";
                    }
                }
                if (this.where != null && this.where.length > 0) {
                    wheres = "WHERE ";
                    for (i = 0; i < this.where.length; i++) {
                        wheres = wheres + this.where[i];
                        if (i != this.where.length - 1){
                            wheres = wheres + " ";
                        }
                    }
                }
                Object[] listadd = new Object[]{};
                for (int b3 = 0; b3 < this.parameters.length; b3++) {
                    if (!removeIndexes.contains(Integer.valueOf(b3))){
                        listadd = ArrayUtils.add(listadd, this.parameters[b3]);
                    }
                }
                this.parameters = listadd;
                this.query = String.format(template, this.from, fieldsValue, wheres);
                break;
            }
            case Query.DELETE: {
                String template = "DELETE FROM %s %s";
                String fieldsValue = "";
                if (this.where != null && this.where.length > 0) {
                    fieldsValue = "WHERE ";
                    for (byte b = 0; b < this.where.length; b++) {
                        fieldsValue = fieldsValue + this.where[b];
                        if (b != this.where.length - 1){
                            fieldsValue = fieldsValue + " ";
                        }
                    }
                }
                this.query = String.format(template, this.from, fieldsValue);
                break;
            }
            default: {
                throw new DatabaseException("Unsupported query type !");
            }
        }
        return this;
    }

    public Query(String database) {
        this.database = database;
    }

    public Query() {}

    public String getDatabase() {
        return this.database;
    }

    public String getFrom() {
        return this.from;
    }

    public String[] getFields() {
        return this.fields;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLimit() {
        return this.limit;
    }

    public String[] getOrder() {
        return this.order;
    }

    public String getDirection() {
        return this.direction;
    }

    public String[] getGroup() {
        return this.group;
    }

    public String[] getJoin() {
        return this.join;
    }

    public String[] getWhere() {
        return this.where;
    }

    public Object[] getWhereParameters() {
        return this.whereParameters;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    public String getQuery() {
        return this.query;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setFrom(String paramString) {
        this.from = paramString;
    }

    public void setFields(String[] paramArrayOfString) {
        this.fields = paramArrayOfString;
    }

    public void setOffset(int paramInt) {
        this.offset = paramInt;
    }

    public void setLimit(int paramInt) {
        this.limit = paramInt;
    }

    public void setOrder(String[] paramArrayOfString) {
        this.order = paramArrayOfString;
    }

    public void setDirection(String paramString) {
        this.direction = paramString;
    }

    public void setGroup(String[] paramArrayOfString) {
        this.group = paramArrayOfString;
    }

    public void setJoin(String[] paramArrayOfString) {
        this.join = paramArrayOfString;
    }

    public void setWhere(String[] paramArrayOfString) {
        this.where = paramArrayOfString;
    }

    public void setWhereParameters(Object[] paramArrayOfObject) {
        this.whereParameters = paramArrayOfObject;
    }

    public void setParameters(Object[] paramArrayOfObject) {
        this.parameters = paramArrayOfObject;
    }

    public void setQuery(String paramString) {
        this.query = paramString;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Query))
            return false;
        Query try1 = (Query)paramObject;
        if (!try1.checkbool(this))
            return false;
        String str1 = getDatabase();
        String str2 = try1.getDatabase();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getFrom();
        String str4 = try1.getFrom();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        if (!Arrays.deepEquals((Object[])getFields(), (Object[])try1.getFields()))
            return false;
        if (getOffset() != try1.getOffset())
            return false;
        if (getLimit() != try1.getLimit())
            return false;
        if (!Arrays.deepEquals((Object[])getOrder(), (Object[])try1.getOrder()))
            return false;
        String str5 = getDirection();
        String str6 = try1.getDirection();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        if (!Arrays.deepEquals((Object[])getGroup(), (Object[])try1.getGroup()))
            return false;
        if (!Arrays.deepEquals((Object[])getJoin(), (Object[])try1.getJoin()))
            return false;
        if (!Arrays.deepEquals((Object[])getWhere(), (Object[])try1.getWhere()))
            return false;
        if (!Arrays.deepEquals(getWhereParameters(), try1.getWhereParameters()))
            return false;
        if (!Arrays.deepEquals(getParameters(), try1.getParameters()))
            return false;
        String str7 = getQuery();
        String str8 = try1.getQuery();
        return !((str7 == null) ? (str8 != null) : !str7.equals(str8));
    }

    protected boolean checkbool(Object paramObject) {
        return paramObject instanceof Query;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getDatabase();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getFrom();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        n = n * 59 + Arrays.deepHashCode((Object[])getFields());
        n = n * 59 + getOffset();
        n = n * 59 + getLimit();
        n = n * 59 + Arrays.deepHashCode((Object[])getOrder());
        String str3 = getDirection();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + Arrays.deepHashCode((Object[])getGroup());
        n = n * 59 + Arrays.deepHashCode((Object[])getJoin());
        n = n * 59 + Arrays.deepHashCode((Object[])getWhere());
        n = n * 59 + Arrays.deepHashCode(getWhereParameters());
        n = n * 59 + Arrays.deepHashCode(getParameters());
        String str4 = getQuery();
        return n * 59 + ((str4 == null) ? 43 : str4.hashCode());
    }

    @Override
    public String toString() {
        return "Query(database=" + getDatabase() + ", from=" + getFrom() + ", fields=" + Arrays.deepToString((Object[])getFields()) + ", offset=" + getOffset() + ", limit=" + getLimit() + ", order=" + Arrays.deepToString((Object[])getOrder()) + ", direction=" + getDirection() + ", group=" + Arrays.deepToString((Object[])getGroup()) + ", join=" + Arrays.deepToString((Object[])getJoin()) + ", where=" + Arrays.deepToString((Object[])getWhere()) + ", whereParameters=" + Arrays.deepToString(getWhereParameters()) + ", parameters=" + Arrays.deepToString(getParameters()) + ", query=" + getQuery() + ")";
    }
}
