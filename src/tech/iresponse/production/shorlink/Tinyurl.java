package tech.iresponse.production.shorlink;

import java.util.LinkedHashMap;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.http.Agents;
import tech.iresponse.exceptions.DatabaseException;

public class Tinyurl {

    public static String shortTinyurl(String url) throws Exception {
        try {
            if (!url.contains("http")){
                url = "http://" + url;
            }

            LinkedHashMap<String, String> records = new LinkedHashMap<>(1);
            records.put("url", url);
            String response = Agents.get("http://tinyurl.com/api-create.php", records, 10);

            if (response == null || "".equals(response)){
                throw new DatabaseException("No response retreived !");
            }
            url = response;
        } catch (Exception exception) {
            throw new DatabaseException("Could not shorten link !", exception);
        }
        return url;
    }
}
