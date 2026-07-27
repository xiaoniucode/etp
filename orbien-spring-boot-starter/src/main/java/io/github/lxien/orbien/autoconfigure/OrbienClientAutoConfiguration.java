package io.github.lxien.orbien.autoconfigure;

import io.github.lxien.orbien.client.TunnelClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TunnelClient.class)
@ConditionalOnProperty(prefix = "orbien.client", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(OrbienClientProperties.class)
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration",
        "org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration"
})
public class OrbienClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OrbienLocalPortLocator orbienLocalPortLocator(Environment environment) {
        return new OrbienLocalPortLocator(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public OrbienClientLifecycle orbienClientLifecycle(Environment environment,
                                                       OrbienLocalPortLocator portLocator,
                                                       OrbienClientProperties properties,
                                                       ResourceLoader resourceLoader) {
        return new OrbienClientLifecycle(environment, properties, portLocator, resourceLoader);
    }
    @Bean
    @ConditionalOnMissingBean(name = "orbienClientApplicationRunner")
    public ApplicationRunner orbienClientApplicationRunner(OrbienClientLifecycle lifecycle) {
        return args -> lifecycle.start();
    }
}
