package com.xiaoniucode.etp.core.handler;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.TimeUnit;

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
            if (pipeline.get("snappyDecoder") == null) {
                pipeline.addLast("snappyDecoder", new SnappyDecoder());
            }
            if (pipeline.get("snappyEncoder") == null) {
                pipeline.addLast("snappyEncoder", new SnappyEncoder());
            }
        }

        if (!enableEncrypt) {
            removeTlsGracefully(pipeline);
        }
    }

    /**
     * 删除 tls 处理器并清空channel
     *
     * @param pipeline 管道
     */
    private static void removeTlsGracefully(ChannelPipeline pipeline) {
        SslHandler sslHandler = pipeline.get(SslHandler.class);
        if (sslHandler != null) {
            // 标记正在移除 TLS，停止新的解码
            sslHandler.setSingleDecode(true);
            // 等待所有待处理数据完成
            Channel channel = pipeline.channel();
            // 触发所有待写入数据
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(f -> {
                        // 确保所有数据都被处理
                        if (sslHandler.engine().isOutboundDone()) {
                            pipeline.remove("tls");
                        } else {
                            // 如果还有数据，等待一小段时间
                            channel.eventLoop().schedule(() -> {
                                pipeline.remove("tls");
                            }, 100, TimeUnit.MILLISECONDS);
                        }
                    });
        }
    }
}
