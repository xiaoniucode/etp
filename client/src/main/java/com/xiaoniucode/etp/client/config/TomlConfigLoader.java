package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.config.domain.*;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.common.utils.TomlUtils;
import com.moandjiezana.toml.Toml;
import com.xiaoniucode.etp.common.config.ConfigSource;
import com.xiaoniucode.etp.common.config.ConfigSourceType;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.core.enums.LoadBalanceStrategy;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TOML 客户端配置加载器
 *
 * @author xiaoniucode
 */
@Getter
public class TomlConfigLoader implements ConfigSource {
    private final String path;

    public TomlConfigLoader(String path) {
        this.path = path;
    }

    @Override
    public AppConfig load() {
        Toml root = TomlUtils.readToml(path);
        DefaultAppConfig.Builder builder = DefaultAppConfig.builder();

        String serverAddrValue = root.getString("server_addr");
        Long serverPortValue = root.getLong("server_port");

        if (StringUtils.hasText(serverAddrValue)) {
            builder.serverAddr(serverAddrValue.trim());
        }

        if (serverPortValue != null) {
            validatePort(serverPortValue.intValue());
            builder.serverPort(serverPortValue.intValue());
        }
        Toml muxTable = root.getTable("multiplex");
        Boolean globalMuxV = null;
        if (muxTable != null) {
            MultiplexConfig muxConfig = new MultiplexConfig();
            globalMuxV = muxTable.getBoolean("enabled", true);
            muxConfig.setEnabled(globalMuxV);
            builder.muxConfig(muxConfig);
        }

        // 读取连接池配置
        Toml connectionPoolTable = root.getTable("connection_pool");
        if (connectionPoolTable != null) {
            ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();
            connectionPoolConfig.setEnabled(connectionPoolTable.getBoolean("enabled", false));
            
            // 读取多路复用连接池配置
            Toml multiplexPoolTable = connectionPoolTable.getTable("multiplex");
            if (multiplexPoolTable != null) {
                ConnectionPoolConfig.MultiplexPoolConfig multiplexPoolConfig = connectionPoolConfig.getMultiplex();
                multiplexPoolConfig.setPlain(multiplexPoolTable.getBoolean("plain_enabled", false));
                multiplexPoolConfig.setEncrypt(multiplexPoolTable.getBoolean("encrypt_enabled", false));
            }
            
            // 读取独立连接池配置
            Toml directPoolTable = connectionPoolTable.getTable("direct");
            if (directPoolTable != null) {
                ConnectionPoolConfig.DirectPoolConfig directPoolConfig = connectionPoolConfig.getDirect();
                directPoolConfig.setPlainCount(directPoolTable.getLong("plain_count", 0L).intValue());
                directPoolConfig.setEncryptCount(directPoolTable.getLong("encrypt_count", 0L).intValue());
            }
            
            builder.connectionPoolConfig(connectionPoolConfig);
        }

        // 读取认证配置
        Toml authTable = root.getTable("auth");
        if (authTable != null) {
            String token = authTable.getString("token", "");
            AuthConfig authConfig = new AuthConfig();
            authConfig.setToken(token.trim());
            Toml retry = authTable.getTable("retry");
            if (retry != null) {
                Long initialDelaySecValue = retry.getLong("initial_delay", 0L);
                Long maxDelaySecValue = retry.getLong("max_delay", 0L);
                Long maxRetriesValue = retry.getLong("max_retries", 0L);
                RetryConfig retryConfig = new RetryConfig();
                retryConfig.setInitialDelay(initialDelaySecValue.intValue());
                retryConfig.setMaxDelay(maxDelaySecValue.intValue());
                retryConfig.setMaxRetries(maxRetriesValue.intValue());
                authConfig.setRetry(retryConfig);
            }
            builder.authConfig(authConfig);
        }

        // 读取 TLS 配置
        Toml tlsTable = root.getTable("tls");
        if (tlsTable != null) {
            Boolean enabled = tlsTable.getBoolean("enabled", false);
            String certFile = tlsTable.getString("cert_file");
            String keyFile = tlsTable.getString("key_file");
            String caFile = tlsTable.getString("ca_file");
            String keyPass = tlsTable.getString("key_pass");
            boolean testMode = tlsTable.getBoolean("test_mode", false);
            builder.tlsConfig(new TlsConfig(enabled, certFile, keyFile, caFile, keyPass, testMode));
        }

        // 读取代理配置
        List<Toml> proxiesTables = root.getTables("proxies");
        if (proxiesTables != null && !proxiesTables.isEmpty()) {
            List<ProxyConfig> proxies = new ArrayList<>();
            for (Toml proxyTable : proxiesTables) {
                ProxyConfig proxyConfig = new ProxyConfig();

                String name = proxyTable.getString("name");
                String protocol = proxyTable.getString("protocol");
                String localIp = proxyTable.getString("localIp", "127.0.0.1");
                Long localPortValue = proxyTable.getLong("localPort");
                Long remotePortValue = proxyTable.getLong("remote_port");

                Boolean enableV = proxyTable.getBoolean("enabled", true);

                if (StringUtils.hasText(name)) {
                    proxyConfig.setName(name.trim());
                }
                if (StringUtils.hasText(protocol)) {
                    ProtocolType protocolType = ProtocolType.getByName(protocol.trim());
                    if (protocolType == null) {
                        throw new IllegalArgumentException("无效的协议类型: " + protocol);
                    }
                    proxyConfig.setProtocol(protocolType);
                }
                List<Target> targets = proxyTable.getList("targets", new ArrayList<>()).stream()
                        .map(item -> {
                            Map map = (Map) item;
                            String host = (String) map.getOrDefault("host", "127.0.0.1");
                            Long port = (Long) map.getOrDefault("port", -1);
                            Long weight = (Long) map.getOrDefault("weight", 1);
                            Object nameV = map.get("name");
                            return new Target(host, port.intValue(), weight.intValue(), nameV != null ? (String) nameV : null);
                        }).collect(Collectors.toList());
                if (localPortValue != null) {
                    int localPort = localPortValue.intValue();
                    validatePort(localPortValue.intValue());
                    targets.add(new Target(localIp, localPort));
                }
                proxyConfig.addTargets(targets);
                if (remotePortValue != null) {
                    validatePort(remotePortValue.intValue());
                    proxyConfig.setRemotePort(remotePortValue.intValue());
                }
                if (proxyConfig.isHttp()) {
                    Boolean autoDomain = proxyTable.getBoolean("auto_domain", true);
                    List<String> customDomains = proxyTable.getList("custom_domains");
                    List<String> subDomains = proxyTable.getList("sub_domains");
                    DomainConfig domainConfig = new DomainConfig();
                    if (autoDomain != null) {
                        domainConfig.setAutoDomain(autoDomain);
                    }
                    if (customDomains != null && !customDomains.isEmpty()) {
                        for (String domain : customDomains) {
                            if (StringUtils.hasText(domain)) {
                                domainConfig.getCustomDomains().add(domain.trim());
                            }
                        }
                    }
                    if (subDomains != null && !subDomains.isEmpty()) {
                        for (String domain : subDomains) {
                            if (StringUtils.hasText(domain)) {
                                domainConfig.getSubDomains().add(domain.trim());
                            }
                        }
                    }
                    proxyConfig.setDomainInfo(domainConfig);
                }

                if (enableV != null) {
                    proxyConfig.setEnabled(enableV);
                }
                //访问控制
                Toml accessControl = proxyTable.getTable("access_control");
                if (accessControl != null) {
                    Boolean enabled = accessControl.getBoolean("enabled", false);
                    String mode = accessControl.getString("mode");
                    if (!StringUtils.hasText(mode)) {
                        throw new IllegalArgumentException("必须指定访问控制模式");
                    }
                    List<String> allow = accessControl.getList("allow", new ArrayList<>());
                    List<String> deny = accessControl.getList("deny", new ArrayList<>());
                    AccessControlConfig accessControlConfig = new AccessControlConfig(enabled,
                            AccessControlMode.fromValue(mode),
                            new HashSet<>(allow),
                            new HashSet<>(deny));
                    proxyConfig.setAccessControl(accessControlConfig);
                }

                proxies.add(proxyConfig);
                //HTTP BASIC AUTH 只有HTTP协议才解析
                if (ProtocolType.isHttp(protocol)) {
                    Toml basicAuth = proxyTable.getTable("basic_auth");
                    if (basicAuth != null) {
                        Boolean enabled = basicAuth.getBoolean("enabled", false);
                        HashSet<HttpUser> sets = new HashSet<>();
                        List<HashMap> users = basicAuth.getList("users");
                        if (users != null && !users.isEmpty()) {
                            for (HashMap map : users) {
                                String user = (String) map.getOrDefault("user", "");
                                String pass = (String) map.getOrDefault("pass", "");
                                sets.add(new HttpUser(user, pass));
                            }
                        }
                        proxyConfig.setBasicAuth(new BasicAuthConfig(enabled, sets));
                    }
                }
                //带宽限制
                Toml bandwidth = proxyTable.getTable("bandwidth");
                if (bandwidth != null) {
                    String limit = bandwidth.getString("limit");
                    String limitIn = bandwidth.getString("limit_in");
                    String limitOut = bandwidth.getString("limit_out");
                    if (StringUtils.hasText(limit) || StringUtils.hasText(limitIn) || StringUtils.hasText(limitOut)) {
                        BandwidthConfig bandwidthConfig = new BandwidthConfig(limit, limitIn, limitOut);
                        proxyConfig.setBandwidth(bandwidthConfig);
                    }
                }
                //负载均衡配置
                Toml loadBalance = proxyTable.getTable("loadbalance");
                if (loadBalance != null) {
                    LoadBalanceConfig loadBalanceConfig = new LoadBalanceConfig();
                    String strategy = loadBalance.getString("strategy");
                    if (StringUtils.hasText(strategy)) {
                        LoadBalanceStrategy strategyType = LoadBalanceStrategy.fromCode(strategy);
                        loadBalanceConfig.setStrategy(strategyType);
                    }
                    proxyConfig.setLoadBalance(loadBalanceConfig);
                }

                //传输
                Toml transport = proxyTable.getTable("transport");
                TransportConfig transportConfig = new TransportConfig();
                if (transport != null) {
                    Boolean muxV = transport.getBoolean("multiplex");
                    Boolean compressV = transport.getBoolean("compress");
                    Boolean encryptV = transport.getBoolean("encrypt");
                    if (compressV != null) {
                        transportConfig.setCompress(compressV);
                    }
                    if (encryptV != null) {
                        transportConfig.setEncrypt(encryptV);
                    }
                    if (muxV != null) {
                        transportConfig.setMultiplex(muxV);
                    } else if (globalMuxV != null) {
                        transportConfig.setMultiplex(globalMuxV);
                    }
                } else if (globalMuxV != null) {
                    transportConfig.setMultiplex(globalMuxV);
                }
                proxyConfig.setTransport(transportConfig);
            }
            builder.addProxies(proxies);
        }

        // 读取日志配置
        Toml logTable = root.getTable("log");
        if (logTable != null) {
            LogConfig logConfig = new LogConfig();
            String level = logTable.getString("level");
            String path = logTable.getString("path");
            String name = logTable.getString("name");
            String archivePattern = logTable.getString("archive_pattern");
            String logPattern = logTable.getString("log_pattern");
            Long maxHistoryValue = logTable.getLong("max_history");
            String totalSizeCap = logTable.getString("total_size_cap");

            if (StringUtils.hasText(level)) {
                logConfig.setLevel(level.trim());
            }
            if (StringUtils.hasText(path)) {
                logConfig.setPath(path.trim());
            }
            if (StringUtils.hasText(name)) {
                logConfig.setName(name.trim());
            }
            if (StringUtils.hasText(archivePattern)) {
                logConfig.setArchivePattern(archivePattern.trim());
            }
            if (StringUtils.hasText(logPattern)) {
                logConfig.setLogPattern(logPattern.trim());
            }
            if (maxHistoryValue != null && maxHistoryValue > 0) {
                logConfig.setMaxHistory(maxHistoryValue.intValue());
            }
            if (StringUtils.hasText(totalSizeCap)) {
                logConfig.setTotalSizeCap(totalSizeCap.trim());
            }

            builder.logConfig(logConfig);
        }

        return builder.build();
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.TOML;
    }

    private void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口号必须在1-65535范围内: " + port);
        }
    }
}
