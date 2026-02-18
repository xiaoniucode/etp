package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * HTTP 代理请求参数类
 */
@Getter
@Setter
public class HttpProxyCreateRequest {
    @NotEmpty
    private String clientId;
    @NotEmpty
    private String name;
    private String localIp;
    @NotNull
    private Integer localPort;
    @NotNull
    private Integer status;
    @NotNull
    private Integer domainType;
    private Set<String> domains;
    @NotNull
    private Boolean encrypt;
    @NotNull
    private Boolean compress;
}