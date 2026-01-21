package com.xiaoniucode.etp.server.config;

import com.xiaoniucode.etp.common.config.ConfigSource;
import com.xiaoniucode.etp.common.config.ConfigSourceType;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.utils.LogUtils;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.common.utils.TomlUtils;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.moandjiezana.toml.Toml;
import com.xiaoniucode.etp.server.config.domain.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xiaoniucode
 */
public class TomlConfigSource implements ConfigSource {
    private final String path;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_BIND_PORT = 9527;
    private static final String DEFAULT_DASHBOARD_HOST = "0.0.0.0";
    private static final int DEFAULT_DASHBOARD_PORT = 8020;

    public TomlConfigSource(String path) {
        this.path = path;
    }

    @Override
    public AppConfig load() {
        Toml root = TomlUtils.readToml(path);
        AppConfig.Builder builder = AppConfig.builder();

        parseRoot(builder, root);
        parseLogConfig(builder, root);
        parseDashboard(builder, root);
        parseTls(builder, root);
        parsePortRange(builder, root);
        parseClients(builder, root);

        return builder.build();
    }

    private void parseRoot(AppConfig.Builder builder, Toml root) {
        String hostValue = root.getString("host", DEFAULT_HOST);
        if (StringUtils.hasText(hostValue)) {
            builder.host(hostValue.trim());
        }

        Long bindPortValue = root.getLong("bindPort", (long) DEFAULT_BIND_PORT);
        if (bindPortValue != null) {
            validatePort(bindPortValue.intValue());
            builder.bindPort(bindPortValue.intValue());
        }
        Long httpProxyPort = root.getLong("httpProxyPort", 80L);
        if (httpProxyPort != null) {
            int httpPort = httpProxyPort.intValue();
            validatePort(httpPort);
            builder.httpProxyPort(httpPort);
        }
        Long httpsProxyPort = root.getLong("httpsProxyPort", 443L);
        if (httpsProxyPort != null) {
            int httpsPort = httpsProxyPort.intValue();
            validatePort(httpsPort);
            builder.httpsProxyPort(httpsPort);
        }
        Boolean tlsValue = root.getBoolean("tls", false);
        if (tlsValue != null) {
            builder.tls(tlsValue);
        }
    }

    private void parseLogConfig(AppConfig.Builder builder, Toml root) {
        LogConfig logConfig = LogUtils.parseLogConfig(root.getTable("log"), true);
        if (logConfig != null) {
            builder.logConfig(logConfig);
        }
    }

    private void parseDashboard(AppConfig.Builder builder, Toml root) {
        Toml dash = root.getTable("dashboard");
        if (dash != null) {
            Boolean enable = dash.getBoolean("enable", false);
            if (enable != null && enable) {
                String addr = dash.getString("addr", DEFAULT_DASHBOARD_HOST);
                Long port = dash.getLong("port", (long) DEFAULT_DASHBOARD_PORT);
                String username = dash.getString("username");
                String password = dash.getString("password");
                Boolean reset = dash.getBoolean("reset", false);

                if (!StringUtils.hasText(username)) {
                    throw new IllegalArgumentException("请配置Dashboard用户名");
                }
                if (!StringUtils.hasText(password)) {
                    throw new IllegalArgumentException("请配置Dashboard密码");
                }

                Dashboard dashboard = new Dashboard(
                        true, username, password, addr, port.intValue(), reset
                );
                builder.dashboard(dashboard);
            }
        }
    }

    private void parseTls(AppConfig.Builder builder, Toml root) {
        Toml keystoreTable = root.getTable("keystore");
        if (keystoreTable != null) {
            String keyPath = keystoreTable.getString("path");
            String keyPass = keystoreTable.getString("keyPass");
            String storePass = keystoreTable.getString("storePass");

            if (StringUtils.hasText(keyPath) && StringUtils.hasText(keyPass) && StringUtils.hasText(storePass)) {
                KeystoreConfig keystoreConfig = new KeystoreConfig(keyPath, keyPass, storePass);
                builder.keystoreConfig(keystoreConfig);
            }
        }
    }

    private void parsePortRange(AppConfig.Builder builder, Toml root) {
        Toml range = root.getTable("port_range");
        if (range != null) {
            Long start = range.getLong("start", 1024L);
            Long end = range.getLong("end", 49151L);
            PortRange portRange = new PortRange(
                    start.intValue(), end.intValue()
            );
            builder.portRange(portRange);
        } else {
            PortRange portRange = new PortRange(1024, 49151);
            builder.portRange(portRange);
        }
    }

    private void parseClients(AppConfig.Builder builder, Toml root) {
        List<Toml> readClients = root.getTables("clients");
        if (readClients == null) {
            return;
        }

        List<ClientInfo> clients = new CopyOnWriteArrayList<>();
        Set<String> tokenTemp = new HashSet<>();
        Set<String> nameTemp = new HashSet<>();

        for (Toml client : readClients) {
            String name = client.getString("name");
            String secretKey = client.getString("secretKey");

            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("必须指定客户端的名称！");
            }

            if (tokenTemp.contains(secretKey)) {
                throw new IllegalArgumentException("客户端[密钥]冲突，不能存在重复的密钥！ " + secretKey);
            }

            if (nameTemp.contains(name)) {
                throw new IllegalArgumentException("客户端[名称]冲突，不能存在重复的名称！ " + name);
            }

            List<ProxyConfig> proxies = parseProxies(client);
            ClientInfo clientInfo = new ClientInfo(name, secretKey, null, proxies);
            clients.add(clientInfo);

            tokenTemp.add(secretKey);
            nameTemp.add(name);
        }

        builder.clients(clients);
    }

    private List<ProxyConfig> parseProxies(Toml client) {
        List<Toml> proxies = client.getTables("proxies");
        if (proxies == null) {
            return new ArrayList<>();
        }

        List<ProxyConfig> proxyConfigs = new CopyOnWriteArrayList<>();
        Set<Integer> portTemp = new HashSet<>();

        for (Toml proxy : proxies) {
            String type = proxy.getString("type");
            String proxyName = proxy.getString("name");
            String localIP = proxy.getString("localIP", "127.0.0.1");
            Long localPort = proxy.getLong("localPort");
            Long remotePort = proxy.getLong("remotePort");
            Long status = proxy.getLong("status", 1L);
            List<String> domains = proxy.getList("domains", new ArrayList<>());

            if (ProtocolType.TCP.name().equalsIgnoreCase(type) && remotePort != null) {
                if (portTemp.contains(remotePort.intValue())) {
                    throw new IllegalArgumentException("公网端口不能重复！" + remotePort.intValue());
                }
                portTemp.add(remotePort.intValue());
            }

            if (localPort == null) {
                throw new IllegalArgumentException("必须指定内网端口");
            }

            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.setType(ProtocolType.getType(type));
            proxyConfig.setName(proxyName);
            proxyConfig.setLocalIP(localIP.trim());
            proxyConfig.setLocalPort(localPort.intValue());
            proxyConfig.setStatus(status.intValue());

            if (ProtocolType.TCP.name().equalsIgnoreCase(type)) {
                proxyConfig.setRemotePort(remotePort == null ? null : remotePort.intValue());
            } else if (ProtocolType.HTTP.name().equalsIgnoreCase(type)) {
                proxyConfig.setDomains(new HashSet<>(domains));
            }
            proxyConfigs.add(proxyConfig);
        }

        return proxyConfigs;
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.TOML;
    }

    public String getPath() {
        return path;
    }

    private void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口号必须在1-65535范围内: " + port);
        }
    }
}
