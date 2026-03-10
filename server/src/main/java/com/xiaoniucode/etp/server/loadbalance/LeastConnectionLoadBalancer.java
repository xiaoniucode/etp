package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LeastConnectionLoadBalancer implements LoadBalancer{
    private static final Logger logger = LoggerFactory.getLogger(LeastConnectionLoadBalancer.class);
    private final Map<Target, AtomicInteger> connectionCountMap = new ConcurrentHashMap<>();

    public LeastConnectionLoadBalancer() {
        logger.debug("创建最少连接数负载均衡器");
    }

    @Override
    public synchronized Target select(List<Target> targets) {
        if (targets == null || targets.isEmpty()) {
            logger.warn("目标服务器列表为空");
            return null;
        }

        // 初始化连接数映射
        for (Target target : targets) {
            connectionCountMap.computeIfAbsent(target, k -> new AtomicInteger(0));
        }

        // 选择连接数最少的目标
        Target selectedTarget = targets.getFirst();
        int minConnections = connectionCountMap.get(selectedTarget).get();

        for (Target target : targets) {
            int connections = connectionCountMap.get(target).get();
            if (connections < minConnections) {
                minConnections = connections;
                selectedTarget = target;
            }
        }

        logger.debug("最少连接数选择目标服务器 {}，当前连接数: {}",
            selectedTarget, connectionCountMap.get(selectedTarget).get());

        return selectedTarget;
    }

    /**
     * 增加目标服务器的连接数
     */
    public void incrementConnection(Target target) {
        if (target != null) {
            connectionCountMap.computeIfAbsent(target, k -> new AtomicInteger(0)).incrementAndGet();
            logger.debug("增加目标服务器 {} 连接数，当前连接数: {}", 
                target, connectionCountMap.get(target).get());
        }
    }

    /**
     * 减少目标服务器的连接数
     */
    public void decrementConnection(Target target) {
        if (target != null) {
            AtomicInteger count = connectionCountMap.get(target);
            if (count != null) {
                int newCount = count.decrementAndGet();
                // 确保连接数不会小于0
                if (newCount < 0) {
                    count.set(0);
                    newCount = 0;
                }
                logger.debug("减少目标服务器 {} 连接数，当前连接数: {}", target, newCount);
            }
        }
    }

    /**
     * 获取目标服务器的当前连接数
     */
    public int getConnectionCount(Target target) {
        if (target == null) {
            return 0;
        }
        AtomicInteger count = connectionCountMap.get(target);
        return count != null ? count.get() : 0;
    }
}
