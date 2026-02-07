package com.xiaoniucode.etp.server.web.controller.accesstoken.request;

/**
 * 创建访问令牌请求对象
 */
public class CreateAccessTokenRequest {

    /**
     * 访问令牌名称
     */
    private String name;

    /**
     * 最大客户端数
     */
    private Integer maxClient;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxClient() {
        return maxClient;
    }

    public void setMaxClient(Integer maxClient) {
        this.maxClient = maxClient;
    }
}