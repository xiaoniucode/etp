package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新访问控制请求
 */
@Getter
@Setter
public class UpdateAccessControlRequest {
    @NotEmpty(message = "ID 不能为空")
    private String proxyId;
    @NotNull(message = "启用状态不能为空")
    private Boolean enable;
    @NotNull(message = "访问控制模式不能为空")
    private Integer mode;
}
