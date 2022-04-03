package tech.iresponse.utils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Matcher {

    public static final Pattern pat1 = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", 2);
    public static final Pattern pat2 = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)", 2);
    public static final String h1serial = "WAJ5sTwdb6Q1tkjgjOuNWS2L5SJWd5+OGnuRcwQmYF2AVcXE3aXjNxFSWweuSY7c";


    public static boolean pat1(String str) {
        return pat1.matcher(str).find();
    }

    public static boolean pat2(String str) {
        return pat2.matcher(str).find();
    }

    public static boolean adress(String subntfiltr, String publicIp) {
        String[] split = subntfiltr.split("/");
        String str = split[0];
        int b = (split.length < 2) ? 0 : Integer.parseInt(split[1]);
        Inet4Address inet4Address1 = null;
        Inet4Address inet4Address2 = null;
        try {
            inet4Address1 = (Inet4Address)Inet4Address.getByName(str);
            inet4Address2 = (Inet4Address)Inet4Address.getByName(publicIp);
        } catch (UnknownHostException unknownHostException) {}

        if (inet4Address1 == null){
            return false;
        }
        if (inet4Address2 == null){
            return false;
        }
        byte[] address = inet4Address1.getAddress();
        byte[] address2 = inet4Address2.getAddress();
        int i = (address[0] & 0xFF) << 24 | (address[1] & 0xFF) << 16 | (address[2] & 0xFF) << 8;
        int j = (address2[0] & 0xFF) << 24 | (address2[1] & 0xFF) << 16 | (address2[2] & 0xFF) << 8;
        int k = (1 << 32 - b) - 1 ^ 0xFFFFFFFF;
        return ((i & k) == (j & k));
    }
}
