package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamConstants;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
            //********************Agent***********************//
            case TMSP.MSG_AUTH_RESP: {
                Message.AuthResponse authResponse = ProtobufUtil.parseFrom(frame.getPayload(), Message.AuthResponse.parser());
                int connectionId = authResponse.getConnectionId();
                int code = authResponse.getCode();
                if (code == 0) {
                    clientContext.setConnectionId(connectionId);
                    clientContext.setAuthenticated(true);
                    clientContext.fireEvent(AgentEvent.AUTH_SUCCESS);
                } else {
                    clientContext.setAuthenticated(false);
                    clientContext.fireEvent(AgentEvent.AUTH_FAILURE);
                }
                break;
            }
            case TMSP.MSG_PROXY_CREATE_RESP: {
                Message.NewProxyResp newProxyResp = ProtobufUtil.parseFrom(frame.getPayload(), Message.NewProxyResp.parser());
                clientContext.setVariable("NEW_PROXY_RESP", newProxyResp);
                clientContext.fireEvent(AgentEvent.PROXY_CREATE_RESP);
                break;
            }
            case TMSP.MSG_GOAWAY: {
                logger.debug("收到停止信号，准备停止客户端");
                clientContext.fireEvent(AgentEvent.STOP);
                break;

            }
            case TMSP.MSG_ERROR: {
                Message.Error error = ProtobufUtil.parseFrom(frame.getPayload(), Message.Error.parser());
                clientContext.setVariable("ERROR", error);
                clientContext.fireEvent(AgentEvent.ERROR);
                break;
            }
            //********************Tunnel***********************//
            case TMSP.MSG_TUNNEL_CREATE_RESP: {
                ByteBuf payload = frame.getPayload();
                Message.TunnelCreateResponse resp = ProtobufUtil.parseFrom(payload, Message.TunnelCreateResponse.parser());
                String tunnelId = resp.getTunnelId();
                clientContext.getControl().eventLoop().execute(() -> {
                    clientContext.setVariable("tunnelId", tunnelId);
                    clientContext.setVariable("compress", frame.isCompressed());
                    clientContext.setVariable("encrypt", frame.isEncrypted());
                    clientContext.setVariable("multiplex", frame.isMuxTunnel());
                    clientContext.setVariable("tunnel_create_response", resp);
                    clientContext.fireEvent(AgentEvent.CREATE_TUNNEL_POOL_RESP);
                });

                break;
            }

            //********************Stream***********************//
            case TMSP.MSG_STREAM_OPEN: {
                NewStreamCodec.NewStreamInfo visitorInfo = NewStreamCodec.decode(frame.getPayload());
                StreamContext streamContext = StreamManager.createStreamContext(frame.getStreamId(), clientContext);
                streamContext.setVariable(StreamConstants.VISIT_INFO, visitorInfo);
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.setMultiplex(frame.isMuxTunnel());

                streamContext.fireEvent(StreamEvent.STREAM_OPEN);
                break;
            }
            case TMSP.MSG_STREAM_DATA: {
                int streamId = frame.getStreamId();
                StreamManager.getStreamContext(streamId).ifPresent(streamContext -> {
                    streamContext.forwardToLocal(frame.getPayload());
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
            clientContext.fireEvent(AgentEvent.RETRY);
        } else {
            logger.error("数据隧道断开：channel-{}", ctx.channel().id());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        //控制隧道
        if (channel == clientContext.getControl()) {
            if (isNetworkException(cause)) {
                if (clientContext.getControl() == ctx.channel()) {
                    logger.error("控制隧道网络错误", cause);
                    clientContext.fireEvent(AgentEvent.NETWORK_ERROR);
                }
            }
        } else {
            logger.error("数据连接异常，关闭数据连接", cause);
            //数据隧道
            ChannelUtils.closeOnFlush(channel);
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

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.warn("隧道可写性发生变化：{}", ctx.channel().isWritable());
        Channel tunnel = ctx.channel();
        if (tunnel == clientContext.getControl()) {
            logger.warn("控制隧道可写性发生变化，暂不处理");
            return;
        }
        boolean writable = tunnel.isWritable();
        if (writable) {
            //数据隧道恢复可写，恢复暂停的从服务器读取
            IntSet pausedStreamIds = StreamManager.getPausedStreamIds(tunnel);
            if (!pausedStreamIds.isEmpty()) {
                logger.debug("控制隧道恢复可写，恢复 {} 个访问者读取", pausedStreamIds.size());
                pausedStreamIds.stream().forEach(streamId -> {
                    Optional<StreamContext> streamContextOpt = StreamManager.getStreamContext(streamId);
                    if (streamContextOpt.isPresent()) {
                        StreamContext streamContext = streamContextOpt.get();
                        Channel server = streamContext.getServer();
                        if (server != null) {
                            ctx.executor().schedule(() -> {
                                server.config().setOption(ChannelOption.AUTO_READ, true);
                                server.read();
                                StreamManager.removePausedStream(tunnel, streamId);
                            }, 5, TimeUnit.MILLISECONDS);
                        }
                    }
                });
            }
        }
        super.channelWritabilityChanged(ctx);
    }
}



