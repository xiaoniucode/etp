/*
 *
 *  *    Copyright 2026 xiaoniucode
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.metrics.HourlyTraffic;
import com.xiaoniucode.etp.server.web.dto.metrics.DailyTrafficQueryResult;
import com.xiaoniucode.etp.server.web.entity.MetricsDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<MetricsDO, Long> {

    void deleteByProxyId(String proxyId);

    @Query(value = """
            SELECT 
                DATE(m.created_at) AS dateStr, 
                CAST(SUM(m.write_bytes) AS UNSIGNED) AS totalWrite, 
                CAST(SUM(m.read_bytes) AS UNSIGNED) AS totalRead 
            FROM metrics m 
            WHERE m.proxy_id = :proxyId 
              AND m.created_at >= :startTime 
              AND m.created_at <= :endTime 
            GROUP BY DATE (m.created_at) 
            ORDER BY DATE(m.created_at) ASC
            """, nativeQuery = true)
    @SuppressWarnings("all")
    List<DailyTrafficQueryResult> queryDailyTrafficByRange(@Param("proxyId") String proxyId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    @Query(value = """
            SELECT
                DATE_ADD(DATE(m.created_at), INTERVAL HOUR(m.created_at) HOUR) AS hour,
                CAST(SUM(m.read_bytes) AS UNSIGNED) AS readBytes,
                CAST(SUM(m.write_bytes) AS UNSIGNED) AS writeBytes,
                CAST(SUM(m.read_messages) AS UNSIGNED) AS readMessages,
                CAST(SUM(m.write_messages) AS UNSIGNED) AS writeMessages
            FROM metrics m
            WHERE m.proxy_id = :proxyId
              AND m.created_at >= :startTime
              AND m.created_at < :endTime
            GROUP BY DATE_ADD(DATE(m.created_at), INTERVAL HOUR(m.created_at) HOUR)
            ORDER BY hour ASC
            """, nativeQuery = true)
    @SuppressWarnings("all")
    List<HourlyTraffic> queryHourlyTrafficByRange(@Param("proxyId") String proxyId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);
}
