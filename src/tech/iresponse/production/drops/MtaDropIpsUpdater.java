package tech.iresponse.production.drops;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.production.component.MtaComponent;

public class MtaDropIpsUpdater extends Thread {

    private ServerVmta serverVmta;
    private MtaComponent component;

    public void run() {
        try {
            MtaDropManager.insertMtaProcesIp(this.component.getId(), this.serverVmta.id);
            this.component.updateVmtasTotals(this.serverVmta.id);
        } catch (Throwable t) {
            Loggers.error(t);
        }
    }

    @ConstructorProperties({"serverVmta", "component"})
    public MtaDropIpsUpdater(ServerVmta serverVmta, MtaComponent component) {
        this.serverVmta = serverVmta;
        this.component = component;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaDropIpsUpdater))
            return false;
        MtaDropIpsUpdater int1 = (MtaDropIpsUpdater)paramObject;
        if (!int1.exists(this))
            return false;
        ServerVmta extends1 = getServerVmta();
        ServerVmta extends2 = int1.getServerVmta();
        if ((extends1 == null) ? (extends2 != null) : !extends1.equals(extends2))
            return false;
        MtaComponent if1 = getComponent();
        MtaComponent if2 = int1.getComponent();
        return !((if1 == null) ? (if2 != null) : !if1.equals(if2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaDropIpsUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        ServerVmta extends1 = getServerVmta();
        n = n * 59 + ((extends1 == null) ? 43 : extends1.hashCode());
        MtaComponent if1 = getComponent();
        return n * 59 + ((if1 == null) ? 43 : if1.hashCode());
    }

    public ServerVmta getServerVmta() {
        return serverVmta;
    }

    public void setServerVmta(ServerVmta serverVmta) {
        this.serverVmta = serverVmta;
    }

    public MtaComponent getComponent() {
        return component;
    }

    public void setComponent(MtaComponent component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "MtaDropIpsUpdater(serverVmta=" + getServerVmta() + ", component=" + getComponent() + ")";
    }
}
