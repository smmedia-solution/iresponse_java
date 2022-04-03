package tech.iresponse.amazon.instance;

import com.amazonaws.services.ec2.model.Instance;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.AwsAccount;
import tech.iresponse.logging.Loggers;
import tech.iresponse.amazon.Ec2Manager;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.TypesParser;

public class InstancesActions extends Thread {

    private String key;
    private LinkedHashMap<String,AwsInstance> instances;
    private String action;

    @Override
    public void run() {
        try {
            if (this.key != null && !this.instances.isEmpty()) {
                int id = TypesParser.safeParseInt(this.key.split(Pattern.quote("|"))[0]);
                String regions = this.key.split(Pattern.quote("|"))[1];

                AwsAccount awsAcc = new AwsAccount(Integer.valueOf(id));
                if (!awsAcc.getEmpty()) {
                    Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
                    if (ec2Mng.authenticate(regions)) {
                        String instanceStateName = null;
                        if ("start".equals(this.action)) {
                            instanceStateName = "stopped";
                        } else if ("stop".equals(this.action) || "restart".equals(this.action)) {
                            instanceStateName = "running";
                        }

                        ArrayList<String> instanceIds = new ArrayList<String>(this.instances.size());
                        this.instances.entrySet().forEach(inst -> instanceIds.add(inst.getKey()));

                        List<Instance> listInstances = ec2Mng.getInstances(instanceIds, instanceStateName);

                        if (listInstances != null && !listInstances.isEmpty()) {
                            instanceIds.clear();
                            for (Instance instance : listInstances){
                                instanceIds.add(instance.getInstanceId());
                            }

                            switch (this.action) {
                                case "start":{
                                    ec2Mng.actionStart(instanceIds);
                                    listInstances = ec2Mng.getInstances(instanceIds, "running");
                                    for (Instance instance : listInstances) {
                                        AwsInstance awsInst = this.instances.containsKey(instance.getInstanceId()) ? (AwsInstance)this.instances.get(instance.getInstanceId()) : null;
                                        if (awsInst != null) {
                                            awsInst.status = "Running";
                                            awsInst.update();
                                            Database.get("system").executeUpdate("UPDATE admin.mta_servers SET main_ip = ? WHERE id = ?", new Object[] { instance.getPublicIpAddress(), Integer.valueOf(awsInst.mtaServerId) }, Connector.AFFECTED_ROWS);
                                            if (instance.getPublicDnsName() != null && !"".equals(instance.getPublicDnsName())){
                                                Database.get("system").executeUpdate("UPDATE admin.servers_vmtas SET domain = ? WHERE mta_server_id = ? AND domain LIKE 'ec2%'", new Object[] { instance.getPublicDnsName(), Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            }
                                        }
                                    }
                                    break;
                                }
                                case "restart":{
                                    ec2Mng.actionStop(instanceIds);
                                    listInstances = ec2Mng.getInstances(instanceIds, "stopped");
                                    instanceIds.clear();
                                    for (Instance instance : listInstances){
                                        instanceIds.add(instance.getInstanceId());
                                    }
                                    ec2Mng.actionStart(instanceIds);
                                    listInstances = ec2Mng.getInstances(instanceIds, "running");
                                    for (Instance instance : listInstances) {
                                        AwsInstance awsInst = this.instances.containsKey(instance.getInstanceId()) ? (AwsInstance)this.instances.get(instance.getInstanceId()) : null;
                                        if (awsInst != null) {
                                            awsInst.status = "Running";
                                            awsInst.update();
                                            Database.get("system").executeUpdate("UPDATE admin.mta_servers SET main_ip = ? WHERE id = ?", new Object[] { instance.getPublicIpAddress(), Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            if (instance.getPublicDnsName() != null && !"".equals(instance.getPublicDnsName())){
                                                Database.get("system").executeUpdate("UPDATE admin.servers_vmtas SET domain = ? WHERE mta_server_id = ? AND domain LIKE 'ec2%'", new Object[] { instance.getPublicDnsName(), Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            }
                                        }
                                    }
                                    break;
                                }
                                case "stop":{
                                    ec2Mng.actionStop(instanceIds);
                                    listInstances = ec2Mng.getInstances(instanceIds, "stopped");
                                    for (Instance instance : listInstances) {
                                        AwsInstance awsInst = this.instances.containsKey(instance.getInstanceId()) ? (AwsInstance)this.instances.get(instance.getInstanceId()) : null;
                                        if (awsInst != null && "stopped".equals(instance.getState().getName())) {
                                            awsInst.status = "Stopped";
                                            awsInst.update();
                                        }
                                    }
                                    break;
                                }
                                case "terminate":{
                                    ec2Mng.actionTeminate(instanceIds);
                                    for (Instance instance : listInstances) {
                                        AwsInstance awsInst = this.instances.containsKey(instance.getInstanceId()) ? (AwsInstance)this.instances.get(instance.getInstanceId()) : null;
                                        if (awsInst != null) {
                                            Database.get("system").executeUpdate("DELETE FROM admin.mta_servers WHERE id = ?", new Object[] { Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            Database.get("system").executeUpdate("DELETE FROM admin.servers_vmtas WHERE mta_server_id = ?", new Object[] { Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , mta_server_id = 0 , ip_id = 0 WHERE mta_server_id = ?", new Object[] { Integer.valueOf(awsInst.mtaServerId) },  Connector.AFFECTED_ROWS);
                                            awsInst.delete();
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    @ConstructorProperties({"key", "instances", "action"})
    public InstancesActions(String key, LinkedHashMap instances, String action) {
        this.key = key;
        this.instances = instances;
        this.action = action;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesActions))
            return false;
        InstancesActions do1 = (InstancesActions)paramObject;
        if (!do1.exists(this))
            return false;
        String str1 = getKey();
        String str2 = do1.getKey();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        LinkedHashMap map1 = getInstances();
        LinkedHashMap map2 = do1.getInstances();
        if ((map1 == null) ? (map2 != null) : !map1.equals(map2))
            return false;
        String str3 = getAction();
        String str4 = do1.getAction();
        return !((str3 == null) ? (str4 != null) : !str3.equals(str4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesActions;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getKey();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        LinkedHashMap map = getInstances();
        n = n * 59 + ((map == null) ? 43 : map.hashCode());
        String str2 = getAction();
        return n * 59 + ((str2 == null) ? 43 : str2.hashCode());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LinkedHashMap getInstances() {
        return instances;
    }

    public void setInstances(LinkedHashMap instances) {
        this.instances = instances;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "InstancesActions(key=" + getKey() + ", instances=" + getInstances() + ", action=" + getAction() + ")";
    }
}
