package io.github.lxien.orbien.server.transport.traffic;

import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;

import java.time.Duration;

/**
 * 基于 Bucket4j 的代理级带宽令牌桶
 */
public class BandwidthLimiter {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(BandwidthLimiter.class);

    private static final int REFILL_TICKS_PER_SECOND = 20;
    private static final long MAX_SCHEDULE_WAIT_MS = 1000L / REFILL_TICKS_PER_SECOND;
    private static final long MIN_BURST_BYTES = 16 * 1024L;

    private final Bucket bucket;
    @Getter
    private final long bytesPerSecond;

    public BandwidthLimiter(Long bandwidthBps) {
        this.bucket = buildBucket(bandwidthBps);
        this.bytesPerSecond = toBytesPerSecond(bandwidthBps);
    }

    private static long toBytesPerSecond(Long bps) {
        if (bps == null || bps <= 0) {
            return 0;
        }
        return Math.max(1, bps / 8);
    }

    private static Bucket buildBucket(Long bps) {
        if (bps == null || bps <= 0) {
            return null;
        }
        long bytesPerSecond = toBytesPerSecond(bps);
        long capacity = Math.max(MIN_BURST_BYTES, bytesPerSecond);
        long refillChunk = Math.max(1, bytesPerSecond / REFILL_TICKS_PER_SECOND);
        Duration period = Duration.ofMillis(1000 / REFILL_TICKS_PER_SECOND);
        return Bucket.builder()
                .addLimit(BandwidthBuilder.builder()
                        .capacity(capacity)
                        .refillGreedy(refillChunk, period)
                        .initialTokens(capacity)
                        .build())
                .build();
    }

    public boolean isEnabled() {
        return bucket != null;
    }

    public int consumeUpTo(TrafficDirection direction, int bytes) {
        if (bytes <= 0) {
            return 0;
        }
        if (bucket == null) {
            return bytes;
        }
        long consumed = bucket.tryConsumeAsMuchAsPossible(bytes);
        if (logger.isDebugEnabled() && consumed < bytes) {
            logger.debug("[限流] 方向={} 请求={} 通过={} 缺口={}",
                    direction, bytes, consumed, bytes - consumed);
        }
        return (int) consumed;
    }

    public long nanosToWait(TrafficDirection direction) {
        if (bucket == null) {
            return 0;
        }
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
    }

    public long scheduleWaitMs(TrafficDirection direction) {
        long nanos = nanosToWait(direction);
        if (nanos <= 0) {
            return 1;
        }
        long waitMs = Math.max(1, nanos / 1_000_000L);
        return Math.min(waitMs, MAX_SCHEDULE_WAIT_MS);
    }
}
