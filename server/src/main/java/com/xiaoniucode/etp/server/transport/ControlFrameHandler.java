package com.xiaoniucode.etp.server.transport;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.statemachine.agent.command.TunnelCreateCmd;
import com.xiaoniucode.etp.server.statemachine.stream.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 控制隧道消息处理器
 */
@Component
@ChannelHandler.Sharable
public class ControlFrameHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private final Logger logger = LoggerFactory.getLogger(ControlFrameHandler.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private StreamManager streamManager;

    @Autowired
    @Qualifier("agentStateMachine")
    private StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) {
        try {
            byte msgType = frame.getMsgType();
            switch (msgType) {
                case TMSP.MSG_AUTH -> {
                    ByteBuf payload = frame.getPayload();
                    AgentContext agentContext = agentManager.createAgent(ctx.channel(), agentStateMachine);
                    Message.AuthInfo authInfo = ProtobufUtil.parseFrom(payload, Message.AuthInfo.parser());
                    agentContext.setVariable(AgentConstants.AGENT_AUTH_INFO, authInfo);
                    agentContext.fireEvent(AgentEvent.AUTH_START);
                }

                case TMSP.MSG_TUNNEL_CREATE -> {
                    Optional<AgentContext> ag = agentManager.getAgentContext(frame.getStreamId());
                    if (ag.isPresent()) {
                        AgentContext agentContext = ag.get();
                        Channel control = agentContext.getControl();
                        Channel tunnel = ctx.channel();
                        if (control == tunnel) {
                            logger.error("控制隧道和消息来源与数据隧道相同，消息异常，关闭连接");
                            ChannelUtils.closeOnFlush(ctx.channel());
                            return;
                        }
                        Message.TunnelCreateRequest req = ProtobufUtil.parseFrom(frame.getPayload(), Message.TunnelCreateRequest.parser());
                        TunnelCreateCmd cmd = new TunnelCreateCmd(tunnel, frame.isEncrypted(), frame.isMuxTunnel(), req.getTunnelId());
                        control.eventLoop().execute(() -> {
                            agentContext.setVariable("tunnelCreateCmd", cmd);
                            agentContext.fireEvent(AgentEvent.CREATE_TUNNEL);
                        });
                    } else {
                        ChannelUtils.closeOnFlush(ctx.channel());
                    }
                }

                //----------------------------------------------------------------------------------------//
                case TMSP.MSG_STREAM_OPEN_RESP -> {
                    int streamId = frame.getStreamId();

                    StreamContext streamContext = streamManager.getStreamContext(streamId);
                    if (streamContext == null) {
                        logger.warn("流上下文不存在 - [streamId={}]", streamId);
                        return;
                    }
                    ByteBuf payload = frame.getPayload();
                    Message.StreamOpenResponse resp = ProtobufUtil.parseFrom(payload, Message.StreamOpenResponse.parser());
                    String tunnelId = resp.getTunnelId();
                    streamContext.setMultiplex(frame.isMuxTunnel());
                    streamContext.setVariable(StreamConstants.TUNNEL_ID, tunnelId);
                    streamContext.setCompress(frame.isCompressed());
                    streamContext.setEncrypt(frame.isEncrypted());
                    streamContext.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
                }
                case TMSP.MSG_STREAM_DATA -> {
                    int streamId = frame.getStreamId();
                    StreamContext streamContext = streamManager.getStreamContext(streamId);
                    if (streamContext!=null){
                        streamContext.forwardToRemote(frame.getPayload());
                    }
                }
                case TMSP.MSG_PROXY_CREATE -> {
                    agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
                        Message.NewProxy newProxy = ProtobufUtil.parseFrom(frame.getPayload(), Message.NewProxy.parser());
                        agentContext.setVariable(AgentConstants.NEWA_PROXY, newProxy);
                        agentContext.fireEvent(AgentEvent.PROXY_CREATE_REQUEST);
                    });
                }
                //内网目标服务健康上报
                case TMSP.MSG_SERVICE_HEALTH_CHANGE -> {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("控制隧道断开");
        agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
            Channel control = agentContext.getControl();
            ChannelUtils.closeOnFlush(control);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("控制连接异常: ", cause);
        agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
            Channel control = agentContext.getControl();
           // ChannelUtils.closeOnFlush(control);
        });
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel tunnel = ctx.channel();
        boolean writable = tunnel.isWritable();
        logger.warn("控制隧道可写性变化：{}", writable);
        if (writable) {
            //数据隧道恢复可写，恢复暂停的访问者读取
            IntSet pausedStreamIds = streamManager.getPausedStreamIds(tunnel);
            logger.debug("获取到 {} 条暂停流数量", pausedStreamIds.size());
            if (!pausedStreamIds.isEmpty()) {
                logger.debug("控制隧道恢复可写，恢复 {} 个访问者读取", pausedStreamIds.size());
                pausedStreamIds.stream().forEach(streamId -> {
                    StreamContext streamContext = streamManager.getStreamContext(streamId);
                    if (streamContext != null) {
                        Channel visitor = streamContext.getVisitor();
                        if (visitor != null) {
                            ctx.executor().schedule(() -> {
                                visitor.config().setOption(ChannelOption.AUTO_READ, true);
                                visitor.read();
                                streamManager.removePausedStream(tunnel, streamId);
                            }, 5, TimeUnit.MILLISECONDS);//延迟5ms，避免隧道在临界状态下来回切换，防止"乒乓效应"
                        }
                    }
                });
            }
        }
    }
}
