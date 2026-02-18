package com.xiaoniucode.etp.autoconfigure;

/**
 * 重试配置
 *
 * @author liuxin
 */
public class Retry {
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

    public Integer getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Integer initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Integer getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(Integer maxDelay) {
        this.maxDelay = maxDelay;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
