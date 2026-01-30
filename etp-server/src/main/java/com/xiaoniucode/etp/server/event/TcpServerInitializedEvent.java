package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.Map;

/**
 * TCP 协议代理服务初始化成功事件
 */
@Getter
public class TcpServerInitializedEvent extends Event {
    private final ServerBootstrap serverBootstrap;

    public TcpServerInitializedEvent(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }
}
