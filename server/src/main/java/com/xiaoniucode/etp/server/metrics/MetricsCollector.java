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
import com.xiaoniucode.etp.common.utils.StringUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
     * 通道激活时调用
     */
    public void onChannelActive(String proxyId) {
        ProxyMetrics m = getOrCreate(proxyId);
        if (m != null) {
            m.incChannels();
        }
    }

    /**
     * 通道关闭时调用
     */
    public void onChannelInactive(String proxyId) {
        if (!StringUtils.hasText(proxyId)) {
            return;
        }
        ProxyMetrics m = PROXY_METRICS.get(proxyId);
        if (m != null) {
            m.decChannels();
        }
    }

    /**
     * 收集流量
     */
    public void collect(String proxyId, Consumer<ProxyMetrics> action) {
        if (proxyId == null || proxyId.isBlank()) {
            return;
        }
        ProxyMetrics m = PROXY_METRICS.get(proxyId);
        if (m != null) {
            action.accept(m);
        }
    }

    /**
     * 删除单个 proxy 的统计信息
     */
    public boolean removeByProxyId(String proxyId) {
        if (proxyId == null || proxyId.isBlank()) {
            return false;
        }
        ProxyMetrics m = PROXY_METRICS.remove(proxyId);
        if (m != null) {
            logger.debug("已移除代理 {} 的流量统计记录", proxyId);
            return true;
        }
        return false;
    }

    /**
     * 批量删除
     */
    public void removeByProxyIds(Set<String> proxyIds) {
        if (proxyIds != null) {
            proxyIds.forEach(this::removeByProxyId);
        }
    }

    /**
     * 获取单个代理的实时统计
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
                .sorted(Comparator.comparingLong(m ->
                        -(m.getTotalReadBytes().sum() + m.getTotalWriteBytes().sum())))
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

    /**
     * 每小时记录所有 proxy 的小时快照
     */
    public void takeAllHourlySnapshots() {
        PROXY_METRICS.values().forEach(ProxyMetrics::takeHourlySnapshot);
    }

    /**
     * 每秒更新所有 proxy 的实时速率（bytes/s）
     */
    public void updateAllRates() {
        PROXY_METRICS.values().forEach(ProxyMetrics::updateRate);
    }

    public List<HourlyTraffic> get24hTraffic(String proxyId) {
        ProxyMetrics pm = PROXY_METRICS.get(proxyId);
        if (pm == null) {
            return Collections.emptyList();
        }
        return pm.getHourlyStatsLast24h();
    }

    /**
     * 获取所有 proxy 汇总后的24小时统计数据
     */
    public List<HourlyTraffic> getTotal24hTraffic() {
        if (PROXY_METRICS.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<HourlyTraffic>> all = new ArrayList<>(PROXY_METRICS.size());
        for (ProxyMetrics pm : PROXY_METRICS.values()) {
            all.add(pm.getHourlyStatsLast24h());
        }

        int size = 24;
        List<HourlyTraffic> result = new ArrayList<>(size);

        for (int i = 0; i < 24; i++) {
            long sumInBytes = 0, sumOutBytes = 0;
            long sumInMsg = 0, sumOutMsg = 0;

            LocalDateTime hourTime = null;

            for (List<HourlyTraffic> data : all) {
                HourlyTraffic item = data.get(i);

                if (hourTime == null) {
                    hourTime = item.getHour();
                }

                sumInBytes += item.getInboundBytes();
                sumOutBytes += item.getOutboundBytes();
                sumInMsg += item.getInboundMessages();
                sumOutMsg += item.getOutboundMessages();
            }

            result.add(new HourlyTraffic(
                    hourTime,
                    sumInBytes,
                    sumOutBytes,
                    sumInMsg,
                    sumOutMsg
            ));
        }

        return result;
    }

    public int getCollectorCount() {
        return PROXY_METRICS.size();
    }

    /**
     * 清空所有统计
     */
    public void clearAll() {
        PROXY_METRICS.clear();
        logger.warn("已清空所有代理流量统计数据");
    }
}