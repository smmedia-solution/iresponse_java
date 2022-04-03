package tech.iresponse.utils;

import com.google.common.net.InternetDomainName;

public class Url {

    public static String checkUrl(String url) {
        try {
            String str = url;
            int index = str.indexOf("://");
            if (index != -1){
                str = str.substring(index + 3);
            }
            index = str.indexOf('/');
            if (index != -1){
                str = str.substring(0, index);
            }
            return InternetDomainName.from(str.replaceFirst("^www.*?\\.", "")).topPrivateDomain().toString();
        } catch (Exception exception) {
            return url;
        }
    }
}
