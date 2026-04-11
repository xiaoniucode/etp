/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.metrics;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 流量指标管理
 */
@Component
public class MetricsCollector {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(MetricsCollector.class);
    private static final Map<String, ProxyMetrics> PROXY_METRICS = new ConcurrentHashMap<>(256);

    public ProxyMetrics getOrCreate(String proxyId) {
        if (proxyId == null || proxyId.isBlank()) {
            return null;
        }
        return PROXY_METRICS.computeIfAbsent(proxyId, ProxyMetrics::new);
    }

    /**
     * 连接建立时调用
     */
    public void onChannelActive(String proxyId) {
        ProxyMetrics metrics = getOrCreate(proxyId);
        if (metrics != null) {
            metrics.incChannels();
        }
    }

    /**
     * 连接断开时调用
     */
    public void onChannelInactive(String proxyId) {
        ProxyMetrics metrics = PROXY_METRICS.get(proxyId);
        if (metrics != null) {
            metrics.decChannels();
        }
    }

    /**
     * 实际流量发生时调用
     */
    public void collect(String proxyId, Consumer<ProxyMetrics> action) {
        if (proxyId == null || proxyId.isBlank()) return;
        ProxyMetrics metrics = PROXY_METRICS.get(proxyId);
        if (metrics != null) {
            action.accept(metrics);
        }
    }

    /**
     * 删除 proxy 时必须调用
     */
    public boolean removeByProxyId(String proxyId) {
        if (proxyId == null || proxyId.isBlank()) return false;

        ProxyMetrics metrics = PROXY_METRICS.remove(proxyId);
        if (metrics != null) {
            metrics.clearHistory();
            logger.debug("为代理ID {} 移除流量指标统计记录", proxyId);
            return true;
        }
        return false;
    }

    public void removeByProxyIds(Set<String> proxyIds) {
        if (proxyIds != null) {
            proxyIds.forEach(this::removeByProxyId);
        }
    }

    public List<Metrics> listMetrics(MetricsQuery query) {
        return PROXY_METRICS.values().stream()
                .filter(m -> matchesQuery(m, query))
                .sorted(Comparator.comparingLong(m -> -(m.getReadBytes().sum() + m.getWriteBytes().sum())))
                .skip((long) query.getPage() * query.getSize())
                .limit(query.getSize())
                .map(ProxyMetrics::toMetrics)
                .toList();
    }

    private boolean matchesQuery(ProxyMetrics m, MetricsQuery q) {
        if (q.getProxyId() != null && !m.getProxyId().contains(q.getProxyId())) return false;
        return q.getMinTraffic() == null || (m.getReadBytes().sum() + m.getWriteBytes().sum()) >= q.getMinTraffic();
    }

    public Count totalCount() {
        Count count = new Count();
        long in = 0, out = 0;
        for (ProxyMetrics m : PROXY_METRICS.values()) {
            in += m.getReadBytes().sum();
            out += m.getWriteBytes().sum();
        }
        count.setIn(in);
        count.setOut(out);
        return count;
    }

    public void takeAllSnapshots() {
        PROXY_METRICS.values().forEach(ProxyMetrics::takeSnapshot);
    }

    /**
     * 清理不活跃的代理流量指标
     * <p>
     * 该方法会清理指定分钟数内不活跃且没有活动连接的代理流量指标，
     * 以防止内存泄漏和资源浪费。
     * </p>
     * @param inactiveMinutes 不活跃分钟数阈值，超过该阈值的代理会被清理
     */
    public void cleanupInactive(long inactiveMinutes) {
        // 计算不活跃阈值时间
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(inactiveMinutes);
        int removed = 0;

        // 遍历所有代理流量指标
        var iterator = PROXY_METRICS.entrySet().iterator();
        while (iterator.hasNext()) {
            ProxyMetrics m = iterator.next().getValue();
            // 检查是否满足清理条件：最后活动时间早于阈值且没有活动连接
            if (m.getLastActiveTime().isBefore(threshold) && m.getActiveChannels().get() == 0) {
                // 清除历史数据
                m.clearHistory();
                // 从映射中移除
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            logger.info("清理了 {} 个不活跃的代理流量指标", removed);
        }
    }

    public int getCollectorCount() {
        return PROXY_METRICS.size();
    }
}