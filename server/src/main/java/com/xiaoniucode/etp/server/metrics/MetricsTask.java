package com.xiaoniucode.etp.server.metrics;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.event.HourlyTrafficEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Metrics 定时任务
 */
@Component
public class MetricsTask {
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private EventBus eventBus;

    /**
     * 每小时执行一次，记录小时快照
     * 每小时第5分钟执行，避免整点流量波动
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void hourlySnapshotTask() {
        Map<String, ProxyMetrics> allMetrics = metricsCollector.getAllMetrics();
        allMetrics.forEach((proxyId, proxyMetrics) -> {
            HourlyTraffic hourlyTraffic = proxyMetrics.takeHourlySnapshot();
            eventBus.publishAsync(new HourlyTrafficEvent(proxyId, hourlyTraffic));
        });
    }

    /**
     * 每秒执行一次，更新所有代理的实时速率
     */
    @Scheduled(fixedRate = 1000)
    public void rateUpdateTask() {
        Map<String, ProxyMetrics> allMetrics = metricsCollector.getAllMetrics();
        allMetrics.values().forEach(ProxyMetrics::updateRate);
    }
}