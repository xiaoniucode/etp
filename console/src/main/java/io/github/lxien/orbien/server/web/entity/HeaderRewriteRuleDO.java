/*
 *    Copyright 2026 lxien
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
package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.core.enums.HeaderAction;
import io.github.lxien.orbien.core.enums.HeaderDirection;
import io.github.lxien.orbien.server.web.entity.converter.HeaderActionConverter;
import io.github.lxien.orbien.server.web.entity.converter.HeaderDirectionConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求头改写规则
 */
@Data
@Entity
@Table(name = "header_rewrite_rule",
        indexes = {
                @Index(name = "idx_header_rewrite_rule_proxy_id", columnList = "proxy_id")
        })
@NoArgsConstructor
public class HeaderRewriteRuleDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 代理ID
     */
    @Column(name = "proxy_id", nullable = false)
    private String proxyId;
    /**
     * 改写方向
     */
    @Convert(converter = HeaderDirectionConverter.class)
    @Column(name = "direction", nullable = false)
    private HeaderDirection direction;
    /**
     * 改写动作
     */
    @Convert(converter = HeaderActionConverter.class)
    @Column(name = "action", nullable = false)
    private HeaderAction action;
    /**
     * 请求头名称
     */
    @Column(name = "name", nullable = false, length = 256)
    private String name;
    /**
     * 请求头值
     */
    @Column(name = "header_value", length = 1024)
    private String value;
}
