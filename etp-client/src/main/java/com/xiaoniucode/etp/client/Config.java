package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.common.LogConfig;
import com.xiaoniucode.etp.common.LogUtils;
import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.common.TomlUtils;
import com.moandjiezana.toml.Toml;

/**
 * 客户端配置管理类
 *
 * @author liuxin
 * @since 2025
 */
public final class Config {
    private static final Config instance = new Config();
    private static final String DEFAULT_SERVER_ADDR = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9527;
    private static final boolean DEFAULT_TLS = false;
    /**
     * 代理服务器地址
     */
    private static String serverAddr = DEFAULT_SERVER_ADDR;

    /**
     * ETP服务端端口号
     */
    private static int serverPort = DEFAULT_SERVER_PORT;

    /**
     * 用于与代理服务器通信认证的密钥
     */
    private static String secretKey;

    /**
     * 是否启用TLS
     */
    private static boolean tls = DEFAULT_TLS;

    /**
     * TLS信任库配置
     */
    private static TruststoreConfig truststore;

    private static LogConfig logConfig;

    /**
     * 初始化重连延迟时间 单位：秒
     */
    private static int initialDelaySec;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private static int maxRetries;
    /**
     * 最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间 单位：秒
     */
    private static int maxDelaySec;

    /**
     * SSL信任库配置内部类
     *
     * @author liuxin
     */
    public static final class TruststoreConfig {
        private final String path;
        private final String storePass;

        public TruststoreConfig(String path, String storePass) {
            this.path = path;
            this.storePass = storePass;
        }

        public String getPath() {
            return path;
        }

        public String getStorePass() {
            return storePass;
        }
    }

    /**
     * 初始化配置
     *
     * @param configPath 配置文件路径
     * @throws IllegalStateException 如果配置初始化失败
     */
    public static Config init(String configPath) {
        createConfig(configPath);
        return instance;
    }

    /**
     * 获取配置实例
     *
     * @return 配置实例
     */
    public static Config get() {
        return instance;
    }

    /**
     * 创建配置实例
     */
    private static void createConfig(String configPath) {
        try {
            Toml root = TomlUtils.readToml(configPath);
            String serverAddrValue = root.getString("serverAddr");
            Long serverPortValue = root.getLong("serverPort");
            String secretKeyValue = root.getString("secretKey");
            Boolean tlsValue = root.getBoolean("tls");
            //连接超时
            Long initialDelaySecValue = root.getLong("initialDelaySec");
            Long maxRetriesValue = root.getLong("maxRetries");
            Long maxDelaySecValue = root.getLong("maxDelaySec");
            if (initialDelaySecValue != null) {
                initialDelaySec = initialDelaySecValue.intValue();
            }
            if (maxRetriesValue != null) {
                maxRetries = maxRetriesValue.intValue();
            }
            if (maxDelaySecValue != null) {
                maxDelaySec = maxDelaySecValue.intValue();
            }
            if (StringUtils.hasText(serverAddrValue)) {
                serverAddr = serverAddrValue.trim();
            }
            if (serverPortValue != null) {
                validatePort(serverPortValue.intValue());
                serverPort = serverPortValue.intValue();
            }
            if (StringUtils.hasText(secretKeyValue)) {
                secretKey = secretKeyValue.trim();
            } else {
                throw new IllegalArgumentException("必须配置认证密钥secretKey");
            }
            if (tlsValue != null) {
                tls = tlsValue;
            }
            //解析日志
            Toml log = root.getTable("log");
            logConfig = LogUtils.parseLogConfig(log,false);
            TruststoreConfig truststoreConfig = null;
            if (tls) {
                Toml truststoreTable = root.getTable("truststore");
                if (truststoreTable != null) {
                    String path = truststoreTable.getString("path");
                    String password = truststoreTable.getString("storePass");
                    if (StringUtils.hasText(path) && StringUtils.hasText(password)) {
                        truststoreConfig = new TruststoreConfig(path, password);
                    }
                    if (truststoreConfig != null) {
                        System.setProperty("client.truststore.path", truststoreConfig.getPath());
                        System.setProperty("client.truststore.storePass", truststoreConfig.getStorePass());
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("配置文件加载失败: " + configPath, e);
        }
    }

    /**
     * 验证端口号有效性
     */
    private static int validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口号必须在1-65535范围内: " + port);
        }
        return port;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isTls() {
        return tls;
    }

    public TruststoreConfig getTruststore() {
        return truststore;
    }

    /**
     * 检查SSL配置是否完整
     */
    public boolean isSslConfigured() {
        return tls && truststore != null;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public int getInitialDelaySec() {
        return initialDelaySec;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getMaxDelaySec() {
        return maxDelaySec;
    }

}
