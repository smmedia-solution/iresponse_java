package tech.iresponse.data.scripts;

import java.beans.ConstructorProperties;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.models.lists.BlackListEmail;
import tech.iresponse.models.lists.BlackList;
import tech.iresponse.orm.Database;
import tech.iresponse.logging.Loggers;
import tech.iresponse.data.update.UpdateData;

public class BlackListManager extends Thread {

    private DataList dataList;
    private BlackList process;
    private BlackListEmail blacklistEmail;
    private int size;

    public void run() {
        try {
            if (this.dataList.id > 0) {
                String query = "UPDATE " + this.dataList.tableSchema + "." + this.dataList.tableName + " t SET is_blacklisted = 't' WHERE EXISTS (SELECT 1 FROM " + this.blacklistEmail.getSchema() + "." + this.blacklistEmail.getTable() + " b WHERE t.email_md5 = b.email_md5)";
                int nbEmailFound = Database.get("clients").executeUpdate(query, null, 0);
                UpdateData.updateCounter(this.process, this.size, nbEmailFound);
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"dataList", "process", "blacklistEmail", "size"})
    public BlackListManager(DataList dataList, BlackList process, BlackListEmail blacklistEmail, int size) {
        this.dataList = dataList;
        this.process = process;
        this.blacklistEmail = blacklistEmail;
        this.size = size;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof BlackListManager))
            return false;
        BlackListManager do1 = (BlackListManager)paramObject;
        if (!do1.exists(this))
            return false;
        DataList for1 = getDataList();
        DataList for2 = do1.getDataList();
        if ((for1 == null) ? (for2 != null) : !for1.equals(for2))
            return false;
        BlackList do2 = getProcess();
        BlackList do3 = do1.getProcess();
        if ((do2 == null) ? (do3 != null) : !do2.equals(do3))
            return false;
        BlackListEmail if1 = getBlacklistEmail();
        BlackListEmail if2 = do1.getBlacklistEmail();
        return ((if1 == null) ? (if2 != null) : !if1.equals(if2)) ? false : (!(getSize() != do1.getSize()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof BlackListManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        DataList for1 = getDataList();
        n = n * 59 + ((for1 == null) ? 43 : for1.hashCode());
        BlackList do1 = getProcess();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        BlackListEmail if1 = getBlacklistEmail();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        return n * 59 + getSize();
    }

    public DataList getDataList() {
        return dataList;
    }

    public void setDataList(DataList dataList) {
        this.dataList = dataList;
    }

    public BlackList getProcess() {
        return process;
    }

    public void setProcess(BlackList process) {
        this.process = process;
    }

    public BlackListEmail getBlacklistEmail() {
        return blacklistEmail;
    }

    public void setBlacklistEmail(BlackListEmail blacklistEmail) {
        this.blacklistEmail = blacklistEmail;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "BlackListManager(dataList=" + getDataList() + ", process=" + getProcess() + ", blacklistEmail=" + getBlacklistEmail() + ", size=" + getSize() + ")";
    }
}
