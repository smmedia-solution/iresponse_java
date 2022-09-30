package tech.iresponse.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPPrefix;
import com.microsoft.azure.management.network.PublicIPPrefixSku;
import com.microsoft.azure.management.network.PublicIPPrefixSkuName;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.LogLevel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import tech.iresponse.models.admin.AzureAccount;
import tech.iresponse.logging.Loggers;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.azure.vm.AzureVMConfig;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.http.HttpEnumeration;

public class AzureManager {

    private AzureAccount account;
    private Azure azure;

    public VirtualMachine createVirtualMachines(AzureVMConfig vmConfig, NetworkInterface ntwrkIntrface) {
        try {
            return createVirtualMachines(vmConfig.getResourceGroupName(), vmConfig.getRegion(), vmConfig.getVirtualMachineName(), ntwrkIntrface, vmConfig.getVirtualMachinePublisher(), vmConfig.getVirtualMachineOffer(), vmConfig.getVirtualMachineSku(), vmConfig.getVirtualMachineSize(), vmConfig.getVirtualMachineComputerName(), vmConfig.getVirtualMachineUsername(), vmConfig.getVirtualMachinePassword());
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public VirtualMachine createVirtualMachines(AzureVMConfig vmConfig, List ntwrkIntrface) {
        try {
            return createVirtualMachines(vmConfig.getResourceGroupName(), vmConfig.getRegion(), vmConfig.getVirtualMachineName(), ntwrkIntrface, vmConfig.getVirtualMachinePublisher(), vmConfig.getVirtualMachineOffer(), vmConfig.getVirtualMachineSku(), vmConfig.getVirtualMachineSize(), vmConfig.getVirtualMachineComputerName(), vmConfig.getVirtualMachineUsername(), vmConfig.getVirtualMachinePassword());
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public VirtualMachine createVirtualMachines(String resourceGrp, String region, String vmName, List<NetworkInterface> ntwrkIntrface, String vmPublisher, String vmOffer, String vmSku, String vmSize, String vmComputerNam, String vmUsername, String vmPassword) {
        try {
            if (ntwrkIntrface == null || ntwrkIntrface.isEmpty()){
                throw new DatabaseException("No network interfaces found for this virtual machine !");
            }
            VirtualMachine.DefinitionStages.WithCreate wthCreate = ((VirtualMachine.DefinitionStages.WithNetwork)((VirtualMachine.DefinitionStages.WithGroup)((VirtualMachine.DefinitionStages.Blank)this.azure.virtualMachines().define(vmName)).withRegion(region)).withExistingResourceGroup(resourceGrp)).withExistingPrimaryNetworkInterface(ntwrkIntrface.get(0)).withLatestLinuxImage(vmPublisher, vmOffer, vmSku).withRootUsername(vmUsername).withRootPassword(vmPassword).withComputerName(vmComputerNam).withOSDiskName(vmName + "_disk").withSize(vmSize);
            if (ntwrkIntrface.size() > 1){
                for (int i = 1; i < ntwrkIntrface.size(); i++){
                    wthCreate = wthCreate.withExistingSecondaryNetworkInterface(ntwrkIntrface.get(i));
                }
            }
            return (VirtualMachine)wthCreate.create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public VirtualMachine createVirtualMachines(String resourceGrp, String region, String vmName, NetworkInterface ntwrkIntrface, String vmPublisher, String vmOffer, String vmSku, String vmSize, String vmComputerNam, String vmUsername, String vmPassword) {
        try {
            return (VirtualMachine)((VirtualMachine.DefinitionStages.WithNetwork)((VirtualMachine.DefinitionStages.WithGroup)((VirtualMachine.DefinitionStages.Blank)this.azure.virtualMachines().define(vmName)).withRegion(region)).withExistingResourceGroup(resourceGrp)).withExistingPrimaryNetworkInterface(ntwrkIntrface).withLatestLinuxImage(vmPublisher, vmOffer, vmSku).withRootUsername(vmUsername).withRootPassword(vmPassword).withComputerName(vmComputerNam).withOSDiskName(vmName + "_disk").withSize(vmSize).create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public List getVirtualMachines(String resourceGrp) {
        try {
            return (List)this.azure.virtualMachines().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public VirtualMachine getVirtualMachines(String resourceGrp, String instanceName) {
        try {
            return (VirtualMachine)this.azure.virtualMachines().getByResourceGroup(resourceGrp, instanceName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public VirtualMachine getVirtualMachinesById(String vmId) {
        try {
            return (VirtualMachine)this.azure.virtualMachines().getById(vmId);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deleteVirtualMachines(String resourceGrp, String region) {
        try {
            this.azure.virtualMachines().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteVirtualMachinesById(String vmId) {
        try {
            this.azure.virtualMachines().deleteById(vmId);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteVmDisks(String resourceGrp, String region) {
        try {
            this.azure.disks().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean executeActions(String resourceGrp, String instanceName, String action, boolean changeIps) {
        try {
            VirtualMachine vm = getVirtualMachines(resourceGrp, instanceName);
            if (vm != null) {
                switch (action) {
                    case "stop":{
                        if ("PowerState/running".equalsIgnoreCase(vm.powerState().toString())) {
                            if (changeIps){
                                vm.deallocate();
                            }
                            vm.powerOff();
                        }
                        break;
                    }
                    case "start":{
                        if ("PowerState/stopped".equalsIgnoreCase(vm.powerState().toString())){
                            vm.start();
                        }
                        break;
                    }
                    case "restart":{
                        if ("PowerState/running".equalsIgnoreCase(vm.powerState().toString())) {
                            if (changeIps){
                                vm.deallocate();
                            }
                            vm.powerOff();
                            vm.start();
                        }
                        break;
                    }
                }
                return true;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return false;
    }

    public NetworkSecurityGroup createNetworkSecurityGroups(String resourceGrpName, String region, String ntworkSecurityGrpName) {
        try {
            return (NetworkSecurityGroup)((NetworkSecurityGroup.DefinitionStages.WithCreate)((NetworkSecurityGroup.DefinitionStages.WithCreate)((NetworkSecurityGroup.DefinitionStages.WithCreate)((NetworkSecurityGroup.DefinitionStages.WithGroup)((NetworkSecurityGroup.DefinitionStages.Blank)this.azure.networkSecurityGroups().define(ntworkSecurityGrpName)).withRegion(region)).withNewResourceGroup(resourceGrpName)).defineRule(ntworkSecurityGrpName + "_inbound").allowInbound().fromAnyAddress().fromAnyPort().toAnyAddress().toAnyPort().withAnyProtocol().attach()).defineRule(ntworkSecurityGrpName + "_outbound").allowOutbound().fromAnyAddress().fromAnyPort().toAnyAddress().toAnyPort().withAnyProtocol().attach()).create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public List getNetworkSecurityGroups(String resourceGrp) {
        try {
            return (List)this.azure.networkSecurityGroups().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public NetworkSecurityGroup getNetworkSecurityGroups(String resourceGrpName, String ntworkSecrityGrpName) {
        try {
            return (NetworkSecurityGroup)this.azure.networkSecurityGroups().getByResourceGroup(resourceGrpName, ntworkSecrityGrpName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public NetworkSecurityGroup getNetworkSecurityGroupsById(String networkSecGrpsId) {
        try {
            return (NetworkSecurityGroup)this.azure.networkSecurityGroups().getById(networkSecGrpsId);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deleteNetworkSecurityGroups(String resourceGrp, String region) {
        try {
            this.azure.networkSecurityGroups().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteNetworkSecurityGroupsById(String networkSecGrpsId) {
        try {
            this.azure.networkSecurityGroups().deleteById(networkSecGrpsId);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public Network createNetworks(String resourceGrpName, String resgion, String ntwrkName, String ntwrkCidr, String subntName, String subntCidr) {
        try {
            return (Network)((Network.DefinitionStages.WithCreate)((Network.DefinitionStages.WithGroup)((Network.DefinitionStages.Blank)this.azure.networks().define(ntwrkName)).withRegion(resgion)).withExistingResourceGroup(resourceGrpName)).withAddressSpace(ntwrkCidr).withSubnet(subntName, subntCidr).create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public Network createNetworks(String resourceGrp, String region, String ntwrkName, String adressSpce, List<String> subnts) {
        try {
            Network.DefinitionStages.WithCreateAndSubnet wthCrateAndSubnt = ((Network.DefinitionStages.WithCreate)((Network.DefinitionStages.WithGroup)((Network.DefinitionStages.Blank)this.azure.networks().define(ntwrkName)).withRegion(region)).withExistingResourceGroup(resourceGrp)).withAddressSpace(adressSpce);
            int i = 1;
            for (String str : subnts) {
                wthCrateAndSubnt = (Network.DefinitionStages.WithCreateAndSubnet)wthCrateAndSubnt.defineSubnet(str).withAddressPrefix("10.0." + i + ".0/24").attach();
                i++;
            }
            return (Network)wthCrateAndSubnt.create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public List getNetworks(String resourceGrp) {
        try {
            return (List)this.azure.networks().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public Network getNetworks(String resourceGrpName, String ntwrkName) {
        try {
            return (Network)this.azure.networks().getByResourceGroup(resourceGrpName, ntwrkName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public Network getNetworksById(String ntworkId) {
        try {
            return (Network)this.azure.networks().getById(ntworkId);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deleteNetworks(String resourceGrp, String region) {
        try {
            this.azure.networks().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteNetworksById(String ntworkId) {
        try {
            this.azure.networks().deleteById(ntworkId);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public NetworkInterface createNetworkInterfaces(String resourceGrpName, String region, String ntwrkIntrfaceName, Network ntwork, String subnetName, NetworkSecurityGroup ntworkSecurityGrp, int nbPrivateIps) {
        try {
            PublicIPAddress publcIPAddrss = createPublicIPAddresses(resourceGrpName, region, ntwrkIntrfaceName + "_pip_1", Strings.rndomSalt(8, false), false);
            if (publcIPAddrss != null) {
                NetworkInterface.DefinitionStages.WithCreate wthCreate = ((NetworkInterface.DefinitionStages.WithPrimaryNetwork)((NetworkInterface.DefinitionStages.WithGroup)((NetworkInterface.DefinitionStages.Blank)this.azure.networkInterfaces().define(ntwrkIntrfaceName)).withRegion(region)).withExistingResourceGroup(resourceGrpName)).withExistingPrimaryNetwork(ntwork).withSubnet(subnetName).withPrimaryPrivateIPAddressStatic("10.0.0.4").withExistingPrimaryPublicIPAddress(publcIPAddrss).withExistingNetworkSecurityGroup(ntworkSecurityGrp);
                int nbPrivatesIpStatic = 5;
                for (int i = 1; i < nbPrivateIps; i++) {
                    publcIPAddrss = createPublicIPAddresses(resourceGrpName, region, ntwrkIntrfaceName + "_pip_" + (i + 1), Strings.rndomSalt(8, false), true);
                    if (publcIPAddrss != null) {
                        wthCreate = (NetworkInterface.DefinitionStages.WithCreate)((NicIPConfiguration.DefinitionStages.WithAttach)((NicIPConfiguration.DefinitionStages.WithAttach)wthCreate.defineSecondaryIPConfiguration(ntwrkIntrfaceName + "_ipconf_" + (i + 1)).withExistingNetwork(ntwork).withSubnet(subnetName).withPrivateIPAddressStatic("10.0.0." + nbPrivatesIpStatic)).withExistingPublicIPAddress(publcIPAddrss)).attach();
                        nbPrivatesIpStatic++;
                    }
                }
                return (NetworkInterface)wthCreate.create();
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public NetworkInterface createNetworkInterfaces(AzureVMConfig vmConfig, Network ntwork, NetworkSecurityGroup ntworkSecurityGrp, int nbPrivateIps) {
        try {
            int countNbIps = 1;
            int nbPrivatesIp = nbPrivateIps;
            int nbPips = 1;
            int nbPrefs = 1;
            int prefixLength = 4;
            PublicIPPrefix publcIPPrfixssss = null;
            ArrayList<PublicIPAddress> publcIPAdrsses = new ArrayList();
            String accssTokens = this.getAccessToken();
            switch (vmConfig.getPrefixesLength()) {
                case 28:{
                    prefixLength = 16;
                    break;
                }
                case 29:{
                    prefixLength = 8;
                    break;
                }
                case 30:{
                    prefixLength = 4;
                    break;
                }
                case 31:{
                    prefixLength = 2;
                    break;
                }
            }
            if (nbPrivateIps > prefixLength) {
                countNbIps = nbPrivateIps / prefixLength;
                if (nbPrivateIps % prefixLength != 0){
                    countNbIps++;
                }
            }
            for (int i = 0; i < countNbIps; i++) {
                int length = (nbPrivatesIp >= prefixLength) ? prefixLength : nbPrivatesIp;
                PublicIPPrefix publcIPPrfix = getPublicIPPrefixs(vmConfig.getResourceGroupName(), vmConfig.getNetworkInterfaceName() + "_pref_" + nbPrefs);
                if (publcIPPrfix == null){
                    publcIPPrfix = createPublicIPPrefixs(vmConfig.getResourceGroupName(), vmConfig.getRegion(), vmConfig.getNetworkInterfaceName() + "_pref_" + nbPrefs, vmConfig.getPrefixesLength());
                }
                if (publcIPPrfix != null){
                    for (int j = 0; j < length; j++) {
                        PublicIPAddress publcIPAddrss = asyncPublicIPAddresses(vmConfig.getResourceGroupName(), vmConfig.getRegion(), vmConfig.getNetworkInterfaceName() + "_pip_" + nbPips, publcIPPrfix.id(), Strings.rndomSalt(8, false), accssTokens);
                        if (publcIPAddrss != null){
                            publcIPAdrsses.add(publcIPAddrss);
                        }
                        nbPips++;
                    }
                }
                nbPrivatesIp = (nbPrivatesIp > prefixLength) ? (nbPrivatesIp - prefixLength) : prefixLength;
                nbPrefs++;
            }
            if (!publcIPAdrsses.isEmpty()) {
                NetworkInterface.DefinitionStages.WithCreate wthCreate = ((NetworkInterface.DefinitionStages.WithPrimaryNetwork)((NetworkInterface.DefinitionStages.WithGroup)((NetworkInterface.DefinitionStages.Blank)this.azure.networkInterfaces().define(vmConfig.getNetworkInterfaceName())).withRegion(vmConfig.getRegion())).withExistingResourceGroup(vmConfig.getResourceGroupName())).withExistingPrimaryNetwork(ntwork).withSubnet(vmConfig.getSubnetName()).withPrimaryPrivateIPAddressStatic("10.0.0.4").withExistingPrimaryPublicIPAddress(publcIPAdrsses.get(0)).withExistingNetworkSecurityGroup(ntworkSecurityGrp);
                if (publcIPAdrsses.size() > 1) {
                    int nbPrivateIPAddrssStatic = 5;
                    for (int k = 1; k < publcIPAdrsses.size(); k++) {
                        wthCreate = (NetworkInterface.DefinitionStages.WithCreate)((NicIPConfiguration.DefinitionStages.WithAttach)((NicIPConfiguration.DefinitionStages.WithAttach)wthCreate.defineSecondaryIPConfiguration(vmConfig.getNetworkInterfaceName() + "_ipconf_" + (k + 1)).withExistingNetwork(ntwork).withSubnet(vmConfig.getSubnetName()).withPrivateIPAddressStatic("10.0.0." + nbPrivateIPAddrssStatic)).withExistingPublicIPAddress(publcIPAdrsses.get(k))).attach();
                        nbPrivateIPAddrssStatic++;
                    }
                }
                return (NetworkInterface)wthCreate.create();
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public NetworkInterface createNetworkInterfaces(String resourceGrp, String region, String ntwrkIntrfaceName, Network ntwork, String subnetName, String primaryPrivateIpStatic, boolean hasDynamicIps, NetworkSecurityGroup ntworkSecurityGrp, int nbPrivateIps) {
        try {
            PublicIPAddress publcIPAddrss = createPublicIPAddresses(resourceGrp, region, ntwrkIntrfaceName + "_pip_1", (String)null, !hasDynamicIps);
            if (publcIPAddrss != null) {
                NetworkInterface.DefinitionStages.WithCreate wthCreate = ((NetworkInterface.DefinitionStages.WithPrimaryNetwork)((NetworkInterface.DefinitionStages.WithGroup)((NetworkInterface.DefinitionStages.Blank)this.azure.networkInterfaces().define(ntwrkIntrfaceName)).withRegion(region)).withExistingResourceGroup(resourceGrp)).withExistingPrimaryNetwork(ntwork).withSubnet(subnetName).withPrimaryPrivateIPAddressStatic(primaryPrivateIpStatic + ".4").withExistingPrimaryPublicIPAddress(publcIPAddrss).withExistingNetworkSecurityGroup(ntworkSecurityGrp);
                int count = 5;
                for (int i = 1; i < nbPrivateIps; i++) {
                    publcIPAddrss = createPublicIPAddresses(resourceGrp, region, ntwrkIntrfaceName + "_pip_" + (i + 1), (String)null, true);
                    if (publcIPAddrss != null) {
                        wthCreate = (NetworkInterface.DefinitionStages.WithCreate)((NicIPConfiguration.DefinitionStages.WithAttach)((NicIPConfiguration.DefinitionStages.WithAttach)wthCreate.defineSecondaryIPConfiguration(ntwrkIntrfaceName + "_ipconf_" + (i + 1)).withExistingNetwork(ntwork).withSubnet(subnetName).withPrivateIPAddressStatic(primaryPrivateIpStatic + "." + count)).withExistingPublicIPAddress(publcIPAddrss)).attach();
                        count++;
                    }
                }
                return (NetworkInterface)wthCreate.create();
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return null;
    }

    public List getNetworkInterfaces(String resourceGrp) {
        try {
            return (List)this.azure.networkInterfaces().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public NetworkInterface getNetworkInterfaces(String resourceGrpName, String ntwrkIntrfaceName) {
        try {
            return (NetworkInterface)this.azure.networkInterfaces().getByResourceGroup(resourceGrpName, ntwrkIntrfaceName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public NetworkInterface createNetworkInterfacesById(String ntwrkIntrfaceId) {
        try {
            return (NetworkInterface)this.azure.networkInterfaces().getById(ntwrkIntrfaceId);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deleteNetworkInterfaces(String resourceGrp, String region) {
        try {
            this.azure.networkInterfaces().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteNetworkInterfaces(String ntwrkIntrfaceId) {
        try {
            this.azure.networkInterfaces().deleteById(ntwrkIntrfaceId);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean updateNetworkInterfaces(NetworkInterface ntwrkIntrface) {
        try {
            if (ntwrkIntrface != null && ntwrkIntrface.ipConfigurations() != null && !ntwrkIntrface.ipConfigurations().isEmpty()) {
                NetworkInterface.Update updte = (NetworkInterface.Update)ntwrkIntrface.update();
                for (Map.Entry entry : ntwrkIntrface.ipConfigurations().entrySet()) {
                    if (!((NicIPConfiguration)entry.getValue()).isPrimary()){
                        updte = (NetworkInterface.Update)((NicIPConfiguration.Update)updte.updateIPConfiguration((String)entry.getKey()).withoutPublicIPAddress()).parent();
                    }
                }
                updte.apply();
                return true;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return false;
    }

    public boolean updatePublicIPAddresses(NetworkInterface ntwrkIntrface) {
        try {
            if (ntwrkIntrface != null && ntwrkIntrface.ipConfigurations() != null && !ntwrkIntrface.ipConfigurations().isEmpty()) {
                NetworkInterface.Update updte = (NetworkInterface.Update)ntwrkIntrface.update();
                for (Map.Entry entry : ntwrkIntrface.ipConfigurations().entrySet()) {
                    if (!((NicIPConfiguration)entry.getValue()).isPrimary()) {
                        String pip = "primary".equalsIgnoreCase((String)entry.getKey()) ? (ntwrkIntrface.name() + "_pip_1") : StringUtils.replace(((NicIPConfiguration)entry.getValue()).name(), "_ipconf_", "_pip_");
                        String pblicIp = "/subscriptions/" + this.account.subscriptionId + "/resourceGroups/" + ntwrkIntrface.resourceGroupName() + "/providers/Microsoft.Network/publicIPAddresses/" + pip;
                        updte = (NetworkInterface.Update)((NicIPConfiguration.Update)updte.updateIPConfiguration((String)entry.getKey()).withExistingPublicIPAddress(pblicIp)).parent();
                    }
                }
                updte.apply();
                return true;
            }
        } catch (Exception exception) {
            Loggers.error(exception);
        }
        return false;
    }

    public PublicIPAddress createPublicIPAddresses(String resourceGrpName, String region, String ntwrkIntrfaceName, String domains, boolean hasDynamicIps) {
        try {
            PublicIPAddress.DefinitionStages.WithCreate wthCreate = (PublicIPAddress.DefinitionStages.WithCreate)((PublicIPAddress.DefinitionStages.WithGroup)((PublicIPAddress.DefinitionStages.Blank)this.azure.publicIPAddresses().define(ntwrkIntrfaceName)).withRegion(region)).withExistingResourceGroup(resourceGrpName);
            if (hasDynamicIps == true) {
                wthCreate = wthCreate.withDynamicIP();
            } else {
                wthCreate = wthCreate.withStaticIP();
            }
            if (domains != null && !"".equals(domains)) {
                domains = Character.isDigit(domains.charAt(0)) ? ("dns" + domains) : domains;
                wthCreate = wthCreate.withLeafDomainLabel(domains).withReverseFqdn(domains + "." + region + ".cloudapp.azure.com");
            } else {
                wthCreate = wthCreate.withoutReverseFqdn();
            }
            return (PublicIPAddress)wthCreate.create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public PublicIPAddress asyncPublicIPAddresses(String resourceGrpName, String region, String ntwrkIntrfaceName, String publcIPPrfixId, String rndom, String authrizations) {
        try {
            if (authrizations == null){
                throw new DatabaseException("Could not generate token");
            }

            JSONObject records = new JSONObject();
            records.put("location", region);
            records.put("sku", new JSONObject());
            records.getJSONObject("sku").put("name", "Standard");
            records.put("properties", new JSONObject());

            records.getJSONObject("properties").put("publicIPAllocationMethod", "Static");
            records.getJSONObject("properties").put("publicIPAddressVersion", "IPv4");
            records.getJSONObject("properties").put("publicIPPrefix", new JSONObject());
            records.getJSONObject("properties").getJSONObject("publicIPPrefix").put("id", publcIPPrfixId);

            LinkedHashMap<String, String> params = new LinkedHashMap<>(3);
            params.put("Accept", "application/json");
            params.put("Content-Type", "application/json");
            params.put("Authorization", authrizations);

            CloseableHttpResponse result = executeRequests(HttpEnumeration.PUT, "management.azure.com", 443, "https", "/subscriptions/" + this.account.getSubscriptionId() + "/resourcegroups/" + resourceGrpName + "/providers/Microsoft.Network/publicIPAddresses/" + ntwrkIntrfaceName + "?api-version=2020-05-01", params, records, null);
            if (result == null){
                throw new DatabaseException("Could not execute this request !");
            }

            JSONObject response = new JSONObject(EntityUtils.toString(result.getEntity()));
            if (response.has("error")){
                throw new DatabaseException(response.getJSONObject("error").getString("message"));
            }

            if (response.has("id")) {
                AzureAsyncOperations(result, authrizations);
                return getPublicIPAddresses(resourceGrpName, ntwrkIntrfaceName);
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return null;
    }

    public List getPublicIPAddresses(String resourceGrp) {
        try {
            return (List)this.azure.publicIPAddresses().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public String getAllPublicIps(PublicIPAddress resource) {
        /*StringBuilder info = new StringBuilder().append("Public IP Address: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tIP Address: ").append(resource.ipAddress())
                .append("\n\tLeaf domain label: ").append(resource.leafDomainLabel())
                .append("\n\tFQDN: ").append(resource.fqdn())
                .append("\n\tReverse FQDN: ").append(resource.reverseFqdn())
                .append("\n\tIdle timeout (minutes): ").append(resource.idleTimeoutInMinutes())
                .append("\n\tIP allocation method: ").append(resource.ipAllocationMethod().toString())
                .append("\n\tIP version: ").append(resource.version().toString());*/
        StringBuilder pip = new StringBuilder().append("\n").append(resource.ipAddress());
        return pip.toString();
    }

    public PublicIPAddress getPublicIPAddresses(String resourceGrpName, String ntwrkIntrfaceName) {
        try {
            return (PublicIPAddress)this.azure.publicIPAddresses().getByResourceGroup(resourceGrpName, ntwrkIntrfaceName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public PublicIPAddress getPublicIPAddressesById(String publicIpId) {
        try {
            return (PublicIPAddress)this.azure.publicIPAddresses().getById(publicIpId);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deletePublicIPAddresses(String resourceGrp, String region) {
        try {
            this.azure.publicIPAddresses().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deletePublicIPAddresses(String publicIpId) {
        try {
            this.azure.publicIPAddresses().deleteById(publicIpId);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public PublicIPPrefix createPublicIPPrefixs(String resourceGrpName, String region, String ntwrkIntrfaceName, int prfisLength) {
        try {
            return (PublicIPPrefix)((PublicIPPrefix.DefinitionStages.WithCreate)((PublicIPPrefix.DefinitionStages.WithGroup)((PublicIPPrefix.DefinitionStages.Blank)this.azure.publicIPPrefixes().define(ntwrkIntrfaceName)).withRegion(region)).withExistingResourceGroup(resourceGrpName)).withPrefixLength(Integer.valueOf(prfisLength)).withSku((new PublicIPPrefixSku()).withName(PublicIPPrefixSkuName.STANDARD)).create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public List getPublicIPPrefixs(String resourceGrp) {
        try {
            return (List)this.azure.publicIPPrefixes().listByResourceGroup(resourceGrp);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public PublicIPPrefix getPublicIPPrefixs(String resourceGrpName, String ntwrkIntrfaceName) {
        try {
            return (PublicIPPrefix)this.azure.publicIPPrefixes().getByResourceGroup(resourceGrpName, ntwrkIntrfaceName);
        } catch (Exception e) {
            return null;
        }
    }

    public PublicIPPrefix getPublicIPPrefixsById(String id) {
        try {
            return (PublicIPPrefix)this.azure.publicIPPrefixes().getById(id);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deletePublicIPPrefixs(String resourceGrp, String region) {
        try {
            this.azure.publicIPPrefixes().deleteByResourceGroup(resourceGrp, region);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deletePublicIPPrefixs(String id) {
        try {
            this.azure.publicIPPrefixes().deleteById(id);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public ResourceGroup createResourceGroups(String resourceGrpName, String region) {
        try {
            return (ResourceGroup)((ResourceGroup.DefinitionStages.WithCreate)((ResourceGroup.DefinitionStages.Blank)this.azure.resourceGroups().define(resourceGrpName)).withRegion(region)).create();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public List getResourceGroups() {
        try {
            return (List)this.azure.resourceGroups().list();
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public ResourceGroup getResourceGroupsByName(String resourceGrpName) {
        try {
            return (ResourceGroup)this.azure.resourceGroups().getByName(resourceGrpName);
        } catch (Exception exception) {
            Loggers.error(exception);
            return null;
        }
    }

    public boolean deleteResourceGroupsByName(String resourceGrpName) {
        try {
            this.azure.resourceGroups().deleteByName(resourceGrpName);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public boolean deleteResourceGroups(String nameAsync) {
        try {
            this.azure.resourceGroups().deleteByNameAsync(nameAsync);
            return true;
        } catch (Exception exception) {
            Loggers.error(exception);
            return false;
        }
    }

    public String getAccessToken() {
        try {
            ArrayList<BasicNameValuePair> params = new ArrayList();
            params.add(new BasicNameValuePair("client_id", this.account.getClientId()));
            params.add(new BasicNameValuePair("client_secret", this.account.getToken()));
            params.add(new BasicNameValuePair("resource", "https://management.azure.com"));
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            CloseableHttpResponse result = this.executeRequests(HttpEnumeration.POST, "login.microsoftonline.com", 443, "https", "/" + this.account.getTenantId() + "/oauth2/token", null, null, params);
            if (result == null){
                throw new DatabaseException("Could not execute this request !");
            }
            JSONObject response = new JSONObject(EntityUtils.toString(result.getEntity()));
            if (response.has("error")){
                throw new DatabaseException(response.getJSONObject("error").getString("message"));
            }
            try {
                result.close();
            } catch (Exception exception) {
                Loggers.error(exception);
            }
            if (response.has("access_token") && !"".equals(response.getString("access_token"))){
                return "Bearer " + response.getString("access_token");
            }
        } catch (Throwable th) {
            Loggers.error(th);
        }
        return null;
    }

    protected CloseableHttpResponse executeRequests(HttpEnumeration postValues, String loginUrl, int ports, String httpType, String accssTokens, LinkedHashMap<String,String> headers, JSONObject entitys, List params) {
        try {
            HttpGet h1Get;
            HttpDelete h1Delete;
            HttpPost h1Post;
            HttpPut h1Put;
            HttpClientBuilder clientBuildr = HttpClients.custom();
            BasicCredentialsProvider credntialPrvider = null;
            RequestConfig reqConfig = null;
            if (this.account.proxyIp != null && !"".equals(this.account.proxyIp) && this.account.proxyPort != null && !"".equals(this.account.proxyPort)) {
                HttpHost httpHost1 = new HttpHost(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort));
                reqConfig = RequestConfig.custom().setProxy(httpHost1).build();
                if (this.account.proxyUsername != null && !"".equals(this.account.proxyUsername) && this.account.proxyPassword != null && !"".equals(this.account.proxyPassword)) {
                    credntialPrvider = new BasicCredentialsProvider();
                    credntialPrvider.setCredentials(new AuthScope(this.account.proxyIp, TypesParser.safeParseInt(this.account.proxyPort)), (Credentials)new UsernamePasswordCredentials(this.account.proxyUsername, this.account.proxyPassword));
                }
                if (credntialPrvider != null){
                    clientBuildr = clientBuildr.setDefaultCredentialsProvider((CredentialsProvider)credntialPrvider);
                }
            }
            CloseableHttpClient result = clientBuildr.build();
            HttpHost httpHost2 = new HttpHost(loginUrl, ports, httpType);
            switch (AzureOrdinal.value[postValues.ordinal()]) {
                case 1:{
                    h1Get = new HttpGet(accssTokens);
                    if (reqConfig != null){
                        h1Get.setConfig(reqConfig);
                    }
                    if (headers != null && !headers.isEmpty()){
                        headers.entrySet().forEach(entry -> h1Get.setHeader((String)entry.getKey(), (String)entry.getValue()));
                    }
                    return result.execute(httpHost2, (HttpRequest)h1Get);
                }
                case 2:{
                    h1Delete = new HttpDelete(accssTokens);
                    if (reqConfig != null){
                        h1Delete.setConfig(reqConfig);
                    }
                    if (headers != null && !headers.isEmpty()){
                        headers.entrySet().forEach(entry -> h1Delete.setHeader((String)entry.getKey(), (String)entry.getValue()));
                    }
                    return result.execute(httpHost2, (HttpRequest)h1Delete);
                }
                case 3:{
                    h1Post = new HttpPost(accssTokens);
                    if (reqConfig != null){
                        h1Post.setConfig(reqConfig);
                    }
                    if (headers != null && !headers.isEmpty()){
                        headers.entrySet().forEach(entry -> h1Post.setHeader((String)entry.getKey(), (String)entry.getValue()));
                    }
                    if (entitys != null) {
                        h1Post.setEntity((HttpEntity)new StringEntity(entitys.toString()));
                    } else if (params != null && !params.isEmpty()) {
                        h1Post.setEntity((HttpEntity)new UrlEncodedFormEntity(params));
                    }
                    return result.execute(httpHost2, (HttpRequest)h1Post);
                }
                case 4:{
                    h1Put = new HttpPut(accssTokens);
                    if (reqConfig != null){
                        h1Put.setConfig(reqConfig);
                    }
                    if (headers != null && !headers.isEmpty()){
                        headers.entrySet().forEach(entry -> h1Put.setHeader((String)entry.getKey(), (String)entry.getValue()));
                    }
                    if (entitys != null) {
                        h1Put.setEntity((HttpEntity)new StringEntity(entitys.toString()));
                    } else if (params != null && !params.isEmpty()) {
                        h1Put.setEntity((HttpEntity)new UrlEncodedFormEntity(params));
                    }
                    return result.execute(httpHost2, (HttpRequest)h1Put);
                }
            }
        } catch (Exception ex) {
            Loggers.error(ex);
        }
        return null;
    }

    protected boolean AzureAsyncOperations(CloseableHttpResponse results, String authrizations) {
        if (results != null) {
            String headers = "";
            for (Header allHeaders : results.getAllHeaders()) {
                if ("Azure-AsyncOperation".equalsIgnoreCase(allHeaders.getName())) {
                    headers = allHeaders.getValue();
                    break;
                }
            }

            try {
                results.close();
            } catch (Exception exception) {
                Loggers.error(exception);
            }

            if (headers != null && headers.contains("https://management.azure.com")) {
                String status = "InProgress";
                headers = headers.split(Pattern.quote("/providers/"))[1];
                while ("InProgress".equals(status)) {
                    try {
                        LinkedHashMap<String, String> params = new LinkedHashMap<>(3);
                        params.put("Accept", "application/json");
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", authrizations);
                        results = executeRequests(HttpEnumeration.GET, "management.azure.com", 443, "https", "/subscriptions/" + this.account.getSubscriptionId() + "/providers/" + headers, params, null, null);
                        if (results == null){
                            throw new DatabaseException("Could not execute this request !");
                        }
                        JSONObject response = new JSONObject(EntityUtils.toString(results.getEntity()));
                        if (response.has("status")){
                            status = response.getString("status");
                        }
                        results.close();
                    } catch (Exception exception) {}
                    if (!"Succeeded".equalsIgnoreCase(status)){
                        ThreadSleep.sleep(5000L);
                    }
                }
            }
        }
        return false;
    }

    public boolean authenticate() {
        try {
            this.azure = ((Azure.Configurable)Azure.configure().withLogLevel(LogLevel.BASIC)).authenticate((AzureTokenCredentials)new ApplicationTokenCredentials(this.account.clientId, this.account.tenantId, this.account.token, AzureEnvironment.AZURE)).withSubscription(this.account.subscriptionId);
            return true;
        } catch (Exception ex) {
            Loggers.error(ex);
            return false;
        }
    }

    public AzureManager(AzureAccount account) {
        this.account = account;
    }

    public AzureAccount getAccount() {
        return this.account;
    }

    public Azure getAzure() {
        return this.azure;
    }

    public void setAccount(AzureAccount account) {
        this.account = account;
    }

    public void setAzure(Azure azure) {
        this.azure = azure;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AzureManager))
            return false;
        AzureManager do1 = (AzureManager)paramObject;
        if (!do1.exists(this))
            return false;
        AzureAccount char1 = getAccount();
        AzureAccount char2 = do1.getAccount();
        if ((char1 == null) ? (char2 != null) : !char1.equals(char2))
            return false;
        Azure azure1 = getAzure();
        Azure azure2 = do1.getAzure();
        return !((azure1 == null) ? (azure2 != null) : !azure1.equals(azure2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AzureManager;
    }

    @Override
    public int hashCode() {
        int n = 1;
        AzureAccount char1 = getAccount();
        n = n * 59 + ((char1 == null) ? 43 : char1.hashCode());
        Azure azure = getAzure();
        return n * 59 + ((azure == null) ? 43 : azure.hashCode());
    }

    @Override
    public String toString() {
        return "AzureManager(account=" + getAccount() + ", azure=" + getAzure() + ")";
    }

}
