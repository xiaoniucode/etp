package com.xiaoniucode.etp.server.web.controller.proxy.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * HTTP 代理请求参数类
 */
@Getter
@Setter
public class HttpProxyUpdateRequest {
    private String id;
    private String name;
    private String localIp;
    private Integer localPort;
    private Integer status;
    private Integer domainType;
    private Set<String> domains;
    private Boolean encrypt;
    private Boolean compress;
}