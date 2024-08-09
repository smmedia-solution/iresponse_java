package tech.iresponse.helpers.scripts;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.iresponse.models.admin.ServerVmta;
import tech.iresponse.models.admin.Domain;
import tech.iresponse.models.admin.SubName;
import tech.iresponse.models.admin.ManagementServer;
import tech.iresponse.models.admin.MtaServer;
import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.Database;
import tech.iresponse.utils.Strings;
import tech.iresponse.utils.Convertion;
import tech.iresponse.utils.ThreadSleep;
import tech.iresponse.utils.Matcher;
import tech.iresponse.utils.TypesParser;
import tech.iresponse.utils.Url;
import tech.iresponse.core.Application;
import tech.iresponse.dns.DnsApi;
import tech.iresponse.remote.SSHConnector;

public class InstallationServices {

    public static void installServices(SSHConnector ssh, MtaServer mtaServ, String prefix, int version, boolean updated) throws Exception {

        ssh.cmd("sed -i 's/^mirrorlist=/#mirrorlist=/g' /etc/yum.repos.d/CentOS-*");
        ssh.cmd("sed -i 's|^#*baseurl=http://[^/]*|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*");
        ssh.shellCommand(prefix + "yum remove -y httpd libopendkim-devel opendkim postfix php*");
        ssh.cmd(prefix + "rm -rf /etc/httpd");
        ssh.cmd(prefix + "rm -rf /var/www/html/*");
        ssh.cmd(prefix + "rm -rf /etc/opendkim/");
        ssh.cmd(prefix + "rm -rf /etc/opendkim.conf");
        ssh.cmd(prefix + "service exim stop");
        ssh.cmd(prefix + "service sendmail stop");
        ssh.cmd(prefix + "setenforce 0");
        ssh.cmd(prefix + "setenforce Disabled");
        if (updated){
            ssh.shellCommand(prefix + "yum update -y");
        }
        ssh.shellCommand(prefix + "yum -y reinstall polkit");
        ssh.shellCommand(prefix + "yum install -y sudo openssh-clients gdb nano wget httpd mod_ssl zip unzip yum-utils cronie perl python3 python3-pip python-pip");
        ssh.shellCommand(prefix + "pip3 install requests");

        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + mtaServ.id + "_proc.log"), "Installing / re-Installing php 7 ......", "utf-8");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.cmd("wget -O /home/" + mtaServ.sshUsername + "/epel.rpm https://dl.fedoraproject.org/pub/epel/epel-release-latest-" + version + ".noarch.rpm");
            ssh.shellCommand(prefix + "rpm -Uvh /home/" + mtaServ.sshUsername + "/epel.rpm");
            ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/epel.rpm");
            ssh.cmd("wget -O /home/" + mtaServ.sshUsername + "/remi.rpm http://rpms.remirepo.net/enterprise/remi-release-" + version + ".rpm");
            ssh.shellCommand(prefix + "rpm -Uvh /home/" + mtaServ.sshUsername + "/remi.rpm");
            ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/remi.rpm");
        } else {
            ssh.cmd("wget -O /home/epel.rpm https://dl.fedoraproject.org/pub/epel/epel-release-latest-" + version + ".noarch.rpm");
            ssh.shellCommand(prefix + "rpm -Uvh /home/epel.rpm");
            ssh.cmd("rm -rf /home/epel.rpm");
            ssh.cmd("wget -O /home/remi.rpm http://rpms.remirepo.net/enterprise/remi-release-" + version + ".rpm");
            ssh.shellCommand(prefix + "rpm -Uvh /home/remi.rpm");
            ssh.cmd("rm -rf /home/remi.rpm");
        }

        if (version == 6) {
            ssh.cmd(prefix + "yum-config-manager --enable remi-php70");
        } else {
            ssh.cmd(prefix + "yum-config-manager --enable remi-php71");
        }

        ssh.shellCommand(prefix + "yum install -y php php-mcrypt php-cli php-gd php-curl php-pgsql php-mysql php-ldap php-zip php-fileinfo php-common php-pdo php-mbstring php-soap php-zip php-xmlrpc php-opcache");
        ssh.cmd(prefix + "sed -i 's/upload_max_filesize = 2M/upload_max_filesize = 3G/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_file_uploads = 20/max_file_uploads = 200/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/post_max_size = 8M/post_max_size = 3G/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/memory_limit = 128M/memory_limit = -1/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/;max_input_nesting_level = 64/max_input_nesting_level = 10000/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/;max_input_vars = 1000/max_input_vars = 100000/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/default_socket_timeout = 60/default_socket_timeout = 360000/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_execution_time = 30/max_execution_time = 360000/g' /etc/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_input_time = 60/max_input_time = 360000/g' /etc/php.ini");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(System.getProperty("assets.path") + "/templates/servers/resolv.conf", "/home/" + mtaServ.sshUsername + "/resolv.conf");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/resolv.conf /etc/resolv.conf");
        } else {
            ssh.upload(System.getProperty("assets.path") + "/templates/servers/resolv.conf", "/etc/resolv.conf");
        }

        if (version == 7) {
            ssh.shellCommand("systemctl restart httpd && " + prefix + "systemctl enable httpd");
        } else {
            ssh.shellCommand("service httpd restart && " + prefix + "chkconfig httpd on");
        }
    }

    public static void installServicesUbuntu(SSHConnector ssh, MtaServer mtaServ, String prefix, boolean updated) throws Exception {
        ssh.shellCommand(prefix + "export DEBIAN_FRONTEND=noninteractive && dpkg --force-confold --configure -a && apt-get update -y");
        ssh.shellCommand(prefix + "apt-get install -y software-properties-common");

        if (updated){
            ssh.shellCommand(prefix + "apt-get upgrade -y");
        }

        ssh.shellCommand(prefix + "apt-get remove -y apache2 opendkim postfix php*");
        ssh.cmd(prefix + "rm -rf /etc/apache2");
        ssh.cmd(prefix + "rm -rf /var/www/html/*");
        ssh.cmd(prefix + "rm -rf /etc/opendkim/");
        ssh.cmd(prefix + "rm -rf /etc/opendkim.conf");
        ssh.cmd(prefix + "systemctl stop exim");
        ssh.cmd(prefix + "systemctl stop sendmail");
        ssh.cmd(prefix + "setenforce 0");
        ssh.cmd(prefix + "setenforce Disabled");


        ssh.shellCommand(prefix + "apt-get install -y gdb nano wget apache2 zip unzip cron perl glibc libstdc++ libgcc");

        FileUtils.writeStringToFile(new File(System.getProperty("logs.path") + "/installations/inst_" + mtaServ.id + "_proc.log"), "Installing / re-Installing php 7 ......", "utf-8");

        ssh.cmd(prefix + "add-apt-repository ppa:ondrej/php -y");
        //install php 7.1
        ssh.shellCommand(prefix + "apt-get install -y php7.1 php7.1-cli php7.1-gd php7.1-curl php7.1-common");
        ssh.cmd(prefix + "sed -i 's/upload_max_filesize = 2M/upload_max_filesize = 3G/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_file_uploads = 20/max_file_uploads = 200/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/post_max_size = 8M/post_max_size = 3G/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/memory_limit = 128M/memory_limit = -1/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/;max_input_nesting_level = 64/max_input_nesting_level = 10000/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/;max_input_vars = 1000/max_input_vars = 100000/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/default_socket_timeout = 60/default_socket_timeout = 360000/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_execution_time = 30/max_execution_time = 360000/g' /etc/php/7.1/apache2/php.ini");
        ssh.cmd(prefix + "sed -i 's/max_input_time = 60/max_input_time = 360000/g' /etc/php/7.1/apache2/php.ini");



        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(System.getProperty("assets.path") + "/templates/servers/resolv.conf", "/home/" + mtaServ.sshUsername + "/resolv.conf");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/resolv.conf /etc/resolv.conf");
        } else {
            ssh.upload(System.getProperty("assets.path") + "/templates/servers/resolv.conf", "/etc/resolv.conf");
        }

        ssh.shellCommand(prefix + "systemctl restart apache2");
        ssh.shellCommand(prefix + "systemctl enable apache2");
    }

    public static void installDkimDmarc(SSHConnector ssh, MtaServer mtaServ, String prefix, JSONArray data, boolean keepOldSubs, boolean usePredefinedSubs, boolean activateDmarc, boolean activateDkim) throws Exception {
        ManagementServer managServ = ManageServerWebmail.getCurrentWebMailServer();
        ssh.cmd(prefix + "rm -rf /etc/opendkim/");
        ssh.shellCommand(prefix + "yum remove -y libopendkim-devel opendkim");

        if (activateDkim) {
            ssh.shellCommand(prefix + "rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm");
            ssh.shellCommand(prefix + "yum install --enablerepo='epel' -y libopendkim-devel opendkim");

            ssh.cmd(prefix + "opendkim-default-keygen");

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/home/" + mtaServ.sshUsername + "/opendkim.conf");
                ssh.cmd(prefix + "mv /home/" + mtaServ.sshUsername + "/opendkim.conf /etc/opendkim.conf");
            } else {
                ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/etc/opendkim.conf");
            }
        }

        ArrayList<String> ipsV46List = new ArrayList();
        ServerVmta.delete(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Custom" });
        ServerVmta.delete(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "DKIM" });

        List<ServerVmta> listSrVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });

        HashMap<Object, Object> hashMap1 = new HashMap<>();
        if (listSrVmta != null && !listSrVmta.isEmpty()){
            listSrVmta.parallelStream().filter(srvmta1 -> !"".equals(srvmta1.name)).map(srvmta -> {
                srvmta.name = srvmta.domain.replaceAll("\r", "").replaceAll("\n", "");
                return srvmta;
            }).forEachOrdered(srvmta -> hashMap1.put(srvmta.ip.replaceAll("\r", "").replaceAll("\n", ""), srvmta));
        }

        DnsApi dnsapi = null;
        int b1 = 0;
        List listSubName = usePredefinedSubs ? (List)SubName.all(SubName.class, 3, "", new Object[0], new String[] { "*" }, new String[] { "id" }, "ASC") : new ArrayList();
        HashMap<Object, Object> hashMap2 = null;
        JSONArray domainInfo = null;
        List<String> hostList = Arrays.asList(new String[] { "@", "mail", "ftp", "www", "ns1", "*" });
        String ipV4v6 = "";
        int vmtaId = 0;
        String dkim1 = "";
        String vspf = "";
        String record = "A";
        int j = 0;
        boolean bool = true;
        for (int b2 = 0; b2 < data.length(); b2++) {
            JSONObject mapping = data.getJSONObject(b2);
            if (mapping != null) {
                String domains = mapping.getString("domain");
                if (domains.contains("|")) {
                    Domain domin = new Domain(Integer.valueOf(TypesParser.safeParseInt(domains.split(Pattern.quote("|"))[0])));
                    if (!"".equals(domin.value)) {
                        Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , ip_id = 0 WHERE id = " + domin.id, null, 0);
                        domin.value = domin.value.replaceAll("\r", "").replaceAll("\n", "");
                        dnsapi = DnsApi.controller(domin.accountType);
                        if (dnsapi != null && keepOldSubs) {
                            hashMap2 = new HashMap<>();
                            domainInfo = dnsapi.getDomainRecords(domin.accountId, domin.value);
                            if (domainInfo != null && domainInfo.length() > 0) {
                                JSONObject jSONObject1 = null;
                                for (int b = 0; b < domainInfo.length(); b++) {
                                    jSONObject1 = domainInfo.getJSONObject(b);
                                    if (jSONObject1 != null && jSONObject1.length() > 0 && "A".equals(jSONObject1.getString("type")) && !hostList.contains(jSONObject1.getString("host"))){
                                        hashMap2.put(jSONObject1.getString("value"), jSONObject1.getString("host"));
                                    }
                                }
                            }
                        }
                        boolean hasIpV6 = false;
                        ipV4v6 = "";
                        vmtaId = 0;
                        vspf = "v=spf1";
                        j = 7;
                        if (mapping.has("ips-v4") && mapping.get("ips-v4") instanceof JSONArray && mapping.getJSONArray("ips-v4").length() > 0) {
                            ipV4v6 = mapping.getJSONArray("ips-v4").getString(0).split(Pattern.quote("|"))[1];
                            vspf = vspf + " ip4:" + ipV4v6;
                        } else if (mapping.has("ips-v6") && mapping.get("ips-v6") instanceof JSONArray && mapping.getJSONArray("ips-v6").length() > 0) {
                            ipV4v6 = mapping.getJSONArray("ips-v6").getString(0).split(Pattern.quote("|"))[1];
                            vspf = vspf + " ip6:" + ipV4v6;
                            hasIpV6 = true;
                        }
                        if (bool) {
                            String pathHost = StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/hosts")), "$p_ip", ipV4v6), "$p_domain", domin.value);
                            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                                ssh.cmd("hostname mail." + domin.value);
                                ssh.uploadContent(pathHost, "/home/" + mtaServ.sshUsername + "/hosts");
                                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/hosts /etc/hosts");
                            } else {
                                ssh.cmd("hostname mail." + domin.value);
                                ssh.uploadContent(pathHost, "/etc/hosts");
                            }

                            String networkContent = ssh.cmd(prefix + "cat /etc/sysconfig/network");
                            if (!"".equals(networkContent)){
                                for (String str7 : networkContent.split("\\n")) {
                                    if (str7 != null && !"".equals(str7) && str7.startsWith("HOSTNAME")){
                                        networkContent = networkContent.replace(str7, "HOSTNAME=\"mail." + domin.value + "\"");
                                    }
                                }
                            }

                            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                                ssh.uploadContent(networkContent, "/home/" + mtaServ.sshUsername + "/network");
                                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/network /etc/sysconfig/network");
                            } else {
                                ssh.uploadContent(networkContent, "/etc/sysconfig/network");
                            }

                            mtaServ.hostName = "mail." + domin.value;
                            mtaServ.update();
                            bool = false;
                        }
                        vmtaId = saveServerVmta(hashMap1.containsKey(ipV4v6) ? (ServerVmta)hashMap1.get(ipV4v6) : null, ipV4v6, "", domin.value, mtaServ);
                        ipsV46List.add(ipV4v6);

                        if (activateDkim) {
                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + domin.value + ";");
                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + domin.value + "/ -d " + domin.value + " -s mail;");
                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + domin.value + "");
                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + domin.value + "/mail.private");
                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + domin.value + "/mail.txt");
                            ssh.cmd(prefix + "echo \"mail._dkim." + domin.value + " " + domin.value + ":mail:/etc/opendkim/keys/" + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                            ssh.cmd(prefix + "echo \"" + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                            ssh.cmd(prefix + "echo \"*@" + domin.value + " mail._domainkey." + domin.value + "\" >> /etc/opendkim/SigningTable");
                            dkim1 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                            dkim1 = dkim1.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                            dkim1 = dkim1.contains("DKIM1") ? dkim1 : "";
                        }

                        if (dnsapi != null) {
                            dnsapi.setRecords(domin.accountId, domin.value);
                            if (!hasIpV6) {
                                dnsapi.setupRecords(record, ipV4v6, domin, managServ);
                            } else {
                                dnsapi.setupRecords(record, mtaServ.mainIp, domin, managServ);
                            }
                        }

                        if (mapping.has("ips-v4")) {
                            JSONArray ipV4 = mapping.getJSONArray("ips-v4");
                            if (ipV4 != null && ipV4.length() > 0) {
                                String subNam4 = "";
                                for (int b = 0; b < ipV4.length(); b++) {
                                    String mainIp4 = ipV4.getString(b).replaceAll("\n", "").replaceAll("\r", "").split(Pattern.quote("|"))[1];
                                    if (!ipV4v6.equals(mainIp4)) {

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp4)) {
                                            subNam4 = (String)hashMap2.get(mainIp4);
                                        } else {
                                            subNam4 = null;
                                            if (usePredefinedSubs && b1 < listSubName.size()) {
                                                subNam4 = ((SubName)listSubName.get(b1)).name;
                                                b1++;
                                            }
                                            if (subNam4 == null){
                                                subNam4 = Strings.getSaltString(4, true, false, true, false);
                                            }
                                        }

                                        String dkim4 = "";
                                        if (activateDkim) {
                                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + subNam4 + "." + domin.value + ";");
                                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/ -d " + subNam4 + "." + domin.value + " -s mail;");
                                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + subNam4 + "." + domin.value + "");
                                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.private");
                                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.txt");
                                            ssh.cmd(prefix + "echo \"mail._dkim." + subNam4 + "." + domin.value + " " + subNam4 + "." + domin.value + ":mail:/etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                                            ssh.cmd(prefix + "echo \"" + subNam4 + "." + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                                            ssh.cmd(prefix + "echo \"*@" + subNam4 + "." + domin.value + " mail._domainkey." + subNam4 + "." + domin.value + "\" >> /etc/opendkim/SigningTable");
                                            dkim4 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                                            dkim4 = dkim4.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                                            dkim4 = dkim4.contains("DKIM1") ? dkim4 : "";
                                        }

                                        if (dnsapi != null) {
                                            dnsapi.addSPFDkim(j, mainIp4, subNam4, "A", "v=spf1 ip4:" + mainIp4 + " -all", dkim4);
                                            vspf = vspf + " ip4:" + mainIp4;
                                            j = !"".equals(dkim4) ? (j + 3) : (j + 2);
                                        }

                                        saveServerVmta(hashMap1.containsKey(mainIp4) ? (ServerVmta)hashMap1.get(mainIp4) : null, mainIp4, subNam4 + ".", domin.value, mtaServ);
                                        ipsV46List.add(mainIp4);
                                    }
                                }
                            }
                        }

                        if (mapping.has("ips-v6")) {
                            JSONArray ipV6 = mapping.getJSONArray("ips-v6");
                            if (ipV6 != null && ipV6.length() > 0) {
                                String subNam6 = "";
                                for (int b = 0; b < ipV6.length(); b++) {
                                    String mainIp6 = ipV6.getString(b).replaceAll("\n", "").replaceAll("\r", "").split(Pattern.quote("|"))[1];
                                    if (!ipV4v6.equals(mainIp6)) {

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp6)) {
                                            subNam6 = (String)hashMap2.get(mainIp6);
                                        } else {
                                            subNam6 = null;
                                            if (usePredefinedSubs && b1 < listSubName.size()) {
                                                subNam6 = ((SubName)listSubName.get(b1)).name;
                                                b1++;
                                            }
                                            if (subNam6 == null){
                                                subNam6 = Strings.getSaltString(4, true, false, true, false);
                                            }
                                        }

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp6)){
                                            subNam6 = (String)hashMap2.get(mainIp6);
                                        }

                                        String dkim6 = "";
                                        if (activateDkim) {
                                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + subNam6 + "." + domin.value + ";");
                                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/ -d " + subNam6 + "." + domin.value + " -s mail;");
                                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + subNam6 + "." + domin.value + "");
                                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.private");
                                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.txt");
                                            ssh.cmd(prefix + "echo \"mail._dkim." + subNam6 + "." + domin.value + " " + subNam6 + "." + domin.value + ":mail:/etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                                            ssh.cmd(prefix + "echo \"" + subNam6 + "." + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                                            ssh.cmd(prefix + "echo \"*@" + subNam6 + "." + domin.value + " mail._domainkey." + subNam6 + "." + domin.value + "\" >> /etc/opendkim/SigningTable");
                                            dkim6 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                                            dkim6 = dkim6.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                                            dkim6 = dkim6.contains("DKIM1") ? dkim6 : "";
                                        }

                                        if (dnsapi != null) {
                                            dnsapi.addSPFDkim(j, mainIp6, subNam6, "AAAA", "v=spf1 ip6:" + mainIp6 + " -all", dkim6);
                                            vspf = vspf + " ip6:" + mainIp6;
                                            j = !"".equals(dkim6) ? (j + 3) : (j + 2);
                                        }

                                        saveServerVmta(hashMap1.containsKey(mainIp6) ? (ServerVmta)hashMap1.get(mainIp6) : null, mainIp6, subNam6 + ".", domin.value, mtaServ);
                                        ipsV46List.add(mainIp6);
                                    }
                                }
                            }
                        }

                        if (dnsapi != null) {
                            dnsapi.setupDkimDmarc(j, dkim1, ipV4v6, hasIpV6, vspf + " -all", activateDmarc, domin);
                            System.out.println(dnsapi.setDomainRecords(domin.accountId, domin.value, null));
                        }

                        Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Taken' , mta_server_id = '" + mtaServ.id + "', ip_id = '" + vmtaId + "' WHERE id = " + domin.id, null, 0);
                        if ("namecheap".equals(domin.accountType) && b2 > 0 && b2 % 5 == 0){
                            ThreadSleep.sleep(60000L);
                        }
                    }
                }
            }
        }

        if (listSrVmta != null && !listSrVmta.isEmpty()){
            for (ServerVmta srVmta : listSrVmta) {
                if (!ipsV46List.contains(srVmta.ip)) {
                    srVmta.delete();
                }
            }
        }

        List list3 = (List)ServerVmta.all(ServerVmta.class, 2, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });
        mtaServ.ipsCount = list3.size();
        mtaServ.update();
    }

    public static void installDkimDmarcUbuntu(SSHConnector ssh, MtaServer mtaServ, String prefix, JSONArray data, boolean keepOldSubs, boolean usePredefinedSubs, boolean activateDmarc, boolean activateDkim) throws Exception {
        ManagementServer managServ = ManageServerWebmail.getCurrentWebMailServer();
        ssh.cmd(prefix + "rm -rf /etc/opendkim/");
        ssh.shellCommand(prefix + "apt-get remove -y opendkim opendkim-tools");

        if (activateDkim) {
            ssh.shellCommand(prefix + "add-apt-repository universe");
            ssh.shellCommand(prefix + "apt-get install -y opendkim opendkim-tools");

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/home/" + mtaServ.sshUsername + "/opendkim.conf");
                ssh.cmd(prefix + "mv /home/" + mtaServ.sshUsername + "/opendkim.conf /etc/opendkim.conf");
            } else {
                ssh.upload(System.getProperty("assets.path") + "/templates/servers/dkim-config.tpl", "/etc/opendkim.conf");
            }
        }

        ArrayList<String> ipsV46List = new ArrayList();
        ServerVmta.delete(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Custom" });
        ServerVmta.delete(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "DKIM" });

        List<ServerVmta> listSrVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });

        HashMap<Object, Object> hashMap1 = new HashMap<>();
        if (listSrVmta != null && !listSrVmta.isEmpty()){
            listSrVmta.parallelStream().filter(srvmta1 -> !"".equals(srvmta1.name)).map(srvmta -> {
                srvmta.name = srvmta.domain.replaceAll("\r", "").replaceAll("\n", "");
                return srvmta;
            }).forEachOrdered(srvmta -> hashMap1.put(srvmta.ip.replaceAll("\r", "").replaceAll("\n", ""), srvmta));
        }

        DnsApi dnsapi = null;
        int b1 = 0;
        List listSubName = usePredefinedSubs ? (List)SubName.all(SubName.class, 3, "", new Object[0], new String[] { "*" }, new String[] { "id" }, "ASC") : new ArrayList();
        HashMap<Object, Object> hashMap2 = null;
        JSONArray domainInfo = null;
        List<String> hostList = Arrays.asList(new String[] { "@", "mail", "ftp", "www", "ns1", "*" });
        String ipV4v6 = "";
        int vmtaId = 0;
        String dkim1 = "";
        String vspf = "";
        String record = "A";
        int j = 0;
        boolean bool = true;
        for (int b2 = 0; b2 < data.length(); b2++) {
            JSONObject mapping = data.getJSONObject(b2);
            if (mapping != null) {
                String domains = mapping.getString("domain");
                if (domains.contains("|")) {
                    Domain domin = new Domain(Integer.valueOf(TypesParser.safeParseInt(domains.split(Pattern.quote("|"))[0])));
                    if (!"".equals(domin.value)) {
                        Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Available' , ip_id = 0 WHERE id = " + domin.id, null, 0);
                        domin.value = domin.value.replaceAll("\r", "").replaceAll("\n", "");
                        dnsapi = DnsApi.controller(domin.accountType);
                        if (dnsapi != null && keepOldSubs) {
                            hashMap2 = new HashMap<>();
                            domainInfo = dnsapi.getDomainRecords(domin.accountId, domin.value);
                            if (domainInfo != null && domainInfo.length() > 0) {
                                JSONObject jSONObject1 = null;
                                for (int b = 0; b < domainInfo.length(); b++) {
                                    jSONObject1 = domainInfo.getJSONObject(b);
                                    if (jSONObject1 != null && jSONObject1.length() > 0 && "A".equals(jSONObject1.getString("type")) && !hostList.contains(jSONObject1.getString("host"))){
                                        hashMap2.put(jSONObject1.getString("value"), jSONObject1.getString("host"));
                                    }
                                }
                            }
                        }
                        boolean hasIpV6 = false;
                        ipV4v6 = "";
                        vmtaId = 0;
                        vspf = "v=spf1";
                        j = 7;
                        if (mapping.has("ips-v4") && mapping.get("ips-v4") instanceof JSONArray && mapping.getJSONArray("ips-v4").length() > 0) {
                            ipV4v6 = mapping.getJSONArray("ips-v4").getString(0).split(Pattern.quote("|"))[1];
                            vspf = vspf + " ip4:" + ipV4v6;
                        } else if (mapping.has("ips-v6") && mapping.get("ips-v6") instanceof JSONArray && mapping.getJSONArray("ips-v6").length() > 0) {
                            ipV4v6 = mapping.getJSONArray("ips-v6").getString(0).split(Pattern.quote("|"))[1];
                            vspf = vspf + " ip6:" + ipV4v6;
                            hasIpV6 = true;
                        }
                        if (bool) {
                            String pathHost = StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(System.getProperty("assets.path") + "/templates/servers/hosts")), "$p_ip", ipV4v6), "$p_domain", domin.value);
                            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                                ssh.cmd("hostname mail." + domin.value);
                                ssh.uploadContent(pathHost, "/home/" + mtaServ.sshUsername + "/hosts");
                                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/hosts /etc/hosts");
                            } else {
                                ssh.cmd("hostname mail." + domin.value);
                                ssh.uploadContent(pathHost, "/etc/hosts");
                            }

                            String networkContent = ssh.cmd(prefix + "cat /etc/hostname");
                            if (!"".equals(networkContent)) {
                                networkContent = "mail." + domin.value;
                            }

                            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                                ssh.uploadContent(networkContent, "/home/" + mtaServ.sshUsername + "/hostname");
                                ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/hostname /etc/hostname");
                                ssh.cmd("sudo hostnamectl set-hostname mail." + domin.value);
                            } else {
                                ssh.uploadContent(networkContent, "/etc/hostname");
                                ssh.cmd("sudo hostnamectl set-hostname mail." + domin.value);
                            }

                            mtaServ.hostName = "mail." + domin.value;
                            mtaServ.update();
                            bool = false;
                        }
                        vmtaId = saveServerVmta(hashMap1.containsKey(ipV4v6) ? (ServerVmta)hashMap1.get(ipV4v6) : null, ipV4v6, "", domin.value, mtaServ);
                        ipsV46List.add(ipV4v6);

                        if (activateDkim) {
                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + domin.value + ";");
                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + domin.value + "/ -d " + domin.value + " -s mail;");
                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + domin.value + "");
                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + domin.value + "/mail.private");
                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + domin.value + "/mail.txt");
                            ssh.cmd(prefix + "echo \"mail._dkim." + domin.value + " " + domin.value + ":mail:/etc/opendkim/keys/" + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                            ssh.cmd(prefix + "echo \"" + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                            ssh.cmd(prefix + "echo \"*@" + domin.value + " mail._domainkey." + domin.value + "\" >> /etc/opendkim/SigningTable");
                            dkim1 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                            dkim1 = dkim1.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                            dkim1 = dkim1.contains("DKIM1") ? dkim1 : "";
                        }

                        if (dnsapi != null) {
                            dnsapi.setRecords(domin.accountId, domin.value);
                            if (!hasIpV6) {
                                dnsapi.setupRecords(record, ipV4v6, domin, managServ);
                            } else {
                                dnsapi.setupRecords(record, mtaServ.mainIp, domin, managServ);
                            }
                        }

                        if (mapping.has("ips-v4")) {
                            JSONArray ipV4 = mapping.getJSONArray("ips-v4");
                            if (ipV4 != null && ipV4.length() > 0) {
                                String subNam4 = "";
                                for (int b = 0; b < ipV4.length(); b++) {
                                    String mainIp4 = ipV4.getString(b).replaceAll("\n", "").replaceAll("\r", "").split(Pattern.quote("|"))[1];
                                    if (!ipV4v6.equals(mainIp4)) {

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp4)) {
                                            subNam4 = (String)hashMap2.get(mainIp4);
                                        } else {
                                            subNam4 = null;
                                            if (usePredefinedSubs && b1 < listSubName.size()) {
                                                subNam4 = ((SubName)listSubName.get(b1)).name;
                                                b1++;
                                            }
                                            if (subNam4 == null){
                                                subNam4 = Strings.getSaltString(4, true, false, true, false);
                                            }
                                        }

                                        String dkim4 = "";
                                        if (activateDkim) {
                                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + subNam4 + "." + domin.value + ";");
                                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/ -d " + subNam4 + "." + domin.value + " -s mail;");
                                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + subNam4 + "." + domin.value + "");
                                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.private");
                                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.txt");
                                            ssh.cmd(prefix + "echo \"mail._dkim." + subNam4 + "." + domin.value + " " + subNam4 + "." + domin.value + ":mail:/etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                                            ssh.cmd(prefix + "echo \"" + subNam4 + "." + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                                            ssh.cmd(prefix + "echo \"*@" + subNam4 + "." + domin.value + " mail._domainkey." + subNam4 + "." + domin.value + "\" >> /etc/opendkim/SigningTable");
                                            dkim4 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + subNam4 + "." + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                                            dkim4 = dkim4.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                                            dkim4 = dkim4.contains("DKIM1") ? dkim4 : "";
                                        }

                                        if (dnsapi != null) {
                                            dnsapi.addSPFDkim(j, mainIp4, subNam4, "A", "v=spf1 ip4:" + mainIp4 + " -all", dkim4);
                                            vspf = vspf + " ip4:" + mainIp4;
                                            j = !"".equals(dkim4) ? (j + 3) : (j + 2);
                                        }

                                        saveServerVmta(hashMap1.containsKey(mainIp4) ? (ServerVmta)hashMap1.get(mainIp4) : null, mainIp4, subNam4 + ".", domin.value, mtaServ);
                                        ipsV46List.add(mainIp4);
                                    }
                                }
                            }
                        }

                        if (mapping.has("ips-v6")) {
                            JSONArray ipV6 = mapping.getJSONArray("ips-v6");
                            if (ipV6 != null && ipV6.length() > 0) {
                                String subNam6 = "";
                                for (int b = 0; b < ipV6.length(); b++) {
                                    String mainIp6 = ipV6.getString(b).replaceAll("\n", "").replaceAll("\r", "").split(Pattern.quote("|"))[1];
                                    if (!ipV4v6.equals(mainIp6)) {

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp6)) {
                                            subNam6 = (String)hashMap2.get(mainIp6);
                                        } else {
                                            subNam6 = null;
                                            if (usePredefinedSubs && b1 < listSubName.size()) {
                                                subNam6 = ((SubName)listSubName.get(b1)).name;
                                                b1++;
                                            }
                                            if (subNam6 == null){
                                                subNam6 = Strings.getSaltString(4, true, false, true, false);
                                            }
                                        }

                                        if (keepOldSubs && hashMap2 != null && hashMap2.containsKey(mainIp6)){
                                            subNam6 = (String)hashMap2.get(mainIp6);
                                        }

                                        String dkim6 = "";
                                        if (activateDkim) {
                                            ssh.cmd(prefix + "mkdir -p /etc/opendkim/keys/" + subNam6 + "." + domin.value + ";");
                                            ssh.cmd(prefix + "opendkim-genkey -D /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/ -d " + subNam6 + "." + domin.value + " -s mail;");
                                            ssh.cmd(prefix + "chown -R opendkim:opendkim /etc/opendkim/keys/" + subNam6 + "." + domin.value + "");
                                            ssh.cmd(prefix + "chmod 640 /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.private");
                                            ssh.cmd(prefix + "chmod 644 /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.txt");
                                            ssh.cmd(prefix + "echo \"mail._dkim." + subNam6 + "." + domin.value + " " + subNam6 + "." + domin.value + ":mail:/etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.private\" >> /etc/opendkim/KeyTable");
                                            ssh.cmd(prefix + "echo \"" + subNam6 + "." + domin.value + "\" >> /etc/opendkim/TrustedHosts");
                                            ssh.cmd(prefix + "echo \"*@" + subNam6 + "." + domin.value + " mail._domainkey." + subNam6 + "." + domin.value + "\" >> /etc/opendkim/SigningTable");
                                            dkim6 = String.valueOf(ssh.cmd("cat /etc/opendkim/keys/" + subNam6 + "." + domin.value + "/mail.txt | cut -d \"(\" -f2 | cut -d \")\" -f1 | awk '{ printf \"%s\", $0 }'"));
                                            dkim6 = dkim6.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", " ").replaceAll(" +", " ").trim();
                                            dkim6 = dkim6.contains("DKIM1") ? dkim6 : "";
                                        }

                                        if (dnsapi != null) {
                                            dnsapi.addSPFDkim(j, mainIp6, subNam6, "AAAA", "v=spf1 ip6:" + mainIp6 + " -all", dkim6);
                                            vspf = vspf + " ip6:" + mainIp6;
                                            j = !"".equals(dkim6) ? (j + 3) : (j + 2);
                                        }

                                        saveServerVmta(hashMap1.containsKey(mainIp6) ? (ServerVmta)hashMap1.get(mainIp6) : null, mainIp6, subNam6 + ".", domin.value, mtaServ);
                                        ipsV46List.add(mainIp6);
                                    }
                                }
                            }
                        }

                        if (dnsapi != null) {
                            dnsapi.setupDkimDmarc(j, dkim1, ipV4v6, hasIpV6, vspf + " -all", activateDmarc, domin);
                            System.out.println(dnsapi.setDomainRecords(domin.accountId, domin.value, null));
                        }

                        Database.get("system").executeUpdate("UPDATE admin.domains SET availability = 'Taken' , mta_server_id = '" + mtaServ.id + "', ip_id = '" + vmtaId + "' WHERE id = " + domin.id, null, 0);
                        if ("namecheap".equals(domin.accountType) && b2 > 0 && b2 % 5 == 0){
                            ThreadSleep.sleep(60000L);
                        }
                    }
                }
            }
        }

        if (listSrVmta != null && !listSrVmta.isEmpty()){
            for (ServerVmta srVmta : listSrVmta) {
                if (!ipsV46List.contains(srVmta.ip)) {
                    srVmta.delete();
                }
            }
        }

        List list3 = (List)ServerVmta.all(ServerVmta.class, 2, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });
        mtaServ.ipsCount = list3.size();
        mtaServ.update();
    }

    public static void installTracking(SSHConnector ssh, MtaServer mtaServ, String prefix, boolean useBrands, boolean useSsl) throws Exception {
        HashMap<Object, Object> hashMap = new HashMap<>();
        boolean installbrands = false;

        List<ServerVmta> listVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Default" });

        if (listVmta == null || listVmta.isEmpty()){
            throw new DatabaseException("No vmtas found !");
        }

        for (ServerVmta srvVmta : listVmta) {
            String vmtaDomain = Url.checkUrl(srvVmta.domain);
            if (!hashMap.containsKey(vmtaDomain)) {
                Domain domin = (Domain)Domain.first(Domain.class, "LOWER(value) = ?", new Object[] { vmtaDomain });
                if (domin != null && !domin.getEmpty()) {
                    hashMap.put(vmtaDomain, domin);
                    installbrands = "no".equalsIgnoreCase(domin.hasBrand);
                }
            }
        }

        String pathTracking = System.getProperty("assets.path") + "/tracking";
        int uploadServerId = TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id"));
        String baseUrl = String.valueOf(Application.getSettingsParam("base_url"));
        String trackingKey = String.valueOf(Application.getSettingsParam("tracking_enc_key"));
        String apiUrl = Convertion.crypt(baseUrl + "/api.json", trackingKey);

        if (uploadServerId > 0) {
            ManagementServer mngServer = new ManagementServer(Integer.valueOf(uploadServerId));
            if (!mngServer.getEmpty()){
                baseUrl = "http://" + ("".equals(mngServer.hostName) ? mngServer.mainIp : mngServer.hostName);
            }
        }

        baseUrl = Convertion.crypt(baseUrl, trackingKey);

        ssh.cmd(prefix + "rm -rf /var/www/html/*");
        ssh.cmd(prefix + "rm -rf /var/www/scripts");
        ssh.cmd(prefix + "rm -rf /var/www/brands");
        ssh.cmd(prefix + "rm -rf /var/log/iresponse");
        ssh.cmd(prefix + "rm -rf /etc/httpd/conf.d/vhosts.conf");
        ssh.cmd(prefix + "mkdir /var/www/scripts/");
        ssh.cmd(prefix + "mkdir /var/www/scripts/prod/");
        ssh.cmd(prefix + "mkdir /var/www/scripts/tmp/");
        ssh.cmd(prefix + "mkdir /var/www/brands/");
        ssh.cmd(prefix + "mkdir /var/log/iresponse/");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiUrl), "/home/" + mtaServ.sshUsername + "/stats.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/stats.php /var/www/scripts/stats.php");
            ssh.upload(pathTracking + "/scripts/gc.php", "/home/" + mtaServ.sshUsername + "/gc.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/gc.php /var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", trackingKey), "/home/" + mtaServ.sshUsername + "/help.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/help.php /var/www/scripts/help.php");
        } else {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiUrl), "/var/www/scripts/stats.php");
            ssh.upload(pathTracking + "/scripts/gc.php", "/var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", String.valueOf(Application.getSettingsParam("tracking_enc_key"))), "/var/www/scripts/help.php");
        }

        if (installbrands) {
            ssh.cmd(prefix + "mkdir -p /var/www/brands/default");
            if (useBrands) {
                ssh.upload(pathTracking + "/brands/default.zip", "/var/www/brands/default.zip");
                ssh.cmd("unzip /var/www/brands/default.zip -d /var/www/brands/default");
                ssh.cmd("rm -rf /var/www/brands/default.zip");
            } else if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(pathTracking + "/system/home.html", "/home/" + mtaServ.sshUsername + "/home.html");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/home.html /var/www/brands/default/home.html");
            } else {
                ssh.upload(pathTracking + "/system/home.html", "/var/www/brands/default/home.html");
            }

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/default/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/default/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/default/optout.php");
            } else {
                ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/default/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/var/www/brands/default/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/default/optout.php");
            }
        }

        String vhostVersionFolder = pathTracking + "/templates/vhosts7.conf";
        String vhosts = FileUtils.readFileToString(new File(vhostVersionFolder), "UTF-8");
        String contents = "";
        for (Map.Entry<Object, Object> entry : hashMap.entrySet()) {
            Domain domain2 = (Domain)entry.getValue();
            domain2.value = domain2.value.replaceAll("\r", "").replaceAll("\n", "");
            String dmn = StringUtils.replace(domain2.value, ".", "_");

            if ("yes".equalsIgnoreCase(domain2.hasBrand) && (new File(pathTracking + "/brands/" + dmn + ".zip")).exists()) {

                ssh.cmd(prefix + "mkdir -p /var/www/brands/" + dmn);
                if (useBrands) {
                    ssh.upload(pathTracking + "/brands/" + dmn + ".zip", "/var/www/brands/" + dmn + ".zip");
                    ssh.cmd("unzip /var/www/brands/" + dmn + ".zip -d /var/www/brands/" + dmn);
                    ssh.cmd("rm -rf /var/www/brands/" + dmn + ".zip");
                } else if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                    ssh.upload(pathTracking + "/system/home.html", "/home/" + mtaServ.sshUsername + "/home.html");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/home.html /var/www/brands/" + dmn + "/home.html");
                } else {
                    ssh.upload(pathTracking + "/system/home.html", "/var/www/brands/" + dmn + "/home.html");
                }

                if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                    ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/" + dmn + "/.htaccess");
                    ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/" + dmn + "/index.php");
                    ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/" + dmn + "/optout.php");
                    continue;
                }

                ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/" + dmn + "/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/var/www/brands/" + dmn + "/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/" + dmn + "/optout.php");
            }
        }

        for (ServerVmta srvVmta2 : listVmta) {
            String str9 = Url.checkUrl(srvVmta2.domain);
            Domain domain3 = (Domain)hashMap.get(str9);
            domain3.value = domain3.value.replaceAll("\r", "").replaceAll("\n", "");
            String str10 = StringUtils.replace(domain3.value, ".", "_");

            if ("yes".equalsIgnoreCase(domain3.hasBrand)) {
                String rdns = StringUtils.replace(vhosts, "$p_rdns", srvVmta2.domain);
                rdns = StringUtils.replace(rdns, "$p_ip", srvVmta2.ip);
                rdns = StringUtils.replace(rdns, "$p_path", "/var/www/brands/" + str10);
                contents = contents + rdns + "\n\n";
                continue;
            }

            String rdns2 = StringUtils.replace(vhosts, "$p_rdns", srvVmta2.domain);
            rdns2 = StringUtils.replace(rdns2, "$p_ip", srvVmta2.ip);
            rdns2 = StringUtils.replace(rdns2, "$p_path", "/var/www/brands/default");
            contents = contents + rdns2 + "\n\n";
        }

        ssh.cmd(prefix + "chown -R apache:apache /var/www/brands/");
        ssh.cmd(prefix + "rm -rf /etc/httpd/conf.d/welcome.conf");
        ssh.uploadContent(contents, "/etc/httpd/conf.d/vhosts.conf");
        ssh.upload(pathTracking + "/scripts/cron", "/home/cron");
        ssh.shellCommand(prefix + "crontab /home/cron");
        ssh.shellCommand(prefix + "rm -rf /home/cron");
        ssh.shellCommand(prefix + "service crond restart");
        ssh.shellCommand(prefix + "service httpd restart");
        String sslEmal = String.valueOf(Application.getSettingsParam("ssl_email"));
        if (useSsl && Matcher.pat1(sslEmal)) {
            ThreadSleep.sleep(60000L);
            ssh.shellCommand(prefix + "yum -y install epel-release");
            ssh.shellCommand(prefix + "yum -y install certbot python2-certbot-apache mod_ssl");

            String generateSsl = prefix + "certbot --apache -n --preferred-challenges http -m " + sslEmal + " --agree-tos --no-redirect --expand ";
            generateSsl = listVmta.stream().map(servervmta -> "-d " + servervmta.domain + " ").reduce(generateSsl, String::concat);
            ssh.shellCommand(generateSsl);
        } else {
            ssh.cmd(prefix + "rm -rf /etc/letsencrypt/");
            ssh.cmd(prefix + "rm -rf /usr/local/bin/certbot");
            ssh.cmd(prefix + "rm -rf /etc/httpd/conf.d/vhosts-le-ssl.conf");
            ssh.shellCommand(prefix + "service httpd restart");
        }
    }

    public static void installTrackingUbuntu(SSHConnector ssh, MtaServer mtaServ, String prefix, boolean useBrands, boolean useSsl) throws Exception {
        HashMap<Object, Object> hashMap = new HashMap<>();
        boolean installbrands = false;

        List<ServerVmta> listVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ? AND type = ?", new Object[] { Integer.valueOf(mtaServ.id), "Default" });

        if (listVmta == null || listVmta.isEmpty()){
            throw new DatabaseException("No vmtas found !");
        }

        for (ServerVmta srvVmta : listVmta) {
            String vmtaDomain = Url.checkUrl(srvVmta.domain);
            if (!hashMap.containsKey(vmtaDomain)) {
                Domain domin = (Domain)Domain.first(Domain.class, "LOWER(value) = ?", new Object[] { vmtaDomain });
                if (domin != null && !domin.getEmpty()) {
                    hashMap.put(vmtaDomain, domin);
                    installbrands = "no".equalsIgnoreCase(domin.hasBrand);
                }
            }
        }

        String pathTracking = System.getProperty("assets.path") + "/tracking";
        int uploadServerId = TypesParser.safeParseInt(Application.getSettingsParam("upload_center_id"));
        String baseUrl = String.valueOf(Application.getSettingsParam("base_url"));
        String trackingKey = String.valueOf(Application.getSettingsParam("tracking_enc_key"));
        String apiUrl = Convertion.crypt(baseUrl + "/api.json", trackingKey);

        if (uploadServerId > 0) {
            ManagementServer mngServer = new ManagementServer(Integer.valueOf(uploadServerId));
            if (!mngServer.getEmpty()){
                baseUrl = "http://" + ("".equals(mngServer.hostName) ? mngServer.mainIp : mngServer.hostName);
            }
        }

        baseUrl = Convertion.crypt(baseUrl, trackingKey);

        ssh.cmd(prefix + "rm -rf /var/www/html/*");
        ssh.cmd(prefix + "rm -rf /var/www/scripts");
        ssh.cmd(prefix + "rm -rf /var/www/brands");
        ssh.cmd(prefix + "rm -rf /var/log/iresponse");
        ssh.cmd(prefix + "rm -rf /etc/apache2/sites-enabled/vhosts.conf");
        ssh.cmd(prefix + "mkdir /var/www/scripts/");
        ssh.cmd(prefix + "mkdir /var/www/scripts/prod/");
        ssh.cmd(prefix + "mkdir /var/www/scripts/tmp/");
        ssh.cmd(prefix + "mkdir /var/www/brands/");
        ssh.cmd(prefix + "mkdir /var/log/iresponse/");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiUrl), "/home/" + mtaServ.sshUsername + "/stats.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/stats.php /var/www/scripts/stats.php");
            ssh.upload(pathTracking + "/scripts/gc.php", "/home/" + mtaServ.sshUsername + "/gc.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/gc.php /var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", trackingKey), "/home/" + mtaServ.sshUsername + "/help.php");
            ssh.cmd("sudo mv /home/" + mtaServ.sshUsername + "/help.php /var/www/scripts/help.php");
        } else {
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/stats.php"), "UTF-8"), "$p_api", apiUrl), "/var/www/scripts/stats.php");
            ssh.upload(pathTracking + "/scripts/gc.php", "/var/www/scripts/gc.php");
            ssh.uploadContent(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/scripts/help.php"), "UTF-8"), "$p_tracking_enc_key", String.valueOf(Application.getSettingsParam("tracking_enc_key"))), "/var/www/scripts/help.php");
        }

        if (installbrands) {
            ssh.cmd(prefix + "mkdir -p /var/www/brands/default");
            if (useBrands) {
                ssh.upload(pathTracking + "/brands/default.zip", "/var/www/brands/default.zip");
                ssh.cmd("unzip /var/www/brands/default.zip -d /var/www/brands/default");
                ssh.cmd("rm -rf /var/www/brands/default.zip");
            } else if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(pathTracking + "/system/home.html", "/home/" + mtaServ.sshUsername + "/home.html");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/home.html /var/www/brands/default/home.html");
            } else {
                ssh.upload(pathTracking + "/system/home.html", "/var/www/brands/default/home.html");
            }

            if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/default/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/default/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
                ssh.cmd("mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/default/optout.php");
            } else {
                ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/default/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/var/www/brands/default/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/default/optout.php");
            }
        }

        String vhostVersionFolder = pathTracking + "/templates/vhosts7.conf";
        String vhosts = FileUtils.readFileToString(new File(vhostVersionFolder), "UTF-8");
        String contents = "";
        for (Map.Entry<Object, Object> entry : hashMap.entrySet()) {
            Domain domain2 = (Domain)entry.getValue();
            domain2.value = domain2.value.replaceAll("\r", "").replaceAll("\n", "");
            String dmn = StringUtils.replace(domain2.value, ".", "_");

            if ("yes".equalsIgnoreCase(domain2.hasBrand) && (new File(pathTracking + "/brands/" + dmn + ".zip")).exists()) {

                ssh.cmd(prefix + "mkdir -p /var/www/brands/" + dmn);
                if (useBrands) {
                    ssh.upload(pathTracking + "/brands/" + dmn + ".zip", "/var/www/brands/" + dmn + ".zip");
                    ssh.cmd("unzip /var/www/brands/" + dmn + ".zip -d /var/www/brands/" + dmn);
                    ssh.cmd("rm -rf /var/www/brands/" + dmn + ".zip");
                } else if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                    ssh.upload(pathTracking + "/system/home.html", "/home/" + mtaServ.sshUsername + "/home.html");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/home.html /var/www/brands/" + dmn + "/home.html");
                } else {
                    ssh.upload(pathTracking + "/system/home.html", "/var/www/brands/" + dmn + "/home.html");
                }

                if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                    ssh.upload(pathTracking + "/system/htaccess", "/home/" + mtaServ.sshUsername + "/.htaccess");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/.htaccess /var/www/brands/" + dmn + "/.htaccess");
                    ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/home/" + mtaServ.sshUsername + "/index.php");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/index.php /var/www/brands/" + dmn + "/index.php");
                    ssh.upload(pathTracking + "/system/optout.php", "/home/" + mtaServ.sshUsername + "/optout.php");
                    ssh.cmd("mv /home/" + mtaServ.sshUsername + "/optout.php /var/www/brands/" + dmn + "/optout.php");
                    continue;
                }

                ssh.upload(pathTracking + "/system/htaccess", "/var/www/brands/" + dmn + "/.htaccess");
                ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(new File(pathTracking + "/system/index.php"), "UTF-8"), "$p_api", apiUrl), "$p_upload_ip", baseUrl), "/var/www/brands/" + dmn + "/index.php");
                ssh.upload(pathTracking + "/system/optout.php", "/var/www/brands/" + dmn + "/optout.php");
            }
        }

        for (ServerVmta srvVmta2 : listVmta) {
            String rdns2 = StringUtils.replace(vhosts, "$p_rdns", srvVmta2.domain);
            rdns2 = StringUtils.replace(rdns2, "$p_ip", srvVmta2.ip);
            rdns2 = StringUtils.replace(rdns2, "$p_path", "/var/www/brands/default");
            contents = contents + rdns2 + "\n\n";
        }

        ssh.cmd(prefix + "chown -R www-data:www-data /var/www");
        ssh.cmd(prefix + "rm -rf /etc/apache2/sites-enabled/000-default.conf");

        ssh.uploadContent(contents, "/etc/apache2/sites-available/vhosts.conf");
        ssh.cmd(prefix + "a2ensite vhosts.conf");

        ssh.upload(pathTracking + "/scripts/cron", "/home/" + mtaServ.sshUsername + "/cron");
        ssh.shellCommand(prefix + "crontab /home/" + mtaServ.sshUsername + "/cron");
        ssh.shellCommand(prefix + "rm -rf /home/" + mtaServ.sshUsername + "/cron");

        ssh.shellCommand(prefix + "systemctl restart cron");
        ssh.shellCommand(prefix + "systemctl restart apache2");

        String sslEmal = String.valueOf(Application.getSettingsParam("ssl_email"));
        if (useSsl && Matcher.pat1(sslEmal)) {
            ThreadSleep.sleep(60000L);
            ssh.shellCommand(prefix + "apt-get update");
            ssh.shellCommand(prefix + "apt-get -y install certbot python3-certbot-apache");

            String generateSsl = prefix + "certbot --apache -n --preferred-challenges http -m " + sslEmal + " --agree-tos --no-redirect --expand ";
            generateSsl = listVmta.stream().map(servervmta -> "-d " + servervmta.domain + " ").reduce(generateSsl, String::concat);
            ssh.shellCommand(generateSsl);
        } else {
            // Clean up SSL-related files if not using SSL
            ssh.cmd(prefix + "rm -rf /etc/letsencrypt/");
            ssh.cmd(prefix + "rm -rf /etc/apache2/sites-available/vhosts-le-ssl.conf");
            ssh.shellCommand(prefix + "systemctl restart apache2");
        }
    }

    public static void installPmta(SSHConnector ssh, MtaServer mtaServ, String prefix) throws Exception {

        List<ServerVmta> listVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });

        if (listVmta == null || listVmta.isEmpty()){
            throw new DatabaseException("No vmtas found !");
        }

        ssh.shellCommand(prefix + "service pmta stop && " + prefix + "service pmtahttp stop");
        ssh.shellCommand(prefix + "rpm -e $(rpm -qa 'PowerMTA*')");
        ssh.cmd(prefix + "rm -rf /etc/pmta");
        ssh.cmd(prefix + "rm -rf /var/lib/pmta");
        ssh.cmd(prefix + "rm -rf /var/log/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/iresponse");

        //String firwalldFolder = System.getProperty("assets.path") + "/scripts";
        String pmtaFolder = System.getProperty("assets.path") + "/pmta";
        boolean getSystm = String.valueOf(ssh.cmd("file /sbin/init")).contains("32-bit");
        String pmtaSystem = getSystm ? "pmta32.rpm" : "pmta64.rpm";

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + "/rpms/" + pmtaSystem, "/home/" + mtaServ.sshUsername + "/pmta.rpm");
            ssh.cmd("rpm -Uvh /home/" + mtaServ.sshUsername + "/pmta.rpm");
        } else {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + "/rpms/" + pmtaSystem, "/home/pmta.rpm");
            ssh.cmd("rpm -Uvh /home/pmta.rpm");
        }

        ssh.cmd(prefix + "rm -rf /etc/pmta/license-notice");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config-defaults");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/parameters/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/vmtas/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/keys/");

        String pmtaConfigPath = String.valueOf(Application.getSettingsParam("pmta_config_type")).toLowerCase();
        pmtaConfigPath = "".equals(pmtaConfigPath) ? (pmtaFolder + "/configs/default") : (pmtaFolder + "/configs/" + pmtaConfigPath);
        String hostNameMail = "host-name mail." + ((ServerVmta)listVmta.get(0)).domain.replaceAll("\r", "").replaceAll("\n", "") + "\n";
        String tplVmta = FileUtils.readFileToString(new File(pmtaConfigPath + "/vmta.tpl"), "UTF-8");

        listVmta.forEach(servervmta -> {
            String domains = servervmta.getDomain().replaceAll("\r", "").replaceAll("\n", "");
            String configTpl = StringUtils.replace(tplVmta, "$p_vmta", servervmta.getName());
            configTpl = StringUtils.replace(configTpl, "$p_ip", servervmta.getIp());
            configTpl = StringUtils.replace(configTpl, "$p_domain", domains);
            if (mtaServ.getDkimInstalled()) {
                ssh.cmd("cp -r /etc/opendkim/keys/" + domains + "/mail.private /etc/pmta/keys/" + servervmta.ip + ".pem");
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "domain-key mail," + domains + ",/etc/pmta/keys/" + servervmta.ip + ".pem");
            } else {
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "");
            }
            if ("user-pass".equals(mtaServ.getSshLoginType()) && !"root".equals(mtaServ.getSshUsername())) {
                ssh.uploadContent(configTpl, "/home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf");
                ssh.cmd("mv /home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf /etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            } else {
                ssh.uploadContent(configTpl, "/etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            }
        });

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(pmtaFolder + "/configs/license", "/home/" + mtaServ.sshUsername + "/license");
            ssh.upload(pmtaConfigPath + "/config", "/home/" + mtaServ.sshUsername + "/config");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/license /etc/pmta/license");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/config /etc/pmta/config");
        } else {
            ssh.upload(pmtaFolder + "/configs/license", "/etc/pmta/license");
            ssh.upload(pmtaConfigPath + "/config", "/etc/pmta/config");
        }

        File[] parametersFiles = (new File(pmtaConfigPath + "/parameters/")).listFiles();
        for (File file : parametersFiles) {
            if (!file.isDirectory()){
                switch (file.getName()) {
                    case "pmta_http.conf":
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/etc/pmta/parameters/" + file.getName());
                        break;
                    default:
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.upload(file.getAbsolutePath(), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.upload(file.getAbsolutePath(), "/etc/pmta/parameters/" + file.getName());
                        break;
                }
            }
        }

        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/archived/");
        ssh.cmd(prefix + "mkdir -p /var/spool/pmta/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/pickup/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/bad/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/tmp/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/log/pmta/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/spool/iresponse/");
        ssh.cmd(prefix + "chmod 640 /etc/pmta/config");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/pickup");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/bad");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/tmp");
        ssh.cmd(prefix + "chown -R pmta:pmta /etc/pmta/");
        ssh.shellCommand(prefix + "systemctl restart pmta");
        ssh.shellCommand(prefix + "systemctl restart pmtahttp");
        ssh.cmd(prefix + "rm -rf /etc/pmta/habeas.sample");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/pmta.rpm");
        } else {
            ssh.cmd("rm -rf /home/pmta.rpm");
        }
        System.out.println("Installing PowerMTA completed !");
    }

    public static void installPmta45(SSHConnector ssh, MtaServer mtaServ, String prefix) throws Exception {

        List<ServerVmta> listVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });

        if (listVmta == null || listVmta.isEmpty()){
            throw new DatabaseException("No vmtas found !");
        }

        ssh.shellCommand(prefix + "service pmta stop && " + prefix + "service pmtahttp stop");
        ssh.shellCommand(prefix + "rpm -e $(rpm -qa 'PowerMTA*')");
        ssh.cmd(prefix + "rm -rf /etc/pmta");
        ssh.cmd(prefix + "rm -rf /var/lib/pmta");
        ssh.cmd(prefix + "rm -rf /var/log/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/iresponse");

        //String firwalldFolder = System.getProperty("assets.path") + "/scripts";
        String pmtaFolder = System.getProperty("assets.path") + "/pmta";
        String pmtaSystem = "/45/pmta.rpm";

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + pmtaSystem, "/home/" + mtaServ.sshUsername + "/pmta.rpm");
            ssh.cmd("rpm -Uvh /home/" + mtaServ.sshUsername + "/pmta.rpm");
        } else {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + pmtaSystem, "/home/pmta.rpm");
            ssh.cmd("rpm -Uvh /home/pmta.rpm");
        }

        ssh.cmd(prefix + "rm -rf /etc/pmta/license-notice");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config-defaults");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/parameters/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/vmtas/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/keys/");

        String pmtaConfigPath = String.valueOf(Application.getSettingsParam("pmta_config_type")).toLowerCase();
        pmtaConfigPath = "".equals(pmtaConfigPath) ? (pmtaFolder + "/configs/default") : (pmtaFolder + "/configs/" + pmtaConfigPath);
        String hostNameMail = "host-name mail." + ((ServerVmta)listVmta.get(0)).domain.replaceAll("\r", "").replaceAll("\n", "") + "\n";
        String tplVmta = FileUtils.readFileToString(new File(pmtaConfigPath + "/vmta.tpl"), "UTF-8");

        listVmta.forEach(servervmta -> {
            String domains = servervmta.getDomain().replaceAll("\r", "").replaceAll("\n", "");
            String configTpl = StringUtils.replace(tplVmta, "$p_vmta", servervmta.getName());
            configTpl = StringUtils.replace(configTpl, "$p_ip", servervmta.getIp());
            configTpl = StringUtils.replace(configTpl, "$p_domain", domains);
            if (mtaServ.getDkimInstalled()) {
                ssh.cmd("cp -r /etc/opendkim/keys/" + domains + "/mail.private /etc/pmta/keys/" + servervmta.ip + ".pem");
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "domain-key mail," + domains + ",/etc/pmta/keys/" + servervmta.ip + ".pem");
            } else {
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "");
            }
            if ("user-pass".equals(mtaServ.getSshLoginType()) && !"root".equals(mtaServ.getSshUsername())) {
                ssh.uploadContent(configTpl, "/home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf");
                ssh.cmd("mv /home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf /etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            } else {
                ssh.uploadContent(configTpl, "/etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            }
        });

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(pmtaFolder + "/45/pmta.lic", "/home/" + mtaServ.sshUsername + "/license");
            ssh.upload(pmtaConfigPath + "/config", "/home/" + mtaServ.sshUsername + "/config");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/pmta.lic /etc/pmta/license");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/config /etc/pmta/config");
        } else {
            ssh.upload(pmtaFolder + "/45/pmta.lic", "/etc/pmta/license");
            ssh.upload(pmtaConfigPath + "/config", "/etc/pmta/config");
        }

        //patch pmta 45
        ssh.upload(pmtaFolder + "/45/pmtad_patch", "/usr/sbin/pmtad");
        ssh.cmd("chown pmta:pmta /usr/sbin/pmtad;");
        ssh.cmd("chmod 755 /usr/sbin/pmtad");

        File[] parametersFiles = (new File(pmtaConfigPath + "/parameters/")).listFiles();
        for (File file : parametersFiles) {
            if (!file.isDirectory()){
                switch (file.getName()) {
                    case "pmta_http.conf":
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/etc/pmta/parameters/" + file.getName());
                        break;
                    default:
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.upload(file.getAbsolutePath(), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.upload(file.getAbsolutePath(), "/etc/pmta/parameters/" + file.getName());
                        break;
                }
            }
        }

        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/archived/");
        ssh.cmd(prefix + "mkdir -p /var/spool/pmta/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/pickup/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/bad/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/tmp/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/log/pmta/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/spool/iresponse/");
        ssh.cmd(prefix + "chmod 640 /etc/pmta/config");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/pickup");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/bad");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/tmp");
        ssh.cmd(prefix + "chown -R pmta:pmta /etc/pmta/");
        ssh.shellCommand(prefix + "systemctl restart pmta");
        ssh.shellCommand(prefix + "systemctl restart pmtahttp");
        ssh.cmd(prefix + "rm -rf /etc/pmta/habeas.sample");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/pmta.rpm");
        } else {
            ssh.cmd("rm -rf /home/pmta.rpm");
        }
        System.out.println("Installing PowerMTA completed !");
    }

    public static void installPmta45Ubuntu(SSHConnector ssh, MtaServer mtaServ, String prefix) throws Exception {

        List<ServerVmta> listVmta = (List)ServerVmta.all(ServerVmta.class, "mta_server_id = ?", new Object[] { Integer.valueOf(mtaServ.id) });

        if (listVmta == null || listVmta.isEmpty()){
            throw new DatabaseException("No vmtas found !");
        }

        ssh.cmd(prefix + "groupadd pmta");
        ssh.cmd(prefix + "useradd -g pmta pmta");
        ssh.shellCommand(prefix + "systemctl stop pmta && " + prefix + "systemctl stop pmtahttp");
        ssh.cmd(prefix + "dpkg -r PowerMTA");
        ssh.cmd(prefix + "rm -rf /etc/pmta");
        ssh.cmd(prefix + "rm -rf /var/lib/pmta");
        ssh.cmd(prefix + "rm -rf /var/log/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/pmta");
        ssh.cmd(prefix + "rm -rf /var/spool/iresponse");

        //String firwalldFolder = System.getProperty("assets.path") + "/scripts";
        String pmtaFolder = System.getProperty("assets.path") + "/pmta";
        String pmtaSystem = "/45/pmta.deb";

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + pmtaSystem, "/home/" + mtaServ.sshUsername + "/pmta.deb");
            ssh.cmd("dpkg -i /home/" + mtaServ.sshUsername + "/pmta.deb");
        } else {
            //ssh.upload(firwalldFolder + "/firewall.pl", "/home/firewall.pl");
            ssh.upload(pmtaFolder + pmtaSystem, "/home/pmta.deb");
            ssh.cmd("dpkg -i /home/pmta.deb");
        }

        ssh.cmd(prefix + "rm -rf /etc/pmta/license-notice");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config");
        ssh.cmd(prefix + "rm -rf /etc/pmta/config-defaults");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/parameters/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/vmtas/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/keys/");
        ssh.cmd(prefix + "mkdir -p /var/spool/pmta; mkdir -p /var/log/pmta;/");

        String pmtaConfigPath = String.valueOf(Application.getSettingsParam("pmta_config_type")).toLowerCase();
        pmtaConfigPath = "".equals(pmtaConfigPath) ? (pmtaFolder + "/configs/default") : (pmtaFolder + "/configs/" + pmtaConfigPath);
        String hostNameMail = "host-name mail." + ((ServerVmta)listVmta.get(0)).domain.replaceAll("\r", "").replaceAll("\n", "") + "\n";
        String tplVmta = FileUtils.readFileToString(new File(pmtaConfigPath + "/vmta.tpl"), "UTF-8");

        listVmta.forEach(servervmta -> {
            String domains = servervmta.getDomain().replaceAll("\r", "").replaceAll("\n", "");
            String configTpl = StringUtils.replace(tplVmta, "$p_vmta", servervmta.getName());
            configTpl = StringUtils.replace(configTpl, "$p_ip", servervmta.getIp());
            configTpl = StringUtils.replace(configTpl, "$p_domain", domains);
            if (mtaServ.getDkimInstalled()) {
                ssh.cmd("cp -r /etc/opendkim/keys/" + domains + "/mail.private /etc/pmta/keys/" + servervmta.ip + ".pem");
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "domain-key mail," + domains + ",/etc/pmta/keys/" + servervmta.ip + ".pem");
            } else {
                configTpl = StringUtils.replace(configTpl, "$p_dkim", "");
            }
            if ("user-pass".equals(mtaServ.getSshLoginType()) && !"root".equals(mtaServ.getSshUsername())) {
                ssh.uploadContent(configTpl, "/home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf");
                ssh.cmd("mv /home/" + mtaServ.getSshUsername() + "/" + servervmta.getName() + ".conf /etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            } else {
                ssh.uploadContent(configTpl, "/etc/pmta/vmtas/" + servervmta.getName() + ".conf");
            }
        });

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.upload(pmtaFolder + "/45/pmta.lic", "/home/" + mtaServ.sshUsername + "/license");
            ssh.upload(pmtaConfigPath + "/config", "/home/" + mtaServ.sshUsername + "/config");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/pmta.lic /etc/pmta/license");
            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/config /etc/pmta/config");
        } else {
            ssh.upload(pmtaFolder + "/45/pmta.lic", "/etc/pmta/license");
            ssh.upload(pmtaConfigPath + "/config", "/etc/pmta/config");
        }

        //patch pmta 45
        ssh.cmd("rm -rf /usr/sbin/pmtad");
        ssh.upload(pmtaFolder + "/45/pmtad_patch", "/usr/sbin/pmtad");
        ssh.cmd("chown pmta:pmta /usr/sbin/pmtad;");
        ssh.cmd("chmod 755 /usr/sbin/pmtad");

        File[] parametersFiles = (new File(pmtaConfigPath + "/parameters/")).listFiles();
        for (File file : parametersFiles) {
            if (!file.isDirectory()){
                switch (file.getName()) {
                    case "pmta_http.conf":
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.uploadContent(StringUtils.replace(StringUtils.replace(FileUtils.readFileToString(file, "UTF-8"), "$p_host", hostNameMail), "$p_http_port", String.valueOf(Application.getSettingsParam("pmta_http_port"))), "/etc/pmta/parameters/" + file.getName());
                        break;
                    default:
                        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
                            ssh.upload(file.getAbsolutePath(), "/home/" + mtaServ.sshUsername + "/" + file.getName());
                            ssh.cmd("mv /home/" + mtaServ.sshUsername + "/" + file.getName() + " /etc/pmta/parameters/" + file.getName());
                            break;
                        }
                        ssh.upload(file.getAbsolutePath(), "/etc/pmta/parameters/" + file.getName());
                        break;
                }
            }
        }

        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/delivered/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/archived/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/process/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/bounces/backup/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/");
        ssh.cmd(prefix + "mkdir -p /etc/pmta/deffered/archived/");
        ssh.cmd(prefix + "mkdir -p /var/spool/pmta/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/pickup/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/bad/");
        ssh.cmd(prefix + "mkdir -p /var/spool/iresponse/tmp/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/log/pmta/");
        ssh.cmd(prefix + "chown -R pmta:pmta /var/spool/iresponse/");
        ssh.cmd(prefix + "chmod 640 /etc/pmta/config");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/pickup");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/bad");
        ssh.cmd(prefix + "chmod 755 /var/spool/iresponse/tmp");
        ssh.cmd(prefix + "chown -R pmta:pmta /etc/pmta/");
        ssh.shellCommand(prefix + "systemctl restart pmta");
        ssh.shellCommand(prefix + "systemctl restart pmtahttp");
        ssh.cmd(prefix + "rm -rf /etc/pmta/habeas.sample");

        if ("user-pass".equals(mtaServ.sshLoginType) && !"root".equals(mtaServ.sshUsername)) {
            ssh.cmd("rm -rf /home/" + mtaServ.sshUsername + "/pmta.deb");
        } else {
            ssh.cmd("rm -rf /home/pmta.deb");
        }
        System.out.println("Installing Ubuntu PowerMTA completed !");
    }

    public static synchronized int saveServerVmta(ServerVmta srvVmta, String ip, String sub, String domain, MtaServer mtaServ) throws Exception {
        int id = 0;
        boolean newVmta = false;
        String user = Application.checkUser();
        String name = ip.replaceAll("\\.+", "_").replaceAll("\\:+", "_");
        Date date = new Date(System.currentTimeMillis());

        if (srvVmta == null) {
            srvVmta = new ServerVmta();
            srvVmta.status = "Activated";
            srvVmta.type = "Default";
            srvVmta.name = name;
            srvVmta.ip = ip.replaceAll("\n", "").replaceAll("\r", "");
            srvVmta.domain = (sub + domain).replaceAll("\n", "").replaceAll("\r", "");
            srvVmta.mtaServerId = mtaServ.id;
            srvVmta.mtaServerName = mtaServ.name;
            srvVmta.createdBy = user;
            srvVmta.createdDate = date;
            srvVmta.lastUpdatedBy = user;
            srvVmta.lastUpdatedDate = date;
            newVmta = true;
        } else {
            srvVmta.status = "Activated";
            srvVmta.type = "Default";
            srvVmta.name = name;
            srvVmta.ip = ip.replaceAll("\n", "").replaceAll("\r", "");
            srvVmta.domain = (sub + domain).replaceAll("\n", "").replaceAll("\r", "");
            srvVmta.mtaServerId = mtaServ.id;
            srvVmta.mtaServerName = mtaServ.name;
            srvVmta.lastUpdatedBy = user;
            srvVmta.lastUpdatedDate = date;
        }

        if (newVmta) {
            id = srvVmta.insert();
        } else {
            id = srvVmta.id;
            srvVmta.update();
        }
        return id;
    }
}
