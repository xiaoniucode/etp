package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 最少连接数负载均衡器
 * 根据目标服务器的当前连接数选择连接数最少的服务器
 */
public final class LeastConnLoadBalancer implements LoadBalancer {
    /**
     * 连接数计数器，用于统计每个目标服务器的当前连接数
     */
    private final LeastConnectionCounter counter;

    /**
     * 构造方法
     * @param counter 连接数计数器
     */
    public LeastConnLoadBalancer(LeastConnectionCounter counter) {
        this.counter = Objects.requireNonNull(counter, "counter");
    }

    /**
     * 选择目标服务器
     * @param proxyId 代理ID
     * @param targets 目标服务器列表
     * @return 选中的目标服务器
     */
    @Override
    public Target select(String proxyId, List<Target> targets) {
        if (targets == null || targets.isEmpty()) return null;

        Target best = null;
        int bestCount = Integer.MAX_VALUE;
        int bestTies = 0;

        for (Target t : targets) {
            if (t == null || t.getHost() == null || t.getPort() == null) continue;
            // 生成目标服务器的唯一标识
            String key = proxyId + ":" + t.getHost() + ":" + t.getPort();
            // 获取当前连接数
            int c = counter.get(key);
            // 如果当前连接数小于最佳连接数，更新最佳服务器
            if (c < bestCount) {
                best = t;
                bestCount = c;
                bestTies = 1;
            } else if (c == bestCount) {
                // 平局：做个轻量随机打散，避免一直选列表第一个
                bestTies++;
                if (ThreadLocalRandom.current().nextInt(bestTies) == 0) {
                    best = t;
                }
            }
        }
        return best;
    }
}