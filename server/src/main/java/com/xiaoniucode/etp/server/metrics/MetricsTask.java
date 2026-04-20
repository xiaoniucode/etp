package com.xiaoniucode.etp.server.metrics;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ProxyMetrics 定时任务
 */
@Component
public class MetricsTask {

    private final MetricsCollector metricsCollector;

    public MetricsTask(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    /**
     * 每小时执行一次，记录小时快照
     * 每小时第5分钟执行，避免整点流量波动
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void hourlySnapshotTask() {
        metricsCollector.takeAllHourlySnapshots();
    }

    /**
     * 每秒执行一次，更新所有代理的实时速率
     */
    @Scheduled(fixedRate = 1000)
    public void rateUpdateTask() {
        metricsCollector.updateAllRates();
    }
}