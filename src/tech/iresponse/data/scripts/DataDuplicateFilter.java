package tech.iresponse.data.scripts;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.DataLists;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.Convertion;

public class DataDuplicateFilter extends Thread {

    private DataList dataList;

    public void run() {
        try {
            if (this.dataList.id > 0) {
                String email = "md5".equalsIgnoreCase(this.dataList.encryptEmails) ? "email_md5" : "email";
                List<String> list = Database.get("clients").availableTables("SELECT " + email + " FROM " + this.dataList.tableSchema + "." + this.dataList.tableName, null, 1, email);

                if (!list.isEmpty() && "enabled".equalsIgnoreCase(this.dataList.encryptEmails)){
                    for (int b = 0; b < list.size(); b++){
                        list.set(b, Convertion.decrypt(list.get(b)));
                    }
                }
                Collections.sort(list);
                HashSet<String> hashSet = new HashSet<>(list);
                DataLists.filtersMd5(hashSet, "md5".equalsIgnoreCase(this.dataList.encryptEmails));
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"dataList"})
    public DataDuplicateFilter(DataList dataList) {
        this.dataList = dataList;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof DataDuplicateFilter))
            return false;
        DataDuplicateFilter if1 = (DataDuplicateFilter)paramObject;
        if (!if1.exists(this))
            return false;
        DataList for1 = getDataList();
        DataList for2 = if1.getDataList();
        return !((for1 == null) ? (for2 != null) : !for1.equals(for2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof DataDuplicateFilter;
    }

    @Override
    public int hashCode() {
        int n = 1;
        DataList for1 = getDataList();
        return n * 59 + ((for1 == null) ? 43 : for1.hashCode());
    }

    public DataList getDataList() {
        return dataList;
    }

    public void setDataList(DataList dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "DataDuplicateFilter(dataList=" + getDataList() + ")";
    }
}
