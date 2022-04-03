package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.component.TestEmail;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.utils.Strings;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class MtaTestServer extends Thread {

    private MtaComponent component;
    private int serverId;
    private List emails;
    private List<Integer> serverVmtas;
    private static final int nbThreads = 10;
    public static volatile HashMap VMTAS = new HashMap<>();

    @Override
    public void run() {
        SSHConnector ssh = null;
        try {
            MtaServer mtaSrver = new MtaServer(Integer.valueOf(this.serverId));
            if (mtaSrver.getEmpty()){
                throw new DatabaseException("Mta server not found");
            }
            if (this.serverVmtas.isEmpty()) {
                throw new DatabaseException("No vmtas found !");
            }
            if (this.emails == null || this.emails.isEmpty()){
                throw new DatabaseException("No emails found !");
            }
            this.component.setTotalEmails(this.emails.size());
            if (this.component.getTotalEmails() == 0){
                throw new DatabaseException("Total emails should be greather than 0 !");
            }
            /*Iterator<Integer> iterator = this.serverVmtas.iterator();
            while (iterator.hasNext()) {
                int m = ((Integer)iterator.next()).intValue();
                updateVmtas(m);
            }*/
            for (int vmtaId : this.serverVmtas) {
                updateVmtas(vmtaId);
            }
            String str = "[domain]".equalsIgnoreCase(this.component.getStaticDomain()) ? ((ServerVmta)VMTAS.get(Integer.valueOf(((TestEmail)this.emails.get(0)).getComponentId()))).domain : this.component.getStaticDomain();
            switch (this.component.getLinkType()) {
                case "routing-bitly":
                case "attr-bitly":
                    this.component.setShortDomain(Bitly.shortBitly(str) + "#");
                    break;
                case "routing-gcloud":
                case "attr-gcloud":
                    this.component.setShortDomain(Google.shortGoogle(str) + "#");
                    break;
                case "routing-tinyurl":
                case "attr-tinyurl":
                    this.component.setShortDomain(Tinyurl.shortTinyurl(str) + "#");
                    break;
            }
            if (!"emails-per-period".equalsIgnoreCase(this.component.getEmailsProcessType())){
                if (this.component.getBatch() > 1) {
                    this.component.setBatch((this.component.getBatch() > this.component.getTotalEmails()) ? this.component.getTotalEmails() : this.component.getBatch());
                } else {
                    this.component.setBatch(1);
                }
            }
            ssh = Authentification.connectToServer(mtaSrver);
            if (ssh == null || !ssh.isConnected()){
                throw new DatabaseException("Could not connect to server : " + mtaSrver.name + " !");
            }
            int pickupsTotal = (this.component.getTotalEmails() % this.component.getBatch() == 0) ? (int)Math.ceil((this.component.getTotalEmails() / this.component.getBatch())) : ((int)Math.ceil((this.component.getTotalEmails() / this.component.getBatch())) + 1);
            int pickupStart = 0;
            int pickupFinish = this.component.getBatch();
            ExecutorService pickupsExecutor = Executors.newFixedThreadPool(10);
            if (this.component.getHasNegative()) {
                String pathNegative = "/var/spool/iresponse/tmp/neg_" + Strings.rndomSalt(10, false) + ".txt";
                ssh.upload(this.component.getNegativeFilePath(), pathNegative);
                this.component.setNegativeFilePath(pathNegative);
            }
            for (int b = 0; b < pickupsTotal; b++) {
                pickupsExecutor.submit(new MtaTestPickup(this.component, mtaSrver, this.emails.subList(pickupStart, pickupFinish)));
                pickupStart += this.component.getBatch();
                pickupFinish += this.component.getBatch();
                if (pickupFinish > this.component.getTotalEmails()) {
                    pickupFinish = this.component.getTotalEmails();
                }
                if (pickupStart >= this.component.getTotalEmails()){
                    break;
                }
            }
            pickupsExecutor.shutdown();
            if (!pickupsExecutor.awaitTermination(1L, TimeUnit.DAYS)){
                pickupsExecutor.shutdownNow();
            }
        } catch (Throwable e) {
            Loggers.error(e);
            MtaProcesses.IS_ERROR_OCCURED = true;
        } finally {
            if (ssh != null && ssh.isConnected()) {
                if (this.component.getHasNegative()){
                    ssh.cmd("rm -rf " + this.component.getNegativeFilePath());
                }
                ssh.disconnect();
            }
        }
    }

    public static synchronized void updateVmtas(int vmtaId) throws Exception{
        ServerVmta srvVmta = new ServerVmta(Integer.valueOf(vmtaId));
        VMTAS.put(Integer.valueOf(vmtaId), srvVmta);
    }

    @ConstructorProperties({"component", "serverId", "emails", "serverVmtas"})
    public MtaTestServer(MtaComponent component, int serverId, List emails, List serverVmtas) {
        this.component = component;
        this.serverId = serverId;
        this.emails = emails;
        this.serverVmtas = serverVmtas;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaTestServer))
            return false;
        MtaTestServer byte1 = (MtaTestServer)paramObject;
        if (!byte1.exists(this))
            return false;
        MtaComponent if1 = getComponent();
        MtaComponent if2 = byte1.getComponent();
        if ((if1 == null) ? (if2 != null) : !if1.equals(if2))
            return false;
        if (getServerId() != byte1.getServerId())
            return false;
        List list1 = getEmails();
        List list2 = byte1.getEmails();
        if ((list1 == null) ? (list2 != null) : !list1.equals(list2))
            return false;
        List list3 = getServerVmtas();
        List list4 = byte1.getServerVmtas();
            return !((list3 == null) ? (list4 != null) : !list3.equals(list4));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaTestServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        MtaComponent if1 = getComponent();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        n = n * 59 + getServerId();
        List list1 = getEmails();
        n = n * 59 + ((list1 == null) ? 43 : list1.hashCode());
        List list2 = getServerVmtas();
        return n * 59 + ((list2 == null) ? 43 : list2.hashCode());
    }

    public MtaComponent getComponent() {
        return component;
    }

    public void setComponent(MtaComponent component) {
        this.component = component;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List getEmails() {
        return emails;
    }

    public void setEmails(List emails) {
        this.emails = emails;
    }

    public List getServerVmtas() {
        return serverVmtas;
    }

    public void setServerVmtas(List serverVmtas) {
        this.serverVmtas = serverVmtas;
    }

    @Override
    public String toString() {
        return "MtaTestServer(component=" + getComponent() + ", serverId=" + getServerId() + ", emails=" + getEmails() + ", serverVmtas=" + getServerVmtas() + ")";
    }
}
