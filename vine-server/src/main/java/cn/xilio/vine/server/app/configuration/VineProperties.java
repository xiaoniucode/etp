package cn.xilio.vine.server.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vine")
public class VineProperties {
    private String bindAddr = "localhost";
    private Integer bindPort;
    private String proxyPath;

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public Integer getBindPort() {
        return bindPort;
    }

    public void setBindPort(Integer bindPort) {
        this.bindPort = bindPort;
    }

    public String getProxyPath() {
        return proxyPath;
    }

    public void setProxyPath(String proxyPath) {
        this.proxyPath = proxyPath;
    }
}
