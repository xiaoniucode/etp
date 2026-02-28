package com.xiaoniucode.etp.server.statemachine.stream.visitor;

public enum ClientStreamEvent {
    STREAM_OPEN,          // 发送打开流请求
    CHECK_TARGET,         //检查访问目标
    TARGET_VALIDATED,     //检查完成
    STREAM_OPEN_SUCCESS,  // 打开流成功
    STREAM_OPEN_FAILURE,  // 打开流失败
    STREAM_CLOSE,         // 发送关闭流请求
    STREAM_RESET,         // 收到重置流通知
    STREAM_DATA           // 发送/接收流数据
}