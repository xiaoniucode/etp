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

package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.metrics.HourlyTraffic;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.dto.metrics.Metrics24LineDTO;
import com.xiaoniucode.etp.server.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetricsServiceImpl implements MetricsService {
    @Autowired
    private MetricsCollector metricsCollector;

    @Override
    public Metrics24LineDTO getTotal24hTraffic() {
        List<HourlyTraffic> list = metricsCollector.getTotal24hTraffic();

        Metrics24LineDTO dto = new Metrics24LineDTO();

        List<String> xAxis = new ArrayList<>(24);
        List<Long> yAxis = new ArrayList<>(24);

        for (HourlyTraffic ht : list) {
            int hour = ht.getHour().getHour();
            String hourStr = String.format("%02d", hour);// 0~23
            long total = ht.getInboundBytes() + ht.getOutboundBytes();

            xAxis.add(hourStr);
            yAxis.add(total);
        }
        dto.setXAxis(xAxis);
        dto.setYAxis(yAxis);

        return dto;
    }
}
