package tech.iresponse.http;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Response implements Serializable {

    private String timestamp;
    private int httpstatus;
    private String message;

    public Response(String message, int status) {
        this.httpstatus = status;
        this.message = message;
        this.timestamp = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(System.currentTimeMillis()));
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Response))
            return false;
        Response for1 = (Response)paramObject;
        if (!for1.exists(this))
            return false;
        String str1 = getTimestamp();
        String str2 = for1.getTimestamp();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        if (getHttpstatus() != for1.getHttpstatus())
            return false;
        String str3 = getMessage();
        String str4 = for1.getMessage();
        return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Response;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getTimestamp();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        n = n * 59 + getHttpstatus();
        String str2 = getMessage();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public int getHttpstatus() {
        return this.httpstatus;
    }

    public String getMessage() {
        return this.message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setHttpstatus(int httpstatus) {
        this.httpstatus = httpstatus;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Response(timestamp=" + getTimestamp() + ", httpStatus=" + getHttpstatus() + ", message=" + getMessage() + ")";
    }
}
