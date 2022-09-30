package tech.iresponse.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.TypesParser;

public class Agents {

    public static final String key = "z#SAsZb#@yPf5Jrzz$9Ug%V$V^_zx!";

    public static final String[] COUNTRIES = new String[] {
        "AF", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR",
        "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY",
        "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BV", "BR",
        "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY",
        "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD",
        "CK", "CR", "CI", "HR", "CU", "CY", "CZ", "DK", "DJ", "DM",
        "DO", "TP", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK",
        "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE",
        "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GN",
        "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN",
        "ID", "IR", "IQ", "IE", "IL", "IT", "JM", "JP", "JO", "KZ",
        "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS",
        "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY",
        "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM",
        "MD", "MC", "MN", "MS", "MA", "MZ", "MM", "NA", "NR", "NP",
        "NL", "AN", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP",
        "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH",
        "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "SH",
        "KN", "LC", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "SC",
        "SL", "SG", "SK", "SI", "SB", "SO", "ZA", "GS", "ES", "LK",
        "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ",
        "TH", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV",
        "UG", "UA", "AE", "GB", "UK", "US", "UM", "UY", "UZ", "VU",
        "VE", "VN", "VG", "VI", "WF", "EH", "YE", "YU", "ZM", "ZW"
    };

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, File destinationFilePath) {
        return get(url, records, timeOut, null, destinationFilePath, null, null);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers, File destinationFilePath) {
        return get(url, records, timeOut, headers, destinationFilePath, null, null);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, File destinationFilePath, String username, String password) {
        return get(url, records, timeOut, null, destinationFilePath, username, password);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap headers) {
        return get(url, records, timeOut, headers, null, null, null);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers, String username, String password) {
        return get(url, records, timeOut, headers, null, username, password);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut) {
        return get(url, records, timeOut, null, null, null, null);
    }

    public static String get(String url, LinkedHashMap<String,String>  records, int timeOut, String username, String password) {
        return get(url, records, timeOut, null, null, username, password);
    }

    public static String get(String url, LinkedHashMap<String,String>  records, int timeOut, LinkedHashMap<String, String> headers, File destinationFilePath, String username, String password) {
        String response = "";
        try {
            if (records != null && !records.isEmpty()) {
                url = url.contains("?") ? url.trim() : (url.trim() + "?");
                for (Map.Entry<String, String>  entry : records.entrySet()) {
                    String key = ((String)entry.getKey()).contains("|") ? ((String)entry.getKey()).split(Pattern.quote("|"))[1] : (String)entry.getKey();
                    url = url + key + "=" + URLEncoder.encode((String)entry.getValue(), "UTF-8") + "&";
                }
                url = url.endsWith("&") ? url.trim().substring(0, url.length() - 1) : url.trim();
            }

            RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).setSocketTimeout(timeOut * 1000).build();
            HttpClientBuilder httpClient = HttpClientBuilder.create();

            if (username != null && password != null) {
                BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
                basicCredentialsProvider.setCredentials(AuthScope.ANY, (Credentials)new UsernamePasswordCredentials(username, password));
                httpClient = httpClient.setDefaultCredentialsProvider((CredentialsProvider)basicCredentialsProvider);
            }

            CloseableHttpClient closeableHttpClient = httpClient.setDefaultRequestConfig(reqConfig).build();
            HttpGet getRequest = new HttpGet(StringUtils.replace(StringUtils.replace(StringUtils.replace(url, "\n", ""), "\r", ""), " ", "%20").trim());
            getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");

            if (headers != null && !headers.isEmpty()){
                for (Map.Entry<String, String> entry : headers.entrySet()){
                    getRequest.setHeader((String)entry.getKey(), (String)entry.getValue());
                }
            }

            CloseableHttpResponse httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
            boolean redirect = false;
            int statusCode = httpRsponse.getStatusLine().getStatusCode();

            if (statusCode != 200 && (statusCode == 302 || statusCode == 301 || statusCode == 303)){
                redirect = true;
            }

            if (redirect == true) {
                Header[] arrayOfHeader = httpRsponse.getHeaders("Location");
                if (arrayOfHeader != null && arrayOfHeader.length > 0) {
                    getRequest = new HttpGet(arrayOfHeader[0].getValue());
                    getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
                    httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
                }
            }

            if (destinationFilePath != null) {
                try(BufferedInputStream bufInputStream = new BufferedInputStream(httpRsponse.getEntity().getContent()); BufferedOutputStream bufOutputStream  = new BufferedOutputStream(new FileOutputStream(destinationFilePath))) {
                    int inByte;
                    while ((inByte = bufInputStream.read()) != -1){
                        bufOutputStream.write(inByte);
                    }
                }
                response = httpRsponse.getEntity().getContentType().getValue();
            } else {
                response = EntityUtils.toString(httpRsponse.getEntity());
            }

        } catch (IOException io) {
            Loggers.error(io);
        }
        return response;
    }

    public static String post(String url, LinkedHashMap<String,String>  records, int timeOut) {
        String response = "";
        try {
            if (records != null && !records.isEmpty()) {
                url = url.contains("?") ? url.trim() : (url.trim() + "?");
                for (Map.Entry entry : records.entrySet()) {
                    String key = ((String)entry.getKey()).contains("|") ? ((String)entry.getKey()).split(Pattern.quote("|"))[1] : (String)entry.getKey();
                    url = url + key + "=" + URLEncoder.encode((String)entry.getValue(), "UTF-8") + "&";
                }
                url = url.endsWith("&") ? url.trim().substring(0, url.length() - 1) : url.trim();
            }

            RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).setSocketTimeout(timeOut * 1000).build();
            HttpClientBuilder httpClient = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClient.setDefaultRequestConfig(reqConfig).build();
            HttpGet getRequest = new HttpGet(StringUtils.replace(StringUtils.replace(StringUtils.replace(url, "\n", ""), "\r", ""), " ", "%20").trim());
            getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
            CloseableHttpResponse httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);

            boolean redirect = false;
            int statusCode = httpRsponse.getStatusLine().getStatusCode();
            if (statusCode != 200 && (statusCode == 302 || statusCode == 301 || statusCode == 303)){
                redirect = true;
            }

            if (redirect == true) {
                Header[] headers = httpRsponse.getHeaders("Location");
                if (headers != null && headers.length > 0) {
                    getRequest = new HttpGet(headers[0].getValue());
                    getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
                    httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
                }
            }
            response = EntityUtils.toString(httpRsponse.getEntity());
        } catch (IOException iOException) {}
        return response;
    }

    public static String put(String url, LinkedHashMap<String,String> records, int timeOut) {
        return get(url, records, timeOut, (LinkedHashMap)null, (File)null, (String)null);
    }

    public static String put(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers) {
        return get(url, records, timeOut, headers, (File)null, (String)null);
    }

    public static String post(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers) {
        return get(url, records, timeOut, headers, (File)null, (String)null);
    }

    public static String post(String url, LinkedHashMap<String,String> records, int timeOut, File destinationFilePath) {
        return get(url, records, timeOut, (LinkedHashMap)null, destinationFilePath, (String)null);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, String entityValue) {
        return get(url, records, timeOut, (LinkedHashMap)null, (File)null, entityValue);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers, String entityValue) {
        return get(url, records, timeOut, headers, (File)null, entityValue);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, File destinationFilePath, String entityValue) {
        return get(url, records, timeOut, null, destinationFilePath, entityValue);
    }

    public static String get(String url, LinkedHashMap<String,String> records, int timeOut, LinkedHashMap<String, String> headers, File destinationFilePath, String entityValue) {
        String response = "";
        try {
            RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).setSocketTimeout(timeOut * 1000).build();
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(reqConfig).build();
            HttpPost postRequest = new HttpPost(StringUtils.replace(StringUtils.replace(StringUtils.replace(url, "\n", ""), "\r", ""), " ", "%20").trim());

            if (entityValue != null) {
                postRequest.setEntity((HttpEntity)new StringEntity(entityValue));
            } else if (records != null && !records.isEmpty()) {
                ArrayList arr = new ArrayList(records.size());
                records.forEach((s1, s2) -> arr.add(new BasicNameValuePair(s1, s2)));
                postRequest.setEntity((HttpEntity)new UrlEncodedFormEntity(arr));
            }

            postRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");

            if (headers != null && !headers.isEmpty()){
                headers.entrySet().forEach(paramEntry -> postRequest.setHeader((String)paramEntry.getKey(), (String)paramEntry.getValue()));
            }
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute((HttpUriRequest)postRequest);

            boolean redirect = false;
            int status = closeableHttpResponse.getStatusLine().getStatusCode();

            if (status != 200 && (status == 302 || status == 301 || status == 303)){
                redirect = true;
            }

            if (redirect == true) {
                Header[] location = closeableHttpResponse.getHeaders("Location");
                if (location != null && location.length > 0) {
                    HttpGet getRequest = new HttpGet(location[0].getValue());
                    getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
                    closeableHttpResponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
                }
            }
            if (destinationFilePath != null) {
                try(BufferedInputStream bis = new BufferedInputStream(closeableHttpResponse.getEntity().getContent()); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFilePath))) {
                    int inByte;
                    while ((inByte = bis.read()) != -1){
                        bos.write(inByte);
                    }
                }
                response = closeableHttpResponse.getEntity().getContentType().getValue();
            } else {
                response = EntityUtils.toString(closeableHttpResponse.getEntity());
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return response;
    }

    public static String get(String link, int timeOut) {
        try {
            RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).setSocketTimeout(timeOut * 1000).build();
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(reqConfig).build();
            HttpGet getRequest = new HttpGet(StringUtils.replace(StringUtils.replace(StringUtils.replace(link, "\n", ""), "\r", ""), " ", "%20").trim());
            getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
            BasicHttpContext basicHttpContext = new BasicHttpContext();
            closeableHttpClient.execute((HttpUriRequest)getRequest, (HttpContext)basicHttpContext);
            HttpUriRequest httpUriRequest = (HttpUriRequest)basicHttpContext.getAttribute("http.request");
            HttpHost httpHost = (HttpHost)basicHttpContext.getAttribute("http.target_host");
            link = httpUriRequest.getURI().isAbsolute() ? httpUriRequest.getURI().toString() : (httpHost.toURI() + httpUriRequest.getURI());
        } catch (IOException io) {
            Loggers.error(io);
        }
        return link;
    }

    public static boolean post(String url, int timeOut) {
        boolean response = false;
        try {
            RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(timeOut * 1000).setConnectionRequestTimeout(timeOut * 1000).setSocketTimeout(timeOut * 1000).build();
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(reqConfig).build();
            HttpGet getRequest = new HttpGet(StringUtils.replace(StringUtils.replace(StringUtils.replace(url, "\n", ""), "\r", ""), " ", "%20").trim());
            getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
            CloseableHttpResponse httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
            boolean redirect = false;
            int status = httpRsponse.getStatusLine().getStatusCode();

            if (status != 200 && (status == 302 || status == 301 || status == 303)){
                redirect = true;
            }

            if (redirect == true) {
                Header[] location = httpRsponse.getHeaders("Location");
                if (location != null && location.length > 0) {
                    getRequest = new HttpGet(location[0].getValue());
                    getRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
                    httpRsponse = closeableHttpClient.execute((HttpUriRequest)getRequest);
                }
            }
            response = !httpRsponse.getEntity().getContentType().getValue().contains("text/html") ? true : false;
        } catch (IOException io) {
            Loggers.error(io);
        }
        return response;
    }

    public static String get(HttpEnumeration httpEnm, String url, LinkedHashMap<String,String> records, LinkedHashMap<String,String> parameters, String headers) {
        String curlType = "curl -i ";
        try {
            switch (HttpOrdinal.value[httpEnm.ordinal()]) {
                case 1:{
                    curlType = curlType + "-X POST ";
                    break;
                }
                case 2:{
                    curlType = curlType + "-X PUT ";
                    break;
                }
                case 3:{
                    curlType = curlType + "-X DELETE ";
                    break;
                }
            }

            if ((httpEnm == HttpEnumeration.POST || httpEnm == HttpEnumeration.PUT) && (parameters == null || parameters.isEmpty()) && (headers == null || "".equals(headers))){
                curlType = curlType + "--data \"\" ";
            }

            if (records != null && !records.isEmpty()){
                curlType = records.entrySet().stream().map(paramEntry -> "-H \"" + (String)paramEntry.getKey() + ":" + (String)paramEntry.getValue() + "\" ").reduce(curlType, String::concat);
            }

            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry entry : parameters.entrySet()){
                    curlType = curlType + "--data \"" + (String)entry.getKey() + "=" + URLEncoder.encode((String)entry.getValue(), "UTF-8") + "\" ";
                }
            } else if (headers != null && (headers.contains("{") || headers.contains("["))) {
                curlType = curlType + "--data '" + headers + "' ";
            }

            curlType = curlType + "\"" + url + "\"";
        } catch (Exception e) {
            Loggers.error(e);
            curlType = null;
        }
        return curlType;
    }

    public static CurLResponse get(String cmd) {
        CurLResponse curlRespse = null;
        try {
            String comands = Terminal.executeCommand(cmd);
            if (comands != null && !"".equals(comands)) {
                String[] responses = comands.split("\n");
                boolean bool = true;
                curlRespse = new CurLResponse();
                for (String str1 : responses) {
                    if (str1.startsWith("HTTP") && !str1.contains(":")) {
                        String[] statusValue = str1.split(" ");
                        if (statusValue.length >= 2){
                            curlRespse.setHttpStatus(TypesParser.safeParseInt(statusValue[1].trim()));
                        }
                    } else if (str1.length() == 0) {
                        bool = false;
                    } else if (bool && str1.contains(":")) {
                        String[] headers = str1.split(":");
                        if (headers.length == 2){
                            curlRespse.getHeaders().put(headers[0].trim(), headers[1].trim());
                        }
                    } else {
                        curlRespse.setBody(curlRespse.getBody() + str1);
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
        return curlRespse;
    }

    public static URL get(URL link) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)link.openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            httpURLConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            httpURLConnection.addRequestProperty("Referer", "https://www.google.com/");
            httpURLConnection.connect();
            int response = httpURLConnection.getResponseCode();
            if (response == 303 || response == 301 || response == 302) {
                String location = httpURLConnection.getHeaderField("Location");
                if (location.startsWith("/")){
                    location = link.getProtocol() + "://" + link.getHost() + location;
                }
                return get(new URL(location));
            }
        } catch (Exception exception) {}
        return link;
    }
}
