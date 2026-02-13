package com.xiaoniucode.etp.server.web.controller.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "验证码不能为空")
    private String code;
    @NotEmpty(message = "验证码ID不能为空")
    private String captchaId;

}
