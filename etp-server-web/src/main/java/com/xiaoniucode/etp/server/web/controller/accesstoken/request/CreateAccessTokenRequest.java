package com.xiaoniucode.etp.server.web.controller.accesstoken.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建访问令牌请求对象
 */
@Getter
@Setter
public class CreateAccessTokenRequest {
    /**
     * 访问令牌名称
     */
    @NotEmpty(message = "name 不能为空")
    private String name;

    /**
     * 最大客户端数
     */
    @NotNull(message = "maxClient 不能为空")
    private Integer maxClient;
}