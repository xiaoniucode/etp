package com.xiaoniucode.etp.client.statemachine.stream;

public enum StreamState {
    /**
     * 初始状态
     */
    IDLE,
    
    /**
     * 打开中
     */
    OPENING,
    
    /**
     * 已打开
     */
    OPENED,
    
    /**
     * 关闭状态
     */
    CLOSED,
    
    /**
     * 失败
     */
    FAILED
}