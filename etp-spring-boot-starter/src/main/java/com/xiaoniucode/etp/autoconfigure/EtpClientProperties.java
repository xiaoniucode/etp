package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.core.enums.ProtocolType;
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
     * 是否启用ETP代理
     */
    private boolean enabled = false;

    /**
     * 代理服务地址
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 代理服务端口
     */
    private Integer serverPort = 9527;
    /**
     * 公网端口
     */
    private Integer remotePort;
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
    private Boolean autoStart = true;

    /**
     * 自定义域名列表
     */
    private List<String> customDomains = new ArrayList<>();

    /**
     * 是否自动生成域名，默认自动生成子域名
     */
    private Boolean autoDomain = true;

    /**
     * 子域名列表
     */
    private List<String> subDomain = new ArrayList<>();

    /**
     * 是否启用 TLS 加密
     */
    private Boolean tls = false;

    /**
     * 初始化重连延迟时间 单位：秒
     */
    private Integer initialDelaySec = 2;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private Integer maxRetries = 5;
    /**
     * 最大延迟时间,如果超过了最大值则取maxDelaySec为最大延迟时间 单位：秒
     */
    private Integer maxDelaySec = 8;

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

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(Boolean autoStart) {
        this.autoStart = autoStart;
    }

    public List<String> getCustomDomains() {
        return customDomains;
    }

    public void setCustomDomains(List<String> customDomains) {
        this.customDomains = customDomains;
    }

    public Boolean getAutoDomain() {
        return autoDomain;
    }

    public void setAutoDomain(Boolean autoDomain) {
        this.autoDomain = autoDomain;
    }

    public List<String> getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(List<String> subDomain) {
        this.subDomain = subDomain;
    }

    public Boolean getTls() {
        return tls;
    }

    public void setTls(Boolean tls) {
        this.tls = tls;
    }

    public Integer getInitialDelaySec() {
        return initialDelaySec;
    }

    public void setInitialDelaySec(Integer initialDelaySec) {
        this.initialDelaySec = initialDelaySec;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getMaxDelaySec() {
        return maxDelaySec;
    }

    public void setMaxDelaySec(Integer maxDelaySec) {
        this.maxDelaySec = maxDelaySec;
    }

    public Truststore getTruststore() {
        return truststore;
    }

    public void setTruststore(Truststore truststore) {
        this.truststore = truststore;
    }
}
