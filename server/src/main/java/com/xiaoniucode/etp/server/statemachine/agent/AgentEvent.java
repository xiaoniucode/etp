package com.xiaoniucode.etp.server.statemachine.agent;

public enum AgentEvent {
    CONNECT,          // 连接
    AUTH_START,       // 开始认证
    AUTH_SUCCESS,     // 认证成功
    AUTH_FAILURE,     // 认证失败
    READY,            // 就绪
    DISCONNECT,       // 断开连接
    ERROR,             // 错误

    PROXY_CREATE_REQUEST,     //接收创建代理请求

}