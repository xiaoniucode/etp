package cn.xilio.vine.server.app.configuration;

import cn.xilio.vine.server.TunnelServer;
import cn.xilio.vine.server.store.ProxyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TunnelServerConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public TunnelServer tunnelServer(VineProperties properties) {
        //初始化代理配置信息
        ProxyManager.init(properties.getProxyPath());
        TunnelServer tunnel = new TunnelServer();
        tunnel.setHost(properties.getBindAddr());
        tunnel.setPort(properties.getBindPort());
        tunnel.setSsl(false);
        return tunnel;
    }
}
