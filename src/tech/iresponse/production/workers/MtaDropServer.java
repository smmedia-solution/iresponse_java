package tech.iresponse.production.workers;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import tech.iresponse.logging.Loggers;
import tech.iresponse.webservices.MtaProcesses;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.orm.Connector;
import tech.iresponse.production.component.Rotator;
import tech.iresponse.production.component.MtaComponent;
import tech.iresponse.production.shorlink.Bitly;
import tech.iresponse.production.shorlink.Google;
import tech.iresponse.production.shorlink.Tinyurl;
import tech.iresponse.production.drops.MtaDropIpsUpdater;
import tech.iresponse.utils.Strings;
import tech.iresponse.remote.Authentification;
import tech.iresponse.remote.SSHConnector;

public class MtaDropServer extends Thread {

    private MtaComponent component;
    private int serverId;
    private List<Integer> vmtasIds;
    private int start;
    private int count;
    private static final int nbThread = 10;

    public void run() {
        SSHConnector ssh = null;
        try {
            MtaServer mtaSrver = new MtaServer(Integer.valueOf(this.serverId));
            if (mtaSrver.getEmpty()){
                throw new DatabaseException("Mta server not found");
            }
            if (this.vmtasIds.isEmpty()) {
                throw new DatabaseException("No vmtas found !");
            }

            String cond = "";
            cond = this.vmtasIds.stream().filter(vmtasId -> vmtasId > 0).map(vmtasId -> "'" + vmtasId + "',").reduce(cond, String::concat);
            cond = cond.substring(0,cond.length() - 1);

            // create vmtas rotator
            this.component.setVmtasRotator(new Rotator((List)ServerVmta.all(ServerVmta.class, "id IN (" + cond + ")", null), this.component.getVmtasRotation()));

            // create the query
            String query = "SELECT sub.* FROM (" + this.component.getQuery() + ") AS sub";
            if (this.component.getDataDuplicate() > 1) {
                query = query + " CROSS JOIN generate_series(1," + this.component.getDataDuplicate() + ") as x";
            }
            query = query + " OFFSET " + this.start + " LIMIT " + this.count;
            List listEmails = Database.get("clients").executeQuery(query, null, Connector.FETCH_ALL);

            this.component.setTotalEmails(!listEmails.isEmpty() ? listEmails.size() : 0);
            if (this.component.getTotalEmails() == 0){
                throw new DatabaseException("Total emails should be greather than 0 !");
            }

            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(this.component.getVmtasRotator().getList().size());
            /*for (ServerVmta srvVmta : this.component.getVmtasRotator().getList()){
                fixedThreadPool.submit((Runnable)new MtaDropIpsUpdater(srvVmta, this.component));
            }*/
            Iterator<ServerVmta> iterator = (Iterator<ServerVmta>)this.component.getVmtasRotator().getList().iterator();
            while (iterator.hasNext()) {
                fixedThreadPool.submit((Runnable)new MtaDropIpsUpdater((ServerVmta)iterator.next(), this.component));
            }
            fixedThreadPool.shutdown();
            if (!fixedThreadPool.awaitTermination(1L, TimeUnit.HOURS)){
                fixedThreadPool.shutdownNow();
            }

            String domain = "[domain]".equalsIgnoreCase(this.component.getStaticDomain()) ? ((ServerVmta)this.component.getVmtasRotator().getList().get(0)).domain : this.component.getStaticDomain();
            switch (this.component.getLinkType()) {
                case "routing-bitly":
                case "attr-bitly":
                    this.component.setShortDomain(Bitly.shortBitly(domain) + "#");
                    break;
                case "routing-gcloud":
                case "attr-gcloud":
                    this.component.setShortDomain(Google.shortGoogle(domain) + "#");
                    break;
                case "routing-tinyurl":
                case "attr-tinyurl":
                    this.component.setShortDomain(Tinyurl.shortTinyurl(domain) + "#");
                    break;
            }

            if (!"emails-per-period".equalsIgnoreCase(this.component.getEmailsProcessType())){
                // check for batch
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
            int threadCount = pickupsTotal > 100 ? 100 : pickupsTotal;
            //fixedThreadPool = Executors.newFixedThreadPool(10);
            fixedThreadPool = Executors.newFixedThreadPool(threadCount);
            ArrayList<ServerVmta> listSrvVmta = null;
            List list2 = null;
            int m = 0;
            if (this.component.getHasNegative()) {
                String pathNegative = "/var/spool/iresponse/tmp/neg_" + Strings.rndomSalt(10, false) + ".txt";
                ssh.upload(this.component.getNegativeFilePath(), pathNegative);
                this.component.setNegativeFilePath(pathNegative);
            }
            for (int i = 0; i < pickupsTotal; i++) {
                if (!listEmails.isEmpty()) {
                    list2 = listEmails.subList(pickupStart, pickupFinish);
                    m = list2.size();
                    listSrvVmta = new ArrayList(m);
                    for (int b1 = 0; b1 < m; b1++){
                        listSrvVmta.add(this.component.getCurrentVmta());
                    }
                    fixedThreadPool.submit(new MtaDropPickup(this.component, mtaSrver, list2, listSrvVmta));
                }
                pickupStart += this.component.getBatch();
                pickupFinish += this.component.getBatch();
                if (pickupFinish > this.component.getTotalEmails()){
                    pickupFinish = this.component.getTotalEmails();
                }
                if (pickupStart >= this.component.getTotalEmails()){
                    break;
                }
            }
            fixedThreadPool.shutdown();
            if (!fixedThreadPool.awaitTermination(1L, TimeUnit.DAYS)){
                fixedThreadPool.shutdownNow();
            }
        } catch (Exception e) {
            Loggers.error(e);
            MtaProcesses.IS_ERROR_OCCURED = true;
        } finally {
            if (ssh != null && ssh.isConnected()) {
                if (this.component.getHasNegative()){
                    ssh.cmd("rm -rf " + this.component.getNegativeFilePath());
                }
                ssh.disconnect();
            }
            if (this.component != null && !this.component.getVmtasTotals().isEmpty()){
                this.component.getVmtasTotals().forEach((paramInteger1, paramInteger2) -> {
                    try {
                        Database.get("system").executeUpdate("UPDATE production.mta_processes_ips SET sent_total = " + paramInteger2 + " WHERE server_vmta_id = " + paramInteger1 + " AND process_id = " + this.component.getId(), null, 0);
                    } catch (Exception e) {
                        Loggers.error(e);
                    }
                });
            }
        }
    }

    @ConstructorProperties({"component", "serverId", "vmtasIds", "start", "count"})
    public MtaDropServer(MtaComponent component, int serverId, List vmtasIds, int start, int count) {
        this.component = component;
        this.serverId = serverId;
        this.vmtasIds = vmtasIds;
        this.start = start;
        this.count = count;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof MtaDropServer))
            return false;
        MtaDropServer new1 = (MtaDropServer)paramObject;
        if (!new1.exists(this))
            return false;
        MtaComponent if1 = getComponent();
        MtaComponent if2 = new1.getComponent();
        if ((if1 == null) ? (if2 != null) : !if1.equals(if2))
            return false;
        if (getServerId() != new1.getServerId())
            return false;
        List list1 = getVmtasIds();
        List list2 = new1.getVmtasIds();
        return ((list1 == null) ? (list2 != null) : !list1.equals(list2)) ? false : ((getStart() != new1.getStart()) ? false : (!(getCount() != new1.getCount())));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof MtaDropServer;
    }

    @Override
    public int hashCode() {
        int n = 1;
        MtaComponent if1 = getComponent();
        n = n * 59 + ((if1 == null) ? 43 : if1.hashCode());
        n = n  * 59 + getServerId();
        List list = getVmtasIds();
        n = n * 59 + ((list == null) ? 43 : list.hashCode());
        n = n * 59 + getStart();
        return n * 59 + getCount();
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

    public java.util.List getVmtasIds() {
        return vmtasIds;
    }

    public void setVmtasIds(java.util.List vmtasIds) {
        this.vmtasIds = vmtasIds;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MtaDropServer(component=" + getComponent() + ", serverId=" + getServerId() + ", vmtasIds=" + getVmtasIds() + ", start=" + getStart() + ", count=" + getCount() + ")";
        }
    }
