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

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 内网穿透 - 单个 Proxy 指标采集器
 */
@Getter
public class ProxyMetrics {

    private final String proxyId;

    private final AtomicInteger activeChannels = new AtomicInteger(0);
    private final LongAdder readBytes = new LongAdder();
    private final LongAdder writeBytes = new LongAdder();
    private final LongAdder readMessages = new LongAdder();
    private final LongAdder writeMessages = new LongAdder();

    private volatile LocalDateTime lastActiveTime = LocalDateTime.now();

    private final Deque<MetricsSnapshot> history = new ArrayDeque<>(180);
    private static final int MAX_HISTORY = 180;

    public ProxyMetrics(String proxyId) {
        this.proxyId = proxyId;
    }

    public void incChannels() {
        activeChannels.incrementAndGet();
        updateActiveTime();
    }

    public void decChannels() {
        activeChannels.decrementAndGet();
        updateActiveTime();
    }

    public void incReadBytes(long bytes) {
        if (bytes > 0) {
            readBytes.add(bytes);
            updateActiveTime();
        }
    }

    public void incWriteBytes(long bytes) {
        if (bytes > 0) {
            writeBytes.add(bytes);
            updateActiveTime();
        }
    }

    public void incReadMessages(long count) {
        if (count > 0) readMessages.add(count);
    }

    public void incWriteMessages(long count) {
        if (count > 0) writeMessages.add(count);
    }

    private void updateActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    // ==================== 快照 & 速率 ====================
    public void takeSnapshot() {
        MetricsSnapshot snapshot = new MetricsSnapshot(
                readBytes.sum(), writeBytes.sum(),
                readMessages.sum(), writeMessages.sum(),
                LocalDateTime.now()
        );
        synchronized (history) {
            history.addLast(snapshot);
            if (history.size() > MAX_HISTORY) {
                history.removeFirst();
            }
        }
    }

    public double getReadBytesRate(int intervalSeconds) {
        return history.size() < 2 ? 0.0 : readBytes.sum() * 1.0 / Math.max(intervalSeconds, 1);
    }

    public double getWriteBytesRate(int intervalSeconds) {
        return history.size() < 2 ? 0.0 : writeBytes.sum() * 1.0 / Math.max(intervalSeconds, 1);
    }

    public Metrics toMetrics() {
        Metrics m = new Metrics();
        m.setProxyId(proxyId);
        m.setActiveChannels(activeChannels.get());
        m.setReadBytes(readBytes.sum());
        m.setWriteBytes(writeBytes.sum());
        m.setReadMessages(readMessages.sum());
        m.setWriteMessages(writeMessages.sum());
        m.setReadRate(getReadBytesRate(60));
        m.setWriteRate(getWriteBytesRate(60));
        m.setLastActiveTime(lastActiveTime);
        return m;
    }

    /**
     * proxy 被删除时彻底清理历史数据，防止内存泄漏
     */
    public void clearHistory() {
        synchronized (history) {
            history.clear();
        }
    }
}