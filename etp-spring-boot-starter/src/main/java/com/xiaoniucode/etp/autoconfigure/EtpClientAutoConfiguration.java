package com.xiaoniucode.etp.autoconfigure;

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
@ConditionalOnProperty(prefix = "etp.client", name = "enable", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(EtpClientProperties.class)
public class EtpClientAutoConfiguration {
    @Bean
    public SmartLifecycle etpClientLifecycle(Environment environment,
                                             WebServerPortListener webServerPortListener,
                                             EtpClientProperties properties) {
        return new EtpClientStartStopLifecycle(environment, webServerPortListener,properties);
    }

    @Bean
    public WebServerPortListener webServerPortListener() {
        return new WebServerPortListener();
    }
}
