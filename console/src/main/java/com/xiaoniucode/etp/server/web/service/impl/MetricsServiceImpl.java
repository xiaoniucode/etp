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
import com.xiaoniucode.etp.server.metrics.Metrics;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.dto.metrics.DailyTrafficQueryResult;
import com.xiaoniucode.etp.server.web.dto.metrics.Metrics24LineDTO;
import com.xiaoniucode.etp.server.web.dto.metrics.TrafficChartVO;
import com.xiaoniucode.etp.server.web.enums.MetricQueryType;
import com.xiaoniucode.etp.server.web.param.metrics.ProxyQueryParam;
import com.xiaoniucode.etp.server.web.repository.MetricsRepository;
import com.xiaoniucode.etp.server.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsServiceImpl implements MetricsService {
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private MetricsRepository metricsRepository;

    @Override
    public TrafficChartVO getTotal24hTraffic() {
        return buildTrafficChartVO(metricsCollector.getTotal24hTraffic(), metricsCollector.getTotalWriteBytes(),
                metricsCollector.getTotalReadBytes(),
                metricsCollector.getTotalWriteBytesRate(),
                metricsCollector.getTotalReadBytesRate()
        );
    }

    @Override
    public TrafficChartVO getProxy24hTraffic(ProxyQueryParam param) {
        String proxyId = param.getProxyId();
        MetricQueryType queryType = MetricQueryType.fromCode(param.getQueryType());
        switch (queryType) {
            //最近24小时实时数据
            case LAST_24_HOURS: {
                List<HourlyTraffic> list = metricsCollector.get24hTraffic(proxyId);
                Metrics metrics = metricsCollector.getProxyMetrics(proxyId);
                if (metrics == null) {
                    return buildTrafficChartVO(list, 0L, 0L, 0.0, 0.0);
                }
                return buildTrafficChartVO(list, metrics.getWriteBytes(), metrics.getReadBytes(),
                        metrics.getWriteRate(),
                        metrics.getReadRate()
                );
            }
            //历史数据
            case LAST_3_DAYS:
            case LAST_7_DAYS:
            case LAST_30_DAYS: {
                int days = queryType.getDays();
                LocalDate startDate = LocalDate.now().minusDays(days - 1);
                LocalDateTime startTime = startDate.atStartOfDay();
                LocalDateTime endTime = LocalDate.now().atTime(23, 59, 59);
                List<DailyTrafficQueryResult> results =
                        metricsRepository.queryDailyTrafficByRange(proxyId, startTime, endTime);
                return buildDailyTrafficChartVO(results, startDate, LocalDate.now());
            }
            case CUSTOM: {
                LocalDate startDate = param.getStartDate();
                LocalDate endDate = param.getEndDate();
                if (startDate == null || endDate == null) {
                    throw new BizException("起始日期不能为空");
                }
                long spanDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                if (spanDays <= 0 || spanDays > 15) {
                    throw new BizException("自定义查询天数跨度必须在 1 ~ 15 天之间");
                }
                LocalDateTime startTime = startDate.atStartOfDay();
                LocalDateTime endTime = endDate.atTime(23, 59, 59);

                if (spanDays == 1) {
                    return buildHistoricalDayHourlyChartVO(
                            metricsRepository.queryHourlyTrafficByRange(
                                    proxyId, startDate.atStartOfDay(), startDate.plusDays(1).atStartOfDay()),
                            startDate);
                } else {
                    List<DailyTrafficQueryResult> results =
                            metricsRepository.queryDailyTrafficByRange(proxyId, startTime, endTime);
                    return buildDailyTrafficChartVO(results, startDate, endDate);
                }
            }
            default:
                return null;
        }
    }

    /**
     * 历史单日图表，24个刻度（01-24）
     */
    private TrafficChartVO buildHistoricalDayHourlyChartVO(List<HourlyTraffic> sparse, LocalDate date) {
        Map<Integer, HourlyTraffic> byHour = new HashMap<>(24);
        if (sparse != null) {
            for (HourlyTraffic ht : sparse) {
                byHour.put(ht.getHour().getHour(), ht);
            }
        }

        Metrics24LineDTO upDto = new Metrics24LineDTO();
        Metrics24LineDTO downDto = new Metrics24LineDTO();
        List<String> xAxis = new ArrayList<>(24);
        List<Long> upYAxis = new ArrayList<>(24);
        List<Long> downYAxis = new ArrayList<>(24);
        long upTotal = 0L;
        long downTotal = 0L;
        LocalDateTime dayStart = date.atStartOfDay();

        for (int slot = 1; slot <= 24; slot++) {
            int hourIndex = slot - 1;
            HourlyTraffic ht = byHour.getOrDefault(hourIndex,
                    new HourlyTraffic(dayStart.plusHours(hourIndex), 0L, 0L, 0L, 0L));

            xAxis.add(String.format("%02d", slot));
            long up = ht.getWriteBytes();
            long down = ht.getReadBytes();
            upYAxis.add(up);
            downYAxis.add(down);
            upTotal += up;
            downTotal += down;
        }

        upDto.setXAxis(xAxis);
        upDto.setYAxis(upYAxis);
        downDto.setXAxis(xAxis);
        downDto.setYAxis(downYAxis);

        return TrafficChartVO.builder().up(upDto).down(downDto).upTotal(upTotal).downTotal(downTotal)
                .upRate(0.0)
                .downRate(0.0)
                .build();
    }

    /**
     * 滚动最近 24 小时，X 轴为实际钟点 00~23
     */
    private TrafficChartVO buildTrafficChartVO(List<HourlyTraffic> list, long upTotal,
                                               long downTotal, double upRate, double downRate) {
        Metrics24LineDTO upDto = new Metrics24LineDTO();
        Metrics24LineDTO downDto = new Metrics24LineDTO();

        List<String> xAxis = new ArrayList<>(24);
        List<Long> upYAxis = new ArrayList<>(24);
        List<Long> downYAxis = new ArrayList<>(24);

        for (HourlyTraffic ht : list) {
            String hourStr = String.format("%02d", ht.getHour().getHour());
            long up = ht.getWriteBytes();
            long down = ht.getReadBytes();

            xAxis.add(hourStr);
            upYAxis.add(up);
            downYAxis.add(down);
        }

        upDto.setXAxis(xAxis);
        upDto.setYAxis(upYAxis);
        downDto.setXAxis(xAxis);
        downDto.setYAxis(downYAxis);

        return TrafficChartVO.builder().up(upDto).down(downDto).upTotal(upTotal).downTotal(downTotal)
                .downRate(downRate)
                .upRate(upRate)
                .build();
    }

    /**
     * 构建每日流量数据的，按天统计（历史数据）
     */
    private TrafficChartVO buildDailyTrafficChartVO(List<DailyTrafficQueryResult> results,
                                                    LocalDate startDate, LocalDate endDate) {
        Metrics24LineDTO upDto = new Metrics24LineDTO();
        Metrics24LineDTO downDto = new Metrics24LineDTO();

        List<String> xAxis = new ArrayList<>();
        List<Long> upYAxis = new ArrayList<>();
        List<Long> downYAxis = new ArrayList<>();

        long upTotal = 0L;
        long downTotal = 0L;

        java.util.Map<LocalDate, DailyTrafficQueryResult> resultMap = new java.util.HashMap<>();
        if (results != null) {
            for (DailyTrafficQueryResult result : results) {
                resultMap.put(result.getDateStr(), result);
            }
        }

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = String.format("%02d-%02d", currentDate.getMonthValue(), currentDate.getDayOfMonth());

            DailyTrafficQueryResult result = resultMap.get(currentDate);
            long write = result != null && result.getTotalWrite() != null ? result.getTotalWrite() : 0L;
            long read = result != null && result.getTotalRead() != null ? result.getTotalRead() : 0L;

            xAxis.add(dateStr);
            upYAxis.add(write);
            downYAxis.add(read);

            upTotal += write;
            downTotal += read;

            currentDate = currentDate.plusDays(1);
        }

        upDto.setXAxis(xAxis);
        upDto.setYAxis(upYAxis);

        downDto.setXAxis(xAxis);
        downDto.setYAxis(downYAxis);

        return TrafficChartVO.builder().up(upDto).down(downDto).upTotal(upTotal).downTotal(downTotal)
                .upRate(0.0)
                .downRate(0.0)
                .build();
    }

    @Override
    public void saveHourlyMetricsSnapshot() {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProxyId(String proxyId) {
        metricsRepository.deleteByProxyId(proxyId);
        metricsCollector.removeByProxyId(proxyId);
    }
}
