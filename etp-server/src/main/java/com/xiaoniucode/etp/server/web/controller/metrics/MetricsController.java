package com.xiaoniucode.etp.server.web.controller.metrics;

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

}
