package io.github.lxien.orbien.core.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

public class PipelineConfigure {

    public static void removeControlIdleCheckHandler(Channel channel) {
        if (channel == null || !channel.isActive()) {
            return;
        }
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(NettyConstants.CONTROL_IDLE_CHECK_HANDLER) != null) {
            pipeline.remove(NettyConstants.CONTROL_IDLE_CHECK_HANDLER);
        }
    }
}
