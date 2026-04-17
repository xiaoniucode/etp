package com.xiaoniucode.etp.server.configuration;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.store.AgentStore;
import com.xiaoniucode.etp.server.store.DomainStore;
import com.xiaoniucode.etp.server.store.InMemoryAgentStore;
import com.xiaoniucode.etp.server.store.InMemoryDomainStore;
import com.xiaoniucode.etp.server.store.InMemoryProxyStore;
import com.xiaoniucode.etp.server.store.InMemoryTokenStore;
import com.xiaoniucode.etp.server.store.ProxyStore;
import com.xiaoniucode.etp.server.store.TokenStore;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

@Configuration
public class EtpConfiguration {
    @Bean
    public EventBus eventBus() {
        return new EventBus(16384);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    @Bean(destroyMethod = "stop")
    public HashedWheelTimer hashedWheelTimer() {
        return new HashedWheelTimer(
                new DefaultThreadFactory("wheel-timer"),
                100,
                TimeUnit.MILLISECONDS,
                512,
                true
        );
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 19位雪花算法生成，单机模式
     *
     * @return ID
     */
    @Bean
    @ConditionalOnMissingBean(UidGenerator.class)
    public UidGenerator uidGenerator() {
        CachedUidGenerator generator = new CachedUidGenerator();
        generator.setTimeBits(29);
        generator.setWorkerBits(21);
        generator.setSeqBits(13);
        generator.setEpochStr("2026-04-14");
        generator.setWorkerIdAssigner(() -> 1L);
        return generator;
    }

    @Bean
    @ConditionalOnMissingBean(AgentStore.class)
    public AgentStore agentStore() {
        return new InMemoryAgentStore();
    }

    @Bean
    @ConditionalOnMissingBean(TokenStore.class)
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    @ConditionalOnMissingBean(ProxyStore.class)
    public ProxyStore proxyStore() {
        return new InMemoryProxyStore();
    }

    @Bean
    @ConditionalOnMissingBean(DomainStore.class)
    public DomainStore domainStore() {
        return new InMemoryDomainStore();
    }
}
