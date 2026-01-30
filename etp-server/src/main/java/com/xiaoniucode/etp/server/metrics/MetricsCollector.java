package com.xiaoniucode.etp.server.metrics;


import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.metrics.domain.Metrics;
import com.xiaoniucode.etp.server.metrics.domain.Count;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * 采集流量统计指标（读写字节、消息、连接数等）
 *
 * @author liuxin
 */
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
    private final LongAdder readMsgs = new LongAdder();
    /**
     * 发送消息数
     */
    private final LongAdder writeMsgs = new LongAdder();
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
        String domain = visitor.attr(EtpConstants.VISIT_DOMAIN).get();
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        int remotePort = sa.getPort();
        String key;
        if (StringUtils.hasText(domain)) {
            key = domain;
        } else {
            if (ConfigHelper.get().getHttpProxyPort() == remotePort||ConfigHelper.get().getHttpsProxyPort()==remotePort) {
                return;
            }
            key = remotePort + "";
        }
        MetricsCollector metricsCollector = COLLECTORS.computeIfAbsent(key, MetricsCollector::new);
        callback.accept(metricsCollector);

    }
    public static Count count(){
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
        metrics.setReadMsgs(readMsgs.sum());
        metrics.setWriteMsgs(writeMsgs.sum());
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

    public void incReadMsgs(long msgs) {
        if (msgs > 0) {
            readMsgs.add(msgs);
        }
    }

    public void incWriteMsgs(long msgs) {
        if (msgs > 0) {
            writeMsgs.add(msgs);
        }
    }

    public void incChannels() {
        channels.incrementAndGet();
    }

    public void decChannels() {
        channels.decrementAndGet();
    }

    public String getKey() {
        return key;
    }

    public AtomicInteger getChannels() {
        return channels;
    }

    public LongAdder getReadMsgs() {
        return readMsgs;
    }

    public LongAdder getWriteMsgs() {
        return writeMsgs;
    }

    public LongAdder getReadBytes() {
        return readBytes;
    }

    public LongAdder getWriteBytes() {
        return writeBytes;
    }
}
