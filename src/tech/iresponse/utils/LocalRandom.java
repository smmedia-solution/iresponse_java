package tech.iresponse.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LocalRandom {

    public static String[] random(String[] paramArrayOfString) {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (int i = paramArrayOfString.length - 1; i > 0; i--) {
            int j = threadLocalRandom.nextInt(i + 1);
            String str = paramArrayOfString[j];
            paramArrayOfString[j] = paramArrayOfString[i];
            paramArrayOfString[i] = str;
        }
        return paramArrayOfString;
    }

    public static void shuffleArray(String[] ar)
    {
        Random rnd = ThreadLocalRandom.current();

        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
