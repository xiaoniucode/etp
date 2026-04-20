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
package com.xiaoniucode.etp.server.web.entity;

import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.server.web.entity.converter.*;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "proxies")
public class ProxyDO {
    /**
     * 代理ID
     */
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "agent_id")
    private String agentId;
    /**
     * 代理名称
     */
    @Column(name = "name", unique = true, nullable = false, length = 30)
    private String name;
    /**
     * 协议类型
     */
    @Column(name = "protocol", nullable = false)
    @Convert(converter = ProtocolTypeConverter.class)
    private ProtocolType protocol;
    /**
     * 是否启用
     *
     */
    @Column(name = "status", nullable = false)
    @Convert(converter = ProxyStatusConverter.class)
    private ProxyStatus status;
    /**
     * 配置来源类型
     * 用于区分是后台手动创建，还是客户端上报等
     */
    @Column(name = "source_type", nullable = false)
    @Convert(converter = ProxySourceTypeConverter.class)
    private ProxySourceType sourceType;
    /**
     * 域名类型
     */
    @Convert(converter = DomainTypeConverter.class)
    @Column(name = "domain_type")
    private DomainType domainType;
    /**
     * 远程端口
     */
    @Column(name = "remote_port")
    private Integer remotePort;
    /**
     * 部署模式
     */
    @Column(name = "deployment_mode", nullable = false)
    @Convert(converter = DeploymentModeConverter.class)
    private DeploymentMode deploymentMode;
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}