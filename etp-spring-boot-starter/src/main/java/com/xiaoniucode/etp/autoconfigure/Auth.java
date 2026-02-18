package com.xiaoniucode.etp.autoconfigure;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class Auth {
    private String token;
    /**
     * 重试配置
     */
    @NestedConfigurationProperty
    private Retry retry = new Retry();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }
}
