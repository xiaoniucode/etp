package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.utils.LogUtils;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.common.utils.TomlUtils;
import com.moandjiezana.toml.Toml;
import com.xiaoniucode.etp.common.config.ConfigSource;
import com.xiaoniucode.etp.common.config.ConfigSourceType;

/**
 * TOML配置源实现
 *
 * @author xiaoniucode
 */
public class TomlConfigSource implements ConfigSource {
    private final String path;

    public TomlConfigSource(String path) {
        this.path = path;
    }

    @Override
    public AppConfig load() {
        Toml root = TomlUtils.readToml(path);

        DefaultAppConfig.Builder builder = DefaultAppConfig.builder();

        String serverAddrValue = root.getString("serverAddr");
        Long serverPortValue = root.getLong("serverPort");
        String secretKeyValue = root.getString("secretKey");
        Boolean tlsValue = root.getBoolean("tls");
        Long initialDelaySecValue = root.getLong("initialDelaySec");
        Long maxRetriesValue = root.getLong("maxRetries");
        Long maxDelaySecValue = root.getLong("maxDelaySec");

        if (StringUtils.hasText(serverAddrValue)) {
            builder.serverAddr(serverAddrValue.trim());
        }

        if (serverPortValue != null) {
            validatePort(serverPortValue.intValue());
            builder.serverPort(serverPortValue.intValue());
        }

        if (StringUtils.hasText(secretKeyValue)) {
            builder.secretKey(secretKeyValue.trim());
        } else {
            throw new IllegalArgumentException("必须配置认证密钥secretKey");
        }

        if (tlsValue != null) {
            builder.tls(tlsValue);
        }

        if (initialDelaySecValue != null) {
            builder.initialDelaySec(initialDelaySecValue.intValue());
        }

        if (maxRetriesValue != null) {
            builder.maxRetries(maxRetriesValue.intValue());
        }

        if (maxDelaySecValue != null) {
            builder.maxDelaySec(maxDelaySecValue.intValue());
        }

        LogConfig logConfig = LogUtils.parseLogConfig(root.getTable("log"), false);
        if (logConfig != null) {
            builder.logConfig(logConfig);
        }

        if (tlsValue != null && tlsValue) {
            Toml truststoreTable = root.getTable("truststore");
            if (truststoreTable != null) {
                String truststorePath = truststoreTable.getString("path");
                String password = truststoreTable.getString("storePass");
                if (StringUtils.hasText(truststorePath) && StringUtils.hasText(password)) {
                    DefaultAppConfig.DefaultTruststoreConfig truststoreConfig =
                            new DefaultAppConfig.DefaultTruststoreConfig(truststorePath, password);
                    builder.truststore(truststoreConfig);
                }
            }
        }

        return builder.build();
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
