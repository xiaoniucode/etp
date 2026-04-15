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
     * 标记连接断开
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
     * 断开连接
     */
    GOAWAY,

    /**
     * 重连窗口超时
     */
    RETRY_TIMEOUT,
    /**
     * 重连
     */
    RETRY_CONNECT,

    /**
     * 接收创建代理请求
     */
    PROXY_CREATE_REQUEST,
    REBUILD_CONTEXT
}