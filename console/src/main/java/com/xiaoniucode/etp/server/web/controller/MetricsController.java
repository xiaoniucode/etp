/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.controller;

import com.xiaoniucode.etp.common.message.PageResult;
import com.xiaoniucode.etp.server.metrics.Metrics;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.common.message.Ajax;
import com.xiaoniucode.etp.server.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
//traffic
@RequestMapping("/api/metrics")
public class MetricsController {
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private MetricsService metricsService;

    @GetMapping("/{proxyId}")
    public Ajax get(@PathVariable String proxyId) {
        Metrics proxyMetrics = metricsCollector.getProxyMetrics(proxyId);
        return Ajax.success(proxyMetrics);
    }

    @GetMapping("list")
    public Ajax list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<Metrics> res = metricsCollector.listAllMetrics(page, size);
        return Ajax.success(res);
    }

    @GetMapping("24h")
    public Ajax getAll24hMetrics() {
        return Ajax.success(metricsService.getTotal24hTraffic());
    }

}
