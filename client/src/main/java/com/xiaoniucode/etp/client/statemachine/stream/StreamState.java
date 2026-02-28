package com.xiaoniucode.etp.client.statemachine.stream;

public enum StreamState {
    INITIALIZED,  // 初始状态
    OPENING,      // 打开中
    OPENED,       // 已打开
    CLOSING,      // 关闭中
    FAILED        // 失败
}