package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.*;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.command.TunnelCreateCmd;
import com.xiaoniucode.etp.server.transport.connection.DirectPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTunnelAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(CreateTunnelAction.class);
    @Autowired
    private DirectPool directPool;
    @Autowired
    private MultiplexPool multiplexPool;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        logger.debug("开始建立隧道");
        TunnelCreateCmd cmd = context.getAndRemoveAs("tunnelCreateCmd", TunnelCreateCmd.class);
        Channel tunnel = cmd.getTunnel();
        String tunnelId = cmd.getTunnelId();
        boolean multiplex = cmd.isMultiplex();
        boolean encrypt = cmd.isEncrypt();
        String clientId = context.getAgentInfo().getAgentId();
        createPool(clientId, tunnelId, multiplex, encrypt, tunnel);
        Channel control = context.getControl();
        Message.TunnelCreateResponse resp = Message.TunnelCreateResponse.newBuilder()
                .setTunnelId(tunnelId)
                .setCode(0)
                .build();

        ByteBuf payload = ProtobufUtil.toByteBuf(resp, control.alloc());
        TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_TUNNEL_CREATE_RESP, payload);
        frame.setEncrypted(encrypt);
        frame.setMultiplexTunnel(multiplex);
        if (!control.isActive() || !control.isWritable()) {
            logger.error("控制通道不可用，隧道创建结果结果发送失败");
            return;
        }
        control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
            logger.debug("隧道创建结果响应引用计数：{}", payload.refCnt());
            if (!future.isSuccess()) {
                logger.error("隧道创建结果响应失败", future.cause());
            }
        });
    }

    public void createPool(String clientId, String tunnelId, boolean isMultiplex, boolean isEncrypt, Channel tunnel) {
        if (tunnel == null) {
            throw new IllegalArgumentException("tunnel 不能为空");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("clientId 不能为空");
        }
        if (tunnelId == null) {
            throw new IllegalArgumentException("tunnelId 不能为空");
        }
        logger.debug("创建隧道 客户端ID={} 隧道ID={} 加密={} 多路复用={}",clientId,tunnelId,isEncrypt,isMultiplex);
        NettyBatchWriteQueue writeQueue = NettyBatchWriteQueue.createWriteQueue(tunnel);
        TunnelEntry poolEntry = new TunnelEntry(tunnelId,isEncrypt, tunnel, writeQueue);
        poolEntry.setActive(true);
        if (isMultiplex) {
            multiplexPool.setChannel(clientId, isEncrypt, poolEntry);
        } else {
            directPool.register(clientId, poolEntry);
        }
    }
}
