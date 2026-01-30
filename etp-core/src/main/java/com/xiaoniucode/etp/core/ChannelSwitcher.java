package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import io.netty.channel.ChannelPipeline;

public class ChannelSwitcher {
    /**
     * 从控制通道切换为数据通道
     *
     * @param pipeline pipeline
     */
    public static void switchToDataTunnel(ChannelPipeline pipeline) {
        pipeline.remove("protoBufVarint32FrameDecoder");
        pipeline.remove("protoBufDecoder");
        pipeline.remove("protoBufVarint32LengthFieldPrepender");
        pipeline.remove("protoBufEncoder");
        pipeline.remove("controlTunnelHandler");
        pipeline.remove("idleCheckHandler");
        //添加字节压缩编解码器
        pipeline.addLast("snappyDecoder", new SnappyDecoder());
        pipeline.addLast("snappyEncoder", new SnappyEncoder());
    }
}
