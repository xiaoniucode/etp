package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TcpProxyDTO(
        String id,
        String agentId,
        String name,
        Integer protocol,
        Integer remotePort,
        Integer agentType,
        Boolean enabled,
        Boolean encrypt,
        BandwidthDTO bandwidth,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
