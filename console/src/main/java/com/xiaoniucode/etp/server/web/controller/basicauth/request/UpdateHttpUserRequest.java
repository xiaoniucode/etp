package com.xiaoniucode.etp.server.web.controller.basicauth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新 HTTP 用户请求
 */
@Getter
@Setter
public class UpdateHttpUserRequest {
    @NotNull(message = "ID 不能为空")
    private Integer id;
    @NotBlank(message = "用户名不能为空")
    private String user;
    @NotBlank(message = "密码不能为空")
    private String pass;
}
