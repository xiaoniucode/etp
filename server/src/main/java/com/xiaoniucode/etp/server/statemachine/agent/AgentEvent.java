package com.xiaoniucode.etp.server.statemachine.agent;

public enum AgentEvent {
    AUTH_START,       // 开始认证
    AUTH_SUCCESS,     // 认证成功
    AUTH_FAILURE,     // 认证失败
    DISCONNECT,       // 断开连接
    CREATE_TUNNEL,   //创建隧道
    PROXY_CREATE_REQUEST,     //接收创建代理请求
}