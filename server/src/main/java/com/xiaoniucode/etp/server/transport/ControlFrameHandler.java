package com.xiaoniucode.etp.server.transport;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
    @Qualifier("tunnelStateMachine")
    private StateMachine<TunnelState, TunnelEvent, TunnelContext> tunnelStateMachine;
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
                int connectionId = frame.getStreamId();
                boolean isMuxTunnel = frame.isMuxTunnel();
                agentManager.getAgentContext(connectionId).ifPresent(agentContext -> {
                    Message.TunnelCreateRequest req = ProtobufUtil.parseFrom(frame.getPayload(), Message.TunnelCreateRequest.parser());
                    TunnelContext context = TunnelContext.builder()
                            .connectionId(connectionId)
                            .compress(frame.isCompressed())
                            .control(agentContext.getControl())
                            .isMux(isMuxTunnel)
                            .encrypt(frame.isEncrypted())
                            .tunnel(ctx.channel())
                            .stateMachine(tunnelStateMachine)
                            .state(TunnelState.IDLE)
                            .tunnelId(req.getTunnelId()).build();

                    TunnelContext tunnelContext = tunnelManager.registerContext(context);
                    tunnelContext.fireEvent(TunnelEvent.CREATE);
                });
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
                streamContext.setVariable("tunnelId", tunnelId);
                streamContext.setVariable("connectionId", resp.getConnectionId());
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.fireEvent(StreamEvent.STREAM_OPEN_SUCCESS);
            }
            case TMSP.MSG_STREAM_DATA -> {
                int streamId = frame.getStreamId();
                ByteBuf payload = frame.getPayload().retain();
                StreamContext streamContext = visitorManager.getStreamContext(streamId);
                streamContext.fireEvent(StreamEvent.STREAM_DATA);
                streamContext.sendPayloadToVisitor(payload);

            }
            case TMSP.MSG_PROXY_CREATE -> {
                agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
                    Message.NewProxy newProxy = ProtobufUtil.parseFrom(frame.getPayload(), Message.NewProxy.parser());
                    agentContext.setVariable("newProxy", newProxy);
                    agentContext.fireEvent(AgentEvent.PROXY_CREATE_REQUEST);
                });
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("控制隧道断开，触发状态机事件");
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
