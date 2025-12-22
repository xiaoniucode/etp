package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.ProxyRegisterClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * etp客户端自动配置
 *
 * @author liuxin
 */
@Configuration
@ConditionalOnClass(SmartLifecycle.class)
@ConditionalOnProperty(prefix = "etp.client", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(EtpClientProperties.class)
public class EtpClientAutoConfiguration {
    @Bean
    public SmartLifecycle etpClientLifecycle(Environment environment,WebServerPortListener webServerPortListener,ProxyRegisterClient proxyRegisterClient,
        EtpClientProperties properties) {
        return new EtpClientStartStopLifecycle(environment,webServerPortListener,proxyRegisterClient, properties);
    }

    @Bean
    public WebServerPortListener webServerPortListener() {
        return new WebServerPortListener();
    }

    @Bean
    public ProxyRegisterClient proxyRegisterClient() {
        return new ProxyRegisterClient();
    }
}
