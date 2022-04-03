package tech.iresponse.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypesParser {

    public static int safeParseInt(Object paramObject) {
        try {
            return Integer.parseInt(String.valueOf(paramObject));
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public static int safeMatcher(String paramString) {
        try {
            Matcher matcher = Pattern.compile("\\d+").matcher(paramString);
            for (paramString = ""; matcher.find(); paramString = paramString + matcher.group());
            return safeParseInt(paramString);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public static long safeParseLong(Object paramObject) {
        try {
            return Long.parseLong(String.valueOf(paramObject));
        } catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    public static double safeParseDouble(Object paramObject) {
        try {
            return Double.parseDouble(String.valueOf(paramObject));
        } catch (NumberFormatException numberFormatException) {
            return 0.0D;
        }
    }

    public static float safeParseFloat(Object paramObject) {
        try {
            return Float.parseFloat(String.valueOf(paramObject));
        } catch (NumberFormatException numberFormatException) {
            return 0.0F;
        }
    }
}
