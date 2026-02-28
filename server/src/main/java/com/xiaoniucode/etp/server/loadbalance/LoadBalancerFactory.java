package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.LoadBalanceStrategy;
import org.springframework.stereotype.Component;

/**
 * 负载均衡器工厂
 */
@Component
public class LoadBalancerFactory {
    /**
     * 获取负载均衡器
     */
    public LoadBalancer getLoadBalancer(ProxyConfig config) {
        LoadBalanceStrategy strategy;
        if (config.hasLoadBalance()) {
            strategy = config.getLoadBalance().getStrategy();
        } else {
            strategy = LoadBalanceStrategy.ROUND_ROBIN;
        }
        return switch (strategy) {
            case RANDOM -> new RandomLoadBalancer();
            case LEAST_CONN -> new LeastConnectionLoadBalancer();
            case WEIGHT -> new WeightRoundRobinLoadBalancer();
            default -> new RoundRobinLoadBalancer();
        };
    }
}