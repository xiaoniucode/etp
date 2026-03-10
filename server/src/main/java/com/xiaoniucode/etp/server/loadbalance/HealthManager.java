package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.Target;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HealthManager {
    /**
     * proxyId:host:port -> 健康状态
     */
    private final Map<String, Boolean> healthMap = new ConcurrentHashMap<>();

    public boolean isHealthy(String proxyId, Target target) {
        String key = proxyId + ":" + target.getHost() + ":" + target.getPort();
        return healthMap.getOrDefault(key, true);
    }

    public void setHealthy(String proxyId, Target target, boolean healthy) {
        String key = proxyId + ":" + target.getHost() + ":" + target.getPort();
        healthMap.put(key, healthy);
    }

    /**
     * 更新健康状态
     */
    public void updateHealthy(String proxyId, Target target, boolean healthy) {
        setHealthy(proxyId, target, healthy);
    }

    /**
     * 批量更新健康状态
     */
    public void batchUpdateHealthy(String proxyId, Map<Target, Boolean> healthStatusMap) {
        if (healthStatusMap != null && !healthStatusMap.isEmpty()) {
            for (Map.Entry<Target, Boolean> entry : healthStatusMap.entrySet()) {
                setHealthy(proxyId, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 获取可用的目标列表
     */
    public List<Target> getAvailableTargets(String proxyId, List<Target> targets) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        return targets.stream()
                .filter(target -> isHealthy(proxyId, target))
                .collect(Collectors.toList());
    }
}