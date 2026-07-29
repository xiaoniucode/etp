/*
 *
 *  *    Copyright 2026 lxien
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

package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.core.domain.HealthCheckConfig;
import io.github.lxien.orbien.core.enums.HealthCheckType;
import io.github.lxien.orbien.server.web.entity.converter.HealthCheckConverter;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 代理健康检查配置
 */
@Data
@Entity
@Table(name = "health_check")
public class HealthCheckDO {
    /**
     * 代理ID
     */
    @Id
    private String proxyId;
    /**
     * 检查类型
     */
    @Convert(converter = HealthCheckConverter.class)
    @Column(name = "type", nullable = false)
    private HealthCheckType type;
    /**
     * 检查间隔（秒）
     */
    @Column(name = "interval_sec", nullable = false)
    private Integer interval;
    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_sec", nullable = false)
    private Integer timeout;
    /**
     * 最大失败次数
     */
    @Column(name = "max_failed", nullable = false)
    private Integer maxFailed;
    /**
     * 检查路径
     */
    @Column(name = "path", nullable = false)
    private String path;
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    public static HealthCheckDO createDefault(String proxyId, HealthCheckType type) {
        HealthCheckDO healthCheckDO = new HealthCheckDO();
        healthCheckDO.setProxyId(proxyId);
        healthCheckDO.setEnabled(false);
        healthCheckDO.setType(type);
        healthCheckDO.setInterval(HealthCheckConfig.DEFAULT_INTERVAL);
        healthCheckDO.setTimeout(HealthCheckConfig.DEFAULT_TIMEOUT);
        healthCheckDO.setMaxFailed(HealthCheckConfig.DEFAULT_MAX_FAILED);
        healthCheckDO.setPath(type.isHttpCheck() ? HealthCheckConfig.DEFAULT_PATH : "/");
        return healthCheckDO;
    }
}
