package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTcpProxyRequest {

    @NotNull(message = "客户端ID不能为空")
    private Integer clientId;

    @NotBlank(message = "代理名称不能为空")
    private String name;

    @NotBlank(message = "本地IP不能为空")
    private String localIp;

    @NotNull(message = "本地端口不能为空")
    private Integer localPort;

    private Integer remotePort;

    private Integer domainType;

}
