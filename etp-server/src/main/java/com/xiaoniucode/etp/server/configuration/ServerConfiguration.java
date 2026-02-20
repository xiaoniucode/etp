package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.handler.http.BasicAuthHandler;
import com.xiaoniucode.etp.server.handler.http.HttpIpCheckHandler;
import com.xiaoniucode.etp.server.handler.message.ControlTunnelHandler;
import com.xiaoniucode.etp.server.handler.http.HttpVisitorHandler;
import com.xiaoniucode.etp.server.handler.tcp.TcpIpCheckHandler;
import com.xiaoniucode.etp.server.handler.tcp.TcpVisitorHandler;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.manager.DomainGenerator;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.strategy.DomainGenerationStrategy;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import com.xiaoniucode.etp.server.proxy.HttpProxyServer;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.proxy.TunnelServer;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    public DomainGenerator domainGenerator(DomainManager domainManager, List<DomainGenerationStrategy> strategies) {
        return new DomainGenerator(config.getBaseDomains(), domainManager, strategies);
    }

    @Bean
    public TcpProxyServer tcpProxyServer(TcpVisitorHandler tcpVisitorHandler, TcpIpCheckHandler tcpIpCheckHandler, EventBus eventBus) {
        return new TcpProxyServer(tcpVisitorHandler,tcpIpCheckHandler, eventBus);
    }

    @Bean
    public HttpProxyServer httpProxyServer(HttpVisitorHandler httpVisitorHandler,
                                           HttpIpCheckHandler httpIpCheckHandler,
                                           BasicAuthHandler basicAuthHandler,
                                           EventBus eventBus,
                                           TrafficMetricsHandler trafficMetricsHandler) {
        return new HttpProxyServer(config,
                httpVisitorHandler,
                httpIpCheckHandler,
                basicAuthHandler,
                eventBus,
                trafficMetricsHandler);
    }
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new SessionIdGenerator();
    }
}
