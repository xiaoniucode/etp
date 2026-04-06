package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record HttpProxyDTO(
        String id,
        String gentId,
        String name,
        Integer protocol,
        Boolean enabled,
        Integer domainType,
        Integer agentType,
        Boolean encrypt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> domains,
        BandwidthDTO bandwidth,
        Integer httpProxyPort
) implements Serializable {
}
