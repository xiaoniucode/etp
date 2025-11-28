package cn.xilio.etp.server.metrics;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 按公网代理端口（remotePort）维度，采集流量统计指标（读写字节、消息、连接数等）
 *
 * @author liuxin
 */
public class MetricsCollector {
    /**
     * 公网代理服务端口，唯一标识
     */
    private final Integer remotePort;
    /**
     * 当前活跃连接数量
     */
    private final AtomicInteger channels = new AtomicInteger();
    /**
     * 收集器
     */
    private static final Map<Integer, MetricsCollector> COLLECTORS = new ConcurrentHashMap<>(16);
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

    private MetricsCollector(Integer remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * 高性能懒加载（线程安全、无锁竞争）
     *
     * @param remotePort 公网端口
     * @return 当前端口对应的指标收集器
     */
    public static MetricsCollector getCollector(Integer remotePort) {
        return COLLECTORS.computeIfAbsent(remotePort, MetricsCollector::new);
    }

    /**
     * 获取当前端口对应的指标
     *
     * @return 指标数据
     */
    public Metrics getMetrics() {
        Metrics metrics = new Metrics();
        metrics.setRemotePort(remotePort);
        metrics.setChannels(channels.get());
        metrics.setReadBytes(readBytes.sum());
        metrics.setWriteBytes(writeBytes.sum());
        metrics.setReadMsgs(readMsgs.sum());
        metrics.setWriteMsgs(writeMsgs.sum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);
        metrics.setTime(time);
        return metrics;
    }

    /**
     * 获取所有端口服务的指标
     *
     * @return 指标数据
     */
    public static List<Metrics> getAllMetrics() {
        List<Metrics> res = new ArrayList<>(COLLECTORS.size());
        for (MetricsCollector c : COLLECTORS.values()) {
            res.add(c.getMetrics());
        }
        return res;
    }

    /**
     * 获取指标模型并重置当前端口服务的指标统计
     *
     * @return 指标模型
     */
    public Metrics getAndResetMetrics() {
        Metrics m = new Metrics();
        m.setRemotePort(remotePort);
        m.setChannels(channels.get());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);
        m.setTime(time);
        m.setReadBytes(readBytes.sumThenReset());
        m.setWriteBytes(writeBytes.sumThenReset());
        m.setReadMsgs(readMsgs.sumThenReset());
        m.setWriteMsgs(writeMsgs.sumThenReset());
        return m;
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

    public Integer getRemotePort() {
        return remotePort;
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
