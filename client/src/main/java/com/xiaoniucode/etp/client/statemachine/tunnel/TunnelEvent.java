package com.xiaoniucode.etp.client.statemachine.tunnel;

public enum TunnelEvent {
    CONNECT,             // 连接
    CREATE_RESPONSE,      // 创建响应
    CREATE_SUCCESS,      // 创建成功
    CREATE_FAILURE,      // 创建失败
    CLOSE,               // 关闭
}
