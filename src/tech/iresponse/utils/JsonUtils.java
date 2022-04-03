package tech.iresponse.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class JsonUtils {

    public static JSONObject toJSON(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException jSONException) {
            return null;
        }
    }

    public static JSONArray toJSONArray(String json) {
        try {
            return new JSONArray(json);
        } catch (JSONException jSONException) {
            return null;
        }
    }

    public static boolean isJSONArray(Object json) {
        return (json instanceof JSONArray && ((JSONArray)json).length() > 0);
    }

    public static boolean isJSONObject(Object json) {
        return (json instanceof JSONObject && ((JSONObject)json).length() > 0);
    }

    public static boolean jsonContainsArray(Object json, String key) {
        try {
            return (isJSONObject(json) && ((JSONObject)json).has(key) && isJSONArray(((JSONObject)json).get(key)));
        } catch (JSONException jSONException) {
            return false;
        }
    }

    public static boolean jsonContainsJsonObject(Object json, String key) {
        try {
            return (isJSONObject(json) && ((JSONObject)json).has(key) && isJSONObject(((JSONObject)json).get(key)));
        } catch (JSONException jSONException) {
            return false;
        }
    }

    public static List<Object> jsonArrayToObjectArrayList(JSONArray json) {
        ArrayList<Object> results = new ArrayList();
        try {
            if (json != null){
                for (int b = 0; b < json.length(); b++){
                    results.add(json.get(b));
                }
            }
        } catch (JSONException jSONException) {}
        return results;
    }

    public static List<Integer> jsonArrayToIntegerArrayList(JSONArray json) {
        ArrayList<Integer> arrayList = new ArrayList();
        try {
            if (json != null){
                for (int b = 0; b < json.length(); b++){
                    arrayList.add(Integer.valueOf(TypesParser.safeParseInt(json.get(b))));
                }
            }
        } catch (JSONException jSONException) {}
        return arrayList;
    }

    public static List<String> jsonArrayToStringArrayList(JSONArray json) {
        ArrayList<String> arrayList = new ArrayList();
        try {
            if (json != null){
                for (int b = 0; b < json.length(); b++){
                    arrayList.add(String.valueOf(json.get(b)));
                }
            }
        } catch (JSONException jSONException) {}
        return arrayList;
    }

    public static JSONArray csvToJson(String csvText, char delimiter) throws IOException, JSONException {
        JSONArray result = null;
        try (ByteArrayInputStream bi = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8))) {
            CSV csv = new CSV(true, delimiter, bi);
            ArrayList<String> arrayList = new ArrayList();
            if (csv.hasNext()){
                arrayList.addAll(csv.next());
            }
            int size = arrayList.size();
            result = new JSONArray();
            while (csv.hasNext()) {
                List<String> list = csv.next();
                if (size == list.size()) {
                    JSONObject jSONObject = new JSONObject();
                    for (int b = 0; b < arrayList.size(); b++){
                        jSONObject.put(arrayList.get(b), list.get(b));
                    }
                    result.put(jSONObject);
                }
            }
        }
        return result;
    }

    public static Object getValueFromJson(JSONObject jsonObject, String key, Object defaultValue) {
        try {
            if (jsonObject != null && jsonObject.has(key)){
                return jsonObject.get(key);
            }
        } catch (JSONException jSONException) {}
        return defaultValue;
    }
}
