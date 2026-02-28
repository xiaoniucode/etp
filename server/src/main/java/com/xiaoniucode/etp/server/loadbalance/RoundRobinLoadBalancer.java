package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer{
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);
    private final AtomicInteger currentIndex = new AtomicInteger(-1);

    public RoundRobinLoadBalancer() {
        logger.debug("创建轮询负载均衡器");
    }

    @Override
    public Target select(List<Target> targets, String proxyId) {
        if (targets == null || targets.isEmpty()) {
            logger.warn("目标服务器列表为空，代理ID: {}", proxyId);
            return null;
        }

        int index = currentIndex.incrementAndGet() % targets.size();
        if (index < 0) {
            index = -index;
        }
        Target selectedTarget = targets.get(index);
        logger.debug("轮询选择目标服务器 {}，代理ID: {}", selectedTarget, proxyId);
        return selectedTarget;
    }
}
