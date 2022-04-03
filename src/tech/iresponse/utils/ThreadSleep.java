package tech.iresponse.utils;

import java.util.Random;

public class ThreadSleep {

    public static final long LONG500 = 500L;

    public static final long LONG1000 = 1000L;

    public static final long LONG2000 = 2000L;

    public static final long LONG3000 = 3000L;

    public static final long LONG4000 = 4000L;

    public static final long LONG5000 = 5000L;

    public static final long LONG6000 = 6000L;

    public static final long LONG7000 = 7000L;

    public static final long LONG8000 = 8000L;

    public static final long LONG9000 = 9000L;

    public static final long LONG10000 = 10000L;

    public static final long LONG20000 = 20000L;

    public static final long LONG30000 = 30000L;

    public static final long LONG60000 = 60000L;

    public static final long LONG120000 = 120000L;

    public static final long LONG180000 = 180000L;

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {}
    }

    public static void sleep(int first, int next) {
        sleep(TypesParser.safeParseLong(Integer.valueOf((first == next) ? (first * 1000) : (((new Random()).nextInt(next - first + 1) + first) * 1000))));
    }
}
