package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.*;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.client.transport.DirectBridgeFactory;
import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.netty.TlsHandlerCleanup;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamOpenAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        if (context.hasVariable(StreamConstants.VISIT_INFO)) {
            Channel control = context.getControl();
            int streamId = context.getStreamId();

            NewStreamCodec.NewStreamInfo visitorInfo = context.getVariableAs(StreamConstants.VISIT_INFO, NewStreamCodec.NewStreamInfo.class);
            String localIp = visitorInfo.getLocalIp();
            int localPort = visitorInfo.getLocalPort();

            context.setLocalIp(localIp);
            context.setLocalPort(localPort);

            AgentContext agentContext = context.getAgentContext();

            Bootstrap serverBootstrap = agentContext.getServerBootstrap();
            serverBootstrap.connect(localIp, localPort).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    logger.debug("连接到目标服务 - [地址={}，端口={}]", localIp, localPort);
                    Channel server = serverFuture.channel();
                    server.config().setOption(ChannelOption.AUTO_READ, false);
                    server.attr(AttributeKeys.STREAM_ID).set(streamId);

                    TunnelConfig tunnelConfig = TunnelConfig.builder()
                            .isMux(context.isMuxTunnel())
                            .encrypt(context.isEncrypt())
                            .compress(context.isCompress())
                            .build();
                    //获取一个连接
                    TunnelManager.acquire(tunnelConfig).ifPresent(tunnelContext -> {
                        Channel tunnel = tunnelContext.getTunnel();
                        if (control != null && control.isActive()) {
                            context.setServer(server);
                            context.setTunnel(tunnel);
                            context.setWriteQueue(tunnelContext.getWriteQueue());

                            Integer connectionId = agentContext.getConnectionId();
                            Message.StreamOpenResponse req = Message.StreamOpenResponse.newBuilder()
                                    .setCode(0)
                                    .setConnectionId(connectionId)
                                    .setTunnelId(tunnelContext.getTunnelId())
                                    .build();
                            ByteBuf payload = ProtobufUtil.toByteBuf(req, control.alloc());
                            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_OPEN_RESP, payload);
                            frame.setCompressed(frame.isCompressed());
                            frame.setEncrypted(frame.isEncrypted());
                            frame.setMuxTunnel(context.isMuxTunnel());
                            control.writeAndFlush(frame).addListener(f -> {
                                if (f.isSuccess()) {
                                    if (!context.isMuxTunnel()) {
                                        handDirectTunnelHandlers(agentContext, context);
                                        logger.debug("独立隧道创建成功 - [隧道ID={},目标地址={}，目标端口={}]", tunnelContext.getTunnelId(), localIp, localPort);
                                    } else {
                                        logger.debug("共享隧道创建成功 - [隧道ID={},目标地址={}，目标端口={}]", tunnelContext.getTunnelId(), localIp, localPort);
                                    }
                                    context.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
                                    server.config().setOption(ChannelOption.AUTO_READ, true);
                                }
                            });
                        }
                    });
                } else {
                    control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_ERROR));
                    logger.error("隧道创建失败 - [服务地址={}:服务端口={}] 不可用!", localIp, localPort);
                }
            });
            context.removeVariable(StreamConstants.VISIT_INFO);
        }
    }

    private void handDirectTunnelHandlers(AgentContext agentContext, StreamContext context) {
        if (context == null || context.isMuxTunnel()) {
            return;
        }
        Channel tunnel = context.getTunnel();
        Channel server = context.getServer();

        if (tunnel == null || server == null) {
            logger.warn("隧道或服务器通道为null，跳过直接隧道处理");
            return;
        }

        ChannelPipeline tunnelPipeline = tunnel.pipeline();
        ChannelPipeline serverPipeline = server.pipeline();

        if (tunnelPipeline == null || serverPipeline == null) {
            logger.warn("隧道或服务器管道为null，跳过直接隧道处理");
            return;
        }
        if (serverPipeline.get(NettyConstants.REAL_SERVER_HANDLER) != null) {
            serverPipeline.remove(NettyConstants.REAL_SERVER_HANDLER);
        }

        if (tunnelPipeline.get(NettyConstants.TMSP_CODEC) != null) {
            tunnelPipeline.remove(NettyConstants.TMSP_CODEC);
        }
        if (tunnelPipeline.get(NettyConstants.CONTROL_FRAME_HANDLER) != null) {
            tunnelPipeline.remove(NettyConstants.CONTROL_FRAME_HANDLER);
        }
        boolean encrypt = context.isEncrypt();
        boolean compress = context.isCompress();
        if (!encrypt && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
            TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
        } else {
            SslContext tlsContext = agentContext.getTlsContext();
            if (tlsContext != null) {
                SslHandler sslHandler = tlsContext.newHandler(tunnel.alloc());
                tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
            }
        }
        if (compress) {
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
        //隧道桥接
        DirectBridgeFactory.bridge(tunnel, server);
    }
}