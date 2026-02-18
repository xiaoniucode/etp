package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.metrics.response.MetricsDTO;

import java.util.List;

public interface MetricsService {
    /**
     * 获取所有指标数据
     * @return 指标数据列表
     */
    List<MetricsDTO> getAllMetrics();
}
