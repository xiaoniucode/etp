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

package com.xiaoniucode.etp.server.web.dto.monitor;

import lombok.Data;

/**
 * 统计概览
 */
@Data
public class TunnelStatsDTO {

    /**
     * 客户端总数
     */
    private Long totalClients;

    /**
     * 在线客户端数
     */
    private Long onlineClients;

    /**
     * 代理总数
     */
    private Long totalProxies;

    /**
     * 已启动代理数
     */
    private Long activeProxies;
    /**
     * 总入站流量（字节）
     */
    private Long totalInboundBytes;

    /**
     * 总出站流量（字节）
     */
    private Long totalOutboundBytes;
}