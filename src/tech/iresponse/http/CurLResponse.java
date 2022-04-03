package tech.iresponse.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.LinkedHashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurLResponse implements Serializable {

    private LinkedHashMap headers = new LinkedHashMap<>();
    private int httpStatus;
    private String body = "";

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof CurLResponse))
            return false;
        CurLResponse do1 = (CurLResponse)paramObject;
        if (!do1.exists(this))
            return false;
        LinkedHashMap linkedHashMap1 = getHeaders();
        LinkedHashMap linkedHashMap2 = do1.getHeaders();
        if ((linkedHashMap1 == null) ? (linkedHashMap2 != null) : !linkedHashMap1.equals(linkedHashMap2))
            return false;
        if (getHttpStatus() != do1.getHttpStatus())
            return false;
        String str1 = getBody();
        String str2 = do1.getBody();
            return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof CurLResponse;
    }

    @Override
    public int hashCode() {
        int n = 1;
        LinkedHashMap linkedHashMap = getHeaders();
        n = n * 59 + ((linkedHashMap == null) ? 43 : linkedHashMap.hashCode());
        n = n * 59 + getHttpStatus();
        String str = getBody();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public LinkedHashMap getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedHashMap headers) {
        this.headers = headers;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "CurLResponse(headers=" + getHeaders() + ", httpStatus=" + getHttpStatus() + ", body=" + getBody() + ")";
    }
}
