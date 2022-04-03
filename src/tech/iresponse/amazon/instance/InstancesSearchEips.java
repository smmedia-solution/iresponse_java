package tech.iresponse.amazon.instance;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import com.amazonaws.services.ec2.model.InstancePrivateIpAddress;
import java.beans.ConstructorProperties;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.amazon.Ec2Manager;
import tech.iresponse.utils.Terminal;
import tech.iresponse.utils.TypesParser;

public class InstancesSearchEips extends Thread {

    private Ec2Manager manager;
    private Instance instance;

    @Override
    public void run() {
        try {
            if (this.manager != null && this.instance != null && this.instance.getNetworkInterfaces() != null && !this.instance.getNetworkInterfaces().isEmpty()) {
                List<Address> adresses = this.manager.getAddresses();
                Address addrss = null;

                if (adresses != null && !adresses.isEmpty()){
                    for (int b = 0; b < adresses.size(); b++) {
                        addrss = adresses.get(b);
                        if (addrss != null && addrss.getInstanceId() != null && this.instance.getInstanceId().equals(addrss.getInstanceId())) {
                            this.manager.disassociateAddresses(addrss.getAssociationId());
                            this.manager.releaseAddresses(addrss.getAllocationId());
                        }
                    }
                }

                String str1 = System.getProperty("assets.path") + File.separator + "scripts" + File.separator + "dbl.sh";
                String str2 = null;

                List<InstancePrivateIpAddress> privateIpAddresses = ((InstanceNetworkInterface)this.instance.getNetworkInterfaces().get(0)).getPrivateIpAddresses();
                if (privateIpAddresses == null || privateIpAddresses.isEmpty()){
                    throw new DatabaseException("no private ips found !");
                }

                AllocateAddressResult result = null;
                for (InstancePrivateIpAddress instanceprivateIp : privateIpAddresses) {
                    boolean associated = false;
                    int b = 0;
                    while (!associated && b != 50) {
                        result = this.manager.allocateAddresses();
                        if (result != null) {
                            str2 = Terminal.executeCommand("sh " + str1 + " " + result.getPublicIp() + " | grep Blacklisted").replaceAll("\\s+", "");
                            int i = 0;
                            Matcher mtch = Pattern.compile("\\d+").matcher(str2);
                            while (mtch.find()){
                                i = TypesParser.safeParseInt(mtch.group());
                            }
                            if (i == 0) {
                                this.manager.associateAddresses(this.instance.getInstanceId(), instanceprivateIp.getPrivateIpAddress(), result.getAllocationId(), ((InstanceNetworkInterface)this.instance.getNetworkInterfaces().get(0)).getNetworkInterfaceId());
                                associated = true;
                                continue;
                            }
                            this.manager.releaseAddresses(result.getAllocationId());
                            b++;
                        }
                    }
                }
            }
        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    @ConstructorProperties({"manager", "instance"})
    public InstancesSearchEips(Ec2Manager manager, Instance instance) {
        this.manager = manager;
        this.instance = instance;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesSearchEips))
            return false;
        InstancesSearchEips new1 = (InstancesSearchEips)paramObject;
        if (!new1.exists(this))
            return false;
        Ec2Manager do1 = getManager();
        Ec2Manager do2 = new1.getManager();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        Instance instance1 = getInstance();
        Instance instance2 = new1.getInstance();
        return !((instance1 == null) ? (instance2 != null) : !instance1.equals(instance2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesSearchEips;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Ec2Manager do1 = getManager();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        Instance instance = getInstance();
        return n * 59 + ((instance == null) ? 43 : instance.hashCode());
    }

    public Ec2Manager getManager() {
        return manager;
    }

    public void setManager(Ec2Manager manager) {
        this.manager = manager;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "InstancesSearchEips(manager=" + getManager() + ", instance=" + getInstance() + ")";
    }
}
