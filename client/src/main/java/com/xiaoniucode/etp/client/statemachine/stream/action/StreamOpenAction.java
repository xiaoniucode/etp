package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;
import com.xiaoniucode.etp.core.codec.NewVisitorCodec;
import com.xiaoniucode.etp.core.constant.AttributeKeys;
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
        if (context.hasVariable("newVisitorInfo")) {
            Channel control = context.getControl();
            int streamId = context.getStreamId();

            NewVisitorCodec.NewVisitorInfo visitor = context.getVariableAs("newVisitorInfo", NewVisitorCodec.NewVisitorInfo.class);
            String localIp = visitor.getLocalIp();
            int localPort = visitor.getLocalPort();
            context.setLocalIp(localIp);
            context.setLocalPort(localPort);
            AgentContext agentContext = context.getAgentContext();
            AppConfig config = agentContext.getConfig();

            Bootstrap serverBootstrap = agentContext.getServerBootstrap();

            serverBootstrap.connect(localIp, localPort).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    logger.debug("连接到目标服务 - [地址={}，端口={}]", localIp, localPort);
                    Channel server = serverFuture.channel();
                    server.config().setOption(ChannelOption.AUTO_READ, false);
                    server.attr(AttributeKeys.STREAM_ID).set(streamId);
                    context.setServer(server);
                    Bootstrap controlBootstrap = agentContext.getControlBootstrap();

                    Channel tunnel = createOrget(controlBootstrap,config);

                    context.setTunnel(tunnel);
                    tunnel.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_STREAM_OPEN_RESP)).addListener(f -> {
                        if (f.isSuccess()) {
                            server.config().setOption(ChannelOption.AUTO_READ, true);
                            context.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
                           // logger.debug("隧道创建成功 - [目标地址={}，目标端口={}]", localIp, localPort);
                        }
                    });


                } else {
                    control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_ERROR));
                    logger.error("隧道创建失败 - [服务地址={}:服务端口={}] 不可用!", localIp, localPort);
                }
            });
            context.removeVariable("newVisitorInfo");
        }
    }

    private Channel createOrget(Bootstrap controlBootstrap, AppConfig config) {
        if (pool.peek()==null){
            logger.error("池子为空，创建新的连接");
            controlBootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    pool.add(future.channel());
                } else {
                    logger.error("连接到隧道失败");
                }
            });
           return createOrget(controlBootstrap,config);
        }else {
            Channel poll = pool.poll();
            if (poll.isActive()){
               logger.debug("从池子获取：{}",poll.id());
                return poll;
            }else {
                pool.remove(poll);
                logger.error("隧道无效，丢弃:"+poll.id());
                return createOrget(controlBootstrap,config);
            }
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