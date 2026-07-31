package io.github.lxien.orbien.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProxyProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 公网远程端口
     */
    private Integer remotePort;

    /**
     * 本地目标 IP
     */
    private String localIp = "127.0.0.1";

    /**
     * 代理协议
     */
    private ProxyProtocol protocol = ProxyProtocol.HTTP;

    /**
     * HTTPS 是否强制跳转
     */
    private Boolean forceHttps;

    /**
     * 自定义域名列表
     */
    private List<String> customDomains = new ArrayList<>();

    /**
     * 是否自动分配域名
     */
    private Boolean autoDomain = true;

    /**
     * 子域名列表
     */
    private List<String> subDomains = new ArrayList<>();

    @NestedConfigurationProperty
    private AccessControlProperties accessControl = new AccessControlProperties();

    @NestedConfigurationProperty
    private TimeAccessProperties timeAccess = new TimeAccessProperties();

    @NestedConfigurationProperty
    private BasicAuthProperties basicAuth = new BasicAuthProperties();

    /**
     * 带宽限制
     */
    private String bandwidth;

    @NestedConfigurationProperty
    private TransportCustomProperties transport = new TransportCustomProperties();
}
