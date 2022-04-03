package tech.iresponse.api;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;

public class GoogleCould {

    private Storage storage;
    private JSONObject project;
    private static final String urlAuth = "https://www.googleapis.com/auth/cloud-platform";
    private static final String urlStorage = "https://storage.googleapis.com";

    public boolean authenticate() {
        try {
            String str = System.getProperty("configs.path") + File.separator + "gcloud.crd.json";
            this.project = new JSONObject(FileUtils.readFileToString(new File(str), "UTF-8"));
            this.storage = (Storage)((StorageOptions.Builder)StorageOptions.newBuilder().setCredentials((Credentials)GoogleCredentials.fromStream(new FileInputStream(str)).createScoped(Lists.newArrayList(new String[] { "https://www.googleapis.com/auth/cloud-platform" })))).build().getService();
        } catch (IOException|JSONException e) {
            Loggers.error(e);
            return false;
        }
        return true;
    }

    public boolean createBucketInfo(String paramString) {
        return (this.storage.create(BucketInfo.of(paramString), new Storage.BucketTargetOption[0]) != null);
    }

    public boolean deleteStorage(String paramString) {
        try {
            Iterable<Blob> iterable = this.storage.list(paramString, new Storage.BlobListOption[] { Storage.BlobListOption.prefix("") }).iterateAll();
            for (Blob blob : iterable) {
                blob.delete(new Blob.BlobSourceOption[] { Blob.BlobSourceOption.generationMatch() });
            }
            return this.storage.delete(paramString, new Storage.BucketSourceOption[] { Storage.BucketSourceOption.userProject(this.project.getString("project_id")) });
        } catch (JSONException e) {
            Loggers.error((Throwable)e);
            return false;
        }
    }

    public List addBucketList() {
        ArrayList<Bucket> arrayList = new ArrayList();
        Page<Bucket> page = this.storage.list(new Storage.BucketListOption[0]);
        for (Bucket bucket : page.iterateAll()){
            arrayList.add(bucket);
        }
        return arrayList;
    }

    public String createUrlStorage(String rndmBucket, String rndmObject, String url) {
        try {
            String str = "<script>window.location.href = '" + url + "#' + window.location.href.split('#')[1];</script>";
            this.storage.create(BlobInfo.newBuilder(BlobId.of(rndmBucket, rndmObject)).setContentType("text/html").build(), str.getBytes("UTF-8"), new Storage.BlobTargetOption[0]);
            this.storage.createAcl(BlobId.of(rndmBucket, rndmObject), Acl.of((Acl.Entity)Acl.User.ofAllUsers(), Acl.Role.READER));
        } catch (UnsupportedEncodingException uns) {
            Loggers.error(uns);
        return "";
        }
        return "https://storage.googleapis.com/" + rndmBucket + "/" + rndmObject;
    }

    public String createUrlStorage(String rndmBucket, String rndmObject, File paramFile, String paramString3) {
        try {
            this.storage.create(BlobInfo.newBuilder(BlobId.of(rndmBucket, rndmObject)).setContentType("image/" + paramString3).build(), FileUtils.readFileToByteArray(paramFile), new Storage.BlobTargetOption[0]);
            this.storage.createAcl(BlobId.of(rndmBucket, rndmObject), Acl.of((Acl.Entity)Acl.User.ofAllUsers(), Acl.Role.READER));
        } catch (IOException io) {
            Loggers.error(io);
        return "";
        }
        return "https://storage.googleapis.com/" + rndmBucket + "/" + rndmObject;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof GoogleCould))
            return false;
        GoogleCould do1 = (GoogleCould)paramObject;
        if (!do1.exists(this))
            return false;
        Storage storage1 = getStorage();
        Storage storage2 = do1.getStorage();
        if ((storage1 == null) ? (storage2 != null) : !storage1.equals(storage2))
            return false;
        JSONObject jSONObject1 = getProject();
        JSONObject jSONObject2 = do1.getProject();
        return !((jSONObject1 == null) ? (jSONObject2 != null) : !jSONObject1.equals(jSONObject2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof GoogleCould;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Storage storage = getStorage();
        n = n * 59 + ((storage == null) ? 43 : storage.hashCode());
        JSONObject jSONObject = getProject();
        return n * 59 + ((jSONObject == null) ? 43 : jSONObject.hashCode());
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public JSONObject getProject() {
        return project;
    }

    public void setProject(JSONObject project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "GoogleCould(storage=" + getStorage() + ", project=" + getProject() + ")";
    }
}
