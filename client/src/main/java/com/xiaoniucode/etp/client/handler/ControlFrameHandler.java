package com.xiaoniucode.etp.client.handler;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.core.codec.NewVisitorCodec;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

/**
 *
 * @author liuxin
 */
@ChannelHandler.Sharable
public class ControlFrameHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private final Logger logger = LoggerFactory.getLogger(ControlFrameHandler.class);
    private final StateMachine<ClientState, ClientEvent, AgentContext> stateMachine;
    private final AgentContext clientContext;

    public ControlFrameHandler(AgentContext clientContext) {
        this.clientContext = clientContext;
        this.stateMachine = clientContext.getStateMachine();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) {
        byte msgType = frame.getMsgType();
        switch (msgType) {
            case TMSP.MSG_AUTH_RESP: {
                ByteBuf payload = frame.getPayload();
                Message.AuthResponse authResponse = ProtobufUtil.parseFrom(payload, Message.AuthResponse.parser());
                int connectionId = authResponse.getConnectionId();
                int code = authResponse.getCode();
                if (code == 0) {
                    clientContext.setConnectionId(connectionId);
                    clientContext.setAuthenticated(true);
                    stateMachine.fireEvent(clientContext.getState(), ClientEvent.AUTH_SUCCESS, clientContext);
                } else {
                    clientContext.setAuthenticated(false);
                    stateMachine.fireEvent(clientContext.getState(), ClientEvent.AUTH_FAILURE, clientContext);
                }
                break;
            }
            case TMSP.MSG_STREAM_OPEN: {
                logger.debug("收到流打开请求");
                NewVisitorCodec.NewVisitorInfo visitorInfo = NewVisitorCodec.decode(frame.getPayload());
                StreamContext streamContext = StreamManager.createStreamContext(frame.getStreamId(), clientContext);
                streamContext.setVariable("newVisitorInfo", visitorInfo);
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.fireEvent(StreamEvent.STREAM_OPEN);
                break;
            }

            case TMSP.MSG_GOAWAY: {
                stateMachine.fireEvent(clientContext.getState(), ClientEvent.STOP, clientContext);
                break;
            }

            case TMSP.MSG_PROXY_CREATE_RESP: {
                break;
            }
            case TMSP.MSG_STREAM_DATA: {
                ByteBuf payload = frame.getPayload();
                int streamId = frame.getStreamId();
                try {
                    StreamContext streamContext = StreamManager.getStreamContext(streamId);
                    if (streamContext == null) {
                        logger.warn("收到数据的流上下文不存在 - [streamId={}]", frame.getStreamId());
                        payload.release();
                        return;
                    }
                    streamContext.fireEvent(StreamEvent.STREAM_DATA);
                    streamContext.relayToServer(payload);
                } catch (Exception e) {
                    logger.error("转发数据失败 - [streamId={}]", streamId, e);
                    payload.release();
                }
                break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (clientContext.getControl() == ctx.channel()) {
            logger.error("控制隧道断开：channel-{}",ctx.channel().id());
            ctx.close();
            clientContext.fireEvent(ClientEvent.NETWORK_ERROR);
        }else {
            logger.error("数据隧道断开：channel-{}",ctx.channel().id());
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ClientState currentState = clientContext.getState();
        logger.info("控制通道断开，当前状态: {}", currentState);
        if (isNetworkException(cause)) {
            if (clientContext.getControl() == ctx.channel()) {
                logger.error("控制隧道网络错误");
                ctx.close();
                clientContext.fireEvent(ClientEvent.NETWORK_ERROR);
            }
        } else {
            ctx.close();

        }
    }
    private boolean isNetworkException(Throwable cause) {
        if (cause instanceof IOException) {
            return true;
        }
        if (cause.getMessage() != null) {
            String msg = cause.getMessage().toLowerCase();
            return msg.contains("reset by peer") ||
                    msg.contains("connection refused") ||
                    msg.contains("network is unreachable") ||
                    msg.contains("timeout");
        }
        return false;
    }
}
