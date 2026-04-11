package com.xiaoniucode.etp.server.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.transport.AbstractStreamContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import io.netty.channel.Channel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class StreamContext extends AbstractStreamContext {
    private StreamState state = StreamState.IDLE;
    private Channel visitor;
    private ProxyConfig proxyConfig;
    private String sourceAddress;
    private Target currentTarget;
    private ProtocolType currentProtocol = ProtocolType.TCP;
    private BandwidthLimiter bandwidthLimiter;
    private AgentContext agentContext;
    private StreamManager streamManager;
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    public StreamContext(int streamId, StateMachine<StreamState, StreamEvent, StreamContext> streamStateMachine) {
        this.streamId = streamId;
        this.stateMachine = streamStateMachine;
    }

    public String getProxyId() {
        return proxyConfig.getProxyId();
    }


    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }
}