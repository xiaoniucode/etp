package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import io.netty.bootstrap.ServerBootstrap;
import lombok.Getter;

/**
 * TCP 协议代理服务初始化成功事件
 */
@Getter
public class TcpProxyInitializedEvent extends Event {
    private final ServerBootstrap serverBootstrap;

    public TcpProxyInitializedEvent(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }
}
