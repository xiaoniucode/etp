package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class LeastConnLoadBalancer implements LoadBalancer {
    private final LeastConnectionCounter counter;

    public LeastConnLoadBalancer(LeastConnectionCounter counter) {
        this.counter = Objects.requireNonNull(counter, "counter");
    }

    @Override
    public Target select(String proxyId, List<Target> targets) {
        if (targets == null || targets.isEmpty()) return null;

        Target best = null;
        int bestCount = Integer.MAX_VALUE;
        int bestTies = 0;

        for (Target t : targets) {
            if (t == null || t.getHost() == null || t.getPort() == null) continue;
            int c = counter.get(proxyId + ":" + t.getHost() + ":" + t.getPort());
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