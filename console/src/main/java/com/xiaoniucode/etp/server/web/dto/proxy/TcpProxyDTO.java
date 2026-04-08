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
package com.xiaoniucode.etp.server.web.dto.proxy;
import com.xiaoniucode.etp.server.web.dto.bandwidth.BandwidthDTO;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
public record TcpProxyDTO(
        String id,
        String agentId,
        String name,
        Integer protocol,
        Integer remotePort,
        Integer agentType,
        Integer status,
        Boolean encrypt,
        BandwidthDTO bandwidth,
        List<TargetDTO> targets,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}