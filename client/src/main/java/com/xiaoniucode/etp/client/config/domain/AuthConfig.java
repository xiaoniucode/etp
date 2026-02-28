package com.xiaoniucode.etp.client.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthConfig {
    private String token;
    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();
}
