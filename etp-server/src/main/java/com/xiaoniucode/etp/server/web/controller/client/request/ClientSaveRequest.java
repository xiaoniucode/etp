package com.xiaoniucode.etp.server.web.controller.client.request;

import com.xiaoniucode.etp.core.enums.ClientType;

public record ClientSaveRequest(
        String clientId,
        String name,
        ClientType clientType,
        String token,
        String arch,
        String os,
        String version
) {
}
