package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TcpProxyUpdateRequest {
    @NotEmpty(message = "id 不能为空")
    private String id;
    @NotEmpty(message = "agentId 不能为空")
    private String agentId;
    @NotEmpty(message = "name 不能为空")
    private String name;
    @NotNull(message = "remotePort 不能为空")
    private Integer remotePort;
    @NotNull(message = "status 不能为空")
    private Integer status;
    @NotNull(message = "encrypt 不能为空")
    private Boolean encrypt;
    @NotNull(message = "tunnelType 不能为空")
    private Integer tunnelType;
    @NotNull(message = "targets 不能为空")
    private List<TargetRequest> targets;
}