package com.xiaoniucode.etp.server.statemachine.agent;

public enum AgentState {
    IDLE,             // 初始状态
    AUTHENTICATING,   // 认证中
    ESTABLISHED,      // 已连接
    FAILED,           // 失败
    DISCONNECTED      // 已断开
}

