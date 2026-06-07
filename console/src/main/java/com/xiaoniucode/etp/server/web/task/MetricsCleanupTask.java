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

package com.xiaoniucode.etp.server.web.task;

import com.xiaoniucode.etp.server.web.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 流量统计数据清理定时任务
 * <p>
 * 每天凌晨1点执行，删除超过90天的流量统计记录
 */
@Component
public class MetricsCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCleanupTask.class);

    private static final int RETENTION_DAYS = 90;

    @Autowired
    private MetricsService metricsService;

    /**
     * 每天凌晨1点执行，删除超过指定期限的流量统计历史记录
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupOldMetrics() {
        logger.info("开始执行流量统计数据清理任务，保留天数：{}天", RETENTION_DAYS);
        try {
            metricsService.deleteOldMetrics(RETENTION_DAYS);
            logger.info("流量统计数据清理任务执行完成");
        } catch (Exception e) {
            logger.error("流量统计数据清理任务执行失败", e);
        }
    }
}
