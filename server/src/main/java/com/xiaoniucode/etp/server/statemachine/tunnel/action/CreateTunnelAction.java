package com.xiaoniucode.etp.server.statemachine.tunnel.action;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.netty.TlsHandlerCleanup;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.tunnel.*;
import com.xiaoniucode.etp.core.netty.NettyBatchWriteQueue;
import com.xiaoniucode.etp.server.transport.TlsContextHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTunnelAction extends TunnelBaseAction {
    private final Logger logger = LoggerFactory.getLogger(CreateTunnelAction.class);
    @Autowired
    protected DirectTunnelPoolManager directTunnelPoolManager;
    @Autowired
    protected MuxTunnelManager muxTunnelManager;

    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("开始建立隧道");
        Boolean compress = context.getVariableAs("compress", Boolean.class);
        Boolean encrypt = context.getVariableAs("encrypt", Boolean.class);
        context.setCompress(compress);
        context.setEncrypt(encrypt);
        boolean isMuxTunnel = context.isMux();
        Channel tunnel = context.getTunnel();
        //只处理共享隧道，独立隧道打开流响应再处理
        if (isMuxTunnel) {
            ChannelPipeline tunnelPipeline = tunnel.pipeline();
            if (Boolean.TRUE.equals(!encrypt) && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
                TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
            } else {
                SslContext tlsContext = TlsContextHolder.get();
                if (tlsContext != null) {
                    SslHandler sslHandler = tlsContext.newHandler(tunnel.alloc());
                    tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
                }
            }
            if (Boolean.TRUE.equals(compress)) {
                tunnelPipeline.addLast(NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
                tunnelPipeline.addLast(NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
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
            muxTunnelManager.add(context);
        } else {
            directTunnelPoolManager.register(context);
        }
        Channel control = context.getControl();
        Message.TunnelCreateResponse resp = Message.TunnelCreateResponse.newBuilder()
                .setTunnelId(context.getTunnelId())
                .setCode(0)
                .setTunnelId(context.getTunnelId())
                .build();
        ByteBuf payload = ProtobufUtil.toByteBuf(resp, control.alloc());
        TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_TUNNEL_CREATE_RESP, payload);
        control.writeAndFlush(frame);
        context.fireEvent(TunnelEvent.CREATE_SUCCESS);
    }
}
