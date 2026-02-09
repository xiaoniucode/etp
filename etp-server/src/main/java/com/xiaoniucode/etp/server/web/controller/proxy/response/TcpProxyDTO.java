package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TcpProxyDTO(
        Integer id,
        String clientId,
        String name,
        Integer protocol,
        String localIp,
        Integer localPort,
        Integer remotePort,
        Integer status,
        Boolean encrypt,
        Boolean compress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
