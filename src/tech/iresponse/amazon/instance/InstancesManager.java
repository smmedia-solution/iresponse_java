package tech.iresponse.amazon.instance;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.NatGateway;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.RuleAction;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import java.beans.ConstructorProperties;
import java.io.File;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.logging.Loggers;
import tech.iresponse.orm.Database;
import tech.iresponse.webservices.Amazon;
import tech.iresponse.models.admin.AwsInstance;
import tech.iresponse.models.admin.AwsProcess;
import tech.iresponse.models.admin.ServerProvider;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.AwsAccount;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.amazon.Ec2Manager;
import tech.iresponse.amazon.update.UpdateData;
import tech.iresponse.amazon.instance.InstancesInstaller;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.Matcher;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.Agents;
import tech.iresponse.core.Application;

public class InstancesManager extends Thread {

    private AwsProcess process;
    private ServerProvider provider;
    private String region;
    private JSONArray domains;
    public static final int MAX = 100;

    @Override
    public void run() {
        try {
            AwsAccount awsAcc = new AwsAccount(Integer.valueOf(this.process.accountId));
            if (!awsAcc.getEmpty()) {
                Ec2Manager ec2Mng = new Ec2Manager(awsAcc);
                if (!ec2Mng.authenticate(this.region)){
                    throw new DatabaseException("could not connect to : " + awsAcc.name + " with region : " + this.region);
                }

                JSONObject awsRegionsMap = new JSONObject(FileUtils.readFileToString(new File(System.getProperty("configs.path") + File.separator + "aws_regions.map.json"), "UTF-8"));
                ExecutorService execService = Executors.newFixedThreadPool(100);

                boolean domainFound = (this.domains != null && this.domains.length() > 0 && "rdns".equalsIgnoreCase(this.domains.getString(0))) ? true : false;
                String vpcs = this.region + "-vpc";
                String gateways = this.region + "-gateway";
                String securityGroups = this.region + "-security-group";
                String subnetPublics = this.region + "-subnet-public";
                String subnetPrivates = this.region + "-subnet-private";
                String routePublics = this.region + "-route-public";
                String routePrivates = this.region + "-route-private";
                String natGatewayPublics = this.region + "-nat-gateway-public";
                String networkAclPublics = this.region + "-network-acl-public";
                String networkAclPrivates = this.region + "-network-acl-private";

                Vpc vpc = ec2Mng.getVpcsByFilters(vpcs);
                if (vpc == null){
                    vpc = ec2Mng.updateVpcs(vpcs, "10.48.0.0/16", "default");
                }

                InternetGateway intGetwy = ec2Mng.getInternetGatewaysByFilters(gateways);
                if (intGetwy == null){
                    intGetwy = ec2Mng.updateInternetGateways(gateways);
                }

                if (vpc != null && intGetwy != null){
                    ec2Mng.attachInternetGateways(vpc.getVpcId(), intGetwy.getInternetGatewayId());
                }

                Random rndm = new Random();
                List<AvailabilityZone> availabeZones = ec2Mng.getAvailabilityZones();
                String zones = ((AvailabilityZone)availabeZones.get(rndm.nextInt(availabeZones.size()))).getZoneName();

                Subnet subnet1 = ec2Mng.getSubnetsByFilters(subnetPublics);
                if (subnet1 == null && vpc != null){
                    subnet1 = ec2Mng.updateSubnets(subnetPublics, "10.48.0.0/20", vpc.getVpcId(), zones, true);
                }

                Subnet subnet2 = ec2Mng.getSubnetsByFilters(subnetPrivates);
                if (subnet2 == null && vpc != null){
                    subnet2 = ec2Mng.updateSubnets(subnetPrivates, "10.48.16.0/20", vpc.getVpcId(), zones, true);
                }

                ThreadSleep.sleep(3000L);

                if (subnet1 != null && subnet2 != null) {
                    RouteTable routeTable1 = ec2Mng.getRouteTablesByFilters(routePublics);
                    if (routeTable1 == null && vpc != null){
                        routeTable1 = ec2Mng.updateRoutables(routePublics, vpc.getVpcId());
                    }
                    RouteTable routeTable2 = ec2Mng.getRouteTablesByFilters(routePrivates);
                    if (routeTable2 == null && vpc != null){
                        routeTable2 = ec2Mng.updateRoutables(routePrivates, vpc.getVpcId());
                    }
                    ThreadSleep.sleep(3000L);
                    if (routeTable1 != null && routeTable2 != null && intGetwy != null) {
                        ec2Mng.associateRouteTables(routeTable1.getRouteTableId(), subnet1.getSubnetId());
                        ec2Mng.associateRouteTables(routeTable2.getRouteTableId(), subnet2.getSubnetId());
                        ec2Mng.createRoutesGatways(routeTable1.getRouteTableId(), intGetwy.getInternetGatewayId(), "0.0.0.0/0");
                        ThreadSleep.sleep(3000L);
                        NatGateway natGtwy = ec2Mng.getNatGatewaysByFilters(natGatewayPublics);
                        if (natGtwy == null) {
                            String alloctionId = ec2Mng.allocateAddresses().getAllocationId();
                            ThreadSleep.sleep(3000L);
                            ec2Mng.createNatGateways(natGatewayPublics, alloctionId, subnet1.getSubnetId());
                        }
                        if (natGtwy != null) {
                            ThreadSleep.sleep(3000L);
                            ec2Mng.createRoutesNatGatways(routeTable2.getRouteTableId(), natGtwy.getNatGatewayId(), "0.0.0.0/0");
                        }
                    }

                    NetworkAcl networkAcl1 = ec2Mng.getNetworkAclsByFilters(networkAclPublics);
                    if (networkAcl1 == null && vpc != null){
                        networkAcl1 = ec2Mng.createNetworkAcls(networkAclPublics, vpc.getVpcId());
                    }

                    NetworkAcl networkAcl2 = ec2Mng.getNetworkAclsByFilters(networkAclPrivates);
                    if (networkAcl2 == null && vpc != null){
                        networkAcl2 = ec2Mng.createNetworkAcls(networkAclPrivates, vpc.getVpcId());
                    }

                    if (networkAcl1 != null && networkAcl2 != null) {
                        ThreadSleep.sleep(3000L);
                        ec2Mng.createNetworkAclsEntry(networkAcl1.getNetworkAclId(), "0.0.0.0/0", 99, RuleAction.Allow, "-1", false);
                        ec2Mng.createNetworkAclsEntry(networkAcl1.getNetworkAclId(), "0.0.0.0/0", 99, RuleAction.Allow, "-1", true);
                        ec2Mng.createNetworkAclsEntry(networkAcl2.getNetworkAclId(), "0.0.0.0/0", 99, RuleAction.Allow, "-1", false);
                        ec2Mng.createNetworkAclsEntry(networkAcl2.getNetworkAclId(), "0.0.0.0/0", 99, RuleAction.Allow, "-1", true);
                    }
                }

                SecurityGroup scrtyGroup = ec2Mng.getSecurityGroupsByName(securityGroups);
                if (scrtyGroup == null && vpc != null) {
                    ec2Mng.authorizeSecurityGroups(securityGroups, vpc.getVpcId(), "tcp", "0.0.0.0/0", 0, 65535);
                    ThreadSleep.sleep(3000L);
                    scrtyGroup = ec2Mng.getSecurityGroupsByName(securityGroups);
                }

                if (scrtyGroup == null){
                    throw new DatabaseException("Security group is not found to create instances !");
                }

                if (subnet1 == null){
                    throw new DatabaseException("Public subnet is not found to create instances !");
                }

                String rootPasswd = Strings.getSaltString(43, true, true, true, false);
                String instancesUserData = FileUtils.readFileToString(new File(System.getProperty("assets.path") + File.separator + "templates" + File.separator + "aws" + File.separator + "instances_pass_userdata.txt"), "UTF-8");
                instancesUserData = StringUtils.replace(instancesUserData, "$p_password", rootPasswd);
                if (this.process.nbPrivateIps > 1) {
                    instancesUserData = instancesUserData + FileUtils.readFileToString(new File(System.getProperty("assets.path") + File.separator + "templates" + File.separator + "aws" + File.separator + "instances_ips_userdata.txt"), "UTF-8");
                    instancesUserData = StringUtils.replace(instancesUserData, "$p_access_key", awsAcc.accessKey);
                    instancesUserData = StringUtils.replace(instancesUserData, "$p_secret_key", awsAcc.secretKey);
                    instancesUserData = StringUtils.replace(instancesUserData, "$p_nb_ips", String.valueOf(this.process.nbPrivateIps - 1));
                }

                if (this.process.subnetsFilter != null && !"".equals(this.process.subnetsFilter)) {
                    String[] sbntsFilters = this.process.subnetsFilter.split(Pattern.quote("|"));
                    if (sbntsFilters.length != 0) {
                        Instance instnce = null;
                        for (int b = 0; b < this.process.nbInstances; b++) {
                            boolean terminateInstnce = false;
                            while (!terminateInstnce) {
                                instnce = ec2Mng.getInstances(instancesUserData, subnet1.getSubnetId(), this.process.instanceType, this.process.os, scrtyGroup.getGroupId(), this.process.storage);
                                for (String subntfiltr : sbntsFilters) {
                                    if (Matcher.adress(subntfiltr, instnce.getPublicIpAddress())){
                                        terminateInstnce = true;
                                    }
                                }
                                if (!terminateInstnce){
                                    ec2Mng.termineInstances(instnce.getInstanceId());
                                }
                            }
                            if (instnce != null && terminateInstnce) {
                                Domain domin = null;
                                if (domainFound) {
                                    domin = new Domain();
                                    domin.setValue(instnce.getPublicDnsName());
                                } else {
                                    domin = new Domain(Integer.valueOf(this.domains.getInt((new Random()).nextInt(this.domains.length()))));
                                }
                                execService.submit(new InstancesInstaller(ec2Mng, this.process, instnce, this.insertDataInfos(awsAcc, instnce, domin, rootPasswd, awsRegionsMap), domin));
                                Amazon.updateCountInstancesCreated(1);
                                UpdateData.updateIstanceCreated(this.process);
                            }
                        }
                    }
                } else {
                    List<Instance> instances = ec2Mng.getInstances(instancesUserData, subnet1.getSubnetId(), this.process.instanceType, this.process.os, scrtyGroup.getGroupId(), this.process.nbInstances, this.process.storage);
                    if (instances == null || instances.isEmpty()){
                        throw new DatabaseException("Instances not found !");
                    }
                    Amazon.updateCountInstancesCreated(instances.size());
                    UpdateData.updateIstanceCreated(this.process);
                    for (Instance instance : instances) {
                        Domain domin = null;
                        if (domainFound) {
                            domin = new Domain();
                            domin.setValue(instance.getPublicDnsName());
                        } else {
                            domin = new Domain(Integer.valueOf(this.domains.getInt((new Random()).nextInt(this.domains.length()))));
                        }
                        execService.submit(new InstancesInstaller(ec2Mng, this.process, instance, this.insertDataInfos(awsAcc, instance, domin, rootPasswd, awsRegionsMap), domin));
                    }
                }
                execService.shutdown();
                if (!execService.awaitTermination(1L, TimeUnit.DAYS)){
                    execService.shutdownNow();
                }
            }
        } catch (Throwable th) {
            Loggers.error(th);
            Amazon.IS_ERROR_OCCURED = true;
        }
    }

    private HashMap insertDataInfos(AwsAccount awsAcc, Instance instnce, Domain domin, String passwd, JSONObject regionsMap) throws Exception {
        HashMap params = new HashMap<>(2);

        AwsInstance awsInst = new AwsInstance();
        awsInst.status = "Running";
        awsInst.accountId = awsAcc.id;
        awsInst.accountName = awsAcc.name;
        awsInst.name = instnce.getInstanceId();
        awsInst.region = this.region;
        awsInst.regionName = regionsMap.getString(this.region);
        awsInst.type = this.process.instanceType;

        Calendar calendar = Calendar.getInstance();
        calendar.add(2, 1);

        MtaServer mtaServ = new MtaServer();
        mtaServ.name = getMtaNames(awsAcc);
        mtaServ.status = "Activated";
        mtaServ.sshConnectivityStatus = "Not Checked";
        mtaServ.providerId = this.provider.id;
        mtaServ.providerName = this.provider.name;
        mtaServ.expirationDate = new Date(calendar.getTimeInMillis());
        mtaServ.hostName = "mail." + domin.value;
        mtaServ.mainIp = instnce.getPublicIpAddress();

        String result = Agents.get("https://freegeoip.live/json/" + mtaServ.mainIp, null, 20);
        if (result != null && result.contains("country_code")) {
            JSONObject response = new JSONObject(result);
            mtaServ.countryCode = response.has("country_code") ? response.getString("country_code") : "US";
        } else {
            mtaServ.countryCode = "US";
        }

        mtaServ.sshPort = 22;
        mtaServ.sshLoginType = "user-pass";
        mtaServ.sshPassword = passwd;
        mtaServ.sshUsername = "root";
        mtaServ.createdBy = (Application.checkAndgetInstance().getUser()).email;
        mtaServ.createdDate = new Date(System.currentTimeMillis());
        mtaServ.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
        mtaServ.lastUpdatedDate = new Date(System.currentTimeMillis());
        mtaServ.id = mtaServ.insert();

        params.put("mta-server", mtaServ);

        awsInst.mtaServerId = mtaServ.id;
        awsInst.mtaServerName = mtaServ.name;
        awsInst.regionName = regionsMap.getString(this.region);
        awsInst.createdBy = (Application.checkAndgetInstance().getUser()).email;
        awsInst.createdDate = new Date(System.currentTimeMillis());
        awsInst.lastUpdatedBy = (Application.checkAndgetInstance().getUser()).email;
        awsInst.lastUpdatedDate = new Date(System.currentTimeMillis());
        awsInst.id = awsInst.insert();

        params.put("instance", awsInst);

        return params;
    }

    public static synchronized String getMtaNames(AwsAccount awsAcc) throws Exception {
        String name = awsAcc.name.trim();
        String nm = name + "_1";

        List nameMtaServer = Database.get("system").availableTables("SELECT name FROM admin.mta_servers WHERE name LIKE '" + name + "%' ORDER BY id DESC LIMIT 1", null, 0, "name");
        if (nameMtaServer != null && !nameMtaServer.isEmpty()) {
            String nmeMtaServ = String.valueOf(nameMtaServer.get(0));
            if (nmeMtaServ.contains("_")) {
                String[] names = nmeMtaServ.split(Pattern.quote("_"));
                if (names.length > 0){
                    nm = name + "_" + (TypesParser.safeParseInt(names[names.length - 1]) + 1);
                }
            }
        }
        return nm;
    }

    @ConstructorProperties({"process", "provider", "region", "domains"})
    public InstancesManager(AwsProcess process, ServerProvider provider, String region, JSONArray domains) {
        this.process = process;
        this.provider = provider;
        this.region = region;
        this.domains = domains;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof InstancesManager))
            return false;
        InstancesManager int1 = (InstancesManager)paramObject;
        if (!int1.exists(this))
            return false;
        AwsProcess case1 = getProcess();
        AwsProcess case2 = int1.getProcess();
        if ((case1 == null) ? (case2 != null) : !case1.equals(case2))
            return false;
        ServerProvider default1 = getProvider();
        ServerProvider default2 = int1.getProvider();
        if ((default1 == null) ? (default2 != null) : !default1.equals(default2))
            return false;
        String str1 = getRegion();
        String str2 = int1.getRegion();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        JSONArray jSONArray1 = getDomains();
        JSONArray jSONArray2 = int1.getDomains();
        return !((jSONArray1 == null) ? (jSONArray2 != null) : !jSONArray1.equals(jSONArray2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof InstancesManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AwsProcess case1 = getProcess();
        n = n * 59 + ((case1 == null) ? 43 : case1.hashCode());
        ServerProvider default1 = getProvider();
        n = n * 59 + ((default1 == null) ? 43 : default1.hashCode());
        String str = getRegion();
        n = n * 59 + ((str == null) ? 43 : str.hashCode());
        JSONArray jSONArray = getDomains();
        return n * 59 + ((jSONArray == null) ? 43 : jSONArray.hashCode());
    }

    public AwsProcess getProcess() {
        return process;
    }

    public void setProcess(AwsProcess process) {
        this.process = process;
    }

    public ServerProvider getProvider() {
        return provider;
    }

    public void setProvider(ServerProvider provider) {
        this.provider = provider;
    }

    public java.lang.String getRegion() {
        return region;
    }

    public void setRegion(java.lang.String region) {
        this.region = region;
    }

    public JSONArray getDomains() {
        return domains;
    }

    public void setDomains(JSONArray domains) {
        this.domains = domains;
    }

    @Override
    public String toString() {
        return "InstancesManager(process=" + getProcess() + ", provider=" + getProvider() + ", region=" + getRegion() + ", domains=" + getDomains() + ")";
    }
}
