package com.xiaoniucode.etp.core;

/**
 * Netty 容器服务生命周期
 * @author liuxin
 */
public interface Lifecycle {
    /**
     * 启动容器服务
     */
    public void start();

    /**
     * 停止服务 清理资源
     */
    public void stop();
}
