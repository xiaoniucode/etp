package com.xiaoniucode.etp.server.transport;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.statemachine.stream.StreamConstants;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    private StreamManager visitorManager;
    @Autowired
    private TunnelManager tunnelManager;

    @Autowired
    @Qualifier("agentStateMachine")
    private StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) {
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
                    Message.TunnelCreateRequest req = ProtobufUtil.parseFrom(frame.getPayload(), Message.TunnelCreateRequest.parser());
                    TunnelContext tunnelContext = tunnelManager.createContext(
                            agentContext,
                            req.getTunnelId(),
                            ctx.channel(),
                            frame.isMuxTunnel());
                    tunnelContext.setVariable(TunnelConstants.COMPRESS, frame.isCompressed());
                    tunnelContext.setVariable(TunnelConstants.ENCRYPT, frame.isEncrypted());
                    tunnelContext.fireEvent(TunnelEvent.CREATE);
                } else {
                    ChannelUtils.closeOnFlush(ctx.channel());
                }
            }

            //----------------------------------------------------------------------------------------//
            case TMSP.MSG_STREAM_OPEN_RESP -> {
                int streamId = frame.getStreamId();

                StreamContext streamContext = visitorManager.getStreamContext(streamId);
                if (streamContext == null) {
                    logger.warn("流上下文不存在 - [streamId={}]", streamId);
                    return;
                }
                ByteBuf payload = frame.getPayload();
                Message.StreamOpenResponse resp = ProtobufUtil.parseFrom(payload, Message.StreamOpenResponse.parser());
                String tunnelId = resp.getTunnelId();
                streamContext.setMux(frame.isMuxTunnel());
                streamContext.setVariable(StreamConstants.TUNNEL_ID, tunnelId);
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
            }
            case TMSP.MSG_STREAM_DATA -> {
                int streamId = frame.getStreamId();
                ByteBuf payload = frame.getPayload().retain();
                StreamContext streamContext = visitorManager.getStreamContext(streamId);
                streamContext.sendPayloadToVisitor(payload);
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("控制隧道断开");
        agentManager.getAgentContext(ctx.channel()).ifPresent(connCtx -> {

        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("控制连接异常", cause);
        agentManager.getAgentContext(ctx.channel()).ifPresent(connCtx -> {

        });
    }
}
