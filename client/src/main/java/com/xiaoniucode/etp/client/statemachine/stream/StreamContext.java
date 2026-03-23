package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.core.transport.AbstractStreamContext;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class StreamContext extends AbstractStreamContext {
    private final Logger logger = LoggerFactory.getLogger(StreamContext.class);
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

    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }
}

