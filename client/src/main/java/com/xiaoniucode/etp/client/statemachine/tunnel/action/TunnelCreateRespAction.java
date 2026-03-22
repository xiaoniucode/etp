package com.xiaoniucode.etp.client.statemachine.tunnel.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.*;
import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.transport.NettyBatchWriteQueue;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.TlsHandlerCleanup;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelCreateRespAction extends TunnelBaseAction {
    private static final Logger logger = LoggerFactory.getLogger(TunnelCreateRespAction.class);

    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("创建隧道, tunnelId={}, 多路复用={}, 加密={}, 压缩={}",
                context.getTunnelId(), context.isMux(), context.isEncrypt(), context.isCompress());
        boolean isMux = context.isMux();
        boolean encrypt = context.isEncrypt();
        boolean compress = context.isCompress();
        Channel tunnel = context.getTunnel();
        ChannelPipeline tunnelPipeline = tunnel.pipeline();
        if (isMux) {
            if (!encrypt && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
                TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
            } else {
                AgentContext agentContext = context.getAgentContext();
                SslContext tlsContext = agentContext.getTlsContext();
                if (tlsContext != null) {
                    AppConfig config = agentContext.getConfig();
                    SslHandler sslHandler = tlsContext.newHandler(tunnel.alloc(), config.getServerAddr(), config.getServerPort());
                    tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
                }
            }
            if (compress) {
                tunnelPipeline.addAfter(NettyConstants.CONTROL_FRAME_HANDLER,NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
                tunnelPipeline.addBefore(NettyConstants.TMSP_CODEC,NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
            } else {
                if (tunnelPipeline.get(NettyConstants.SNAPPY_ENCODER) != null) {
                    tunnelPipeline.remove(NettyConstants.SNAPPY_ENCODER);
                }
                if (tunnelPipeline.get(NettyConstants.SNAPPY_DECODER) != null) {
                    tunnelPipeline.remove(NettyConstants.SNAPPY_DECODER);
                }
            }
            NettyBatchWriteQueue writeQueue = NettyBatchWriteQueue.createWriteQueue(tunnel);
            context.setWriteQueue(writeQueue);
            MuxConnectionPool.add(context);
        } else {
            DirectConnectionPool.add(context);
        }
    }
}
