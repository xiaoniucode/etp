package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.transport.TlsHandlerCleanup;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.transport.NettyBatchWriteQueue;
import com.xiaoniucode.etp.server.statemachine.agent.command.TunnelCreateCmd;
import com.xiaoniucode.etp.server.transport.TlsContextHolder;
import com.xiaoniucode.etp.server.transport.connection.DirectPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTunnelAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(CreateTunnelAction.class);
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
        String clientId = context.getClientId();

        //只处理共享隧道，独立隧道打开流响应再处理
        if (multiplex) {
            ChannelPipeline tunnelPipeline = tunnel.pipeline();
            if (!encrypt && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
                TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
            } else {
                TlsContextHolder.get().ifPresent(sslContext -> {
                    SslHandler sslHandler = sslContext.newHandler(tunnel.alloc());
                    if (tunnelPipeline.get(NettyConstants.TLS_HANDLER) == null) {
                        tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
                        logger.debug("添加 TLS handler");
                    } else {
                        tunnelPipeline.replace(NettyConstants.TLS_HANDLER, NettyConstants.TLS_HANDLER, sslHandler);
                        logger.debug("替换 TLS handler");
                    }
                });
            }
        }
        createPool(clientId, tunnelId, multiplex, encrypt, tunnel);
        Channel control = context.getControl();
        Message.TunnelCreateResponse resp = Message.TunnelCreateResponse.newBuilder()
                .setTunnelId(tunnelId)
                .setCode(0)
                .build();

        ByteBuf payload = ProtobufUtil.toByteBuf(resp, control.alloc());
        TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_TUNNEL_CREATE_RESP, payload);
        frame.setEncrypted(encrypt);
        frame.setMuxTunnel(multiplex);
        control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
            logger.debug("隧道创建结果响应引用计数：{}", payload.refCnt());
            ReferenceCountUtil.release(payload);
        });
    }

    public void createPool(String clientId, String tunnelId, boolean isMultiplex, boolean isEncrypt, Channel tunnel) {
        NettyBatchWriteQueue writeQueue = NettyBatchWriteQueue.createWriteQueue(tunnel);
        TunnelEntry poolEntry = new TunnelEntry(tunnelId, tunnel, writeQueue);
        poolEntry.setActive(true);
        if (isMultiplex) {
            multiplexPool.setChannel(clientId, isEncrypt, poolEntry);
        } else {
            directPool.register(clientId, poolEntry);
        }
    }
}
