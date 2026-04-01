package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.PortPolicyConfig;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import com.xiaoniucode.etp.server.vhost.DomainGenerationStrategy;
import com.xiaoniucode.etp.server.registry.DefaultProxyManager;
import com.xiaoniucode.etp.server.registry.ProxyConfigListener;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.registry.ProxyOperationDelegateFactory;
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
    public ProxyManager proxyManager(ProxyOperationDelegateFactory proxyRegisterDelegateFactory,
                                     List<ProxyConfigListener> allCallbacks,
                                     ProxyStore proxyStore,
                                     UUIDGenerator uuidGenerator) {
        return new DefaultProxyManager(allCallbacks,
                proxyStore,
                uuidGenerator,
                proxyRegisterDelegateFactory);
    }

    @Bean
    public PortAcceptor portListenerManager() {
        return new PortAcceptor();
    }

    @Bean
    public PortManager portManager() {
        PortPolicyConfig portPolicy = config.getPortPolicy();
        return new PortManager(portPolicy.getStart(), portPolicy.getEnd());
    }

    @Bean
    public DomainGenerator domainGenerator(DomainManager domainManager, List<DomainGenerationStrategy> strategies) {
        return new DomainGenerator(config.getBaseDomains(), domainManager, strategies);
    }
}
