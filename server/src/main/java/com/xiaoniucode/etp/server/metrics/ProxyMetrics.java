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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@Getter
public class ProxyMetrics {
    private final String proxyId;
    private final AtomicInteger activeChannels = new AtomicInteger(0);
    private final LongAdder totalReadBytes = new LongAdder();
    private final LongAdder totalWriteBytes = new LongAdder();
    private final LongAdder totalReadMessages = new LongAdder();
    private final LongAdder totalWriteMessages = new LongAdder();

    private volatile LocalDateTime lastActiveTime = LocalDateTime.now();

    private static final int HOURS_24 = 24;
    private final HourlySnapshot[] hourlyRing = new HourlySnapshot[HOURS_24];
    private final AtomicLong hourlyIndex = new AtomicLong(0);

    /**
     * 上一次小时快照的累计值（用于计算小时增量）
     */
    private volatile long lastHourReadBytes = 0;
    private volatile long lastHourWriteBytes = 0;
    private volatile long lastHourReadMessages = 0;
    private volatile long lastHourWriteMessages = 0;
    /**
     * 实时速率计算（最近60秒）
     */
    private static final int RATE_WINDOW_SECONDS = 60;
    private static final int RATE_SNAPSHOTS = 60;
    private final RateSnapshot[] rateRing = new RateSnapshot[RATE_SNAPSHOTS];
    private final AtomicLong rateIndex = new AtomicLong(0);

    private final DoubleAdder readBytesRate = new DoubleAdder();
    private final DoubleAdder writeBytesRate = new DoubleAdder();
    private final DoubleAdder readMessagesRate = new DoubleAdder();
    private final DoubleAdder writeMessagesRate = new DoubleAdder();

    public ProxyMetrics(String proxyId) {
        this.proxyId = proxyId;
    }

    public void incChannels() {
        activeChannels.incrementAndGet();
        lastActiveTime = LocalDateTime.now();
    }

    public void decChannels() {
        activeChannels.decrementAndGet();
        lastActiveTime = LocalDateTime.now();
    }

    public void incReadBytes(long bytes) {
        if (bytes > 0) {
            totalReadBytes.add(bytes);
            lastActiveTime = LocalDateTime.now();
        }
    }

    public void incWriteBytes(long bytes) {
        if (bytes > 0) {
            totalWriteBytes.add(bytes);
            lastActiveTime = LocalDateTime.now();
        }
    }

    public void incReadMessages(long count) {
        if (count > 0) {
            totalReadMessages.add(count);
            lastActiveTime = LocalDateTime.now();
        }
    }

    public void incWriteMessages(long count) {
        if (count > 0) {
            totalWriteMessages.add(count);
            lastActiveTime = LocalDateTime.now();
        }
    }

    public void takeHourlySnapshot() {
        long currReadBytes = totalReadBytes.sum();
        long currWriteBytes = totalWriteBytes.sum();
        long currReadMsg = totalReadMessages.sum();
        long currWriteMsg = totalWriteMessages.sum();

        long deltaBytesRead = currReadBytes - lastHourReadBytes;
        long deltaBytesWrite = currWriteBytes - lastHourWriteBytes;
        long deltaMsgRead = currReadMsg - lastHourReadMessages;
        long deltaMsgWrite = currWriteMsg - lastHourWriteMessages;

        HourlySnapshot snapshot = new HourlySnapshot(
                LocalDateTime.now().truncatedTo(ChronoUnit.HOURS),
                Math.max(0, deltaBytesRead),
                Math.max(0, deltaBytesWrite),
                Math.max(0, deltaMsgRead),
                Math.max(0, deltaMsgWrite)
        );

        long idx = hourlyIndex.getAndIncrement();
        hourlyRing[(int) (idx % HOURS_24)] = snapshot;

        lastHourReadBytes = currReadBytes;
        lastHourWriteBytes = currWriteBytes;
        lastHourReadMessages = currReadMsg;
        lastHourWriteMessages = currWriteMsg;
    }

    /**
     * 更新实时速率
     */
    public void updateRate() {
        long nowReadBytes = totalReadBytes.sum();
        long nowWriteBytes = totalWriteBytes.sum();
        long nowReadMsg = totalReadMessages.sum();
        long nowWriteMsg = totalWriteMessages.sum();

        RateSnapshot snapshot = new RateSnapshot(nowReadBytes, nowWriteBytes, nowReadMsg, nowWriteMsg);

        long idx = rateIndex.getAndIncrement();
        rateRing[(int) (idx % RATE_SNAPSHOTS)] = snapshot;

        readBytesRate.reset();
        writeBytesRate.reset();
        readMessagesRate.reset();
        writeMessagesRate.reset();

        double rBytesRate = computeRate(true, true, RATE_WINDOW_SECONDS);
        double wBytesRate = computeRate(false, true, RATE_WINDOW_SECONDS);
        double rMsgRate = computeRate(true, false, RATE_WINDOW_SECONDS);
        double wMsgRate = computeRate(false, false, RATE_WINDOW_SECONDS);

        readBytesRate.add(rBytesRate);
        writeBytesRate.add(wBytesRate);
        readMessagesRate.add(rMsgRate);
        writeMessagesRate.add(wMsgRate);
    }

    private double computeRate(boolean isRead, boolean isBytes, int windowSeconds) {
        long end = rateIndex.get();
        if (end < 2) return 0.0;

        long windowMs = windowSeconds * 1000L;
        long now = System.currentTimeMillis();

        RateSnapshot latest = getRateSnapshot(end - 1);
        if (latest == null) return 0.0;

        RateSnapshot target = null;
        long start = Math.max(0, end - RATE_SNAPSHOTS);

        for (long i = end - 1; i >= start; i--) {
            RateSnapshot snap = getRateSnapshot(i);
            if (snap == null) continue;

            if (now - snap.getTimestamp() >= windowMs) {
                target = snap;
                break;
            }
        }
        if (target == null) {
            target = getRateSnapshot(start);
            if (target == null) return 0.0;
        }

        long latestVal = getValue(latest, isRead, isBytes);
        long targetVal = getValue(target, isRead, isBytes);

        long delta = latestVal - targetVal;
        long deltaTime = latest.getTimestamp() - target.getTimestamp();

        if (deltaTime <= 0) return 0.0;

        return (double) delta * 1000.0 / deltaTime;
    }

    private long getValue(RateSnapshot snap, boolean isRead, boolean isBytes) {
        if (isBytes) {
            return isRead ? snap.getReadBytes() : snap.getWriteBytes();
        } else {
            return isRead ? snap.getReadMessages() : snap.getWriteMessages();
        }
    }

    private RateSnapshot getRateSnapshot(long seq) {
        return rateRing[(int) (seq % RATE_SNAPSHOTS)];
    }

    public List<HourlyTraffic> getHourlyStatsLast24h() {
        Map<LocalDateTime, HourlyTraffic> map = new HashMap<>();

        long total = hourlyIndex.get();
        long start = Math.max(0, total - HOURS_24);

        for (long i = start; i < total; i++) {
            HourlySnapshot snap = hourlyRing[(int) (i % HOURS_24)];
            if (snap != null) {
                map.put(snap.getHour(), new HourlyTraffic(snap));
            }
        }

        List<HourlyTraffic> result = new ArrayList<>(24);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        for (int i = 23; i >= 0; i--) {
            LocalDateTime time = now.minusHours(i);
            result.add(map.getOrDefault(
                    time,
                    new HourlyTraffic(time, 0L, 0L, 0L, 0L)
            ));
        }

        return result;
    }

    public Metrics toMetrics() {
        Metrics m = new Metrics();
        m.setProxyId(proxyId);
        m.setActiveChannels(activeChannels.get());
        m.setReadBytes(totalReadBytes.sum());
        m.setWriteBytes(totalWriteBytes.sum());
        m.setReadMessages(totalReadMessages.sum());
        m.setWriteMessages(totalWriteMessages.sum());

        m.setReadRate(readBytesRate.doubleValue());
        m.setWriteRate(writeBytesRate.doubleValue());
        m.setLastActiveTime(lastActiveTime);
        return m;
    }
}