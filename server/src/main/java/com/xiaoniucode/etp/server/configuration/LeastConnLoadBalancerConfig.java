package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.server.loadbalance.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeastConnLoadBalancerConfig {

    @Bean
    public LeastConnectionCounter connectionCounter() {
        return new LeastConnectionCounter();
    }

    @Bean
    public StreamConnLedger streamConnLedger() {
        return new StreamConnLedger();
    }

    @Bean
    public LeastConnLoadBalancer leastConnLoadBalancer(LeastConnectionCounter leastConnectionCounter) {
        return new LeastConnLoadBalancer(leastConnectionCounter);
    }

    @Bean
    public LeastConnHooks leastConnHooks(LeastConnectionCounter counter, StreamConnLedger ledger) {
        return new LeastConnHooks(counter, ledger);
    }
}