package com.xiaoniucode.etp.client.statemachine.stream;

public enum StreamEvent {
    STREAM_OPEN,          // 打开流
    STREAM_OPEN_SUCCESS,  // 打开流成功
    STREAM_OPEN_FAILURE,  // 打开流失败
    STREAM_CLOSE,         // 收到关闭流请求
    STREAM_RESET,         // 收到重置流请求
    STREAM_DATA           // 收到流数据
}