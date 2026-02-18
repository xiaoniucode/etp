package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.RetryConfig;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
    private boolean enable = false;

    /**
     * 代理服务地址
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 代理服务端口
     */
    private Integer serverPort = 9527;

    @NestedConfigurationProperty
    private Auth auth = new Auth();
    /**
     * TLS 加密
     */
    @NestedConfigurationProperty
    private TlsConfig tls = new TlsConfig();
    /**
     * 公网端口
     */
    private Integer remotePort;
    /**
     * 内网IP
     */
    private String localIp = "127.0.0.1";
    /**
     * 协议
     */
    private ProtocolType protocol = ProtocolType.HTTP;
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public TlsConfig getTls() {
        return tls;
    }

    public void setTls(TlsConfig tls) {
        this.tls = tls;
    }
   public AuthConfig getAuthConfig(){
       AuthConfig authConfig = new AuthConfig();
       if (auth != null && StringUtils.hasText(auth.getToken())) {
           authConfig.setToken(auth.getToken());
       }

       if (auth != null && auth.getRetry() != null) {
           RetryConfig retryConfig = new RetryConfig();
           Retry retry = auth.getRetry();
           if (retry.getMaxRetries() != null) {
               retryConfig.setMaxRetries(retry.getMaxRetries());
           }
           if (retry.getMaxDelay() != null) {
               retryConfig.setMaxDelay(retry.getMaxDelay());
           }
           if (retry.getInitialDelay() != null) {
               retryConfig.setInitialDelay(retry.getInitialDelay());
           }

           authConfig.setRetry(retryConfig);
       }
       return authConfig;
   }
    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
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
}
