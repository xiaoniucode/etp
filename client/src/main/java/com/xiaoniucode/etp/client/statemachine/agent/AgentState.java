package com.xiaoniucode.etp.client.statemachine.agent;

public enum AgentState {
    IDLE,      // 初始状态
    CONFIG_CHECKING,  // 检查配置
    SSL_INITIALIZING, // 初始化 SSL
    CONNECTING,       // 连接中
    AUTHENTICATING,   // 认证中
    CONNECTED,        // 已连接
    FAILED,           // 失败
    STOPPED           // 已停止
}