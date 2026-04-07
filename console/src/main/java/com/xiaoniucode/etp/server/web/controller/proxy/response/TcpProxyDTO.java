package com.xiaoniucode.etp.server.web.controller.proxy.response;

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
        List<TargetDTO>targets,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
