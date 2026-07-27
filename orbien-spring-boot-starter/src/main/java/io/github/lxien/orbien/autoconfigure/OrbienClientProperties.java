package io.github.lxien.orbien.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "orbien.client")
public class OrbienClientProperties {

    /**
     * 是否启用Orbien
     */
    private boolean enabled = false;

    /**
     * 服务器地址
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 控制面服务端口
     */
    private Integer serverPort = 9527;

    @NestedConfigurationProperty
    private AuthProperties auth = new AuthProperties();

    @NestedConfigurationProperty
    private ProxyProperties proxy = new ProxyProperties();

    @NestedConfigurationProperty
    private TransportProperties transport = new TransportProperties();

    @NestedConfigurationProperty
    private ConnectionProperties connection = new ConnectionProperties();
}
