package tech.iresponse.azure.instance;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.azure.AzureManager;

public class InstancesAutoRestarts extends Thread {

    private AzureAccount account;
    private AzureInstance instance;
    private boolean changeIps;

    @Override
    public void run() {
        try {
            if (this.instance.getEmpty()){
                throw new DatabaseException("Instance with id " + this.instance.getId() + " not found !");
            }

            if (this.account.getEmpty()){
                throw new DatabaseException("Account with id " + this.account.getId() + " not found !");
            }

            AzureManager azureMng = new AzureManager(this.account);
            if (azureMng.authenticate()) {
                azureMng.executeActions(this.instance.getName() + "_res_gr", this.instance.getName(), "restart", this.changeIps);
                this.instance.status = "Running";
                this.instance.update();
            }

        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    @ConstructorProperties({"account", "instance", "changeIps"})
    public InstancesAutoRestarts(AzureAccount account, AzureInstance instance, boolean changeIps) {
        this.account = account;
        this.instance = instance;
        this.changeIps = changeIps;
    }

    public AzureAccount getAccount() {
        return account;
    }

    public void setAccount(AzureAccount account) {
        this.account = account;
    }

    public AzureInstance getInstance() {
        return instance;
    }

    public void setInstance(AzureInstance instance) {
        this.instance = instance;
    }

    public boolean isChangeIps() {
        return changeIps;
    }

    public void setChangeIps(boolean changeIps) {
        this.changeIps = changeIps;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesAutoRestarts))
            return false;
        InstancesAutoRestarts int1 = (InstancesAutoRestarts)paramObject;
        if (!int1.exists(this))
            return false;
        AzureAccount char1 = getAccount();
        AzureAccount char2 = int1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        AzureInstance goto1 = getInstance();
        AzureInstance goto2 = int1.getInstance();
        return ((goto1 == null) ? (goto2 != null) : !goto1.equals(goto2)) ? false : (!(isChangeIps() != int1.isChangeIps()));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesAutoRestarts;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        AzureInstance goto1 = getInstance();
        n = n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
        return n * 59 + (isChangeIps() ? 79 : 97);
    }

    @Override
    public String toString() {
        return "InstancesAutoRestarts(account=" + getAccount() + ", instance=" + getInstance() + ", changeIps=" + isChangeIps() + ")";
    }
}
