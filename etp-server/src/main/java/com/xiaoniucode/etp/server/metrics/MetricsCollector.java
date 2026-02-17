package com.xiaoniucode.etp.server.metrics;


import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.metrics.domain.Metrics;
import com.xiaoniucode.etp.server.metrics.domain.Count;
import io.netty.channel.Channel;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * 采集流量统计指标（读写字节、消息、连接数等）
 *
 * @author liuxin
 */
@Getter
public class MetricsCollector {
    /**
     * 唯一标识
     */
    private final String key;
    /**
     * 当前活跃连接数量
     */
    private final AtomicInteger channels = new AtomicInteger();
    /**
     * 收集器
     */
    private static final Map<String, MetricsCollector> COLLECTORS = new ConcurrentHashMap<>(16);
    /**
     * 接收消息数
     */
    private final LongAdder readMessages = new LongAdder();
    /**
     * 发送消息数
     */
    private final LongAdder writeMessages = new LongAdder();
    /**
     * 接收总字节数
     */
    private final LongAdder readBytes = new LongAdder();
    /**
     * 发送总字节数
     */
    private final LongAdder writeBytes = new LongAdder();

    private MetricsCollector(String key) {
        this.key = key;
    }


    public static void doCollector(Channel visitor, Consumer<MetricsCollector> callback) {
        String domain = visitor.attr(ChannelConstants.VISIT_DOMAIN).get();
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        int remotePort = sa.getPort();
        String key;
        if (StringUtils.hasText(domain)) {
            key = domain;
        } else {
            AppConfig config = BeanHelper.getBean(AppConfig.class);
            if (config.getHttpProxyPort() == remotePort || config.getHttpsProxyPort() == remotePort) {
                return;
            }
            key = remotePort + "";
        }
        MetricsCollector metricsCollector = COLLECTORS.computeIfAbsent(key, MetricsCollector::new);
        callback.accept(metricsCollector);

    }

    public static Count count() {
        Count res = new Count();
        long totalInBytes = 0;
        long totalOutBytes = 0;
        for (MetricsCollector collector : COLLECTORS.values()) {
            totalInBytes += collector.readBytes.sum();
            totalOutBytes += collector.writeBytes.sum();
        }
        res.setIn(totalInBytes);
        res.setOut(totalOutBytes);
        return res;
    }

    public static void removeCollectors(Set<String> keys) {
        keys.forEach(MetricsCollector::removeCollector);

    }

    public static void removeCollector(String key) {
        COLLECTORS.remove(key);
    }

    /**
     * 获取指标数据
     *
     * @return 指标数据
     */
    public Metrics getMetrics() {
        Metrics metrics = new Metrics();
        metrics.setKey(key);
        metrics.setChannels(channels.get());
        metrics.setReadBytes(readBytes.sum());
        metrics.setWriteBytes(writeBytes.sum());
        metrics.setReadMessages(readMessages.sum());
        metrics.setWriteMessages(writeMessages.sum());
        metrics.setTime(LocalDateTime.now());
        return metrics;
    }

    public static List<Metrics> getAllMetrics() {
        List<Metrics> res = new ArrayList<>();
        for (MetricsCollector c : COLLECTORS.values()) {
            res.add(c.getMetrics());
        }
        return res;
    }

    public void incReadBytes(long bytes) {
        if (bytes > 0) {
            readBytes.add(bytes);
        }
    }

    public void incWriteBytes(long bytes) {
        if (bytes > 0) {
            writeBytes.add(bytes);
        }
    }

    public void incReadMessages(long messages) {
        if (messages > 0) {
            readMessages.add(messages);
        }
    }

    public void incWriteMessages(long messages) {
        if (messages > 0) {
            writeMessages.add(messages);
        }
    }

    public void incChannels() {
        channels.incrementAndGet();
    }

    public void decChannels() {
        channels.decrementAndGet();
    }

}
