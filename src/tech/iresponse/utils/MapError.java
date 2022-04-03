package tech.iresponse.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import tech.iresponse.logging.Loggers;
import tech.iresponse.http.Response;
import tech.iresponse.http.ResponseData;

public class MapError {
    public static void ini(Response response) {
        if (response != null){
            try {
                HashMap<Object, Object> map = new HashMap<>();
                map.put("message", response.getMessage());
                map.put("httpStatus", Integer.valueOf(response.getHttpstatus()));
                map.put("timestamp", response.getTimestamp());
                if (response instanceof ResponseData){
                    map.put("data", ((ResponseData)response).getData());
                }
                System.out.println((new ObjectMapper()).writeValueAsString(map));

            } catch (JsonProcessingException ex) {
                Loggers.error((Throwable)ex);
                System.out.println("{\"httpStatus\":500,\"message\":\"Could not get response !\",\"timestamp\":\"" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(System.currentTimeMillis())) + "\"}");
            }
        }
    }
}
