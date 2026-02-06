package com.xiaoniucode.etp.server.web.controller.proxy.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class HttpProxyVo implements Serializable {
    private Integer id;
    private String name;
    private String protocol;
    private Integer clientId;
    private Integer listenPort;
    private String targetHost;
    private Integer targetPort;
    private List<String> domains;
}
