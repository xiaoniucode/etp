package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.metrics.domain.Metrics;
import com.xiaoniucode.etp.server.web.controller.metrics.convert.MetricsConvert;
import com.xiaoniucode.etp.server.web.controller.metrics.response.MetricsDTO;
import com.xiaoniucode.etp.server.web.service.MetricsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsServiceImpl implements MetricsService {
    @Override
    public List<MetricsDTO> getAllMetrics() {
        List<Metrics> metricsList = MetricsCollector.getAllMetrics();
        return MetricsConvert.INSTANCE.toDTOList(metricsList);
    }
}
