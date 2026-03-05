package com.xiaoniucode.etp.client.statemachine.tunnel.action;

import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelState;
import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelCreateRespAction extends TunnelBaseAction {
    private static final Logger logger = LoggerFactory.getLogger(TunnelCreateRespAction.class);

    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("处理隧道创建响应, tunnelId={}, 多路复用={}, 加密={}, 压缩={}", 
            context.getTunnelId(), context.isMux(), context.isEncrypt(), context.isCompress());

        boolean mux = context.isMux();
        boolean encrypt = context.isEncrypt();
        boolean compress = context.isCompress();
        Channel tunnel = context.getTunnel();
        ChannelPipeline pipeline = tunnel.pipeline();
        if (mux) {
            if (!encrypt) {
                if (pipeline.get(NettyConstants.TLS_HANDLER) != null) {
                    pipeline.remove(NettyConstants.TLS_HANDLER);
                }
            }
            if (compress) {
                if (encrypt) {
                    pipeline.addAfter(NettyConstants.TLS_HANDLER, NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
                    pipeline.addAfter(NettyConstants.TLS_HANDLER, NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
                } else {
                    pipeline.addFirst(NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
                    pipeline.addFirst(NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
                }
            }
        }

    }
}
