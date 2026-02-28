package com.xiaoniucode.etp.server.web.controller.client.request;

import com.xiaoniucode.etp.core.enums.ClientType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ClientSaveRequest(
        @NotEmpty(message = "clientId 不能为空")
        String clientId,
        @NotEmpty(message = "name 不能为空")
        String name,
        @NotNull(message = "clientType 不能为空")
        ClientType clientType,
        @NotEmpty(message = "token 不能为空")
        String token,
        @NotEmpty(message = "arch 不能为空")
        String arch,
        @NotEmpty(message = "os 不能为空")
        String os,
        @NotEmpty(message = "version 不能为空")
        String version
) {
}
