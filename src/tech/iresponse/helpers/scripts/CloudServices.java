package tech.iresponse.helpers.scripts;

import java.io.File;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.orm.Connector;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.remote.SSHConnector;
import tech.iresponse.dns.DnsApi;
import tech.iresponse.core.Application;

public class CloudServices {

    public static boolean setupNetwork(SSHConnector ssh, MtaServer mtaServ, String prefix, int version, Domain domin, String ip, String[] ipsV6) throws Exception {
        boolean updatedNetwork = false;
        String pathHostTemplate = StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/hosts")), "$p_ip", ip), "$p_domain", domin.value);
        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.cmd("hostname mail." + domin.value);
            ssh.uploadContent(pathHostTemplate, "/home/" + mtaServ.sshUsername + "/hosts");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/hosts /etc/hosts");
        } else {
            ssh.cmd("hostname mail." + domin.value);
            ssh.uploadContent(pathHostTemplate, "/etc/hosts");
        }

        String netwrks = ssh.cmd(prefix + " cat /etc/sysconfig/network");
        if (!"".equals(netwrks)){
            for (String str : netwrks.split("\\n")) {
                if (str != null && !"".equals(str) && str.startsWith("HOSTNAME")){
                    netwrks = netwrks.replace(str, "HOSTNAME=\"mail." + domin.value + "\"");
                }
            }
        }

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.uploadContent(netwrks, "/home/" + mtaServ.sshUsername + "/network");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/network /etc/sysconfig/network");
        } else {
            ssh.uploadContent(netwrks, "/etc/sysconfig/network");
        }

        mtaServ.setHostName("mail." + domin.value);

        if (domin.id > 0 && !"".equals(domin.value)) {
            DnsApi api = DnsApi.controller(domin.accountType);

            if (api != null) {
                updatedNetwork = true;
                domin.value = domin.value.replaceAll("\r", "").replaceAll("\n", "");
                Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , ip_id = 0 WHERE id = " + domin.id, null, Connector.AFFECTED_ROWS);
                ssh.cmd(prefix + "rm -rf /etc/opendkim/");
                ssh.shellCommand(prefix + "yum remove -y libopendkim-devel opendkim");

                if (version == 7){
                    ssh.shellCommand(prefix + "rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm");
                }

                ssh.shellCommand(prefix + "yum install -y libopendkim-devel opendkim");
                ssh.cmd(prefix + "opendkim-default-keygen");

                if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                    ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/home/" + mtaServ.sshUsername + "/opendkim.conf");
                    ssh.cmd(prefix + "mv /home/" + mtaServ.sshUsername + "/opendkim.conf /etc/opendkim.conf");
                } else {
                    ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/etc/opendkim.conf");
                }

                ssh.cmd(prefix + " mkdir -p /etc/opendkim/keys/" + domin.value + ";");
                ssh.cmd(prefix + " opendkim-genkey -D /etc/opendkim/keys/" + domin.value + "/ -d " + domin.value + " -s mail;");
                ssh.cmd(prefix + " chown -R opendkim:opendkim /etc/opendkim/keys/" + domin.value + "");
                ssh.cmd(prefix + " chmod 640 /etc/opendkim/keys/" + domin.value + "/mail.private");
                ssh.cmd(prefix + " chmod 644 /etc/opendkim/keys/" + domin.value + "/mail.txt");
                ssh.cmd(prefix + " echo \"mail._dkim." + domin.value + " " + domin.value + ":mail:/etc/opendkim/keys/" + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                ssh.cmd(prefix + " echo \"" + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                ssh.cmd(prefix + " echo \"*@" + domin.value + " mail._domainkey." + domin.value + "\" >> /etc/opendkim/SigningTable");
                String dkim = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                dkim = dkim.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                dkim = dkim.contains("DKIM1") ? dkim : "";

                api.setRecords(domin.accountId, domin.value);
                api.setupRecords("A", ip, domin, ManageServerWebmail.getCurrentWebMailServer());

                String vspf = "v=spf1 ";
                int nb = 7;
                if (ipsV6 != null && ipsV6.length > 0) {
                    String hostNme = "";
                    for (String ipv6 : ipsV6) {
                        if (ipv6 != null && ipv6.contains(":") && !ip.equals(ipv6)) {
                            hostNme = Strings.getSaltString(4, true, false, true, false);
                            ssh.cmd(prefix + " mkdir -p /etc/opendkim/keys/" + hostNme + "." + domin.value + ";");
                            ssh.cmd(prefix + " opendkim-genkey -D /etc/opendkim/keys/" + hostNme + "." + domin.value + "/ -d " + hostNme + "." + domin.value + " -s mail;");
                            ssh.cmd(prefix + " chown -R opendkim:opendkim /etc/opendkim/keys/" + hostNme + "." + domin.value + "");
                            ssh.cmd(prefix + " chmod 640 /etc/opendkim/keys/" + hostNme + "." + domin.value + "/mail.private");
                            ssh.cmd(prefix + " chmod 644 /etc/opendkim/keys/" + hostNme + "." + domin.value + "/mail.txt");
                            ssh.cmd(prefix + " echo \"mail._dkim." + hostNme + "." + domin.value + " " + hostNme + "." + domin.value + ":mail:/etc/opendkim/keys/" + hostNme + "." + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                            ssh.cmd(prefix + " echo \"" + hostNme + "." + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                            ssh.cmd(prefix + " echo \"*@" + hostNme + "." + domin.value + " mail._domainkey." + hostNme + "." + domin.value + "\" >> /etc/opendkim/SigningTable");
                            String dkim6 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + hostNme + "." + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                            dkim6 = dkim6.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                            dkim6 = dkim6.contains("DKIM1") ? dkim6 : "";
                            api.addSPFDkim(nb, ipv6, hostNme, "AAAA", "v=spf1 ip6:" + ipv6 + " -all", dkim6);
                            vspf = vspf + " ip6:" + ipv6;
                            nb = !"".equals(dkim6) ? (nb + 3) : (nb + 2);
                            InstallationServices.saveServerVmta((ServerVmta)null, ipv6, hostNme + ".", domin.value, mtaServ);
                        }
                    }
                }

                api.setupDkimDmarc(8, dkim, ip, false, vspf + " -all", true, domin);
                api.setDomainRecords(domin.accountId, domin.value, null);

                mtaServ.setDkimInstalled(true);
                mtaServ.setDmarcInstalled(true);

            } else if (ipsV6 != null && ipsV6.length > 0) {
                String key = "";
                for (String ipv6 : ipsV6) {
                    if (ipv6 != null && ipv6.contains(":") && !ip.equals(ipv6)) {
                        key = Strings.getSaltString(4, true, false, true, false);
                        InstallationServices.saveServerVmta((ServerVmta)null, ipv6, key + ".", domin.value, mtaServ);
                    }
                }
            }
        }
        mtaServ.update();
        return updatedNetwork;
    }

    public static void installTracking(SSHConnector ssh, MtaServer mtaServ, String prefix, int version, Domain domin, boolean updatedNetwork, boolean installCronie) throws Exception {
        String pathTracking = System.getProperty("assets.path") + "/tracking";
        int uploadCenterId = TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id"));
        String baseUrl = String.valueOf(Application.getSettingsParam("base_url"));
        String trackingKey = String.valueOf(Application.getSettingsParam("tracking_enc_key"));
        String apiLink = Convertion.crypt(baseUrl + "/api.json", trackingKey);

        if (uploadCenterId > 0) {
            ManagementServer mngServ = new ManagementServer(Integer.valueOf(uploadCenterId));
            if (!mngServ.getEmpty()){
                baseUrl = "http://" + ("".equals(mngServ.hostName) ? mngServ.mainIp : mngServ.hostName);
            }
        }
        baseUrl = Convertion.crypt(baseUrl, trackingKey);

        ssh.cmd(prefix + " rm -rf /var/www/html/*");
        ssh.cmd(prefix + " rm -rf /var/www/scripts");
        ssh.cmd(prefix + " rm -rf /var/www/brands");
        ssh.cmd(prefix + " rm -rf /var/log/iresponse");
        ssh.cmd(prefix + " rm -rf /etc/httpd/conf.d/vhosts.conf");
        ssh.cmd(prefix + " mkdir /var/www/scripts/");
        ssh.cmd(prefix + " mkdir /var/www/brands/");
        ssh.cmd(prefix + " mkdir /var/log/iresponse/");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiLink), "/home/" + mtaServ.sshUsername + "/stats.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/stats.php /var/www/scripts/stats.php");
            //ssh.upload(pathTracking + "/scripts/stat_calculator.py", "/home/" + mtaServ.sshUsername + "/stat_calculator.py");
            //ssh.cmd("mv /home/" + mtaServ.sshUsername + "/stat_calculator.py /var/www/scripts/stat_calculator.py");
            ssh.upload(pathTracking + "/scripts/gc.php", "/home/" + mtaServ.sshUsername + "/gc.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/gc.php /var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", trackingKey), "/home/" + mtaServ.sshUsername + "/help.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/help.php /var/www/scripts/help.php");
            ssh.upload(pathTracking + "/scripts/request.php", "/home/" + mtaServ.sshUsername + "/request.php");
            //ssh.cmd("mv /home/" + mtaServ.sshUsername + "/request.php /var/www/scripts/request.php");
        } else {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiLink), "/var/www/scripts/stats.php");
            //ssh.upload(pathTracking + "/scripts/stat_calculator.py", "/var/www/scripts/stat_calculator.py");
            ssh.upload(pathTracking + "/scripts/gc.php", "/var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", String.valueOf(Application.getSettingsParam("tracking_enc_key"))), "/var/www/scripts/help.php");
            //ssh.upload(pathTracking + "/scripts/request.php", "/var/www/scripts/request.php");
        }

        String cloudVhosts6 = (version == 6) ? (pathTracking + "/templates/cloud_vhosts6.conf") : (pathTracking + "/templates/cloud_vhosts7.conf");
        String vhosts = FileUtils.readFileToString(new File(cloudVhosts6), "UTF-8");
        String contents = "";

        ssh.cmd(prefix + "mkdir -p /var/www/brands/default");
        ssh.upload(pathTracking + "/brands/default.zip", "/var/www/brands/default.zip");
        ssh.cmd("unzip /var/www/brands/default.zip -d /var/www/brands/default");
        ssh.cmd("rm -rf /var/www/brands/default.zip");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/default/.htaccess");
            ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiLink), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/default/index.php");
            ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/default/optout.php");
        } else {
            ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/default/.htaccess");
            ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiLink), "$p_upload_ip", baseUrl), "/var/www/brands/default/index.php");
            ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/default/optout.php");
        }

        String hostTemplate = StringUtils.replace(vhosts, "$p_rdns", "");
        hostTemplate = StringUtils.replace(hostTemplate, "$p_ip", mtaServ.mainIp);
        hostTemplate = StringUtils.replace(hostTemplate, "$p_path", "/var/www/brands/default");
        contents = contents + hostTemplate + "\n\n";

        if (updatedNetwork) {
            domin.value = domin.value.replaceAll("\r", "").replaceAll("\n", "");
            String nameBrand = StringUtils.replace(domin.value, ".", "_");
            if ("yes".equalsIgnoreCase(domin.hasBrand)) {
                if ((new File(pathTracking + "/brands/" + nameBrand + ".zip")).exists()) {
                    ssh.cmd(prefix + " mkdir -p /var/www/brands/" + nameBrand);
                    ssh.upload(pathTracking + "/brands/" + nameBrand + ".zip", "/var/www/brands/" + nameBrand + ".zip");
                    ssh.cmd("unzip /var/www/brands/" + nameBrand + ".zip -d /var/www/brands/" + nameBrand);
                    ssh.cmd("rm -rf /var/www/brands/" + nameBrand + ".zip");
                    if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                        ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
                        ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/" + nameBrand + "/.htaccess");
                        ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiLink), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
                        ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/" + nameBrand + "/index.php");
                        ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
                        ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/" + nameBrand + "/optout.php");
                    } else {
                        ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/" + nameBrand + "/.htaccess");
                        ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiLink), "$p_upload_ip", baseUrl), "/var/www/brands/" + nameBrand + "/index.php");
                        ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/" + nameBrand + "/optout.php");
                    }
                }
                hostTemplate = StringUtils.replace(vhosts, "$p_rdns", "ServerName " + domin.value);
                hostTemplate = StringUtils.replace(hostTemplate, "$p_ip", mtaServ.mainIp);
                hostTemplate = StringUtils.replace(hostTemplate, "$p_path", "/var/www/brands/" + nameBrand);
                contents = contents + hostTemplate + "\n\n";
            }
        }

        ssh.uploadContent(contents, "/etc/httpd/conf.d/vhosts.conf");
        ssh.shellCommand(prefix + "service httpd restart");

        if (installCronie) {
            ssh.upload(pathTracking + "/scripts/cron", "/home/cron");
            ssh.cmd(prefix + "crontab /home/cron");
            ssh.cmd(prefix + "rm -rf /home/cron");
            ssh.cmd(prefix + "service crond restart");
        }
    }

}
