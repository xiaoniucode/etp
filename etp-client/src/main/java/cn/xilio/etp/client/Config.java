package cn.xilio.etp.client;

import cn.xilio.etp.common.TomlUtils;
import com.moandjiezana.toml.Toml;

import java.util.Objects;

/**
 * 客户端配置类
 * 使用单例模式管理全局配置
 *
 * @author YourName
 * @since 2024
 */
public final class Config {

    /**
     * 配置单例实例
     */
    private static volatile Config instance;

    /**
     * 代理服务器IP地址
     */
    private final String serverAddr;

    /**
     * 代理服务器隧道端口号
     */
    private final int serverPort;

    /**
     * 用于与代理服务器通信认证的密钥
     */
    private final String secretKey;

    /**
     * 是否启用SSL
     */
    private final boolean ssl;

    /**
     * SSL信任库配置
     */
    private final TruststoreConfig truststore;

    /**
     * SSL信任库配置内部类
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
     * 私有构造方法
     */
    private Config(String serverAddr, int serverPort, String secretKey,
                   boolean ssl, TruststoreConfig truststore) {
        this.serverAddr = Objects.requireNonNull(serverAddr, "服务器地址不能为空");
        this.serverPort = validatePort(serverPort);
        this.secretKey = Objects.requireNonNull(secretKey, "密钥不能为空");
        this.ssl = ssl;
        this.truststore = truststore;
    }

    /**
     * 初始化配置（线程安全）
     *
     * @param configPath 配置文件路径
     * @throws IllegalStateException 如果配置初始化失败
     */
    public static void init(String configPath) {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = createConfig(configPath);
                }
            }
        }
    }

    /**
     * 获取配置实例
     *
     * @return 配置实例
     * @throws IllegalStateException 如果配置未初始化
     */
    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("配置未初始化，请先调用init方法");
        }
        return instance;
    }

    /**
     * 创建配置实例
     */
    private static Config createConfig(String configPath) {
        try {
            Toml toml = TomlUtils.readToml(configPath);

            String serverAddr = Objects.requireNonNull(toml.getString("serverAddr"), "serverAddr配置不能为空");
            Long port = Objects.requireNonNull(toml.getLong("serverPort"), "serverPort配置不能为空");
            int serverPort = port.intValue();
            String secretKey = Objects.requireNonNull(toml.getString("secretKey"), "secretKey配置不能为空");
            Boolean sslEnabled = toml.getBoolean("ssl");
            boolean ssl = sslEnabled != null ? sslEnabled : false;
            TruststoreConfig truststoreConfig = null;
            if (ssl) {
                Toml truststoreTable = toml.getTable("truststore");
                if (truststoreTable != null) {
                    String path = truststoreTable.getString("path");
                    String password = truststoreTable.getString("storePass");
                    if (path != null && password != null) {
                        truststoreConfig = new TruststoreConfig(path, password);
                    }
                    if (truststoreConfig != null) {
                        // 清理可能存在的旧配置
                        System.clearProperty("client.truststore.path");
                        System.clearProperty("client.truststore.storePass");
                        //添加到系统属性中
                        System.setProperty("client.truststore.path", truststoreConfig.getPath());
                        System.setProperty("client.truststore.storePass", truststoreConfig.getStorePass());
                    }

                }
            }

            return new Config(serverAddr, serverPort, secretKey, ssl, truststoreConfig);

        } catch (Exception e) {
            throw new IllegalStateException("配置文件解析失败: " + configPath, e);
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

    // Getter方法
    public String getServerAddr() {
        return serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isSsl() {
        return ssl;
    }

    public TruststoreConfig getTruststore() {
        return truststore;
    }

    /**
     * 检查SSL配置是否完整
     */
    public boolean isSslConfigured() {
        return ssl && truststore != null;
    }
}
