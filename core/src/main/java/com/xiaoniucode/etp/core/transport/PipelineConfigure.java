package com.xiaoniucode.etp.core.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
public class PipelineConfigure {

    public static void removeControlHandler(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        String[] handlersToRemove = {
                NettyConstants.TMSP_CODEC,
                NettyConstants.CONTROL_FRAME_HANDLER
        };
        for (String handlerName : handlersToRemove) {
            if (pipeline.get(handlerName) != null) {
                pipeline.remove(handlerName);
            }
        }
    }
}
