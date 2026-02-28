package com.xiaoniucode.etp.server.web.controller.basicauth.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 更新 BasicAuth 请求
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddBasicAuthRequest {
    @NotNull(message = "代理 ID 不能为空")
    private String proxyId;
    @NotNull(message = "启用状态不能为空")
    private Boolean enable;

}
