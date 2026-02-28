package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.transport.ControlFrameHandler;
import com.xiaoniucode.etp.server.transport.http.BasicAuthHandler;
import com.xiaoniucode.etp.server.transport.http.HttpIpCheckHandler;
import com.xiaoniucode.etp.server.transport.http.HttpVisitorHandler;
import com.xiaoniucode.etp.server.transport.tcp.TcpIpCheckHandler;
import com.xiaoniucode.etp.server.transport.tcp.TcpVisitorHandler;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.manager.DomainGenerator;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.strategy.DomainGenerationStrategy;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import com.xiaoniucode.etp.server.proxy.HttpProxyServer;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.proxy.TunnelServer;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class ServerConfiguration {
    @Resource
    private AppConfig config;

    @Bean
    public EventBus eventBus() {
        return new EventBus(16384);
    }

    @Bean
    public TunnelServer tunnelServer(EventBus eventBus, ControlFrameHandler controlFrameHandler) {
        return new TunnelServer(config, eventBus, controlFrameHandler);
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
        return new TcpProxyServer(tcpVisitorHandler, tcpIpCheckHandler, eventBus);
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

    @Bean(destroyMethod = "stop")
    public HashedWheelTimer hashedWheelTimer() {
        return new HashedWheelTimer(
                new DefaultThreadFactory("wheel-timer"),
                100, // tick 时长
                TimeUnit.MILLISECONDS,// 时间单位
                512,  // 槽位数
                true  // 内存泄漏检测
        );
    }
}
