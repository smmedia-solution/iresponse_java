package tech.iresponse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import tech.iresponse.logging.Loggers;

public class Terminal {

    public static String executeCommand(String cmd) {
        StringBuilder output = new StringBuilder();
        Process proc = null;
        BufferedReader br = null;
        try {
            proc = (new ProcessBuilder(new String[] { "/bin/bash", "-c", cmd })).start();
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null){
                output.append(line).append("\n");
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return output.toString();
    }

    public static void killProcess(String process) {
        String path = System.getProperty("assets.path") + File.separator + "scripts" + File.separator + "kill_process.sh";
        if (process != null && process.contains("_")) {
            String[] p = process.split("_");
            for (String str1 : p){
                executeCommand("sh " + path + " " + str1);
            }
        } else {
            executeCommand("sh " + path + " " + process);
        }
    }
}