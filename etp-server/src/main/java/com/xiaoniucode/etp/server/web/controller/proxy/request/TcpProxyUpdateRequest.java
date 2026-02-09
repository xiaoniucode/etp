package com.xiaoniucode.etp.server.web.controller.proxy.request;

import lombok.Getter;
import lombok.Setter;

/**
 * TCP 代理请求参数类
 */
@Getter
@Setter
public class TcpProxyUpdateRequest {
    private Integer id;
    private String clientId;
    private String name;
    private String localIp;
    private Integer localPort;
    private Integer remotePort;
    private Integer status;
    private Boolean encrypt;
    private Boolean compress;
}