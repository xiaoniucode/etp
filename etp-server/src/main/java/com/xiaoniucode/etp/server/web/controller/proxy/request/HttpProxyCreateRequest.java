package com.xiaoniucode.etp.server.web.controller.proxy.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * HTTP 代理请求参数类
 */
@Getter
@Setter
public class HttpProxyCreateRequest {
    private String clientId;
    private String name;
    private Integer protocol;
    private String localIp;
    private Integer localPort;
    private Integer status;
    private Integer domainType;
    private List<String> domains;
    private Boolean encrypt;
    private Boolean compress;
}