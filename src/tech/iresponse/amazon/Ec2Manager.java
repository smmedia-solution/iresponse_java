package tech.iresponse.amazon;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.AttachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateNatGatewayRequest;
import com.amazonaws.services.ec2.model.CreateNetworkAclEntryRequest;
import com.amazonaws.services.ec2.model.CreateNetworkAclRequest;
import com.amazonaws.services.ec2.model.CreateRouteRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.DeleteInternetGatewayRequest;
import com.amazonaws.services.ec2.model.DeleteRouteRequest;
import com.amazonaws.services.ec2.model.DeleteRouteTableRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSubnetRequest;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.DescribeNatGatewaysRequest;
import com.amazonaws.services.ec2.model.DescribeNetworkAclsRequest;
import com.amazonaws.services.ec2.model.DescribeRouteTablesRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.ModifySubnetAttributeRequest;
import com.amazonaws.services.ec2.model.ModifyVpcAttributeRequest;
import com.amazonaws.services.ec2.model.NatGateway;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.RuleAction;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.VolumeType;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.waiters.WaiterParameters;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.iresponse.models.admin.AwsAccount;
import tech.iresponse.logging.Loggers;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.Matcher;
import tech.iresponse.utils.TypesParser;


public class Ec2Manager {

    private final AwsAccount account;
    private AmazonEC2 amznEc2;

    public List<Instance> getInstances(String instanceStateName) {
        ArrayList instances = new ArrayList();
        DescribeInstancesRequest decrInstRequest = new DescribeInstancesRequest();
        if (instanceStateName != null){
            decrInstRequest.withFilters(new Filter[] { (new Filter("instance-state-name")).withValues(new String[] { instanceStateName }) });
        }
        DescribeInstancesResult decrInstResult = this.amznEc2.describeInstances(decrInstRequest);
        List<Reservation> reservations = decrInstResult.getReservations();
        if (reservations == null || reservations.isEmpty()){
            return null;
        }
        reservations.forEach(reserv -> instances.addAll(reserv.getInstances()));
        return instances;
    }

    public Instance getInstances(String instncesIds, String instanceStateName) {
        DescribeInstancesRequest decrInstRequest = new DescribeInstancesRequest();
        decrInstRequest.setInstanceIds(Arrays.asList(new String[] { instncesIds }));
        if (instanceStateName != null){
            decrInstRequest.withFilters(new Filter[] { (new Filter("instance-state-name")).withValues(new String[] { instanceStateName }) });
        }
        DescribeInstancesResult decrInstResult = this.amznEc2.describeInstances(decrInstRequest);
        List<Reservation> list = decrInstResult.getReservations();
        if (list == null || list.isEmpty()){
            return null;
        }
        List<Instance> instances = ((Reservation)list.get(0)).getInstances();
        return (instances == null || instances.isEmpty()) ? null : instances.get(0);
    }

    public List<Instance> getInstances(List instanceIds, String instanceStateName) {
        ArrayList instances = new ArrayList();
        DescribeInstancesRequest decrInstRequest = (new DescribeInstancesRequest()).withInstanceIds(instanceIds);
        if (instanceStateName != null){
            decrInstRequest.withFilters(new Filter[] { (new Filter("instance-state-name")).withValues(new String[] { instanceStateName }) });
        }
        DescribeInstancesResult decrInstResult = this.amznEc2.describeInstances(decrInstRequest);
        List<Reservation> reservations = decrInstResult.getReservations();
        if (reservations == null || reservations.isEmpty()){
            return null;
        }
        reservations.forEach(res -> instances.addAll(res.getInstances()));
        return instances;
    }

    public List<Instance> getInstances(String userData, String subnetId, String instnceType, String os, String scrtyGroupId, int nbInstances, int storage) {
        RunInstancesRequest runInstRequest = null;
        if (storage > 8) {
            runInstRequest = (new RunInstancesRequest()).withImageId(this.getImagesOs(os)).withInstanceType((InstanceType)this.getInstanceType(instnceType)).withMinCount(Integer.valueOf(nbInstances)).withMaxCount(Integer.valueOf(nbInstances)).withUserData(Crypto.Base64Encode(userData)).withNetworkInterfaces((Collection)ImmutableSet.of((new InstanceNetworkInterfaceSpecification()).withSubnetId(subnetId).withAssociatePublicIpAddress(Boolean.valueOf(true)).withDeviceIndex(Integer.valueOf(0)).withGroups(new String[] { scrtyGroupId }))).withBlockDeviceMappings((Collection)ImmutableSet.of((new BlockDeviceMapping()).withDeviceName("/dev/sda1").withEbs((new EbsBlockDevice()).withVolumeSize(Integer.valueOf(storage)).withVolumeType(VolumeType.Gp2))));
        } else {
            runInstRequest = (new RunInstancesRequest()).withImageId(this.getImagesOs(os)).withInstanceType((InstanceType)this.getInstanceType(instnceType)).withMinCount(Integer.valueOf(nbInstances)).withMaxCount(Integer.valueOf(nbInstances)).withUserData(Crypto.Base64Encode(userData)).withNetworkInterfaces((Collection)ImmutableSet.of((new InstanceNetworkInterfaceSpecification()).withSubnetId(subnetId).withAssociatePublicIpAddress(Boolean.valueOf(true)).withDeviceIndex(Integer.valueOf(0)).withGroups(new String[] { scrtyGroupId })));
        }
        ArrayList instnces = new ArrayList();
        this.amznEc2.runInstances(runInstRequest).getReservation().getInstances().forEach(inst -> instnces.add(inst.getInstanceId()));
        DescribeInstancesRequest decrInstRequest = (new DescribeInstancesRequest()).withInstanceIds(instnces);
        this.amznEc2.waiters().instanceRunning().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)decrInstRequest));
        return ((Reservation)this.amznEc2.describeInstances(decrInstRequest).getReservations().get(0)).getInstances();
    }

    public Instance getInstances(String userData, String subnetId, String instnceType, String os, String scrtyGroupId, int storage) {
        RunInstancesRequest runInstRequest = (new RunInstancesRequest()).withImageId(this.getImagesOs(os)).withInstanceType((InstanceType)this.getInstanceType(instnceType)).withMinCount(Integer.valueOf(1)).withMaxCount(Integer.valueOf(1)).withUserData(Crypto.Base64Encode(userData)).withNetworkInterfaces((Collection)ImmutableSet.of((new InstanceNetworkInterfaceSpecification()).withSubnetId(subnetId).withAssociatePublicIpAddress(Boolean.valueOf(true)).withDeviceIndex(Integer.valueOf(0)).withGroups(new String[] { scrtyGroupId })));
        if (storage > 8){
            runInstRequest = runInstRequest.withBlockDeviceMappings((Collection)ImmutableSet.of((new BlockDeviceMapping()).withDeviceName("/dev/sda1").withEbs((new EbsBlockDevice()).withVolumeSize(Integer.valueOf(storage)).withVolumeType(VolumeType.Gp2))));
        }
        DescribeInstancesRequest decrInstRequest = (new DescribeInstancesRequest()).withInstanceIds(Arrays.asList(new String[] { ((Instance)this.amznEc2.runInstances(runInstRequest).getReservation().getInstances().get(0)).getInstanceId() }));
        this.amznEc2.waiters().instanceRunning().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)decrInstRequest));
        return ((Reservation)this.amznEc2.describeInstances(decrInstRequest).getReservations().get(0)).getInstances().get(0);
    }

    public void startInstances(String instncesIds) {
        this.amznEc2.startInstances((new StartInstancesRequest()).withInstanceIds(new String[] { instncesIds }));
        this.amznEc2.waiters().instanceRunning().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)(new DescribeInstancesRequest()).withInstanceIds(new String[] { instncesIds })));
    }

    public void actionStart(List instanceIds) {
        this.amznEc2.startInstances((new StartInstancesRequest()).withInstanceIds(instanceIds));
        this.amznEc2.waiters().instanceRunning().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)(new DescribeInstancesRequest()).withInstanceIds(instanceIds)));
    }

    public void stopInstances(String instncesIds) {
        this.amznEc2.stopInstances((new StopInstancesRequest()).withInstanceIds(new String[] { instncesIds }));
        this.amznEc2.waiters().instanceStopped().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)(new DescribeInstancesRequest()).withInstanceIds(new String[] { instncesIds })));
    }

    public void actionStop(List instanceIds) {
        this.amznEc2.stopInstances((new StopInstancesRequest()).withInstanceIds(instanceIds));
        this.amznEc2.waiters().instanceStopped().run((new WaiterParameters()).withRequest((AmazonWebServiceRequest)(new DescribeInstancesRequest()).withInstanceIds(instanceIds)));
    }

    public void termineInstances(String instancesId) {
        this.amznEc2.terminateInstances((new TerminateInstancesRequest()).withInstanceIds(new String[] { instancesId }));
    }

    public void actionTeminate(List instanceIds) {
        this.amznEc2.terminateInstances((new TerminateInstancesRequest()).withInstanceIds(instanceIds));
    }

    public List getVpcs() {
        return this.amznEc2.describeVpcs().getVpcs();
    }

    public boolean hasVpcs(String vpcs) {
        return (getVpcsByFilters(vpcs) != null);
    }

    public Vpc getVpcsByFilters(String vpcs) {
        Vpc vpc = null;
        try {
            vpc = this.amznEc2.describeVpcs((new DescribeVpcsRequest()).withFilters(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { vpcs }) })).getVpcs().get(0);
        } catch (Exception exception) {}
            return vpc;
    }

    public Vpc getVpcsById(String vpcId) {
        Vpc vpc = null;
        try {
            vpc = this.amznEc2.describeVpcs((new DescribeVpcsRequest()).withVpcIds(new String[] { vpcId })).getVpcs().get(0);
        } catch (Exception exception) {}
        return vpc;
    }

    public Vpc updateVpcs(String vpcs, String blocks, String tenancy) {
        Vpc vpc = null;
        try {
            vpc = this.amznEc2.createVpc((new CreateVpcRequest()).withCidrBlock(blocks).withInstanceTenancy(tenancy)).getVpc();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { vpc.getVpcId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(vpcs) }));
            this.amznEc2.modifyVpcAttribute((new ModifyVpcAttributeRequest()).withVpcId(vpc.getVpcId()).withEnableDnsHostnames(Boolean.valueOf(true)));
            this.amznEc2.modifyVpcAttribute((new ModifyVpcAttributeRequest()).withVpcId(vpc.getVpcId()).withEnableDnsSupport(Boolean.valueOf(true)));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return vpc;
    }

    public void deleteVpcs(String vpcId) {
        try {
            this.amznEc2.deleteVpc((new DeleteVpcRequest()).withVpcId(vpcId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public boolean hasInternetGateways(String gateways) {
        return (getInternetGatewaysByFilters(gateways) != null);
    }

    public List getInternetGateways() {
        return this.amznEc2.describeInternetGateways().getInternetGateways();
    }

    public InternetGateway getInternetGatewaysByFilters(String gateways) {
        InternetGateway internetGateway = null;
        try {
            internetGateway = this.amznEc2.describeInternetGateways((new DescribeInternetGatewaysRequest()).withFilters(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { gateways }) })).getInternetGateways().get(0);
        } catch (Exception exception) {}
        return internetGateway;
    }

    public InternetGateway getInternetGatewaysByIds(String gatewayIds) {
        InternetGateway internetGateway = null;
        try {
            internetGateway = this.amznEc2.describeInternetGateways((new DescribeInternetGatewaysRequest()).withInternetGatewayIds(new String[] { gatewayIds })).getInternetGateways().get(0);
        } catch (Exception exception) {}
        return internetGateway;
    }

    public InternetGateway updateInternetGateways(String gateways) {
        InternetGateway internetGateway = null;
        try {
            internetGateway = this.amznEc2.createInternetGateway().getInternetGateway();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { internetGateway.getInternetGatewayId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(gateways) }));
        } catch (Exception e) {
            Loggers.error(e);
        }
        return internetGateway;
    }

    public void deleteInternetGateways(String gatewayIds) {
        try {
            this.amznEc2.deleteInternetGateway((new DeleteInternetGatewayRequest()).withInternetGatewayId(gatewayIds));
        } catch (Exception exception) {
            Loggers.error(exception);
        }
    }

    public void attachInternetGateways(String VpcId, String gatewayId) {
        try {
            this.amznEc2.attachInternetGateway((new AttachInternetGatewayRequest()).withVpcId(VpcId).withInternetGatewayId(gatewayId));
        } catch (Exception exception) {}
    }

    public List getAvailabilityZones() {
        List list = null;
        try {
            list = this.amznEc2.describeAvailabilityZones().getAvailabilityZones();
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return list;
    }

    public List getSubnets() {
        return this.amznEc2.describeSubnets().getSubnets();
    }

    public boolean hasVpcs2(String vpcs) {
        return (getVpcsByFilters(vpcs) != null);
    }

    public Subnet getSubnetsByFilters(String subnets) {
        Subnet subnet = null;
        try {
            subnet = this.amznEc2.describeSubnets((new DescribeSubnetsRequest()).withFilters(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { subnets }) })).getSubnets().get(0);
        } catch (Exception exception) {}
        return subnet;
    }

    public Subnet getSubnetsById(String subntId) {
        Subnet subnet = null;
        try {
            subnet = this.amznEc2.describeSubnets((new DescribeSubnetsRequest()).withSubnetIds(new String[] { subntId })).getSubnets().get(0);
        } catch (Exception exception) {}
        return subnet;
    }

    public Subnet updateSubnets(String subnetPublics, String blocks, String vpcId, String zones, boolean updated) {
        Subnet subnet = null;
        try {
            subnet = this.amznEc2.createSubnet((new CreateSubnetRequest()).withCidrBlock(blocks).withVpcId(vpcId).withAvailabilityZone(zones)).getSubnet();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { subnet.getSubnetId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(subnetPublics) }));
            if (updated){
                this.amznEc2.modifySubnetAttribute((new ModifySubnetAttributeRequest()).withSubnetId(subnet.getSubnetId()).withMapPublicIpOnLaunch(Boolean.valueOf(true)));
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return subnet;
    }

    public void deleteSubnets(String subntId) {
        try {
            this.amznEc2.deleteSubnet((new DeleteSubnetRequest()).withSubnetId(subntId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public List getRouteTables() {
        return this.amznEc2.describeRouteTables().getRouteTables();
    }

    public boolean hasRouteTables(String routables) {
        return (getRouteTablesByFilters(routables) != null);
    }

    public RouteTable getRouteTablesByFilters(String routables) {
        RouteTable routeTable = null;
        try {
            routeTable = this.amznEc2.describeRouteTables((new DescribeRouteTablesRequest()).withFilters(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { routables }) })).getRouteTables().get(0);
        } catch (Exception exception) {}
        return routeTable;
    }

    public RouteTable getRouteTablesById(String routableId) {
        RouteTable routeTable = null;
        try {
            routeTable = this.amznEc2.describeRouteTables((new DescribeRouteTablesRequest()).withRouteTableIds(new String[] { routableId })).getRouteTables().get(0);
        } catch (Exception exception) {}
        return routeTable;
    }

    public RouteTable updateRoutables(String routes, String vpcId) {
        RouteTable routeTable = null;
        try {
            routeTable = this.amznEc2.createRouteTable((new CreateRouteTableRequest()).withVpcId(vpcId)).getRouteTable();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { routeTable.getRouteTableId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(routes) }));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return routeTable;
    }

    public void deleteRouteTables(String routableId) {
        try {
            this.amznEc2.deleteRouteTable((new DeleteRouteTableRequest()).withRouteTableId(routableId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public void associateRouteTables(String routeTableId, String subnetId) {
        try {
            this.amznEc2.associateRouteTable((new AssociateRouteTableRequest()).withRouteTableId(routeTableId).withSubnetId(subnetId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public void createRoutesGatways(String routeTableId, String gatewayId, String blocks) {
        try {
            this.amznEc2.createRoute((new CreateRouteRequest()).withGatewayId(gatewayId).withDestinationCidrBlock(blocks).withRouteTableId(routeTableId));
        } catch (Exception exception) {}
    }

    public void createRoutesNatGatways(String routesId, String natGtwayId, String blocks) {
        try {
            this.amznEc2.createRoute((new CreateRouteRequest()).withNatGatewayId(natGtwayId).withDestinationCidrBlock(blocks).withRouteTableId(routesId));
        } catch (Exception exception) {}
    }

    public void deleteRoutes(String routableId) {
        try {
            this.amznEc2.deleteRoute((new DeleteRouteRequest()).withRouteTableId(routableId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public AllocateAddressResult allocateAddresses() {
        try {
            return this.amznEc2.allocateAddress((new AllocateAddressRequest()).withDomain(DomainType.Vpc));
        } catch (Exception ex) {
            Loggers.error(ex);
            return null;
        }
    }

    public void associateAddresses(String instanceId, String privateIp, String allocationId, String ntwrkInterfaceId) {
        try {
            this.amznEc2.associateAddress((new AssociateAddressRequest()).withAllocationId(allocationId).withNetworkInterfaceId(ntwrkInterfaceId).withInstanceId(instanceId).withPrivateIpAddress(privateIp));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public void disassociateAddresses(String associationId) {
        try {
            this.amznEc2.disassociateAddress((new DisassociateAddressRequest()).withAssociationId(associationId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public List getAddresses() {
        try {
            return this.amznEc2.describeAddresses().getAddresses();
        } catch (Exception ex) {
            Loggers.error(ex);
            return null;
        }
    }

    public void releaseAddresses(String allocationId) {
        try {
            this.amznEc2.releaseAddress((new ReleaseAddressRequest()).withAllocationId(allocationId));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public NatGateway createNatGateways(String natGtewayId, String alloctionId, String subnetId) {
        NatGateway natGateway = null;
        try {
            natGateway = this.amznEc2.createNatGateway((new CreateNatGatewayRequest()).withAllocationId(alloctionId).withSubnetId(subnetId)).getNatGateway();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { natGateway.getNatGatewayId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(natGtewayId) }));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return natGateway;
    }

    public NatGateway getNatGatewaysByFilters(String natGteway) {
        NatGateway natGateway = null;
        try {
            natGateway = this.amznEc2.describeNatGateways((new DescribeNatGatewaysRequest()).withFilter(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { natGteway }) })).getNatGateways().get(0);
        } catch (Exception exception) {}
        return natGateway;
    }

    public NatGateway getNatGatewaysById(String natGtewayId) {
        NatGateway natGateway = null;
        try {
            natGateway = this.amznEc2.describeNatGateways((new DescribeNatGatewaysRequest()).withNatGatewayIds(new String[] { natGtewayId })).getNatGateways().get(0);
        } catch (Exception exception) {}
        return natGateway;
    }

    public NetworkAcl createNetworkAcls(String ntwrkAclId, String vpcId) {
        NetworkAcl networkAcl = null;
        try {
            networkAcl = this.amznEc2.createNetworkAcl((new CreateNetworkAclRequest()).withVpcId(vpcId)).getNetworkAcl();
            ThreadSleep.sleep(3000L);
            this.amznEc2.createTags((new CreateTagsRequest()).withResources(new String[] { networkAcl.getNetworkAclId() }).withTags(new Tag[] { (new Tag()).withKey("Name").withValue(ntwrkAclId) }));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return networkAcl;
    }

    public NetworkAcl getNetworkAclsByFilters(String ntwrkAcls) {
        NetworkAcl networkAcl = null;
        try {
            networkAcl = this.amznEc2.describeNetworkAcls((new DescribeNetworkAclsRequest()).withFilters(new Filter[] { (new Filter()).withName("tag:Name").withValues(new String[] { ntwrkAcls }) })).getNetworkAcls().get(0);
        } catch (Exception exception) {}
        return networkAcl;
    }

    public NetworkAcl getNetworkAclsById(String ntworkAclId) {
        NetworkAcl networkAcl = null;
        try {
            networkAcl = this.amznEc2.describeNetworkAcls((new DescribeNetworkAclsRequest()).withNetworkAclIds(new String[] { ntworkAclId })).getNetworkAcls().get(0);
        } catch (Exception exception) {}
        return networkAcl;
    }

    public void createNetworkAclsEntry(String ntwrkAclId, String blocks, int rulesNumber, RuleAction rulesAct, String protocols, boolean egressUpdated) {
        try {
            this.amznEc2.createNetworkAclEntry((new CreateNetworkAclEntryRequest()).withNetworkAclId(ntwrkAclId).withCidrBlock(blocks).withRuleNumber(Integer.valueOf(rulesNumber)).withRuleAction(rulesAct).withProtocol(protocols).withEgress(Boolean.valueOf(egressUpdated)));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
    }

    public String authorizeSecurityGroups(String scrtyGroupName, String vpcId, String protocls, String blocks, int frmPort, int toPort) {
        String str = null;
        try {
            str = this.amznEc2.createSecurityGroup((new CreateSecurityGroupRequest()).withDescription(scrtyGroupName + " Security Group").withGroupName(scrtyGroupName).withVpcId(vpcId)).getGroupId();
            ThreadSleep.sleep(3000L);
            this.amznEc2.authorizeSecurityGroupIngress((new AuthorizeSecurityGroupIngressRequest()).withGroupId(str).withIpPermissions(new IpPermission[] { (new IpPermission()).withIpProtocol(protocls).withFromPort(Integer.valueOf(frmPort)).withToPort(Integer.valueOf(toPort)).withIpv4Ranges(new IpRange[] { (new IpRange()).withCidrIp(blocks) }) }));
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return str;
    }

    public void deleteSecurityGroups(String groupName) {
        this.amznEc2.deleteSecurityGroup((new DeleteSecurityGroupRequest()).withGroupName(groupName));
    }

    public SecurityGroup getSecurityGroupsByName(String groupName) {
        SecurityGroup securityGroup = null;
        try {
            List<SecurityGroup> list = this.amznEc2.describeSecurityGroups(new DescribeSecurityGroupsRequest()).getSecurityGroups();
            for (SecurityGroup sg : list) {
                if (sg.getGroupName().equals(groupName)) {
                    securityGroup = sg;
                    break;
                }
            }
        } catch (Exception exception) {}
        return securityGroup;
    }

    public SecurityGroup getSecurityGroupsById(String groupIds) {
        SecurityGroup securityGroup = null;
        try {
            securityGroup = this.amznEc2.describeSecurityGroups((new DescribeSecurityGroupsRequest()).withGroupIds(new String[] { groupIds })).getSecurityGroups().get(0);
        } catch (Exception exception) {}
        return securityGroup;
    }

    public String getImagesOs(String os) {
        DescribeImagesRequest describeImgRequest = new DescribeImagesRequest();
        describeImgRequest.getFilters().add((new Filter()).withName("architecture").withValues(new String[] { "x86_64" }));
        if ("centos-6".equalsIgnoreCase(os)) {
            describeImgRequest.getFilters().add((new Filter()).withName("name").withValues(new String[] { "ultraserve-centos-6.9-ami-nat-hvm-2018*" }));
        } else {
            describeImgRequest.getFilters().add((new Filter()).withName("name").withValues(new String[] { "ultraserve-centos-7.4-ami-nat-hvm-2018*" }));
        }
        DescribeImagesResult describeImagesResult = this.amznEc2.describeImages(describeImgRequest);
        return ((Image)describeImagesResult.getImages().get(0)).getImageId();
    }

    public List collectInstanceType() {
        return (List)Stream.<InstanceType>of(InstanceType.values()).map(Enum::name).collect(Collectors.toList());
    }

    public Enum getInstanceType(String instnceType) {
        switch (instnceType) {
            case "T1Micro":{
                return (Enum)InstanceType.T1Micro;
            }
            case "T2Nano":{
                return (Enum)InstanceType.T2Nano;
            }
            case "T2Micro":{
                return (Enum)InstanceType.T2Micro;
            }
            case "T2Small":{
                return (Enum)InstanceType.T2Small;
            }
            case "T2Medium":{
                return (Enum)InstanceType.T2Medium;
            }
            case "T2Large":{
                return (Enum)InstanceType.T2Large;
            }
            case "T2Xlarge":{
                return (Enum)InstanceType.T2Xlarge;
            }
            case "T22xlarge":{
                return (Enum)InstanceType.T22xlarge;
            }
            case "T3Nano":{
                return (Enum)InstanceType.T3Nano;
            }
            case "T3Micro":{
                return (Enum)InstanceType.T3Micro;
            }
            case "T3Small":{
                return (Enum)InstanceType.T3Small;
            }
            case "T3Medium":{
                return (Enum)InstanceType.T3Medium;
            }
            case "T3Large":{
                return (Enum)InstanceType.T3Large;
            }
            case "T3Xlarge":{
                return (Enum)InstanceType.T3Xlarge;
            }
            case "T32xlarge":{
                return (Enum)InstanceType.T32xlarge;
            }
            case "T3aNano":{
                return (Enum)InstanceType.T3aNano;
            }
            case "T3aMicro":{
                return (Enum)InstanceType.T3aMicro;
            }
            case "T3aSmall":{
                return (Enum)InstanceType.T3aSmall;
            }
            case "T3aMedium":{
                return (Enum)InstanceType.T3aMedium;
            }
            case "T3aLarge":{
                return (Enum)InstanceType.T3aLarge;
            }
            case "T3aXlarge":{
                return (Enum)InstanceType.T3aXlarge;
            }
            case "T3a2xlarge":{
                return (Enum)InstanceType.T3a2xlarge;
            }
            case "M1Small":{
                return (Enum)InstanceType.M1Small;
            }
            case "M1Medium":{
                return (Enum)InstanceType.M1Medium;
            }
            case "M1Large":{
                return (Enum)InstanceType.M1Large;
            }
            case "M1Xlarge":{
                return (Enum)InstanceType.M1Xlarge;
            }
            case "M3Medium":{
                return (Enum)InstanceType.M3Medium;
            }
            case "M3Large":{
                return (Enum)InstanceType.M3Large;
            }
            case "M3Xlarge":{
                return (Enum)InstanceType.M3Xlarge;
            }
            case "M32xlarge":{
                return (Enum)InstanceType.M32xlarge;
            }
            case "M4Large":{
                return (Enum)InstanceType.M4Large;
            }
            case "M4Xlarge":{
                return (Enum)InstanceType.M4Xlarge;
            }
            case "M42xlarge":{
                return (Enum)InstanceType.M42xlarge;
            }
            case "M44xlarge":{
                return (Enum)InstanceType.M44xlarge;
            }
            case "M410xlarge":{
                return (Enum)InstanceType.M410xlarge;
            }
            case "M416xlarge":{
                return (Enum)InstanceType.M416xlarge;
            }
            case "M2Xlarge":{
                return (Enum)InstanceType.M2Xlarge;
            }
            case "M22xlarge":{
                return (Enum)InstanceType.M22xlarge;
            }
            case "M24xlarge":{
                return (Enum)InstanceType.M24xlarge;
            }
            case "Cr18xlarge":{
                return (Enum)InstanceType.Cr18xlarge;
            }
            case "R3Large":{
                return (Enum)InstanceType.R3Large;
            }
            case "R3Xlarge":{
                return (Enum)InstanceType.R3Xlarge;
            }
            case "R32xlarge":{
                return (Enum)InstanceType.R32xlarge;
            }
            case "R34xlarge":{
                return (Enum)InstanceType.R34xlarge;
            }
            case "R38xlarge":{
                return (Enum)InstanceType.R38xlarge;
            }
            case "R4Large":{
                return (Enum)InstanceType.R4Large;
            }
            case "R4Xlarge":{
                return (Enum)InstanceType.R4Xlarge;
            }
            case "R42xlarge":{
                return (Enum)InstanceType.R42xlarge;
            }
            case "R44xlarge":{
                return (Enum)InstanceType.R44xlarge;
            }
            case "R48xlarge":{
                return (Enum)InstanceType.R48xlarge;
            }
            case "R416xlarge":{
                return (Enum)InstanceType.R416xlarge;
            }
            case "R5Large":{
                return (Enum)InstanceType.R5Large;
            }
            case "R5Xlarge":{
                return (Enum)InstanceType.R5Xlarge;
            }
            case "R52xlarge":{
                return (Enum)InstanceType.R52xlarge;
            }
            case "R54xlarge":{
                return (Enum)InstanceType.R54xlarge;
            }
            case "R58xlarge":{
                return (Enum)InstanceType.R58xlarge;
            }
            case "R512xlarge":{
                return (Enum)InstanceType.R512xlarge;
            }
            case "R516xlarge":{
                return (Enum)InstanceType.R516xlarge;
            }
            case "R524xlarge":{
                return (Enum)InstanceType.R524xlarge;
            }
            case "R5Metal":{
                return (Enum)InstanceType.R5Metal;
            }
            case "R5aLarge":{
                return (Enum)InstanceType.R5aLarge;
            }
            case "R5aXlarge":{
                return (Enum)InstanceType.R5aXlarge;
            }
            case "R5a2xlarge":{
                return (Enum)InstanceType.R5a2xlarge;
            }
            case "R5a4xlarge":{
                return (Enum)InstanceType.R5a4xlarge;
            }
            case "R5a8xlarge":{
                return (Enum)InstanceType.R5a8xlarge;
            }
            case "R5a12xlarge":{
                return (Enum)InstanceType.R5a12xlarge;
            }
            case "R5a16xlarge":{
                return (Enum)InstanceType.R5a16xlarge;
            }
            case "R5a24xlarge":{
                return (Enum)InstanceType.R5a24xlarge;
            }
            case "R5dLarge":{
                return (Enum)InstanceType.R5dLarge;
            }
            case "R5dXlarge":{
                return (Enum)InstanceType.R5dXlarge;
            }
            case "R5d2xlarge":{
                return (Enum)InstanceType.R5d2xlarge;
            }
            case "R5d4xlarge":{
                return (Enum)InstanceType.R5d4xlarge;
            }
            case "R5d8xlarge":{
                return (Enum)InstanceType.R5d8xlarge;
            }
            case "R5d12xlarge":{
                return (Enum)InstanceType.R5d12xlarge;
            }
            case "R5d16xlarge":{
                return (Enum)InstanceType.R5d16xlarge;
            }
            case "R5d24xlarge":{
                return (Enum)InstanceType.R5d24xlarge;
            }
            case "R5dMetal":{
                return (Enum)InstanceType.R5dMetal;
            }
            case "R5adLarge":{
                return (Enum)InstanceType.R5adLarge;
            }
            case "R5adXlarge":{
                return (Enum)InstanceType.R5adXlarge;
            }
            case "R5ad2xlarge":{
                return (Enum)InstanceType.R5ad2xlarge;
            }
            case "R5ad4xlarge":{
                return (Enum)InstanceType.R5ad4xlarge;
            }
            case "R5ad8xlarge":{
                return (Enum)InstanceType.R5ad8xlarge;
            }
            case "R5ad12xlarge":{
                return (Enum)InstanceType.R5ad12xlarge;
            }
            case "R5ad16xlarge":{
                return (Enum)InstanceType.R5ad16xlarge;
            }
            case "R5ad24xlarge":{
                return (Enum)InstanceType.R5ad24xlarge;
            }
            case "X116xlarge":{
                return (Enum)InstanceType.X116xlarge;
            }
            case "X132xlarge":{
                return (Enum)InstanceType.X132xlarge;
            }
            case "X1eXlarge":{
                return (Enum)InstanceType.X1eXlarge;
            }
            case "X1e2xlarge":{
                return (Enum)InstanceType.X1e2xlarge;
            }
            case "X1e4xlarge":{
                return (Enum)InstanceType.X1e4xlarge;
            }
            case "X1e8xlarge":{
                return (Enum)InstanceType.X1e8xlarge;
            }
            case "X1e16xlarge":{
                return (Enum)InstanceType.X1e16xlarge;
            }
            case "X1e32xlarge":{
                return (Enum)InstanceType.X1e32xlarge;
            }
            case "I2Xlarge":{
                return (Enum)InstanceType.I2Xlarge;
            }
            case "I22xlarge":{
                return (Enum)InstanceType.I22xlarge;
            }
            case "I24xlarge":{
                return (Enum)InstanceType.I24xlarge;
            }
            case "I28xlarge":{
                return (Enum)InstanceType.I28xlarge;
            }
            case "I3Large":{
                return (Enum)InstanceType.I3Large;
            }
            case "I3Xlarge":{
                return (Enum)InstanceType.I3Xlarge;
            }
            case "I32xlarge":{
                return (Enum)InstanceType.I32xlarge;
            }
            case "I34xlarge":{
                return (Enum)InstanceType.I34xlarge;
            }
            case "I38xlarge":{
                return (Enum)InstanceType.I38xlarge;
            }
            case "I316xlarge":{
                return (Enum)InstanceType.I316xlarge;
            }
            case "I3Metal":{
                return (Enum)InstanceType.I3Metal;
            }
            case "I3enLarge":{
                return (Enum)InstanceType.I3enLarge;
            }
            case "I3enXlarge":{
                return (Enum)InstanceType.I3enXlarge;
            }
            case "I3en2xlarge":{
                return (Enum)InstanceType.I3en2xlarge;
            }
            case "I3en3xlarge":{
                return (Enum)InstanceType.I3en3xlarge;
            }
            case "I3en6xlarge":{
                return (Enum)InstanceType.I3en6xlarge;
            }
            case "I3en12xlarge":{
                return (Enum)InstanceType.I3en12xlarge;
            }
            case "I3en24xlarge":{
                return (Enum)InstanceType.I3en24xlarge;
            }
            case "I3enMetal":{
                return (Enum)InstanceType.I3enMetal;
            }
            case "Hi14xlarge":{
                return (Enum)InstanceType.Hi14xlarge;
            }
            case "Hs18xlarge":{
                return (Enum)InstanceType.Hs18xlarge;
            }
            case "C1Medium":{
                return (Enum)InstanceType.C1Medium;
            }
            case "C1Xlarge":{
                return (Enum)InstanceType.C1Xlarge;
            }
            case "C3Large":{
                return (Enum)InstanceType.C3Large;
            }
            case "C3Xlarge":{
                return (Enum)InstanceType.C3Xlarge;
            }
            case "C32xlarge":{
                return (Enum)InstanceType.C32xlarge;
            }
            case "C34xlarge":{
                return (Enum)InstanceType.C34xlarge;
            }
            case "C38xlarge":{
                return (Enum)InstanceType.C38xlarge;
            }
            case "C4Large":{
                return (Enum)InstanceType.C4Large;
            }
            case "C4Xlarge":{
                return (Enum)InstanceType.C4Xlarge;
            }
            case "C42xlarge":{
                return (Enum)InstanceType.C42xlarge;
            }
            case "C44xlarge":{
                return (Enum)InstanceType.C44xlarge;
            }
            case "C48xlarge":{
                return (Enum)InstanceType.C48xlarge;
            }
            case "C5Large":{
                return (Enum)InstanceType.C5Large;
            }
            case "C5Xlarge":{
                return (Enum)InstanceType.C5Xlarge;
            }
            case "C52xlarge":{
                return (Enum)InstanceType.C52xlarge;
            }
            case "C54xlarge":{
                return (Enum)InstanceType.C54xlarge;
            }
            case "C59xlarge":{
                return (Enum)InstanceType.C59xlarge;
            }
            case "C512xlarge":{
                return (Enum)InstanceType.C512xlarge;
            }
            case "C518xlarge":{
                return (Enum)InstanceType.C518xlarge;
            }
            case "C524xlarge":{
                return (Enum)InstanceType.C524xlarge;
            }
            case "C5Metal":{
                return (Enum)InstanceType.C5Metal;
            }
            case "C5dLarge":{
                return (Enum)InstanceType.C5dLarge;
            }
            case "C5dXlarge":{
                return (Enum)InstanceType.C5dXlarge;
            }
            case "C5d2xlarge":{
                return (Enum)InstanceType.C5d2xlarge;
            }
            case "C5d4xlarge":{
                return (Enum)InstanceType.C5d4xlarge;
            }
            case "C5d9xlarge":{
                return (Enum)InstanceType.C5d9xlarge;
            }
            case "C5d18xlarge":{
                return (Enum)InstanceType.C5d18xlarge;
            }
            case "C5nLarge":{
                return (Enum)InstanceType.C5nLarge;
            }
            case "C5nXlarge":{
                return (Enum)InstanceType.C5nXlarge;
            }
            case "C5n2xlarge":{
                return (Enum)InstanceType.C5n2xlarge;
            }
            case "C5n4xlarge":{
                return (Enum)InstanceType.C5n4xlarge;
            }
            case "C5n9xlarge":{
                return (Enum)InstanceType.C5n9xlarge;
            }
            case "C5n18xlarge":{
                return (Enum)InstanceType.C5n18xlarge;
            }
            case "Cc14xlarge":{
                return (Enum)InstanceType.Cc14xlarge;
            }
            case "Cc28xlarge":{
                return (Enum)InstanceType.Cc28xlarge;
            }
            case "G22xlarge":{
                return (Enum)InstanceType.G22xlarge;
            }
            case "G28xlarge":{
                return (Enum)InstanceType.G28xlarge;
            }
            case "G34xlarge":{
                return (Enum)InstanceType.G34xlarge;
            }
            case "G38xlarge":{
                return (Enum)InstanceType.G38xlarge;
            }
            case "G316xlarge":{
                return (Enum)InstanceType.G316xlarge;
            }
            case "G3sXlarge":{
                return (Enum)InstanceType.G3sXlarge;
            }
            case "Cg14xlarge":{
                return (Enum)InstanceType.Cg14xlarge;
            }
            case "P2Xlarge":{
                return (Enum)InstanceType.P2Xlarge;
            }
            case "P28xlarge":{
                return (Enum)InstanceType.P28xlarge;
            }
            case "P216xlarge":{
                return (Enum)InstanceType.P216xlarge;
            }
            case "P32xlarge":{
                return (Enum)InstanceType.P32xlarge;
            }
            case "P38xlarge":{
                return (Enum)InstanceType.P38xlarge;
            }
            case "P316xlarge":{
                return (Enum)InstanceType.P316xlarge;
            }
            case "P3dn24xlarge":{
                return (Enum)InstanceType.P3dn24xlarge;
            }
            case "D2Xlarge":{
                return (Enum)InstanceType.D2Xlarge;
            }
            case "D22xlarge":{
                return (Enum)InstanceType.D22xlarge;
            }
            case "D24xlarge":{
                return (Enum)InstanceType.D24xlarge;
            }
            case "D28xlarge":{
                return (Enum)InstanceType.D28xlarge;
            }
            case "F12xlarge":{
                return (Enum)InstanceType.F12xlarge;
            }
            case "F14xlarge":{
                return (Enum)InstanceType.F14xlarge;
            }
            case "F116xlarge":{
                return (Enum)InstanceType.F116xlarge;
            }
            case "M5Large":{
                return (Enum)InstanceType.M5Large;
            }
            case "M5Xlarge":{
                return (Enum)InstanceType.M5Xlarge;
            }
            case "M52xlarge":{
                return (Enum)InstanceType.M52xlarge;
            }
            case "M54xlarge":{
                return (Enum)InstanceType.M54xlarge;
            }
            case "M58xlarge":{
                return (Enum)InstanceType.M58xlarge;
            }
            case "M512xlarge":{
                return (Enum)InstanceType.M512xlarge;
            }
            case "M516xlarge":{
                return (Enum)InstanceType.M516xlarge;
            }
            case "M524xlarge":{
                return (Enum)InstanceType.M524xlarge;
            }
            case "M5Metal":{
                return (Enum)InstanceType.M5Metal;
            }
            case "M5aLarge":{
                return (Enum)InstanceType.M5aLarge;
            }
            case "M5aXlarge":{
                return (Enum)InstanceType.M5aXlarge;
            }
            case "M5a2xlarge":{
                return (Enum)InstanceType.M5a2xlarge;
            }
            case "M5a4xlarge":{
                return (Enum)InstanceType.M5a4xlarge;
            }
            case "M5a8xlarge":{
                return (Enum)InstanceType.M5a8xlarge;
            }
            case "M5a12xlarge":{
                return (Enum)InstanceType.M5a12xlarge;
            }
            case "M5a16xlarge":{
                return (Enum)InstanceType.M5a16xlarge;
            }
            case "M5a24xlarge":{
                return (Enum)InstanceType.M5a24xlarge;
            }
            case "M5dLarge":{
                return (Enum)InstanceType.M5dLarge;
            }
            case "M5dXlarge":{
                return (Enum)InstanceType.M5dXlarge;
            }
            case "M5d2xlarge":{
                return (Enum)InstanceType.M5d2xlarge;
            }
            case "M5d4xlarge":{
                return (Enum)InstanceType.M5d4xlarge;
            }
            case "M5d8xlarge":{
                return (Enum)InstanceType.M5d8xlarge;
            }
            case "M5d12xlarge":{
                return (Enum)InstanceType.M5d12xlarge;
            }
            case "M5d16xlarge":{
                return (Enum)InstanceType.M5d16xlarge;
            }
            case "M5d24xlarge":{
                return (Enum)InstanceType.M5d24xlarge;
            }
            case "M5dMetal":{
                return (Enum)InstanceType.M5dMetal;
            }
            case "M5adLarge":{
                return (Enum)InstanceType.M5adLarge;
            }
            case "M5adXlarge":{
                return (Enum)InstanceType.M5adXlarge;
            }
            case "M5ad2xlarge":{
                return (Enum)InstanceType.M5ad2xlarge;
            }
            case "M5ad4xlarge":{
                return (Enum)InstanceType.M5ad4xlarge;
            }
            case "M5ad8xlarge":{
                return (Enum)InstanceType.M5ad8xlarge;
            }
            case "M5ad12xlarge":{
                return (Enum)InstanceType.M5ad12xlarge;
            }
            case "M5ad16xlarge":{
                return (Enum)InstanceType.M5ad16xlarge;
            }
            case "M5ad24xlarge":{
                return (Enum)InstanceType.M5ad24xlarge;
            }
            case "H12xlarge":{
                return (Enum)InstanceType.H12xlarge;
            }
            case "H14xlarge":{
                return (Enum)InstanceType.H14xlarge;
            }
            case "H18xlarge":{
                return (Enum)InstanceType.H18xlarge;
            }
            case "H116xlarge":{
                return (Enum)InstanceType.H116xlarge;
            }
            case "Z1dLarge":{
                return (Enum)InstanceType.Z1dLarge;
            }
            case "Z1dXlarge":{
                return (Enum)InstanceType.Z1dXlarge;
            }
            case "Z1d2xlarge":{
                return (Enum)InstanceType.Z1d2xlarge;
            }
            case "Z1d3xlarge":{
                return (Enum)InstanceType.Z1d3xlarge;
            }
            case "Z1d6xlarge":{
                return (Enum)InstanceType.Z1d6xlarge;
            }
            case "Z1d12xlarge":{
                return (Enum)InstanceType.Z1d12xlarge;
            }
            case "Z1dMetal":{
                return (Enum)InstanceType.Z1dMetal;
            }
            case "U6tb1Metal":{
                return (Enum)InstanceType.U6tb1Metal;
            }
            case "U9tb1Metal":{
                return (Enum)InstanceType.U9tb1Metal;
            }
            case "U12tb1Metal":{
                return (Enum)InstanceType.U12tb1Metal;
            }
            case "A1Medium":{
                return (Enum)InstanceType.A1Medium;
            }
            case "A1Large":{
                return (Enum)InstanceType.A1Large;
            }
            case "A1Xlarge":{
                return (Enum)InstanceType.A1Xlarge;
            }
            case "A12xlarge":{
                return (Enum)InstanceType.A12xlarge;
            }
            case "A14xlarge":{
                return (Enum)InstanceType.A14xlarge;
            }
        }
        return (Enum)InstanceType.T2Micro;
    }

    public boolean authenticate(String regions) {
        try {
            ClientConfiguration clientCoonfig = new ClientConfiguration();
            clientCoonfig.setResponseMetadataCacheSize(0);
            if (this.account.proxyIp != null && Matcher.pat2(this.account.proxyIp)) {
                clientCoonfig.setProxyProtocol(Protocol.HTTP);
                clientCoonfig.withProxyHost(this.account.proxyIp).withProxyPort(TypesParser.safeParseInt(this.account.proxyPort));
                if (this.account.proxyUsername != null && !"".equals(this.account.proxyUsername) && this.account.proxyPassword != null && !"".equals(this.account.proxyPassword)){
                    clientCoonfig.withProxyUsername(this.account.proxyUsername).withProxyPassword(this.account.proxyPassword);
                }
            }
            this.amznEc2 = (AmazonEC2)((AmazonEC2ClientBuilder)((AmazonEC2ClientBuilder)((AmazonEC2ClientBuilder)AmazonEC2ClientBuilder.standard().withCredentials((AWSCredentialsProvider)new AWSStaticCredentialsProvider((AWSCredentials)new BasicAWSCredentials(this.account.accessKey, this.account.secretKey)))).withClientConfiguration(clientCoonfig)).withRegion(regions)).build();
        } catch (Throwable th) {
            Loggers.error(th);
            return false;
        }
        return true;
    }

    public Ec2Manager(AwsAccount account) {
        this.account = account;
    }

    public AwsAccount getAccount() {
        return account;
    }

    public AmazonEC2 getEc2Client() {
        return amznEc2;
    }

    public void setEc2Client(AmazonEC2 amznEc2) {
        this.amznEc2 = amznEc2;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Ec2Manager))
            return false;
        Ec2Manager do1 = (Ec2Manager)paramObject;
        if (!do1.exists(this))
            return false;
        AwsAccount new1 = getAccount();
        AwsAccount new2 = do1.getAccount();
        if ((new1 == null) ? (new2 != null) : !new1.equals(new2))
            return false;
        AmazonEC2 amazonEC21 = getEc2Client();
        AmazonEC2 amazonEC22 = do1.getEc2Client();
        return !((amazonEC21 == null) ? (amazonEC22 != null) : !amazonEC21.equals(amazonEC22));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Ec2Manager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AwsAccount new1 = getAccount();
        n = n * 59 + ((new1 == null) ? 43 : new1.hashCode());
        AmazonEC2 amazonEC2 = getEc2Client();
        return n * 59 + ((amazonEC2 == null) ? 43 : amazonEC2.hashCode());
    }

    @Override
    public String toString() {
        return "Ec2Manager(account=" + getAccount() + ", amznEc2=" + getEc2Client() + ")";
    }
}
