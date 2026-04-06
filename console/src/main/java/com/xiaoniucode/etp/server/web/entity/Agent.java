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

import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.server.web.entity.converter.AgentTypeConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 代理节点实体类
 */
@Data
@Entity
@Table(name = "agent")
public class Agent {
    /**
     * 代理节点ID
     */
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    /**
     * 代理节点名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 代理类型
     */
    @Column(name = "agentType", nullable = false)
    @Convert(converter = AgentTypeConverter.class)
    private AgentType agentType;

    /**
     * 操作系统
     */
    @Column(name = "os", nullable = false)
    private String os;

    /**
     * 架构
     */
    @Column(name = "arch", nullable = false)
    private String arch;

    /**
     * 版本
     */
    @Column(name = "version", nullable = false)
    private String version;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
