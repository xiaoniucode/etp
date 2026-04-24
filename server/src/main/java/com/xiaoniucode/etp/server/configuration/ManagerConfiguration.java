package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.PortPolicyConfig;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.vhost.DefaultDomainManager;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerConfiguration {
    @Resource
    private AppConfig config;

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
