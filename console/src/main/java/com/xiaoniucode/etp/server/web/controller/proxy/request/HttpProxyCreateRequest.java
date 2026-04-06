package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * HTTP 代理请求参数类
 */
@Getter
@Setter
public class HttpProxyCreateRequest {
    @NotEmpty(message = "clientId 不能为空")
    private String clientId;
    @NotEmpty(message = "name 不能为空")
    private String name;
    private Integer localPort;
    @NotNull(message = "status 不能为空")
    private Integer status;
    @NotNull(message = "domainType 不能为空")
    private Integer domainType;
    @NotNull(message = "domains 不能为空")
    private Set<String> domains;
    @NotNull(message = "encrypt 不能为空")
    private Boolean encrypt;
    @NotNull(message = "targets 不能为空")
    private List<TargetRequest> targets;
    @NotNull(message = "tunnelType 不能为空")
    private Integer tunnelType;
}