package com.xiaoniucode.etp.core;

import io.netty.channel.ChannelPipeline;

public class ChannelSwitcher {
    /**
     * 从控制通道切换为数据通道
     *
     * @param pipeline pipeline
     */
    public static void switchToDataTunnel(ChannelPipeline pipeline) {
        pipeline.remove("tunnelMessageCodec");
        pipeline.remove("controlChannelHandler");
    }
}
