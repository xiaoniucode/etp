package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.PortPolicyConfig;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.registry.ConfigChangeDetector;
import com.xiaoniucode.etp.server.store.DomainStore;
import com.xiaoniucode.etp.server.vhost.DefaultDomainManager;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import com.xiaoniucode.etp.server.registry.DefaultProxyManager;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.registry.ConfigRegistrarFactory;
import com.xiaoniucode.etp.server.store.ProxyStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ManagerConfiguration {
    @Resource
    private AppConfig config;

    @Bean
    public ProxyManager proxyManager(MetricsCollector metricsCollector,ConfigRegistrarFactory configRegistrarFactory, DomainStore domainStore, ConfigChangeDetector configChangeDetector, ProxyStore proxyStore) {
        return new DefaultProxyManager(metricsCollector,proxyStore, domainStore, configChangeDetector, configRegistrarFactory);
    }

    @Bean
    public PortAcceptor portAcceptor() {
        return new PortAcceptor();
    }

    @Bean
    public PortManager portManager() {
        PortPolicyConfig portPolicy = config.getPortPolicy();
        return new PortManager(portPolicy.getStart(), portPolicy.getEnd());
    }

    @Bean
    public DomainManager domainManager(DomainGenerator domainGenerator, DomainStore domainStore) {
        return new DefaultDomainManager(domainGenerator, domainStore);
    }

    @Bean
    public DomainGenerator domainGenerator() {
        return new DomainGenerator(config.getBaseDomain());
    }
}
