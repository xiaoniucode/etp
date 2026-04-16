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
     * 本地关闭流
     */
    STREAM_LOCAL_CLOSE,
    /**
     * 来自远程的关闭流
     */
    STREAM_REMOTE_CLOSE,

    /**
     * 收到重置流通知
     */
    STREAM_RESET,
}