package com.xiaoniucode.etp.server.web.controller.accesstoken.request;

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
    private String name;

    /**
     * 最大客户端数
     */
    private Integer maxClient;
}