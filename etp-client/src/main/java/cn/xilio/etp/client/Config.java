package cn.xilio.etp.client;

import cn.xilio.etp.common.TomlUtils;
import com.moandjiezana.toml.Toml;

import java.util.Objects;

public class Config {
    /**
     * 代理服务器IP地址
     */
    private static String serverAddr;
    /**
     * 代理服务器隧道端口号
     */
    private static Integer serverPort;
    /**
     * 用于与代理服务器通信认证的密钥
     */
    private static String secretKey;

    private Config() {
        // 私有构造方法防止外部实例化
    }

    /**
     * 初始化配置
     *
     * @param configPath 配置文件路径
     */
    public static void init(String configPath) {
        Toml toml = TomlUtils.readToml(configPath);
        serverAddr = Objects.requireNonNull(toml.getString("serverAddr"));
        serverPort = Objects.requireNonNull(toml.getLong("serverPort")).intValue();
        secretKey = Objects.requireNonNull(toml.getString("secretKey"));
    }

    public static String getServerAddr() {
        return serverAddr;
    }

    public static Integer getServerPort() {
        return serverPort;
    }

    public static String getSecretKey() {
        return secretKey;
    }
}
