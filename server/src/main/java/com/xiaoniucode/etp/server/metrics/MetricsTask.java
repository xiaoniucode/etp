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

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricsTask {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(MetricsTask.class);

    @Autowired
    private MetricsCollector metricsCollector;

    @Scheduled(fixedRate = 1000)
    public void snapshot() {
        if (logger.isTraceEnabled()) {
            logger.trace("metrics snapshot tick");
        }
        metricsCollector.takeAllSnapshots();
    }

    /**
     * 清理任务，每天凌晨 0 点执行一次
     * 将当天的所有流量统计指标清理掉
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearDailyTrafficMetrics() {
        logger.debug("执行流量指标清理任务");
        metricsCollector.cleanupInactive();
        logger.debug("今日流量指标清理任务执行完成");
    }
}
