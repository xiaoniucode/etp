package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAccessControlRequest {
    @NotEmpty(message = "proxyId 不能为空")
    private String proxyId;
    private Boolean enable;
    @NotNull(message = "mode 不能为空")
    private Integer mode;
}
