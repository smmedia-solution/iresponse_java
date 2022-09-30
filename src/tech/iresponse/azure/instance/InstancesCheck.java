package tech.iresponse.azure.instance;

import com.microsoft.azure.management.compute.VirtualMachine;
import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.models.admin.AzureInstance;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.azure.AzureManager;

public class InstancesCheck extends Thread {

    private AzureAccount account;
    private AzureInstance instance;

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
            if (!azureMng.authenticate()){
                throw new DatabaseException("Could not connect to azure account " + this.account.name + " !");
            }

            VirtualMachine vm = azureMng.getVirtualMachines(this.instance.getName() + "_res_gr", this.instance.getName());

            if ("PowerState/stopped".equalsIgnoreCase(vm.powerState().toString())){
                azureMng.executeActions(this.instance.getName() + "_res_gr", this.instance.getName(), "start", false);
            }

            this.instance.status = "Running";
            this.instance.update();

        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    @ConstructorProperties({"account", "instance"})
    public InstancesCheck(AzureAccount account, AzureInstance instance) {
        this.account = account;
        this.instance = instance;
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

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesCheck))
            return false;
        InstancesCheck try1 = (InstancesCheck)paramObject;
        if (!try1.exists(this))
            return false;
        AzureAccount char1 = getAccount();
        AzureAccount char2 = try1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        AzureInstance goto1 = getInstance();
        AzureInstance goto2 = try1.getInstance();
        return !((goto1 == null) ? (goto2 != null) : !goto1.equals(goto2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesCheck;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        AzureInstance goto1 = getInstance();
        return n * 59 + ((goto1 == null) ? 43 : goto1.hashCode());
    }

    @Override
    public String toString() {
        return "InstancesCheck(account=" + getAccount() + ", instance=" + getInstance() + ")";
    }
}
