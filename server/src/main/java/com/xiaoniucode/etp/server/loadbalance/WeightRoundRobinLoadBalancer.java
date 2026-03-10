package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WeightRoundRobinLoadBalancer implements LoadBalancer{
    private static final Logger logger = LoggerFactory.getLogger(WeightRoundRobinLoadBalancer.class);
    private int currentIndex = -1;
    private int currentWeight = 0;

    public WeightRoundRobinLoadBalancer() {
        logger.debug("创建加权轮询负载均衡器");
    }

    @Override
    public synchronized Target select(List<Target> targets) {
        if (targets == null || targets.isEmpty()) {
            logger.warn("目标服务器列表为空");
            return null;
        }

        // 计算总权重
        int totalWeight = 0;
        for (Target target : targets) {
            totalWeight += target.getWeight();
        }

        while (true) {
            currentIndex = (currentIndex + 1) % targets.size();
            if (currentIndex == 0) {
                currentWeight = currentWeight - totalWeight;
                if (currentWeight <= 0) {
                    currentWeight = 0;
                }
            }
            if (targets.get(currentIndex).getWeight() > currentWeight) {
                Target selectedTarget = targets.get(currentIndex);
                currentWeight++;
                logger.debug("加权轮询选择目标服务器 {}", selectedTarget);
                return selectedTarget;
            }
        }
    }
}
