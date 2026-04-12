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

import com.xiaoniucode.etp.common.message.PageResult;
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
        if (proxyId == null || proxyId.isBlank()) return null;
        return PROXY_METRICS.computeIfAbsent(proxyId, ProxyMetrics::new);
    }

    public void onChannelActive(String proxyId) {
        ProxyMetrics m = getOrCreate(proxyId);
        if (m != null) m.incChannels();
    }

    public void onChannelInactive(String proxyId) {
        ProxyMetrics m = PROXY_METRICS.get(proxyId);
        if (m != null) m.decChannels();
    }

    public void collect(String proxyId, Consumer<ProxyMetrics> action) {
        if (proxyId == null || proxyId.isBlank()) return;
        ProxyMetrics m = PROXY_METRICS.get(proxyId);
        if (m != null) action.accept(m);
    }

    public boolean removeByProxyId(String proxyId) {
        if (proxyId == null || proxyId.isBlank()) return false;
        ProxyMetrics m = PROXY_METRICS.remove(proxyId);
        if (m != null) {
            m.clearHistory();
            logger.debug("为代理ID {} 移除流量指标统计记录", proxyId);
            return true;
        }
        return false;
    }

    public void removeByProxyIds(Set<String> proxyIds) {
        if (proxyIds != null) proxyIds.forEach(this::removeByProxyId);
    }

    /**
     * 获取单个代理的流量详情
     */
    public Metrics getProxyMetrics(String proxyId) {
        ProxyMetrics pm = PROXY_METRICS.get(proxyId);
        return pm != null ? pm.toMetrics() : null;
    }

    /**
     * 分页获取所有代理的流量列表
     */
    public PageResult<Metrics> listAllMetrics(int page, int size) {
        List<ProxyMetrics> allMetrics = PROXY_METRICS.values().stream()
                .sorted(Comparator.comparingLong(m -> -(m.getReadBytes().sum() + m.getWriteBytes().sum())))
                .toList();

        long total = allMetrics.size();

        int currentPage = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 100));

        List<Metrics> pageData = allMetrics.stream()
                .skip((long) currentPage * pageSize)
                .limit(pageSize)
                .map(ProxyMetrics::toMetrics)
                .toList();

        return new PageResult<>(pageData, total, currentPage, pageSize);
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

    public void cleanupInactive(long inactiveMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(inactiveMinutes);
        int removed = 0;
        var iterator = PROXY_METRICS.entrySet().iterator();
        while (iterator.hasNext()) {
            ProxyMetrics m = iterator.next().getValue();
            if (m.getLastActiveTime().isBefore(threshold) && m.getActiveChannels().get() == 0) {
                m.clearHistory();
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