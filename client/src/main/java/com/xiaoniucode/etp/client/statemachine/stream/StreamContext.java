package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import jdk.javadoc.doclet.Taglet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Getter
@Setter
public class StreamContext extends ProcessContextImpl {
    private final Logger logger= LoggerFactory.getLogger(StreamContext.class);
    private StreamState state = StreamState.INITIALIZED;
    private int streamId;
    private Channel control;
    private Channel tunnel;
    private Channel server;
    private String localIp;
    private int localPort;
    private boolean compress;
    private boolean encrypt;
    private boolean isMuxTunnel;
    private AgentContext agentContext;
    private StreamManager streamManager;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    public StreamContext(Integer streamId, StateMachine<StreamState, StreamEvent, StreamContext> stateMachine, AgentContext agentContext) {
        this.streamId = streamId;
        this.stateMachine = stateMachine;
        this.agentContext = agentContext;
    }

    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

    public void relayToServer(ByteBuf payload) {
        if (server == null || !server.isActive()) {
            payload.release();
            return;
        }
        server.writeAndFlush(payload);
    }

    public void relayToTunnel(ByteBuf payload) {
        if (tunnel == null || !tunnel.isActive()) {
            payload.release();
            logger.debug("隧道未激活，丢弃数据 streamId={}", streamId);
            return;
        }
        if (!tunnel.isWritable()) {
            logger.debug("tunnel 写缓冲区满，暂停真实服务读并丢弃当前包 streamId={}", streamId);
            payload.release();
            return;
        }
        TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_DATA, payload);
        tunnel.writeAndFlush(frame).addListener(future -> {
            if (!future.isSuccess()) {
                logger.warn("转发到隧道失败 streamId={}", streamId);
            }
        });
    }
}

