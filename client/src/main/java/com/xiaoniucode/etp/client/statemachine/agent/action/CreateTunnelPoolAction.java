package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;

import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.client.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预创建数据传输隧道
 */
@Slf4j
public class CreateTunnelPoolAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(CreateTunnelPoolAction.class);
    private static final int DEFAULT_DIRECT_COUNT = 0;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        createMultiplexTunnel(context);
        createDirectTunnels(context);
    }

    /**
     * 创建多路复用隧道
     */
    private void createMultiplexTunnel(AgentContext context) {
        createTunnel(context, true, true);
        createTunnel(context, false, true);
    }

    /**
     * 创建独立隧道
     */
    private void createDirectTunnels(AgentContext context) {
        for (int i = 0; i < DEFAULT_DIRECT_COUNT; i++) {
            createTunnel(context, context.getTlsContext() != null, false);
        }
    }

    private void createTunnel(AgentContext agentContext, boolean isTls, boolean isMultiplex) {
        Integer connectionId = agentContext.getConnectionId();
        Bootstrap bootstrap = agentContext.getControlBootstrap();
        AppConfig config = agentContext.getConfig();

        bootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
            Channel tunnel = future.channel();
            TunnelEntry tunnelEntry;
            if (future.isSuccess()) {
                if (isMultiplex) {
                    MultiplexPool multiplexPool = agentContext.getMultiplexPool();
                    tunnelEntry = multiplexPool.createChannel(isTls, tunnel);
                } else {
                    DirectPool directPool = agentContext.getDirectPool();
                    tunnelEntry = directPool.createTunnel(tunnel);
                }

                Message.TunnelCreateRequest body = Message.TunnelCreateRequest.newBuilder()
                        .setTunnelId(tunnelEntry.getTunnelId())
                        .build();
                ByteBuf payload = ProtobufUtil.toByteBuf(body, tunnel.alloc());
                TMSPFrame frame = new TMSPFrame(connectionId, TMSP.MSG_TUNNEL_CREATE, payload);
                frame.setMultiplexTunnel(isMultiplex);
                frame.setEncrypted(isTls);

                tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                    logger.debug("隧道创建请求数据包引用计数：{}", payload.refCnt());
                    if (!f.isSuccess()) {
                        logger.error("隧道创建请求发送失败！", f.cause());
                    } else {
                        logger.debug("隧道创建请求发送成功");
                    }
                });
            }
        });
    }
}
