package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.manager.MuxConnHolder;
import com.xiaoniucode.etp.client.manager.MuxConnManager;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamConstants;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;
import com.xiaoniucode.etp.client.transport.DirectBridgeFactory;
import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StreamOpenAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenAction.class);
    private static final Queue<Channel> pool = new ConcurrentLinkedQueue<>();

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
            MuxConnManager muxManager = MuxConnHolder.get();
            Bootstrap serverBootstrap = agentContext.getServerBootstrap();
            serverBootstrap.connect(localIp, localPort).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    logger.debug("连接到目标服务 - [地址={}，端口={}]", localIp, localPort);
                    Channel server = serverFuture.channel();
                    server.config().setOption(ChannelOption.AUTO_READ, false);
                    server.attr(AttributeKeys.STREAM_ID).set(streamId);

                    Channel tunnel = muxManager.getOrCreate(context.isCompress(), context.isEncrypt());
                    if (tunnel != null && tunnel.isActive()) {
                        context.setServer(server);
                        context.setTunnel(tunnel);
                        tunnel.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_STREAM_OPEN_RESP)).addListener(f -> {
                            if (f.isSuccess()) {
                                context.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
                                if (!context.isMuxTunnel()) {
                                    //删除自定义协议
                                    server.pipeline().remove(NettyConstants.REAL_SERVER_HANDLER);
                                    tunnel.pipeline().remove(NettyConstants.TMSP_CODEC);
                                    tunnel.pipeline().remove(NettyConstants.CONTROL_FRAME_HANDLER);
                                    tunnel.pipeline().remove(NettyConstants.IDLE_CHECK_HANDLER);
                                    //隧道桥接
                                    DirectBridgeFactory.bridge(tunnel, server);
                                    logger.debug("独立隧道创建成功 - [目标地址={}，目标端口={}]", localIp, localPort);
                                } else {
                                    logger.debug("共享隧道创建成功 - [目标地址={}，目标端口={}]", localIp, localPort);
                                }
                                //开启真实目标服务可读
                                server.config().setOption(ChannelOption.AUTO_READ, true);
                            }
                        });
                    }
                } else {
                    control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_ERROR));
                    logger.error("隧道创建失败 - [服务地址={}:服务端口={}] 不可用!", localIp, localPort);
                }
            });
            context.removeVariable(StreamConstants.VISIT_INFO);
        }
    }
}
//独立隧道
//                        ConnectionPool.acquire().thenAccept(tunnel ->
//        tunnel.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_CONN)).addListener(f -> {
//        if (f.isSuccess()) {
//        //控制通道转换为数据通道
//        PipelineSwitcher.switchToDirectDataTunnel(tunnel.pipeline(), compress, encrypt);
//        //隧道双向桥接
//        ClientBridgeFactory.bridge(tunnel, server);
/// /创建连接会话
//                                        ServerStreamManager.createServerSession(streamId, tunnel, server, new Target(localIP, localPort)).ifPresent(serverSession -> {
//        //设置通道可读
//        server.config().setOption(ChannelOption.AUTO_READ, true);
//                                            logger.debug("隧道创建成功 - [目标地址={}，目标端口={}]", localIP, localPort);
//                                        });
//                                                }
//                                                })).exceptionally(cause -> {
//        logger.error(cause.getMessage(), cause);
//        control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_ERROR));
//        return null;
//        });