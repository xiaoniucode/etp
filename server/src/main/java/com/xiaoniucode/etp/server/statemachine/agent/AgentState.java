package com.xiaoniucode.etp.server.statemachine.agent;

public enum AgentState {
    INITIALIZED,      // 初始状态
    CONNECTED,        // 已连接
    AUTHENTICATING,   // 认证中
    AUTHENTICATED,    // 已认证
    FAILED,           // 失败
    DISCONNECTED      // 已断开
}

