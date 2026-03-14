package com.xiaoniucode.etp.server.transport.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * 桥接器接口，用于将数据包转发到目标Channel
 */
public interface ChannelBridge extends ChannelHandler {
    /**
     * 数据包转发目标
     *
     * @return Channel 通道
     */
    Channel getTarget();
}