package tech.iresponse.amazon.instance;

import com.amazonaws.services.ec2.model.Instance;
import java.beans.ConstructorProperties;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.AwsAccount;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.amazon.Ec2Manager;

public class InstancesUpdaterProxy extends Thread {

    private int accountId;
    private String region;

    @Override
    public void run() {
        try {
            if (this.accountId > 0 && !"".equals(this.region)) {
                AwsAccount awsAcc = new AwsAccount(Integer.valueOf(this.accountId));
                Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
                ec2Mng.authenticate(this.region);

                List<AwsInstance> awsInstances = (List)AwsInstance.all(AwsInstance.class, "account_id = ? AND region = ?", new Object[] { Integer.valueOf(this.accountId), this.region });
                if (awsInstances != null && !awsInstances.isEmpty()) {
                    HashMap instancesIds = new HashMap<>();
                    awsInstances.forEach(inst -> instancesIds.put(inst.name, inst));
                    List<Instance> instances = ec2Mng.getInstances("running");
                    JSONObject awsRegionsMap = new JSONObject(FileUtils.readFileToString(new File(System.getProperty("configs.path") + File.separator + "aws_regions.map.json"), "UTF-8"));
                    if (instances != null && !instances.isEmpty())
                    for (Instance instance : instances) {
                        if (instancesIds.containsKey(instance.getInstanceId())) {
                            AwsInstance awsInst = (AwsInstance)instancesIds.get(instance.getInstanceId());
                            awsInst.regionName = awsRegionsMap.getString(awsInst.region);
                            awsInst.status = "Running";
                            awsInst.update();
                            Database.get("system").executeUpdate("UPDATE admin.mta_servers SET main_ip = ? WHERE id = ?", new Object[] { instance.getPublicIpAddress(), Integer.valueOf(awsInst.mtaServerId) }, Connector.AFFECTED_ROWS);
                            Database.get("system").executeUpdate("UPDATE admin.smtp_users SET proxy_ip = ?", new Object[] { instance.getPublicIpAddress() }, Connector.AFFECTED_ROWS);
                            if (instance.getPublicDnsName() != null && !"".equals(instance.getPublicDnsName())){
                                Database.get("system").executeUpdate("UPDATE admin.servers_vmtas SET domain = ? WHERE mta_server_id = ? AND domain LIKE 'ec2%'", new Object[] { instance.getPublicDnsName(), Integer.valueOf(awsInst.mtaServerId) }, Connector.AFFECTED_ROWS);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loggers.error(e);
        }
    }

    @ConstructorProperties({"accountId", "region"})
    public InstancesUpdaterProxy(int accountId, String region) {
        this.accountId = accountId;
        this.region = region;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesUpdaterProxy))
            return false;
        InstancesUpdaterProxy try1 = (InstancesUpdaterProxy)paramObject;
        if (!try1.exists(this))
            return false;
        if (getAccountId() != try1.getAccountId())
            return false;
        String str1 = getRegion();
        String str2 = try1.getRegion();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesUpdaterProxy;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getAccountId();
        String str = getRegion();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "InstancesUpdaterProxy(accountId=" + getAccountId() + ", region=" + getRegion() + ")";
    }
}
