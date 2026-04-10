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

import com.xiaoniucode.etp.core.enums.LoadBalanceType;
import com.xiaoniucode.etp.server.web.entity.converter.LoadBalanceConverter;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 负载均衡实体类
 */
@Data
@Entity
@Table(name = "load_balance")
public class LoadBalanceDO {
    /**
     * 代理ID
     */
    @Id
    @Column(nullable = false)
    private String proxyId;
    /**
     * 负载均衡类型
     */
    @Convert(converter = LoadBalanceConverter.class)
    private LoadBalanceType strategy;
}