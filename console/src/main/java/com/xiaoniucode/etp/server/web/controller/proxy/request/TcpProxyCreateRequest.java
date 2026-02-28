package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * TCP 代理请求参数类
 */
@Getter
@Setter
public class TcpProxyCreateRequest {
    @NotEmpty(message = "clientId 不能为空")
    private String clientId;
    @NotEmpty(message = "name 不能为空")
    private String name;
    @NotEmpty(message = "localIp 不能为空")
    private String localIp;
    @NotNull(message = "localPort 不能为空")
    private Integer localPort;
    @NotNull(message = "remotePort 不能为空")
    private Integer remotePort;
    @NotNull(message = "status 不能为空")
    private Integer status;
    @NotNull(message = "encrypt 不能为空")
    private Boolean encrypt;
    @NotNull(message = "compress 不能为空")
    private Boolean compress;
}