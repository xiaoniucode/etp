package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.*;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.client.transport.bridge.TunnelBridgeFactory;
import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamOpenAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        if (context.hasVariable(StreamConstants.VISIT_INFO)) {
            AgentContext agentContext = (AgentContext) context.getAgentContext();
            Channel control = agentContext.getControl();
            int streamId = context.getStreamId();

            NewStreamCodec.NewStreamInfo visitorInfo = context.getVariableAs(StreamConstants.VISIT_INFO, NewStreamCodec.NewStreamInfo.class);
            String localIp = visitorInfo.getLocalIp();
            int localPort = visitorInfo.getLocalPort();

            context.setLocalIp(localIp);
            context.setLocalPort(localPort);



            Bootstrap serverBootstrap = agentContext.getServerBootstrap();
            serverBootstrap.connect(localIp, localPort).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    logger.debug("连接到目标服务 - [地址={}，端口={}]", localIp, localPort);
                    Channel server = serverFuture.channel();
                    server.config().setOption(ChannelOption.AUTO_READ, false);
                    server.attr(AttributeKeys.STREAM_ID).set(streamId);

                    TunnelConfig tunnelConfig = TunnelConfig.builder()
                            .isMux(context.isMultiplex())
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
                            frame.setCompressed(context.isCompress());
                            frame.setEncrypted(context.isEncrypt());
                            frame.setMuxTunnel(context.isMultiplex());
                            control.writeAndFlush(frame).addListener(f -> {
                                if (f.isSuccess()) {
                                    TunnelBridge tunnelBridge;
                                    if (context.isMultiplex()) {
                                        tunnelBridge = TunnelBridgeFactory.buildMux(context);
                                        logger.debug("共享隧道创建成功 - [隧道ID={},目标地址={}，目标端口={}]", tunnelContext.getTunnelId(), localIp, localPort);
                                    } else {
                                        tunnelBridge = TunnelBridgeFactory.buildDirect(context);
                                        logger.debug("独立隧道创建成功 - [隧道ID={},目标地址={}，目标端口={}]", tunnelContext.getTunnelId(), localIp, localPort);
                                    }
                                    tunnelBridge.open();
                                    context.setTunnelBridge(tunnelBridge);
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
}