package com.xiaoniucode.etp.server.statemachine.stream;

public enum StreamEvent {
    /**
     * 发送打开流请求
     */
    STREAM_OPEN,
    
    /**
     * 检查访问目标
     */
    CHECK_TARGET,
    
    /**
     * 检查完成
     */
    TARGET_VALIDATED,
    
    /**
     * 打开流成功
     */
    STREAM_OPEN_SUCCESS,
    
    /**
     * 打开流失败
     */
    STREAM_OPEN_FAILURE,
    
    /**
     * 发送关闭流请求
     */
    STREAM_CLOSE,
    
    /**
     * 收到重置流通知
     */
    STREAM_RESET,
    
    /**
     * 发送/接收流数据
     */
    STREAM_DATA
}