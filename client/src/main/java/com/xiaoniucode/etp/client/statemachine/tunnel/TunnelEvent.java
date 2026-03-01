package com.xiaoniucode.etp.client.statemachine.tunnel;

public enum TunnelEvent {
    CONNECT,             // 连接
    CREATE_SUCCESS,      // 创建成功
    CREATE_FAILURE,      // 创建失败
    CREATE_RESPONSE,     // 创建回复
    CLOSE,               // 关闭
}
