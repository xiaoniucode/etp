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
package com.xiaoniucode.etp.server.transport;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.statemachine.agent.command.ConnCreateCmd;
import com.xiaoniucode.etp.server.statemachine.stream.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 控制隧道消息处理器
 *
 * @author xiaoniucode
 */
@Component
@ChannelHandler.Sharable
public class ControlFrameHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ControlFrameHandler.class);
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
            Optional<AgentContext> opt = agentManager.getAgentContext(ctx.channel());
            opt.ifPresent(context -> {
                if (context.getState() != AgentState.CONNECTED) {
                    return;
                }
                context.updateActiveTime();
                context.getMissedHeartbeats().set(0);
                logger.debug("更新客户端 {} 最后激活时间", context.getAgentInfo().getAgentId());
            });
            switch (msgType) {
                case TMSP.MSG_AUTH -> {
                    ByteBuf payload = frame.getPayload();
                    Message.AuthInfo authInfo = ProtobufUtil.parseFrom(payload, Message.AuthInfo.parser());

                    String agentId = authInfo.getAgentId();
                    Optional<AgentContext> contextOpt = agentManager.getAgentContext(ctx.channel());
                    if (contextOpt.isPresent()) {
                        AgentContext context = contextOpt.get();
                        //如果连接是断开状态，说明是断线重连，更新连接并重试连接
                        if (context.getState() == AgentState.DISCONNECTED) {
                            logger.debug("断线重连：{}",context.getAgentId());
                            Channel oldChannel = context.getControl();
                            ChannelUtils.closeOnFlush(oldChannel);
                            // 再设置新连接
                            context.setControl(ctx.channel());
                            context.setVariable(AgentConstants.AGENT_AUTH_INFO, authInfo);
                            context.fireEvent(AgentEvent.RETRY_CONNECT);
                        } else {
                            //重复登录，断开旧连接，设置新连接
                            ChannelUtils.closeOnFlush(context.getControl());
                            context.setControl(ctx.channel());
                            logger.debug("客户端 {} 重新登录", agentId);
                        }
                    } else {
                        AgentContext agentContext = agentManager.createAgent(ctx.channel(), agentStateMachine);
                        agentContext.setVariable(AgentConstants.AGENT_AUTH_INFO, authInfo);
                        agentContext.fireEvent(AgentEvent.AUTH_START);
                    }
                }

                case TMSP.MSG_TUNNEL_CREATE -> {
                    logger.debug("收到连接池创建消息");
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
                        ConnCreateCmd cmd = new ConnCreateCmd(tunnel, frame.isEncrypted(), frame.isMuxTunnel(), req.getTunnelId());
                        control.eventLoop().execute(() -> {
                            agentContext.setVariable("tunnelCreateCmd", cmd);
                            agentContext.fireEvent(AgentEvent.CREATE_TUNNEL);
                        });
                    } else {
                        ChannelUtils.closeOnFlush(ctx.channel());
                    }
                }
                case TMSP.MSG_PING -> {
                    logger.debug("收到来自客户端PING消息");
                    Optional<AgentContext> ag = agentManager.getAgentContext(ctx.channel());
                    if (ag.isPresent()) {
                        AgentContext agentContext = ag.get();
                        TMSPFrame pong = new TMSPFrame(0, TMSP.MSG_PONG);
                        Channel control = agentContext.getControl();
                        control.writeAndFlush(pong);
                        logger.debug("回复客户端 {} PONG 消息",agentContext.getAgentId());
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
                    if (streamContext != null) {
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
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
            logger.debug("与客户端断开连接");
            agentContext.fireEvent(AgentEvent.DISCONNECT);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        agentManager.getAgentContext(ctx.channel()).ifPresent(agentContext -> {
            logger.error("控制连接异常: ", cause);
        });
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
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
