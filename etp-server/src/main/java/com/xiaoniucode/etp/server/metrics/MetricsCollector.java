package com.xiaoniucode.etp.server.metrics;


import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import io.netty.channel.Channel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String domain = visitor.attr(EtpConstants.VISITOR_DOMAIN).get();
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
    public static JSONObject summaryMetrics(){
        JSONObject res = new JSONObject();
        long totalInBytes = 0;
        long totalOutBytes = 0;
        for (MetricsCollector collector : COLLECTORS.values()) {
            totalInBytes += collector.readBytes.sum();
            totalOutBytes += collector.writeBytes.sum();
        }
        res.put("in", totalInBytes);
        res.put("out", totalOutBytes);
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
    public JSONObject getMetrics() {
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("channels", channels.get());
        json.put("readBytes", readBytes.sum());
        json.put("writeBytes", writeBytes.sum());
        json.put("readMsgs", readMsgs.sum());
        json.put("writeMsgs", writeMsgs.sum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        json.put("time", LocalDateTime.now().format(formatter));
        return json;
    }

    public static JSONArray getAllMetrics() {
        JSONArray res = new JSONArray();
        for (MetricsCollector c : COLLECTORS.values()) {
            res.put(c.getMetrics());
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
