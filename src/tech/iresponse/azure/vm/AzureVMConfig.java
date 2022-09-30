package tech.iresponse.azure.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureVMConfig implements Serializable {

    private String virtualMachineName; //do;
    private String virtualMachineSize; //if;
    private String virtualMachineUsername = "iresponseuser"; //for
    private String virtualMachineComputerName = "iresponseuser"; //int
    private String virtualMachinePublisher = "OpenLogic"; //new
    private String virtualMachineOffer = "CentOS";  //try
    private String virtualMachineSku = "7.5";  //byte
    private String virtualMachinePassword;  //case;
    private String resourceGroupName;  //char;
    private String networkSecurityGroupName;  //else;
    private String networkName;  //goto;
    private String networkCidr = "10.0.0.0/16";  //long
    private String subnetName;  //this;
    private String subnetCidr = "10.0.0.0/20";  //void
    private String networkInterfaceName; //break;
    private String region;  //catch;
    private int prefixesLength;  //class;
    private int centosVersion = 7;  //const
    private String sudo = "";  //final

    public String getVirtualMachineName() {
        return virtualMachineName;
    }

    public void setVirtualMachineName(String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    public String getVirtualMachineSize() {
        return virtualMachineSize;
    }

    public void setVirtualMachineSize(String virtualMachineSize) {
        this.virtualMachineSize = virtualMachineSize;
    }

    public String getVirtualMachineUsername() {
        return virtualMachineUsername;
    }

    public void setVirtualMachineUsername(String virtualMachineUsername) {
        this.virtualMachineUsername = virtualMachineUsername;
    }

    public String getVirtualMachineComputerName() {
        return virtualMachineComputerName;
    }

    public void setVirtualMachineComputerName(String virtualMachineComputerName) {
        this.virtualMachineComputerName = virtualMachineComputerName;
    }

    public String getVirtualMachinePublisher() {
        return virtualMachinePublisher;
    }

    public void setVirtualMachinePublisher(String virtualMachinePublisher) {
        this.virtualMachinePublisher = virtualMachinePublisher;
    }

    public String getVirtualMachineOffer() {
        return virtualMachineOffer;
    }

    public void setVirtualMachineOffer(String virtualMachineOffer) {
        this.virtualMachineOffer = virtualMachineOffer;
    }

    public String getVirtualMachineSku() {
        return virtualMachineSku;
    }

    public void setVirtualMachineSku(String virtualMachineSku) {
        this.virtualMachineSku = virtualMachineSku;
    }

    public String getVirtualMachinePassword() {
        return virtualMachinePassword;
    }

    public void setVirtualMachinePassword(String virtualMachinePassword) {
        this.virtualMachinePassword = virtualMachinePassword;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getNetworkSecurityGroupName() {
        return networkSecurityGroupName;
    }

    public void setNetworkSecurityGroupName(String networkSecurityGroupName) {
        this.networkSecurityGroupName = networkSecurityGroupName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkCidr() {
        return networkCidr;
    }

    public void setNetworkCidr(String networkCidr) {
        this.networkCidr = networkCidr;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public void setNetworkInterfaceName(String networkInterfaceName) {
        this.networkInterfaceName = networkInterfaceName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getPrefixesLength() {
        return prefixesLength;
    }

    public void setPrefixesLength(int prefixesLength) {
        this.prefixesLength = prefixesLength;
    }

    public int getCentosVersion() {
        return centosVersion;
    }

    public void setCentosVersion(int centosVersion) {
        this.centosVersion = centosVersion;
    }

    public String getSudo() {
        return sudo;
    }

    public void setSudo(String sudo) {
        this.sudo = sudo;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AzureVMConfig))
            return false;
        AzureVMConfig do1 = (AzureVMConfig)paramObject;
        if (!do1.exists(this))
            return false;
        String str1 = getVirtualMachineName();
        String str2 = do1.getVirtualMachineName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getVirtualMachineSize();
        String str4 = do1.getVirtualMachineSize();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        String str5 = getVirtualMachineUsername();
        String str6 = do1.getVirtualMachineUsername();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
            return false;
        String str7 = getVirtualMachineComputerName();
        String str8 = do1.getVirtualMachineComputerName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
            return false;
        String str9 = getVirtualMachinePublisher();
        String str10 = do1.getVirtualMachinePublisher();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
            return false;
        String str11 = getVirtualMachineOffer();
        String str12 = do1.getVirtualMachineOffer();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
            return false;
        String str13 = getVirtualMachineSku();
        String str14 = do1.getVirtualMachineSku();
        if ((str13 == null) ? (str14 != null) : !str13.equals(str14))
            return false;
        String str15 = getVirtualMachinePassword();
        String str16 = do1.getVirtualMachinePassword();
        if ((str15 == null) ? (str16 != null) : !str15.equals(str16))
            return false;
        String str17 = getResourceGroupName();
        String str18 = do1.getResourceGroupName();
        if ((str17 == null) ? (str18 != null) : !str17.equals(str18))
            return false;
        String str19 = getNetworkSecurityGroupName();
        String str20 = do1.getNetworkSecurityGroupName();
        if ((str19 == null) ? (str20 != null) : !str19.equals(str20))
            return false;
        String str21 = getNetworkName();
        String str22 = do1.getNetworkName();
        if ((str21 == null) ? (str22 != null) : !str21.equals(str22))
            return false;
        String str23 = getNetworkCidr();
        String str24 = do1.getNetworkCidr();
        if ((str23 == null) ? (str24 != null) : !str23.equals(str24))
            return false;
        String str25 = getSubnetName();
        String str26 = do1.getSubnetName();
        if ((str25 == null) ? (str26 != null) : !str25.equals(str26))
            return false;
        String str27 = getSubnetCidr();
        String str28 = do1.getSubnetCidr();
        if ((str27 == null) ? (str28 != null) : !str27.equals(str28))
            return false;
        String str29 = getNetworkInterfaceName();
        String str30 = do1.getNetworkInterfaceName();
        if ((str29 == null) ? (str30 != null) : !str29.equals(str30))
            return false;
        String str31 = getRegion();
        String str32 = do1.getRegion();
        if ((str31 == null) ? (str32 != null) : !str31.equals(str32))
            return false;
        if (getPrefixesLength() != do1.getPrefixesLength())
            return false;
        if (getCentosVersion() != do1.getCentosVersion())
            return false;
        String str33 = getSudo();
        String str34 = do1.getSudo();
        return !((str33 == null) ? (str34 != null) : !str33.equals(str34));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AzureVMConfig;
    }

    @Override
    public int hashCode() {
        int n = 1;
        String str1 = getVirtualMachineName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getVirtualMachineSize();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getVirtualMachineUsername();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        String str4 = getVirtualMachineComputerName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        String str5 = getVirtualMachinePublisher();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        String str6 = getVirtualMachineOffer();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        String str7 = getVirtualMachineSku();
        n = n * 59 + ((str7 == null) ? 43 : str7.hashCode());
        String str8 = getVirtualMachinePassword();
        n = n * 59 + ((str8 == null) ? 43 : str8.hashCode());
        String str9 = getResourceGroupName();
        n = n * 59 + ((str9 == null) ? 43 : str9.hashCode());
        String str10 = getNetworkSecurityGroupName();
        n = n * 59 + ((str10 == null) ? 43 : str10.hashCode());
        String str11 = getNetworkName();
        n = n * 59 + ((str11 == null) ? 43 : str11.hashCode());
        String str12 = getNetworkCidr();
        n = n * 59 + ((str12 == null) ? 43 : str12.hashCode());
        String str13 = getSubnetName();
        n = n * 59 + ((str13 == null) ? 43 : str13.hashCode());
        String str14 = getSubnetCidr();
        n = n * 59 + ((str14 == null) ? 43 : str14.hashCode());
        String str15 = getNetworkInterfaceName();
        n = n * 59 + ((str15 == null) ? 43 : str15.hashCode());
        String str16 = getRegion();
        n = n * 59 + ((str16 == null) ? 43 : str16.hashCode());
        n = n * 59 + getPrefixesLength();
        n = n * 59 + getCentosVersion();
        String str17 = getSudo();
        return n * 59 + ((str17 == null) ? 43 : str17.hashCode());
    }

    @Override
    public String toString() {
        return "AzureVMConfig(virtualMachineName=" + getVirtualMachineName() + ", virtualMachineSize=" + getVirtualMachineSize() + ", virtualMachineUsername=" + getVirtualMachineUsername() + ", virtualMachineComputerName=" + getVirtualMachineComputerName() + ", virtualMachinePublisher=" + getVirtualMachinePublisher() + ", virtualMachineOffer=" + getVirtualMachineOffer() + ", virtualMachineSku=" + getVirtualMachineSku() + ", virtualMachinePassword=" + getVirtualMachinePassword() + ", resourceGroupName=" + getResourceGroupName() + ", networkSecurityGroupName=" + getNetworkSecurityGroupName() + ", networkName=" + getNetworkName() + ", networkCidr=" + getNetworkCidr() + ", subnetName=" + getSubnetName() + ", subnetCidr=" + getSubnetCidr() + ", networkInterfaceName=" + getNetworkInterfaceName() + ", region=" + getRegion() + ", prefixesLength=" + getPrefixesLength() + ", centosVersion=" + getCentosVersion() + ", sudo=" + getSudo() + ")";
    }
}
