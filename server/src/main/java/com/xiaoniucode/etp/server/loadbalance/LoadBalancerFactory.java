package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.LoadBalanceConfig;
import com.xiaoniucode.etp.core.enums.LoadBalanceStrategy;
import org.springframework.stereotype.Component;

/**
 * 负载均衡器工厂
 */
@Component
public class LoadBalancerFactory {
    /**
     * 获取负载均衡器，如果为空返回一个默认的
     */
    public LoadBalancer getLoadBalancer(LoadBalanceConfig config) {
        LoadBalanceStrategy strategy;
        if (config == null || !config.hasStrategy()) {
            strategy = LoadBalanceConfig.DEFAULT_STRATEGY;
        } else {
            strategy = config.getStrategy();
        }
        return switch (strategy) {
            case RANDOM -> new RandomLoadBalancer();
            case LEAST_CONN -> new LeastConnectionLoadBalancer();
            case WEIGHT -> new WeightRoundRobinLoadBalancer();
            default -> new RoundRobinLoadBalancer();
        };
    }
}