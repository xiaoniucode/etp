package cn.xilio.vine.server.app.configuration;

import cn.xilio.vine.server.TunnelServer;
import cn.xilio.vine.server.app.console.ConsoleServer;
import cn.xilio.vine.server.store.ProxyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TunnelServerConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public TunnelServer tunnelServer(VineProperties properties) {
        //初始化代理配置信息
        ProxyManager.init(properties.getProxyPath());
        TunnelServer tunnelServer = new TunnelServer();
        tunnelServer.setHost(properties.getBindAddr());
        tunnelServer.setPort(properties.getBindPort());
        tunnelServer.setSsl(false);
        return tunnelServer;
    }
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vine.console", name = "enable", havingValue = "true")
    public ConsoleServer consoleServer(VineProperties properties) {
        ConsoleServer consoleServer = new ConsoleServer();
        return consoleServer;
    }
}
