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

import jakarta.persistence.*;
import lombok.Data;

/**
 * 带宽限制实体类
 */
@Data
@Entity
@Table(name = "bandwidth")
public class Bandwidth {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 代理ID
     */
    private String proxyId;

    /**
     * 总带宽限制
     */
    @Column(name = "limit_total")
    private String limitTotal;

    /**
     * 入站带宽限制
     */
    @Column(name = "limit_in")
    private String limitIn;

    /**
     * 出站带宽限制
     */
    @Column(name = "limit_out")
    private String limitOut;
}