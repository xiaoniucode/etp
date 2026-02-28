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
    @NotEmpty(message = "clientId 不能为空")
    private String clientId;
    @NotEmpty(message = "name 不能为空")
    private String name;
    @NotEmpty(message = "localIp 不能为空")
    private String localIp;
    @NotNull(message = "localPort 不能为空")
    private Integer localPort;
    @NotNull(message = "status 不能为空")
    private Integer status;
    @NotNull(message = "domainType 不能为空")
    private Integer domainType;
    @NotNull(message = "domains 不能为空")
    private Set<String> domains;
    @NotNull(message = "encrypt 不能为空")
    private Boolean encrypt;
    @NotNull(message = "compress 不能为空")
    private Boolean compress;
}