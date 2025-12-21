package com.xiaoniucode.etp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * etp客户端配置属性
 *
 * @author liuxin
 */
@ConfigurationProperties(prefix = "etp.client")
public class EtpClientProperties {
    /**
     * 是否启用 ETP 客户端自动启动
     */
    private boolean enabled = true;

    /**
     * 服务端地址，默认 127.0.0.1
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 服务端端口，默认 9527
     */
    private int serverPort = 9527;
//    /**
//     * 公网端口
//     */
//    private int remotePort;

    /**
     * 密钥，必填
     */
    private String secretKey;

    /**
     * 是否启用 TLS 加密，默认 false
     */
    private boolean tls = false;

    /**
     * 初始化重连延迟时间 单位：秒
     */
    private int initialDelaySec;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private int maxRetries;
    /**
     * 最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间 单位：秒
     */
    private int maxDelaySec;

    /**
     * ==================== TLS 加密配置 ====================
     */
    private Truststore truststore = new Truststore();

    public static class Truststore {
        /**
         * 客户端信任库路径（PKCS12 格式）
         */
        private String path;

        /**
         * 信任库密码
         */
        private String storePass;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getStorePass() {
            return storePass;
        }

        public void setStorePass(String storePass) {
            this.storePass = storePass;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public Truststore getTruststore() {
        return truststore;
    }

    public void setTruststore(Truststore truststore) {
        this.truststore = truststore;
    }

    public int getInitialDelaySec() {
        return initialDelaySec;
    }

    public void setInitialDelaySec(int initialDelaySec) {
        this.initialDelaySec = initialDelaySec;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxDelaySec() {
        return maxDelaySec;
    }

    public void setMaxDelaySec(int maxDelaySec) {
        this.maxDelaySec = maxDelaySec;
    }
}
