package com.xiaoniucode.etp.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "etp.client")
public class EtpClientProperties {
    /**
     * 是否启用 ETP 代理
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
    private ProxyProperties proxy = new ProxyProperties();

    @NestedConfigurationProperty
    private AuthProperties auth = new AuthProperties();

    /**
     * TLS 加密
     */
    @NestedConfigurationProperty
    private TlsProperties tls = new TlsProperties();

    /**
     * 访问控制
     */
    @NestedConfigurationProperty
    private AccessControlProperties accessControl = new AccessControlProperties();

    /**
     * 基础认证
     */
    @NestedConfigurationProperty
    private BasicAuthProperties basicAuth = new BasicAuthProperties();

    /**
     * 传输配置
     */
    @NestedConfigurationProperty
    private TransportProperties transport = new TransportProperties();

    /**
     * 带宽限制配置
     */
    @NestedConfigurationProperty
    private BandwidthProperties bandwidth = new BandwidthProperties();
}
