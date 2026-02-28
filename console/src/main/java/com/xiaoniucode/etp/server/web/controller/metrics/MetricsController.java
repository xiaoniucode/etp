package com.xiaoniucode.etp.server.web.controller.metrics;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.metrics.response.MetricsDTO;
import com.xiaoniucode.etp.server.web.service.MetricsService;
import com.xiaoniucode.etp.server.web.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据统计接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * 获取所有指标数据
     *
     * @return 指标数据列表
     */
    @GetMapping()
    public Ajax getAllMetrics() {
        return Ajax.success(metricsService.getAllMetrics());
    }

}
