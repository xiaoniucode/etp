package cn.xilio.vine.server.app.configuration;

import cn.xilio.vine.server.TunnelServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TunnelServerConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public TunnelServer tunnelServer(VineProperties properties) {
        TunnelServer tunnel = new TunnelServer();
        tunnel.setHost("localhost");
        tunnel.setPort(8523);
        tunnel.setSsl(false);
        return tunnel;
    }
}
