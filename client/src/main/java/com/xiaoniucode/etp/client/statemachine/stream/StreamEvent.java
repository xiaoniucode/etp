package com.xiaoniucode.etp.client.statemachine.stream;

public enum StreamEvent {
    /**
     * 打开流
     */
    STREAM_OPEN,
    
    /**
     * 打开流成功
     */
    STREAM_OPEN_SUCCESS,
    
    /**
     * 打开流失败
     */
    STREAM_OPEN_FAILURE,
    
    /**
     * 收到关闭流请求
     */
    STREAM_CLOSE,
    
    /**
     * 收到重置流请求
     */
    STREAM_RESET,

    /**
     * 创建一个新的连接
     */
    CREATE_NEW_CONN
}