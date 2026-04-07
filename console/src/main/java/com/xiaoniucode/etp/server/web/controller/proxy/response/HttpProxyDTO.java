package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record HttpProxyDTO(
        String id,
        String agentId,
        String name,
        Integer protocol,
        Integer status,
        Integer domainType,
        Integer agentType,
        Boolean encrypt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> domains,
        List<TargetDTO>targets,
        BandwidthDTO bandwidth,
        Integer httpProxyPort
) implements Serializable {
}
