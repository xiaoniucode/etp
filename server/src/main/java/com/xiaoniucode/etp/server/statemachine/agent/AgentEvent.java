package com.xiaoniucode.etp.server.statemachine.agent;

public enum AgentEvent {
    /**
     * 开始认证
     */
    AUTH_START,
    
    /**
     * 认证成功
     */
    AUTH_SUCCESS,
    
    /**
     * 认证失败
     */
    AUTH_FAILURE,
    
    /**
     * 断开连接
     */
    DISCONNECT,
    
    /**
     * 创建隧道
     */
    CREATE_TUNNEL,
    
    /**
     * 心跳超时
     */
    HEARTBEAT_TIMEOUT,
    
    /**
     * 管理员手动剔除
     */
    ADMIN_SHUTDOWN,
    
    /**
     * 接收到来自客户端 goaway 指令
     */
    GOAWAY_RECEIVED,
    
    /**
     * 重连窗口超时
     */
    RETRY_TIMEOUT,
    
    /**
     * 接收创建代理请求
     */
    PROXY_CREATE_REQUEST
}