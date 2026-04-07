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
     * 令牌名称
     */
    private String name;

    /**
     * 最大设备数
     */
    private Integer maxDevice;

    /**
     * 设备超时时间
     */
    private Integer deviceTimeout;

    /**
     * 最大连接数
     */
    private Integer maxConnection;
}