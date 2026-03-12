package com.xiaoniucode.etp.server.statemachine.stream;

public enum StreamState {
    IDLE,  // 初始状态
    CHECKING_TARGET,//检查目标
    OPENING,      // 打开中
    OPENED,       // 已打开
    CLOSED,       // 已关闭
}