package tech.iresponse.production.drops;

import java.beans.ConstructorProperties;
import tech.iresponse.logging.Loggers;
import tech.iresponse.models.admin.SmtpUser;
import tech.iresponse.production.component.SmtpComponent;

public class SmtpDropUsersUpdater extends Thread {

    private SmtpUser smtpUser;
    private SmtpComponent component;

    public void run() {
        try {
            this.component.updateSmtpUsersTotals(this.smtpUser.id);
        } catch (Throwable th) {
            Loggers.error(th);
        }
    }

    @ConstructorProperties({"smtpUser", "component"})
    public SmtpDropUsersUpdater(SmtpUser smtpUser, SmtpComponent component) {
        this.smtpUser = smtpUser;
        this.component = component;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SmtpDropUsersUpdater))
            return false;
        SmtpDropUsersUpdater byte1 = (SmtpDropUsersUpdater)paramObject;
        if (!byte1.exists(this))
            return false;
        SmtpUser package1 = getSmtpUser();
        SmtpUser package2 = byte1.getSmtpUser();
        if ((package1 == null) ? (package2 != null) : !package1.equals(package2))
            return false;
        SmtpComponent int1 = getComponent();
        SmtpComponent int2 = byte1.getComponent();
        return !((int1 == null) ? (int2 != null) : !int1.equals(int2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof SmtpDropUsersUpdater;
    }

    @Override
    public int hashCode() {
        int n = 1;
        SmtpUser package1 = getSmtpUser();
        n = n * 59 + ((package1 == null) ? 43 : package1.hashCode());
        SmtpComponent int1 = getComponent();
        return n * 59 + ((int1 == null) ? 43 : int1.hashCode());
    }

    public SmtpUser getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(SmtpUser smtpUser) {
        this.smtpUser = smtpUser;
    }

    public SmtpComponent getComponent() {
        return component;
    }

    public void setComponent(SmtpComponent component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "SmtpDropUsersUpdater(smtpUser=" + getSmtpUser() + ", component=" + getComponent() + ")";
    }
}
