package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAccessControlRequest {
    private String proxyId;
    private Boolean enable;
    private Integer mode;
}
