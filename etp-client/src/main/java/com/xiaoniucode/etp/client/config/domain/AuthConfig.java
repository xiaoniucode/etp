package com.xiaoniucode.etp.client.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthConfig {
    private String token;
    /**
     * 初始重试延迟（秒）
     */
    private Integer initialDelay = 1;
    /**
     * 最大延迟时间（秒）
     */
    private Integer maxDelay = 30;
    /**
     * 最大重试次数
     */
    private Integer maxRetries = 5;
}
