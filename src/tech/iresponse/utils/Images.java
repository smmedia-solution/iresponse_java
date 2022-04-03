package tech.iresponse.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.core.Application;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class Images {

    public static boolean isImage(String paramString) {
        return Arrays.asList(new String[] { "jpg", "jpeg", "png", "gif", "bmp" }).contains(paramString.toLowerCase());
    }

    public static ManagementServer getMngemetServer() {
        try {
            if (TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id")) > 0) {
                ManagementServer mngServ = new ManagementServer(TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id")));
                if (!mngServ.getEmpty()){
                    return mngServ;
                }
            }
        } catch (Exception exception) {}
        return null;
    }

    public static String getBaseUrl(ManagementServer mngServ) {
        return (mngServ != null && !mngServ.getEmpty()) ? ("http://" + ("".equals(mngServ.hostName) ? mngServ.mainIp : mngServ.hostName)) : String.valueOf(Application.getSettingsParam("base_url"));
    }

    public static void uploadImages(ManagementServer uploadCenter, File[] images) {
        if (uploadCenter != null && !uploadCenter.getEmpty()) {
            SSHConnector ssh = null;
            try {
                ssh = Authentification.connectToServer(uploadCenter);
                if (ssh != null && ssh.isConnected()) {
                    ssh.cmd("mkdir -p /var/www/html/media/");
                    for (File image : images) {
                        if (image.exists()){
                            ssh.upload(image.getCanonicalPath(), "/var/www/html/media/" + image.getName());
                        }
                    }
                } else {
                    throw new DatabaseException("Could not connect to upload center !");
                }
            } catch (IOException|DatabaseException iOException) {
                Loggers.error(iOException);
            } finally {
                if (ssh != null && ssh.isConnected()){
                    ssh.disconnect();
                }
            }
        }
    }
}
