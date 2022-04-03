package tech.iresponse.utils;

import java.util.Random;

public class Strings {

    public static final String key = "UCt5UVF3TmJYRmRVM0l4VWZKTk9ENERFcG52RURwNnh6L1RGWk9lcGNzOGVBdGJlVVV4aFBBUWFGenE0S3E5aQ==";

    public static String getSaltString(int length, boolean letters, boolean uppercase, boolean numbers, boolean specialCharacters) {
        String SALTCHARS = "";
        if (letters == true) {
            SALTCHARS = SALTCHARS + "abcdefghijklmnopqrstuvwxyz";
            if (uppercase){
                SALTCHARS = SALTCHARS + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            }
        }
        if (numbers){
            SALTCHARS = SALTCHARS + "1234567890";
        }
        if (specialCharacters){
            SALTCHARS = SALTCHARS + "@\\\\/_*$&-#[](){}";
        }
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int)(rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static String rndomSalt(int paramInt, boolean paramBoolean) {
        String str = "";
        if (paramBoolean) {
            str = str + "ABCDEF";
        } else {
            str = str + "abcdef";
        }
        str = str + "1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        while (sb.length() < paramInt) {
            int index = (int)(random.nextFloat() * str.length());
            sb.append(str.charAt(index));
        }
        return sb.toString();
    }

    public static String randomizeCase(String paramString) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(paramString.length());
        for (char c : paramString.toCharArray()){
            sb.append(rnd.nextBoolean() ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return sb.toString();
    }

    public static boolean isEmpty(String paramString) {
        return (paramString == null || "".equals(paramString) || "null".equalsIgnoreCase(paramString));
    }
}
