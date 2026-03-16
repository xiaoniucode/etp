package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RandomLoadBalancer implements LoadBalancer{
    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalancer.class);

    public RandomLoadBalancer() {
        logger.debug("创建随机负载均衡器");
    }

    @Override
    public Target select(String proxyId,List<Target> targets) {
        if (targets == null || targets.isEmpty()) {
            logger.warn("目标服务器列表为空");
            return null;
        }
        int index = (int) (Math.random() * targets.size());
        Target selectedTarget = targets.get(index);
        logger.debug("随机选择目标服务器 {}", selectedTarget);
        return selectedTarget;
    }
}
