package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * etp客户端配置属性
 *
 * @author liuxin
 */
@ConfigurationProperties(prefix = "etp.client")
public class EtpClientProperties {
    /**
     * 是否启用 ETP客户端自动启动
     */
    private boolean enabled = false;

    /**
     * 服务端地址，默认 127.0.0.1
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 服务端端口，默认 9527
     */
    private int serverPort = 9527;
    /**
     * 公网端口
     */
    private int remotePort = -1;
    /**
     * 内网IP
     */
    private String localIP = "127.0.0.1";
    /**
     * 协议
     */
    private ProtocolType protocol = ProtocolType.TCP;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * 是否自动启动代理服务
     */
    private boolean autoStart = true;

    /**
     * 自定义域名列表
     */
    private List<String> customDomains = new ArrayList<>();

    /**
     * 是否自动生成域名，默认自动生成子域名
     */
    private boolean autoDomain = true;

    /**
     * 子域名列表
     */
    private List<String> subDomain = new ArrayList<>();

    /**
     * 是否启用 TLS 加密
     */
    private boolean tls = false;

    /**
     * 初始化重连延迟时间 单位：秒
     */
    private int initialDelaySec = 2;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private int maxRetries = 5;
    /**
     * 最大延迟时间,如果超过了最大值则取maxDelaySec为最大延迟时间 单位：秒
     */
    private int maxDelaySec = 8;

    /**
     * TLS 加密配置
     */
    private Truststore truststore = new Truststore();

    public static class Truststore {
        private String path;
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

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
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

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public List<String> getCustomDomains() {
        return customDomains;
    }

    public void setCustomDomains(List<String> customDomains) {
        this.customDomains = customDomains;
    }

    public boolean isAutoDomain() {
        return autoDomain;
    }

    public void setAutoDomain(boolean autoDomain) {
        this.autoDomain = autoDomain;
    }

    public List<String> getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(List<String> subDomain) {
        this.subDomain = subDomain;
    }
}
