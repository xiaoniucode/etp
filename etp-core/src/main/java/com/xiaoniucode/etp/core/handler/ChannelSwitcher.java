package com.xiaoniucode.etp.core.handler;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import io.netty.channel.ChannelPipeline;

public class ChannelSwitcher {
    /**
     * 从控制通道切换为数据通道
     * 发送时：原始数据 → 压缩 → 加密 → 发送
     * 接收时：接收 → 解密 → 解压 → 原始数据
     *
     * @param pipeline       pipeline
     * @param enableCompress 是否启用压缩
     * @param enableEncrypt  是否启用加密
     */
    public static void switchToDataTunnel(ChannelPipeline pipeline, boolean enableCompress, boolean enableEncrypt) {
        pipeline.remove("protoBufVarint32FrameDecoder");
        pipeline.remove("protoBufDecoder");
        pipeline.remove("protoBufVarint32LengthFieldPrepender");
        pipeline.remove("protoBufEncoder");
        pipeline.remove("controlTunnelHandler");
        pipeline.remove("idleCheckHandler");

        if (enableCompress) {
            pipeline.addLast("snappyDecoder", new SnappyDecoder());
            pipeline.addLast("snappyEncoder", new SnappyEncoder());
        }

        if (!enableEncrypt && pipeline.get("tls") != null) {
            pipeline.remove("tls");
        }
    }
}
