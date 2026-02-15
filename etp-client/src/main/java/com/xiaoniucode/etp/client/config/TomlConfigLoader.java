package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.TlsConfig;
import com.xiaoniucode.etp.client.config.domain.LogConfig;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.common.utils.TomlUtils;
import com.moandjiezana.toml.Toml;
import com.xiaoniucode.etp.common.config.ConfigSource;
import com.xiaoniucode.etp.common.config.ConfigSourceType;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

        String serverAddrValue = root.getString("serverAddr");
        Long serverPortValue = root.getLong("serverPort");

        if (StringUtils.hasText(serverAddrValue)) {
            builder.serverAddr(serverAddrValue.trim());
        }

        if (serverPortValue != null) {
            validatePort(serverPortValue.intValue());
            builder.serverPort(serverPortValue.intValue());
        }

        // 读取认证配置
        Toml authTable = root.getTable("auth");
        if (authTable != null) {
            String token = authTable.getString("token");
            if (StringUtils.hasText(token)) {
                AuthConfig authConfig = new AuthConfig();
                authConfig.setToken(token.trim());

                // 读取新增的认证相关字段
                Long initialDelaySecValue = authTable.getLong("initialDelay");
                Long maxDelaySecValue = authTable.getLong("maxDelay");
                Long maxRetriesValue = authTable.getLong("maxRetries");

                if (initialDelaySecValue != null && initialDelaySecValue > 0) {
                    authConfig.setInitialDelay(initialDelaySecValue.intValue());
                }
                if (maxDelaySecValue != null && maxDelaySecValue > 0) {
                    authConfig.setMaxDelay(maxDelaySecValue.intValue());
                }
                if (maxRetriesValue != null && maxRetriesValue >= 0) {
                    authConfig.setMaxRetries(maxRetriesValue.intValue());
                }

                builder.authConfig(authConfig);
            } else {
                throw new IllegalArgumentException("必须配置 token");
            }
        } else {
            throw new IllegalArgumentException("必须配置认证信息");
        }

        // 读取TLS配置
        Toml tlsTable = root.getTable("tls");
        if (tlsTable != null) {

            Boolean enable = tlsTable.getBoolean("enable",false);
            String certFile = tlsTable.getString("cert");
            String keyFile = tlsTable.getString("key");
            String caFile = tlsTable.getString("ca");
            builder.tlsConfig(new TlsConfig(enable,certFile,keyFile,caFile));
        }

        // 读取代理配置
        List<Toml> proxiesTables = root.getTables("proxies");
        if (proxiesTables != null && !proxiesTables.isEmpty()) {
            List<ProxyConfig> proxies = new ArrayList<>();
            for (Toml proxyTable : proxiesTables) {
                ProxyConfig proxyConfig = new ProxyConfig();

                String name = proxyTable.getString("name");
                String protocol = proxyTable.getString("protocol");
                String localIp = proxyTable.getString("localIp");
                Long localPortValue = proxyTable.getLong("localPort");
                Long remotePortValue = proxyTable.getLong("remotePort");
                Boolean autoDomain = proxyTable.getBoolean("autoDomain", true);
                List<String> customDomains = proxyTable.getList("customDomains");
                List<String> subDomains = proxyTable.getList("subDomains");
                Long statusValue = proxyTable.getLong("status", ProxyStatus.OPEN.getCode().longValue());
                Boolean encrypt = proxyTable.getBoolean("encrypt");
                Boolean compress = proxyTable.getBoolean("compress");

                if (StringUtils.hasText(name)) {
                    proxyConfig.setName(name.trim());
                }
                if (StringUtils.hasText(protocol)) {
                    try {
                        proxyConfig.setProtocol(ProtocolType.getByName(protocol.trim()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("无效的协议类型: " + protocol.trim(), e);
                    }
                }
                if (StringUtils.hasText(localIp)) {
                    proxyConfig.setLocalIp(localIp.trim());
                }
                if (localPortValue != null) {
                    validatePort(localPortValue.intValue());
                    proxyConfig.setLocalPort(localPortValue.intValue());
                }
                if (remotePortValue != null) {
                    validatePort(remotePortValue.intValue());
                    proxyConfig.setRemotePort(remotePortValue.intValue());
                }
                if (autoDomain != null) {
                    proxyConfig.setAutoDomain(autoDomain);
                }
                if (customDomains != null && !customDomains.isEmpty()) {
                    for (String domain : customDomains) {
                        if (StringUtils.hasText(domain)) {
                            proxyConfig.getCustomDomains().add(domain.trim());
                        }
                    }
                }
                if (subDomains != null && !subDomains.isEmpty()) {
                    for (String domain : subDomains) {
                        if (StringUtils.hasText(domain)) {
                            proxyConfig.getSubDomains().add(domain.trim());
                        }
                    }
                }
                if (statusValue != null) {
                    try {
                        proxyConfig.setStatus(ProxyStatus.fromStatus(statusValue.intValue()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("无效的代理状态: " + statusValue, e);
                    }
                }
                if (encrypt != null) {
                    proxyConfig.setEncrypt(encrypt);
                }
                if (compress != null) {
                    proxyConfig.setCompress(compress);
                }

                proxies.add(proxyConfig);
            }
            builder.proxies(proxies);
        }

        // 读取日志配置
        Toml logTable = root.getTable("log");
        if (logTable != null) {
            LogConfig logConfig = new LogConfig();
            String level = logTable.getString("level");
            String path = logTable.getString("path");
            String name = logTable.getString("name");
            String archivePattern = logTable.getString("archivePattern");
            String logPattern = logTable.getString("logPattern");
            Long maxHistoryValue = logTable.getLong("maxHistory");
            String totalSizeCap = logTable.getString("totalSizeCap");

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
