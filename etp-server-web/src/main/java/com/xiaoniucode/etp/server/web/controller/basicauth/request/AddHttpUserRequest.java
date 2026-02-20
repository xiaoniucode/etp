package com.xiaoniucode.etp.server.web.controller.basicauth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 添加 HTTP 用户请求
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddHttpUserRequest {
    @NotNull(message = "代理 ID 不能为空")
    private String proxyId;
    @NotBlank(message = "用户名不能为空")
    private String user;
    @NotBlank(message = "密码不能为空")
    private String pass;
}
