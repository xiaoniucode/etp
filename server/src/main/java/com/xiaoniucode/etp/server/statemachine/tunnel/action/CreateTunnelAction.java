package com.xiaoniucode.etp.server.statemachine.tunnel.action;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.tunnel.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTunnelAction extends TunnelBaseAction {
    private final Logger logger = LoggerFactory.getLogger(CreateTunnelAction.class);
    @Autowired
    protected DirectTunnelPoolManager directTunnelPoolManager;


    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("开始建立隧道");
        directTunnelPoolManager.register(context);

        boolean mux = context.isMux();
        boolean encrypt = context.isEncrypt();
        boolean compress = context.isCompress();
        Channel tunnel = context.getTunnel();
        ChannelPipeline pipeline = tunnel.pipeline();
        //只处理共享隧道，独立隧道打开流响应再处理
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
