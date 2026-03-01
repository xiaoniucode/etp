package com.xiaoniucode.etp.server.statemachine.tunnel.action;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.tunnel.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTunnelAction extends TunnelBaseAction {
    private final Logger logger= LoggerFactory.getLogger(CreateTunnelAction.class);
    @Autowired
    protected DirectTunnelPoolManager directTunnelPoolManager;


    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("开始建立隧道");
        Channel control = context.getControl();

        Message.TunnelCreateResponse resp = Message.TunnelCreateResponse.newBuilder()
                .setTunnelId(context.getTunnelId())
                .setCode(0)
                .setTunnelId(context.getTunnelId())
                .build();
        ByteBuf payload = ProtobufUtil.toByteBuf(resp, control.alloc());
        TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_TUNNEL_CREATE_RESP, payload);
        control.writeAndFlush(frame).addListener(future -> {
            if (future.isSuccess()){
                System.out.println();
            }else {

            }
        });

    }
}
