package com.xiaoniucode.etp.server.web.controller.metrics;

import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.service.StatsService;
import org.springframework.web.bind.annotation.*;

/**
 * 数据统计接口控制器
 *
 * @author liuxin
 */
@RestController
public class MetricsController {

    private final StatsService statsService;

    public MetricsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/metrics")
    public Ajax getAllMetrics() {
        return Ajax.success(MetricsCollector.getAllMetrics());
    }

    @GetMapping("/metrics/summary")
    public Ajax getMetricsSummary() {
        return Ajax.success(MetricsCollector.count());
    }

//    @GetMapping("/monitor")
//    public Ajax getMonitorInfo() {
//        return Ajax.success(statsService.monitorInfo());
//    }
//
//    @GetMapping("/monitor/server")
//    public Ajax getServerInfo() {
//        return Ajax.success(statsService.getServerInfo());
//    }
}
