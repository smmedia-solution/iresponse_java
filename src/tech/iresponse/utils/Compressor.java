package tech.iresponse.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import tech.iresponse.logging.Loggers;

public class Compressor {

    public static boolean zip(String zipFilePath, File[] files) throws Exception{
        boolean created = false;
        FileOutputStream fileOut = null;
        ZipOutputStream zipOutput = null;
        if (files != null && files.length > 0)
        try {
            fileOut = new FileOutputStream(zipFilePath);
            zipOutput = new ZipOutputStream(fileOut);
            for (File fileToZip : files) {
                try (FileInputStream fileInput = new FileInputStream(fileToZip)) {
                    zipOutput.putNextEntry(new ZipEntry(fileToZip.getName()));
                    byte[] arrOfByte = new byte[1024];
                    int length;
                    while ((length = fileInput.read(arrOfByte)) >= 0){
                        zipOutput.write(arrOfByte, 0, length);
                    }
                }
            }
            created = true;
        } catch (IOException iOException) {
                Loggers.error(iOException);
        } finally {
            if (zipOutput != null){
                zipOutput.close();
            }
            if (fileOut != null){
                fileOut.close();
            }
        }
        return created;
    }

    public static void unzip(String zipFilePath, String destDirectory) throws Exception {
        File destDir = new File(destDirectory);
        if (!destDir.exists()){
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws Exception{
        try (BufferedOutputStream bfs = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1){
                bfs.write(bytesIn, 0, read);
            }
        }
    }
}
