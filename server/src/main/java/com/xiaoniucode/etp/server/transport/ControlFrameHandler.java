package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.VisitorManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private VisitorManager visitorManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel control = ctx.channel();
        AgentContext agent = agentManager.createAgent(control);
       agent.fireEvent(AgentEvent.CONNECT );
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) {
        AgentContext agentContext = agentManager.getAgentContext(ctx.channel())
                .orElseThrow(() -> new IllegalStateException("连接上下文不存在"));
        byte msgType = frame.getMsgType();
        switch (msgType) {
            case TMSP.MSG_AUTH -> {
                ByteBuf payload = frame.getPayload();
                Message.AuthInfo authInfo = ProtobufUtil.parseFrom(payload, Message.AuthInfo.parser());
                agentContext.setVariable("authInfo",authInfo);
                //开始处理认证
              agentContext.fireEvent(AgentEvent.AUTH_START);
            }
            case TMSP.MSG_STREAM_OPEN_RESP -> {
                int streamId = frame.getStreamId();
                Channel tunnel = ctx.channel();
                StreamContext streamContext = visitorManager.getServerStreamContext(streamId);
                if (streamContext==null){
                    logger.warn("流上下文不存在 - [streamId={}]", streamId);
                    return;
                }
                streamContext.setTunnel(tunnel);
                streamContext.fireEvent(ClientStreamEvent.STREAM_OPEN_SUCCESS);
            }
            case TMSP.MSG_STREAM_DATA -> {
                int streamId = frame.getStreamId();
                ByteBuf payload = frame.getPayload().retain();
                StreamContext streamContext = visitorManager.getServerStreamContext(streamId);
                streamContext.fireEvent(ClientStreamEvent.STREAM_DATA);
                streamContext.sendPayloadToVisitor(payload);

            }
            case TMSP.MSG_PROXY_CREATE -> {
                Message.NewProxy newProxy = ProtobufUtil.parseFrom(frame.getPayload(), Message.NewProxy.parser());
                agentContext.setVariable("newProxy",newProxy);
               agentContext.fireEvent(AgentEvent.PROXY_CREATE_REQUEST);
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
