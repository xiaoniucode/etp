package com.xiaoniucode.etp.client.statemachine.tunnel;

public enum TunnelState {
    IDLE,           // 空闲
    CREATING,        //创建中
    ESTABLISHED,    // 已建立
    CLOSED          // 已关闭
}
