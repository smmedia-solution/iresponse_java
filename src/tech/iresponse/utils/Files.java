package tech.iresponse.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class Files {

    public static List listAllFilesNames(File folder, boolean recurcive) {
        ArrayList<String> files = new ArrayList();
        for (File file : folder.listFiles()) {
            if (file.isDirectory() && recurcive == true) {
                files.addAll(listAllFilesNames(file, recurcive));
            } else {
                files.add(file.getName());
            }
        }
        return files;
    }

    public static List listAllFiles(File folder, boolean recurcive) {
        ArrayList<File> files = new ArrayList();
        for (File file : folder.listFiles()) {
            if (file.isDirectory() && recurcive == true) {
                files.addAll(listAllFiles(file, recurcive));
            } else {
                files.add(file);
            }
        }
        return files;
    }

    public static List listAllFiles(File folder) {
        ArrayList<File> files = new ArrayList();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                files.add(file);
            }
        }
        return files;
    }

    public static String getMachine(long paramLong) {
        long l1 = (paramLong == Long.MIN_VALUE) ? Long.MAX_VALUE : Math.abs(paramLong);
        if (l1 < 1024L){
            return paramLong + " B";
        }

        long l2 = l1;
        StringCharacterIterator it = new StringCharacterIterator("KMGTPE");

        for (int b = 40; b >= 0 && l1 > 1152865209611504844L >> b; b -= 10) {
            l2 >>= 10L;
            it.next();
        }
        l2 *= Long.signum(paramLong);
        return String.format("%.1f %cB", new Object[] { Double.valueOf(l2 / 1024.0D), Character.valueOf(it.current()) });
    }

    public static boolean read(File file) throws Exception {
        int i;
        if (file.isDirectory()){
            return false;
        }
        if (!file.canRead()){
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
        if (file.length() < 4L){
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            i = dis.readInt();
        }
        return (i == 1347093252);
    }
}