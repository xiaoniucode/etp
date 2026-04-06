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

package com.xiaoniucode.etp.server.web.entity;

import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.server.web.entity.converter.DomainTypeConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理实体类
 */
@Data
@Entity
@Table(name = "proxies")
public class Proxy {
    /**
     * 代理ID
     */
    @Id
    private String id;

    /**
     * 代理名称
     */
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    /**
     * 协议类型
     */
    @Column(nullable = false)
    private String protocol;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 远程端口
     */
    private Integer remotePort;

    /**
     * 域名类型
     */
    @Convert(converter = DomainTypeConverter.class)
    @Column(name = "domain_type")
    private DomainType domainType;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 持久化前操作
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前操作
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}