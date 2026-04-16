package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.core.transport.AbstractStreamContext;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamContext extends AbstractStreamContext {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(StreamContext.class);
    private StreamState state = StreamState.IDLE;
    private Channel server;
    private String localIp;
    private int localPort;
    private StreamManager streamManager;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    public StreamContext(Integer streamId, StateMachine<StreamState, StreamEvent, StreamContext> stateMachine, AgentContext agentContext) {
        this.streamId = streamId;
        this.stateMachine = stateMachine;
        this.agentContext = agentContext;
    }

    public Channel getControl() {
        return agentContext.getControl();
    }

    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

}

