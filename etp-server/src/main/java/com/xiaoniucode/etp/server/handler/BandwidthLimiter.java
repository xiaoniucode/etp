package com.xiaoniucode.etp.server.handler;


import com.xiaoniucode.etp.core.domain.BandwidthConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

/**
 * 带宽限流器
 *
 * @author xiaoniucode
 */
public class BandwidthLimiter {

    /**
     * 上传限流桶 公网 -> 内网 - 对应 limitOut
     */
    private final Bucket uploadBucket;

    /**
     * 下载限流桶内网 -> 公网- 对应 limitIn
     */
    private final Bucket downloadBucket;

    public BandwidthLimiter(BandwidthConfig config) {
        Long uploadLimit = config.getEffectiveOutLimit();
        this.uploadBucket = uploadLimit != null && uploadLimit > 0
                ? Bucket.builder()
                .addLimit(Bandwidth.simple(uploadLimit, Duration.ofSeconds(1)))
                .build()
                : null;

        Long downloadLimit = config.getEffectiveInLimit();
        this.downloadBucket = downloadLimit != null && downloadLimit > 0
                ? Bucket.builder()
                .addLimit(Bandwidth.simple(downloadLimit, Duration.ofSeconds(1)))
                .build()
                : null;
    }

    /**
     * 尝试上传
     *
     * @return true: 允许通过, false: 被限流
     */
    public boolean tryUpload(ByteBuf msg) {
        if (uploadBucket == null) return true;
        return uploadBucket.tryConsume(msg.readableBytes());
    }

    /**
     * 尝试下载
     *
     * @return true: 允许通过, false: 被限流
     */
    public boolean tryDownload(ByteBuf msg) {
        if (downloadBucket == null) return true;
        return downloadBucket.tryConsume(msg.readableBytes());
    }

    /**
     * 获取下载需要等待的时间（纳秒）
     *
     * @param bytes 需要的字节数
     * @return 需要等待的纳秒数，0表示不需要等待
     */
    public long getDownloadWaitNanos(int bytes) {
        if (downloadBucket == null) return 0;
        return downloadBucket.estimateAbilityToConsume(bytes).getNanosToWaitForRefill();
    }

    /**
     * 获取上传需要等待的纳秒数
     *
     * @param bytes 要发送的字节数
     * @return 需要等待的纳秒数，0表示可以立即发送
     */
    public long getUploadWaitNanos(int bytes) {
        if (uploadBucket == null) return 0;
        return uploadBucket.estimateAbilityToConsume(bytes)
                .getNanosToWaitForRefill();
    }
}