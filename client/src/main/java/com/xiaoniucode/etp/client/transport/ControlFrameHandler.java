/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author xiaoniucode
 */
@ChannelHandler.Sharable
public class ControlFrameHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ControlFrameHandler.class);
    private final AgentContext agentContext;

    public ControlFrameHandler(AgentContext agentContext) {
        this.agentContext = agentContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) {
        byte msgType = frame.getMsgType();
        agentContext.updateActiveTime();
        agentContext.getMissedHeartbeats().set(0);
        switch (msgType) {
            //********************Agent***********************//
            case TMSP.MSG_AUTH_RESP: {
                Message.AuthResponse authResponse = ProtobufUtil.parseFrom(frame.getPayload(), Message.AuthResponse.parser());
                agentContext.setVariable("authResponse", authResponse);
                agentContext.fireEvent(AgentEvent.AUTH_RESPONSE);
                break;
            }
            case TMSP.MSG_PROXY_CREATE_RESP: {
                Message.NewProxyResp newProxyResp = ProtobufUtil.parseFrom(frame.getPayload(), Message.NewProxyResp.parser());
                agentContext.setVariable("NEW_PROXY_RESP", newProxyResp);
                agentContext.fireEvent(AgentEvent.PROXY_CREATE_RESP);
                break;
            }
            case TMSP.MSG_GOAWAY: {
                logger.debug("收到停止信号，准备停止客户端");
                agentContext.fireEvent(AgentEvent.STOP);
                break;
            }
            case TMSP.MSG_ERROR: {
                Message.Error error = ProtobufUtil.parseFrom(frame.getPayload(), Message.Error.parser());
                agentContext.setVariable("ERROR", error);
                agentContext.fireEvent(AgentEvent.ERROR);
                break;
            }
            case TMSP.MSG_PONG: {
                logger.debug("收到来自服务端的 PONG 消息");
                break;
            }
            //********************Tunnel***********************//
            case TMSP.MSG_TUNNEL_CREATE_RESP: {
                ByteBuf payload = frame.getPayload();
                Message.TunnelCreateResponse resp = ProtobufUtil.parseFrom(payload, Message.TunnelCreateResponse.parser());
                String tunnelId = resp.getTunnelId();
                agentContext.getControl().eventLoop().execute(() -> {
                    agentContext.setVariable("tunnelId", tunnelId);
                    agentContext.setVariable("compress", frame.isCompressed());
                    agentContext.setVariable("encrypt", frame.isEncrypted());
                    agentContext.setVariable("multiplex", frame.isMuxTunnel());
                    agentContext.setVariable("tunnel_create_response", resp);
                    agentContext.fireEvent(AgentEvent.CREATE_TUNNEL_POOL_RESP);
                });

                break;
            }

            //********************Stream***********************//
            case TMSP.MSG_STREAM_OPEN: {
                NewStreamCodec.NewStreamInfo visitorInfo = NewStreamCodec.decode(frame.getPayload());
                StreamContext streamContext = StreamManager.createStreamContext(frame.getStreamId(), agentContext);
                streamContext.setVariable(StreamConstants.VISIT_INFO, visitorInfo);
                streamContext.setCompress(frame.isCompressed());
                streamContext.setEncrypt(frame.isEncrypted());
                streamContext.setMultiplex(frame.isMuxTunnel());
                streamContext.setAgentContext(agentContext);
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
        //如果是控制连接断开，需要进行重连
        if (agentContext.getControl() == ctx.channel()) {
            logger.warn("与服务器断开连接");
            agentContext.fireEvent(AgentEvent.DISCONNECT);
        } else {
            logger.error("数据连接断开：channel-{}", ctx.channel().id());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        //控制隧道
        if (channel == agentContext.getControl()) {
            if (isNetworkException(cause)) {
                if (agentContext.getControl() == ctx.channel()) {
                    logger.error("控制隧道网络错误", cause);
                    agentContext.fireEvent(AgentEvent.NETWORK_ERROR);
                }
            }
        } else {
            logger.error("数据连接异常，关闭数据连接", cause);
            //数据隧道
            // ChannelUtils.closeOnFlush(channel);
        }
        logger.error(cause.getMessage(), cause);
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
        logger.debug("隧道可写性发生变化：{}", ctx.channel().isWritable());
        Channel tunnel = ctx.channel();
        if (tunnel == agentContext.getControl()) {
            logger.debug("控制隧道可写性发生变化，暂不处理");
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



