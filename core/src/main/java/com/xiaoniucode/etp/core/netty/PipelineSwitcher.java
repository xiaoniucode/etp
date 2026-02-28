package com.xiaoniucode.etp.core.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.TimeUnit;

public class PipelineSwitcher {

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
