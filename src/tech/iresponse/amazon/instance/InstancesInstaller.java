package tech.iresponse.amazon.instance;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.AwsProcess;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.amazon.Ec2Manager;
import tech.iresponse.webservices.Amazon;
import tech.iresponse.amazon.update.UpdateData;
import tech.iresponse.helpers.scripts.InstallationServices;
import tech.iresponse.helpers.scripts.CloudServices;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class InstancesInstaller extends Thread {

    private Ec2Manager manager;
    private AwsProcess process;
    private Instance ec2Instance;
    private HashMap bundle;
    private Domain domain;

    @Override
    public void run() {
        try {
            if (this.ec2Instance != null && !this.bundle.isEmpty() && this.bundle.containsKey("instance") && this.bundle.containsKey("mta-server")) {
                MtaServer mtaServ = (MtaServer)this.bundle.get("mta-server");
                AwsInstance awsInst = (AwsInstance)this.bundle.get("instance");
                if (mtaServ != null && !mtaServ.getEmpty() && awsInst != null && !awsInst.getEmpty()) {
                    SSHConnector ssh = null;
                    try {
                        ssh = Authentification.connectToServer(mtaServ);
                        if (ssh == null || !ssh.isConnected()) {
                            ThreadSleep.sleep(60000L);
                            ssh = Authentification.connectToServer(mtaServ);
                        }

                        if (ssh == null || !ssh.isConnected()){
                            throw new DatabaseException("Could not connect to this instance : " + mtaServ.name + " !");
                        }

                        this.ec2Instance = this.manager.getInstances(this.ec2Instance.getInstanceId(), "running");

                        String prefix = "root".equals(mtaServ.sshUsername) ? "" : "sudo";

                        int version = String.valueOf(ssh.cmd("cat /etc/*release* | grep 'centos:7'")).replaceAll("\n", "").contains("centos:7") ? 7 : 6;

                        ArrayList<String> nbrePrivateIps = new ArrayList(this.process.nbPrivateIps);

                        ((InstanceNetworkInterface)this.ec2Instance.getNetworkInterfaces().get(0)).getPrivateIpAddresses().forEach(instPrivateIp -> nbrePrivateIps.add(instPrivateIp.getPrivateIpAddress()));

                        if (this.domain.value.endsWith(".")){
                            this.domain.value = this.domain.value.substring(0, this.domain.value.length() - 1);
                        }

                        InstallationServices.installServices(ssh, mtaServ, prefix, version, false);

                        int serverVmtaId = nbrePrivateIps.isEmpty() ? InstallationServices.saveServerVmta(null, this.ec2Instance.getPrivateIpAddress(), "", this.domain.value, mtaServ) : InstallationServices.saveServerVmta(null, nbrePrivateIps.get(0), "", this.domain.value, mtaServ);

                        if (this.domain.id > 0){
                            Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Taken' , mta_server_id = '" + mtaServ.id + "', ip_id = '" + serverVmtaId + "' WHERE id = " + this.domain.id, null, Connector.AFFECTED_ROWS);
                        }

                        if (nbrePrivateIps.size() > 1){
                            for (int i = 1; i < nbrePrivateIps.size(); i++){
                                InstallationServices.saveServerVmta(null, nbrePrivateIps.get(i), "", this.domain.value, mtaServ);
                            }
                        }

                        String[] ipsV6 = ssh.cmd(prefix + "ip addr show | grep 'inet6' | grep -i 'global' | cut -f2 | awk '{ print $2}' | cut -f 1 -d '/' | awk '{ print $1}'").split("\n");

                        boolean updatedNetwork = CloudServices.setupNetwork(ssh, mtaServ, prefix, version, this.domain, mtaServ.mainIp, ipsV6);
                        CloudServices.installTracking(ssh, mtaServ, prefix, version, this.domain, updatedNetwork, false);
                        InstallationServices.installPmta(ssh, mtaServ, prefix);

                        mtaServ.os = "CentOS " + version + " 64bits";
                        mtaServ.sshConnectivityStatus = "Connected";
                        //mtaServ.setIpsCount(nbrePrivateIps.size() + ipsV6.length);
                        mtaServ.setIpsCount(nbrePrivateIps.size());
                        mtaServ.setInstalled(true);
                        mtaServ.update();
                        Amazon.updateCountInstancsInstalled();
                        UpdateData.updateInstanceInstalled(this.process);
                    } finally {
                        if (ssh != null){
                            ssh.disconnect();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
            Amazon.IS_ERROR_OCCURED = true;
        }
    }

    @ConstructorProperties({"manager", "process", "ec2Instance", "bundle", "domain"})
    public InstancesInstaller(Ec2Manager manager, AwsProcess process, Instance ec2Instance, HashMap bundle, Domain domain) {
        this.manager = manager;
        this.process = process;
        this.ec2Instance = ec2Instance;
        this.bundle = bundle;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesInstaller))
            return false;
        InstancesInstaller if1 = (InstancesInstaller)paramObject;
        if (!if1.exists(this))
            return false;
        Ec2Manager do1 = getManager();
        Ec2Manager do2 = if1.getManager();
        if ((do1 == null) ? (do2 != null) : !do1.equals(do2))
            return false;
        AwsProcess case1 = getProcess();
        AwsProcess case2 = if1.getProcess();
        if ((case1 == null) ? (case2 != null) : !case1.equals(case2))
            return false;
        Instance instance1 = getEc2Instance();
        Instance instance2 = if1.getEc2Instance();
        if ((instance1 == null) ? (instance2 != null) : !instance1.equals(instance2))
            return false;
        HashMap hashMap1 = getBundle();
        HashMap hashMap2 = if1.getBundle();
        if ((hashMap1 == null) ? (hashMap2 != null) : !hashMap1.equals(hashMap2))
            return false;
        Domain long1 = getDomain();
        Domain long2 = if1.getDomain();
        return !((long1 == null) ? (long2 != null) : !long1.equals(long2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesInstaller;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Ec2Manager do1 = getManager();
        n = n * 59 + ((do1 == null) ? 43 : do1.hashCode());
        AwsProcess case1 = getProcess();
        n = n * 59 + ((case1 == null) ? 43 : case1.hashCode());
        Instance instance = getEc2Instance();
        n = n * 59 + ((instance == null) ? 43 : instance.hashCode());
        HashMap hashMap = getBundle();
        n = n * 59 + ((hashMap == null) ? 43 : hashMap.hashCode());
        Domain long1 = getDomain();
        return n * 59 + ((long1 == null) ? 43 : long1.hashCode());
    }

    public Ec2Manager getManager() {
        return manager;
    }

    public void setManager(Ec2Manager manager) {
        this.manager = manager;
    }

    public AwsProcess getProcess() {
        return process;
    }

    public void setProcess(AwsProcess process) {
        this.process = process;
    }

    public Instance getEc2Instance() {
        return ec2Instance;
    }

    public void setEc2Instance(Instance ec2Instance) {
        this.ec2Instance = ec2Instance;
    }

    public HashMap getBundle() {
        return bundle;
    }

    public void setBundle(HashMap bundle) {
        this.bundle = bundle;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "InstancesInstaller(manager=" + getManager() + ", process=" + getProcess() + ", ec2Instance=" + getEc2Instance() + ", bundle=" + getBundle() + ", domain=" + getDomain() + ")";
    }
}
