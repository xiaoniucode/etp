package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.handler.tunnel.ControlTunnelHandler;
import com.xiaoniucode.etp.server.handler.tunnel.HttpVisitorHandler;
import com.xiaoniucode.etp.server.handler.tunnel.TcpVisitorHandler;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import com.xiaoniucode.etp.server.proxy.HttpProxyServer;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.proxy.TunnelServer;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {
    @Resource
    private AppConfig config;

    @Bean
    public EventBus eventBus() {
        return new EventBus(16384);
    }

    @Bean
    public TunnelServer tunnelServer(EventBus eventBus, ControlTunnelHandler controlTunnelHandler) {
        return new TunnelServer(config, eventBus, controlTunnelHandler);
    }

    @Bean
    public PortManager portManager() {
        return new PortManager(config);
    }

    @Bean
    public DomainManager domainManager() {
        return new DomainManager(config);
    }

    @Bean
    public TcpProxyServer tcpProxyServer(TcpVisitorHandler tcpVisitorHandler, EventBus eventBus) {
        return new TcpProxyServer(tcpVisitorHandler, eventBus);
    }

    @Bean
    public HttpProxyServer httpProxyServer(HttpVisitorHandler httpVisitorHandler,
                                           EventBus eventBus,
                                           TrafficMetricsHandler trafficMetricsHandler) {
        return new HttpProxyServer(config,
                httpVisitorHandler,
                eventBus,
                trafficMetricsHandler);
    }

    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new SessionIdGenerator();
    }
}
