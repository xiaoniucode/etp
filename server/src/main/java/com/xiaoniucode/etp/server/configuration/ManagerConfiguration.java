package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.PortPolicyConfig;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
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
}
