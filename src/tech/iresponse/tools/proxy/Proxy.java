package tech.iresponse.tools.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.beans.ConstructorProperties;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxy implements Serializable {

    private String host;
    private String port;

    @ConstructorProperties({"host", "port"})
    public Proxy(String host, String port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Proxy))
            return false;
        Proxy do1 = (Proxy)paramObject;
        if (!do1.exists(this))
            return false;
        String str1 = getHost();
        String str2 = do1.getHost();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getPort();
        String str4 = do1.getPort();
            return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Proxy;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getHost();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getPort();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Proxy(host=" + getHost() + ", port=" + getPort() + ")";
    }
}
