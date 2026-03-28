package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;

import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.client.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.codec.TMSPCodec;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
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
    private static final int DEFAULT_DIRECT_COUNT = 10;

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
            createTunnel(context, false, false);
        }
    }

    private void createTunnel(AgentContext agentContext, boolean isTls, boolean isMultiplex) {
        Integer connectionId = agentContext.getConnectionId();
        AppConfig config = agentContext.getConfig();
        Bootstrap dataBootstrap = new Bootstrap()
                .group(agentContext.getControlWorkerGroup())
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (isTls && agentContext.getTlsContext() != null) {
                            SslHandler sslHandler = agentContext.getTlsContext().newHandler(sc.alloc(), config.getServerAddr(), config.getServerPort());
                            sc.pipeline().addLast(NettyConstants.TLS_HANDLER, sslHandler);
                        }
                        sc.pipeline()
                                .addLast(NettyConstants.TMSP_CODEC, TMSPCodec.create(10 * 1024 * 1024))
                                .addLast(NettyConstants.CONTROL_FRAME_HANDLER, agentContext.getControlFrameHandler());
                    }
                });

        dataBootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
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
