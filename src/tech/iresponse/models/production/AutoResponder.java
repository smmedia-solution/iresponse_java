package tech.iresponse.models.production;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class AutoResponder extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "name", type = "text", nullable = false, length = 200)
    public String name;

    @Column(name = "status", type = "text", nullable = false, length = 20)
    public String status;

    @Column(name = "type", type = "text", nullable = false, length = 100)
    public String type;

    @Column(name = "server_id", type = "integer", nullable = false)
    public int serverId;

    @Column(name = "server_name", type = "text", nullable = false, length = 200)
    public String serverName;

    @Column(name = "component_id", type = "integer", nullable = false)
    public int componentId;

    @Column(name = "component_name", type = "text", nullable = false, length = 200)
    public String componentName;

    @Column(name = "affiliate_network_id", type = "integer", nullable = true)
    public int affiliateNetworkId;

    @Column(name = "offer_id", type = "integer", nullable = true)
    public int offerId;

    @Column(name = "clients_excluded", type = "text", nullable = true)
    public String clientsExcluded;

    @Column(name = "on_open", type = "boolean", nullable = true)
    public boolean onOpen;

    @Column(name = "on_click", type = "boolean", nullable = true)
    public boolean onClick;

    @Column(name = "on_unsub", type = "boolean", nullable = true)
    public boolean onUnsub;

    @Column(name = "on_optout", type = "boolean", nullable = true)
    public boolean onOptout;

    @Column(name = "content", type = "text", nullable = false)
    public String content;

    public AutoResponder() throws Exception{
        setDatabase("system");
        setSchema("production");
        setTable("auto_responders");
    }

    public AutoResponder(Object paramObject) throws Exception{
        super(paramObject);
        setDatabase("system");
        setSchema("production");
        setTable("auto_responders");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof AutoResponder))
            return false;
        AutoResponder do1 = (AutoResponder)paramObject;
        if (!do1.exists(this))
            return false;
        if (getId() != do1.getId())
           return false;
        String str1 = getName();
        String str2 = do1.getName();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
           return false;
        String str3 = getStatus();
        String str4 = do1.getStatus();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
           return false;
        String str5 = getType();
        String str6 = do1.getType();
        if ((str5 == null) ? (str6 != null) : !str5.equals(str6))
           return false;
        if (getServerId() != do1.getServerId())
           return false;
        String str7 = getServerName();
        String str8 = do1.getServerName();
        if ((str7 == null) ? (str8 != null) : !str7.equals(str8))
           return false;
        if (getComponentId() != do1.getComponentId())
           return false;
        String str9 = getComponentName();
        String str10 = do1.getComponentName();
        if ((str9 == null) ? (str10 != null) : !str9.equals(str10))
           return false;
        if (getAffiliateNetworkId() != do1.getAffiliateNetworkId())
           return false;
        if (getOfferId() != do1.getOfferId())
           return false;
        String str11 = getClientsExcluded();
        String str12 = do1.getClientsExcluded();
        if ((str11 == null) ? (str12 != null) : !str11.equals(str12))
           return false;
        if (isOnOpen() != do1.isOnOpen())
           return false;
        if (isOnClick() != do1.isOnClick())
           return false;
        if (isOnUnsub() != do1.isOnUnsub())
           return false;
        if (isOnOptout() != do1.isOnOptout())
           return false;
        String str13 = getContent();
        String str14 = do1.getContent();
            return !((str13 == null) ? (str14 != null) : !str13.equals(str14));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof AutoResponder;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str1 = getName();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getStatus();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        String str3 = getType();
        n = n * 59 + ((str3 == null) ? 43 : str3.hashCode());
        n = n * 59 + getServerId();
        String str4 = getServerName();
        n = n * 59 + ((str4 == null) ? 43 : str4.hashCode());
        n = n * 59 + getComponentId();
        String str5 = getComponentName();
        n = n * 59 + ((str5 == null) ? 43 : str5.hashCode());
        n = n * 59 + getAffiliateNetworkId();
        n = n * 59 + getOfferId();
        String str6 = getClientsExcluded();
        n = n * 59 + ((str6 == null) ? 43 : str6.hashCode());
        n = n * 59 + (isOnOpen() ? 79 : 97);
        n = n * 59 + (isOnClick() ? 79 : 97);
        n = n * 59 + (isOnUnsub() ? 79 : 97);
        n = n * 59 + (isOnOptout() ? 79 : 97);
        String str7 = getContent();
        return n * 59 + ((str7 == null) ? 43 : str7.hashCode());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getAffiliateNetworkId() {
        return affiliateNetworkId;
    }

    public void setAffiliateNetworkId(int affiliateNetworkId) {
        this.affiliateNetworkId = affiliateNetworkId;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public String getClientsExcluded() {
        return clientsExcluded;
    }

    public void setClientsExcluded(String clientsExcluded) {
        this.clientsExcluded = clientsExcluded;
    }

    public boolean isOnOpen() {
        return onOpen;
    }

    public void setOnOpen(boolean onOpen) {
        this.onOpen = onOpen;
    }

    public boolean isOnClick() {
        return onClick;
    }

    public void setOnClick(boolean onClick) {
        this.onClick = onClick;
    }

    public boolean isOnUnsub() {
        return onUnsub;
    }

    public void setOnUnsub(boolean onUnsub) {
        this.onUnsub = onUnsub;
    }

    public boolean isOnOptout() {
        return onOptout;
    }

    public void setOnOptout(boolean onOptout) {
        this.onOptout = onOptout;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "AutoResponder(id=" + getId() + ", name=" + getName() + ", status=" + getStatus() + ", type=" + getType() + ", serverId=" + getServerId() + ", serverName=" + getServerName() + ", componentId=" + getComponentId() + ", componentName=" + getComponentName() + ", affiliateNetworkId=" + getAffiliateNetworkId() + ", offerId=" + getOfferId() + ", clientsExcluded=" + getClientsExcluded() + ", onOpen=" + isOnOpen() + ", onClick=" + isOnClick() + ", onUnsub=" + isOnUnsub() + ", onOptout=" + isOnOptout() + ", content=" + getContent() + ")";
    }
}
