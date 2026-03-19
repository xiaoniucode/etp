package com.xiaoniucode.etp.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
@Data
public class AuthProperties  implements Serializable {
    private String token;
    @NestedConfigurationProperty
    private RetryProperties retry = new RetryProperties();
    @Data
    static class RetryProperties implements Serializable {
        /**
         * 初始重试延迟（秒）
         */
        private Integer initialDelay = 1;
        /**
         * 最大延迟时间（秒）
         */
        private Integer maxDelay = 20;
        /**
         * 最大重试次数
         */
        private Integer maxRetries = 5;
    }
}
