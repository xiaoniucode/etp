package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamConstants;
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

/**
 *
 * @author liuxin
 */
@ChannelHandler.Sharable
public class ControlFrameHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private final Logger logger = LoggerFactory.getLogger(ControlFrameHandler.class);
    private final AgentContext clientContext;

    public ControlFrameHandler(AgentContext clientContext) {
        this.clientContext = clientContext;
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
                    clientContext.fireEvent(ClientEvent.AUTH_SUCCESS);
                } else {
                    clientContext.setAuthenticated(false);
                    clientContext.fireEvent(ClientEvent.AUTH_FAILURE);
                }
                break;
            }
            case TMSP.MSG_PROXY_CREATE_RESP: {
                clientContext.fireEvent(ClientEvent.PROXY_CREATE_RESP);
                break;
            }
            case TMSP.MSG_GOAWAY: {
                clientContext.fireEvent(ClientEvent.STOP);
                break;

            }
            //********************Stream***********************//
            case TMSP.MSG_STREAM_OPEN: {
                NewVisitorCodec.NewVisitorInfo visitorInfo = NewVisitorCodec.decode(frame.getPayload());
                StreamContext streamContext = StreamManager.createStreamContext(frame.getStreamId(), clientContext);
                streamContext.setVariable(StreamConstants.VISIT_INFO, visitorInfo);
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.setMuxTunnel(frame.isMuxTunnel());

                streamContext.fireEvent(StreamEvent.STREAM_OPEN);
                break;
            }
            case TMSP.MSG_STREAM_DATA: {
                ByteBuf payload = frame.getPayload();
                int streamId = frame.getStreamId();
                StreamManager.getStreamContext(streamId).ifPresent(streamContext -> {
                    streamContext.fireEvent(StreamEvent.STREAM_DATA);
                    streamContext.relayToServer(payload);
                });
                break;
            }
            case TMSP.MSG_STREAM_CLOSE: {
                StreamManager.getStreamContext(frame.getStreamId()).ifPresent(streamContext -> {
                    streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
                });
                break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (clientContext.getControl() == ctx.channel()) {
            clientContext.fireEvent(ClientEvent.NETWORK_ERROR);
        } else {
            logger.error("数据隧道断开：channel-{}", ctx.channel().id());
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //控制隧道
        if (ctx.channel() == clientContext.getControl()) {
            if (isNetworkException(cause)) {
                if (clientContext.getControl() == ctx.channel()) {
                    logger.error("控制隧道网络错误", cause);
                    clientContext.fireEvent(ClientEvent.NETWORK_ERROR);
                }
            } else {
                clientContext.fireEvent(ClientEvent.STOP);
            }
        } else {
            logger.error("数据隧道异常",cause);
            //数据隧道
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
