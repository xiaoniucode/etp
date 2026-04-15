package com.xiaoniucode.etp.server.metrics;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Getter
public class ProxyMetrics {

    private final String proxyId;

    private final AtomicInteger activeChannels = new AtomicInteger(0);
    private final LongAdder readBytes = new LongAdder();
    private final LongAdder writeBytes = new LongAdder();
    private final LongAdder readMessages = new LongAdder();
    private final LongAdder writeMessages = new LongAdder();

    private volatile MetricsSnapshot lastSnapshot;
    private volatile LocalDateTime lastActiveTime = LocalDateTime.now();

    // ring buffer
    private static final int SIZE = 180;
    private final MetricsSnapshot[] ring = new MetricsSnapshot[SIZE];
    private final AtomicLong index = new AtomicLong(0);

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

    private void updateActiveTime() {
        lastActiveTime = LocalDateTime.now();
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
        if (count > 0) {
            readMessages.add(count);
            updateActiveTime();
        }
    }

    public void incWriteMessages(long count) {
        if (count > 0) {
            writeMessages.add(count);
            updateActiveTime();
        }
    }

    public void takeSnapshot() {
        long rBytes = readBytes.sum();
        long wBytes = writeBytes.sum();
        long rMsg = readMessages.sum();
        long wMsg = writeMessages.sum();

        MetricsSnapshot prev = lastSnapshot;

        if (prev != null
                && prev.getReadBytes() == rBytes
                && prev.getWriteBytes() == wBytes
                && prev.getReadMessages() == rMsg
                && prev.getWriteMessages() == wMsg) {
            return;
        }

        MetricsSnapshot snapshot = new MetricsSnapshot(
                rBytes, wBytes, rMsg, wMsg,
                LocalDateTime.now()
        );

        lastSnapshot = snapshot;

        long i = index.getAndIncrement();
        ring[(int) (i % SIZE)] = snapshot;
    }

    public void clearHistory() {
        Arrays.fill(ring, null);
        index.set(0);
        lastSnapshot = null;
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

    private double getReadBytesRate(int windowSeconds) {
        return computeRate(true, windowSeconds);
    }

    private double getWriteBytesRate(int windowSeconds) {
        return computeRate(false, windowSeconds);
    }

    private double computeRate(boolean read, int windowSeconds) {
        long end = index.get();
        if (end < 2) {
            return 0.0;
        }

        MetricsSnapshot latest = get(end - 1);
        if (latest == null) {
            return 0.0;
        }

        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        MetricsSnapshot target = null;

        long start = Math.max(0, end - SIZE);

        for (long i = end - 1; i >= start; i--) {
            MetricsSnapshot snap = get(i);
            if (snap == null) continue;

            long ts = snap.getTimestamp()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            if (now - ts >= windowMs) {
                target = snap;
                break;
            }
        }

        if (target == null) {
            target = get(start);
            if (target == null) return 0.0;
        }

        long latestBytes = read ? latest.getReadBytes() : latest.getWriteBytes();
        long targetBytes = read ? target.getReadBytes() : target.getWriteBytes();

        long latestTime = toMillis(latest);
        long targetTime = toMillis(target);

        long deltaBytes = latestBytes - targetBytes;
        long deltaTime = latestTime - targetTime;

        if (deltaTime <= 0) {
            return 0.0;
        }

        return deltaBytes * 1000.0 / deltaTime;
    }

    private MetricsSnapshot get(long seq) {
        return ring[(int) (seq % SIZE)];
    }

    private long toMillis(MetricsSnapshot s) {
        return s.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}