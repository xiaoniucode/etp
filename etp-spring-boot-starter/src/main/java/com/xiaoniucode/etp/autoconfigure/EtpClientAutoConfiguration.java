package com.xiaoniucode.etp.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnClass(SmartLifecycle.class)
@ConditionalOnProperty(prefix = "etp.client", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(EtpClientProperties.class)
public class EtpClientAutoConfiguration {
    @Bean
    public SmartLifecycle etpClientLifecycle(Environment environment,
                                             WebServerPortListener webServerPortListener,
                                             EtpClientProperties properties, ResourceLoader resourceLoader) {
        return new EtpClientStartStopLifecycle(environment, webServerPortListener,properties,resourceLoader);
    }

    @Bean
    public WebServerPortListener webServerPortListener() {
        return new WebServerPortListener();
    }
}
