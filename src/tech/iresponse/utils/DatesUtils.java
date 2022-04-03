package tech.iresponse.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import tech.iresponse.logging.Loggers;

public class DatesUtils {

    public static Date substractHours(String date, int hours) {
        try {
            return new Date((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(date).getTime() - (3600000 * hours));
        } catch (ParseException pe) {
            Loggers.error(pe);
            return null;
        }
    }

    public static Date addHours(String date, int hours) {
        try {
            return new Date((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(date).getTime() + (3600000 * hours));
        } catch (ParseException pe) {
            Loggers.error(pe);
            return null;
        }
    }

    public static Date toDate(String date, String format) {
        try {
            return (new SimpleDateFormat(format)).parse(date);
        } catch (ParseException pe) {
            Loggers.error(pe);
            return null;
        }
    }

    public static String format(Date date, String format) {
        try {
            return (new SimpleDateFormat(format)).format(date);
        } catch (Exception e) {
            Loggers.error(e);
            return null;
        }
    }
}
