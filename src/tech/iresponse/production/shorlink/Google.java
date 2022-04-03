package tech.iresponse.production.shorlink;

import java.io.File;
import java.util.regex.Pattern;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.api.GoogleCould;
import tech.iresponse.http.Agents;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.core.Application;

public class Google {

    public static String shortGoogle(String url) throws Exception {
        try {
            int bucketSize = 0;
            int objectSize = 0;
            if (Application.checkAndgetInstance().getSettings().has("gcloud_bucket_size")){
                bucketSize = TypesParser.safeParseInt(Application.checkAndgetInstance().getSettings().getString("gcloud_bucket_size"));
            }
            if (Application.checkAndgetInstance().getSettings().has("gcloud_object_size")){
                objectSize = TypesParser.safeParseInt(Application.checkAndgetInstance().getSettings().getString("gcloud_object_size"));
            }
            GoogleCould googlCloud = new GoogleCould();
            if (!googlCloud.authenticate()){
                throw new DatabaseException("Could not authenticate to google cloud service !");
            }
            if (!url.contains("http")) {
                url = "http://" + url;
            }
            bucketSize = (bucketSize > 0) ? bucketSize : 15;
            objectSize = (objectSize > 0) ? objectSize : 15;
            String rndmBucket = Strings.rndomSalt(bucketSize, false);
            googlCloud.createBucketInfo(rndmBucket);
            String rndmObject = Strings.rndomSalt(objectSize, false);
            url = googlCloud.createUrlStorage(rndmBucket, rndmObject, url);
        } catch (Exception e) {
            throw new DatabaseException("Could not shorten link !", e);
        }
        return url;
    }

    public static String shortUrlG(String url) throws Exception {
        try {
            int bucketSize = 0;
            int objectSize = 0;
            if (Application.checkAndgetInstance().getSettings().has("gcloud_bucket_size")){
                bucketSize = TypesParser.safeParseInt(Application.checkAndgetInstance().getSettings().getString("gcloud_bucket_size"));
            }
            if (Application.checkAndgetInstance().getSettings().has("gcloud_object_size")){
                objectSize = TypesParser.safeParseInt(Application.checkAndgetInstance().getSettings().getString("gcloud_object_size"));
            }
            GoogleCould googlCloud = new GoogleCould();
            if (!googlCloud.authenticate()){
                throw new DatabaseException("Could not authenticate to google cloud service !");
            }
            if (!url.contains("http")){
                url = "http://" + url;
            }
            bucketSize = (bucketSize > 0) ? bucketSize : 15;
            objectSize = (objectSize > 0) ? objectSize : 15;
            String rndmBucket = Strings.rndomSalt(bucketSize, false);
            googlCloud.createBucketInfo(rndmBucket);
            String rndmObject = Strings.rndomSalt(objectSize, false);
            String str3 = System.getProperty("trash.path") + File.separator + Strings.rndomSalt(20, false);
            File file = new File(str3);
            String rsp = Agents.get(url, null, 30, file);
            url = googlCloud.createUrlStorage(rndmBucket, rndmObject, file, rsp.replaceAll(Pattern.quote("image/"), ""));
            file.delete();
        } catch (Exception e) {
            throw new DatabaseException("Could not shorten link !", e);
        }
        return url;
    }

}
