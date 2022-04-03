package tech.iresponse.affiliate.workers;

import java.beans.ConstructorProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.Affiliate;
import tech.iresponse.models.lists.DataList;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.affiliate.affiliate.UpdateData;
import tech.iresponse.models.affiliate.Suppression;
import tech.iresponse.models.affiliate.Offer;
import tech.iresponse.models.lists.SuppressionEmail;
import tech.iresponse.utils.Strings;

public class SuppressionUpdater extends Thread {
    private Offer offer;
    private int dataListsSize;
    private Suppression process;
    private DataList list;
    private String suppressionFolder;

    public void run() {
        try {
            if (this.list == null || this.list.getEmpty()){
                throw new DatabaseException("No list found for this proccess !");
            }
            String tableSuppList = "sup_list_" + this.offer.affiliateNetworkId + "_" + this.offer.productionId + "_" + this.list.id;
            SuppressionEmail supmail = new SuppressionEmail();
            supmail.setTable(tableSuppList);
            if (!Database.get("clients").existeTable("suppressions", tableSuppList)){
                supmail.sync();
            }
            int i = 0;
            if (Database.get("clients").existeTable(this.list.tableSchema, this.list.tableName)) {
                String query = "SELECT email_md5 FROM " + this.list.tableSchema + "." + this.list.tableName + " t WHERE (is_seed = 'f' OR is_seed IS NULL) AND (is_hard_bounced = 'f' OR is_hard_bounced IS NULL) AND (is_blacklisted = 'f' OR is_blacklisted IS NULL) ORDER BY email_md5 ASC";
                HashSet hashSet = Database.get("clients").executeQuery(query, null, 1, "email_md5");
                if (hashSet != null && !hashSet.isEmpty()){
                    hashSet.retainAll(Affiliate.SuppEmailList);
                    if (!hashSet.isEmpty()){
                        i = hashSet.size();
                        String newDataSupp = this.suppressionFolder + File.separator + Strings.rndomSalt(15, false) + ".txt";
                        Database.get("clients").executeUpdate("DELETE FROM suppressions." + tableSuppList, null, 0);
                        int b = 1;
                        try (FileWriter fw = new FileWriter(newDataSupp, true); BufferedWriter bw = new BufferedWriter(fw)) {
                            bw.write("id;email_md5\n");
                            Iterator<String> it = hashSet.iterator();
                            while (it.hasNext()) {
                                bw.write(b + ";" + it.next() + "\n");
                                b++;
                            }
                        }
                        Database.get("clients").executeUpdate("COPY " + supmail.getSchema() + "." + supmail.getTable() + " FROM '" + newDataSupp + "' WITH CSV HEADER DELIMITER AS ';' NULL AS ''", null, 0);
                        Database.get("clients").executeQuery("SELECT setval('" + supmail.getSchema() + ".seq_id_" + supmail.getTable() + "', (SELECT MAX(id) FROM " + supmail.getSchema() + "." + supmail.getTable() + "));", null, 1);
                    }
                }
            }
            UpdateData.updateCounter(this.process, this.dataListsSize, i);
        } catch (Exception exception) {
            Affiliate.IS_ERROR_OCCURED = true;
            Loggers.error(exception);
        }
    }

    @ConstructorProperties({"offer", "dataListsSize", "process", "list", "suppressionFolder"})
    public SuppressionUpdater(Offer offer, int dataListsSize, Suppression process, DataList list, String suppressionFolder) {
        this.offer = offer;
        this.dataListsSize = dataListsSize;
        this.process = process;
        this.list = list;
        this.suppressionFolder = suppressionFolder;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SuppressionUpdater))
            return false;
        SuppressionUpdater byte1 = (SuppressionUpdater)paramObject;
        if (!byte1.exists(this))
            return false;
        Offer new1 = getOffer();
        Offer new2 = byte1.getOffer();
        if ((new1 == null) ? (new2 != null) : !new1.equals(new2))
            return false;
        if (getDataListsSize() != byte1.getDataListsSize())
            return false;
        Suppression byte2 = getProcess();
        Suppression byte3 = byte1.getProcess();
        if ((byte2 == null) ? (byte3 != null) : !byte2.equals(byte3))
            return false;
        DataList for1 = getList();
        DataList for2 = byte1.getList();
        if ((for1 == null) ? (for2 != null) : !for1.equals(for2))
            return false;
        String str1 = getSuppressionFolder();
        String str2 = byte1.getSuppressionFolder();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object instance) {
        return instance instanceof SuppressionUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Offer new1 = getOffer();
        n = n * 59 + ((new1 == null) ? 43 : new1.hashCode());
        n = n * 59 + getDataListsSize();
        Suppression byte1 = getProcess();
        n = n * 59 + ((byte1 == null) ? 43 : byte1.hashCode());
        DataList for1 = getList();
        n = n * 59 + ((for1 == null) ? 43 : for1.hashCode());
        String str = getSuppressionFolder();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public Offer getOffer() {
        return this.offer;
    }

    public int getDataListsSize() {
        return this.dataListsSize;
    }

    public Suppression getProcess() {
        return this.process;
    }

    public DataList getList() {
        return this.list;
    }

    public String getSuppressionFolder() {
        return this.suppressionFolder;
    }

    public void setOffe(Offer offer) {
        this.offer = offer;
    }

    public void setDataListsSize(int dataListsSize) {
        this.dataListsSize = dataListsSize;
    }

    public void setProcess(Suppression process) {
        this.process = process;
    }

    public void setList(DataList list) {
        this.list = list;
    }

    public void setSuppressionFolder(String suppressionFolder) {
        this.suppressionFolder = suppressionFolder;
    }

    @Override
    public String toString() {
        return "SuppressionUpdater(offer=" + getOffer() + ", dataListsSize=" + getDataListsSize() + ", list=" + getList() + ", suppressionFolder=" + getSuppressionFolder() + ")";
    }
}
