package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record HttpProxyDTO(
        Integer id,
        String clientId,
        String name,
        Integer protocol,
        Integer source,
        String localIp,
        String baseDomain,
        Integer localPort,
        Integer status,
        Integer domainType,
        Boolean encrypt,
        Boolean compress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> domains,
        Integer httpProxyPort
) implements Serializable {
}
