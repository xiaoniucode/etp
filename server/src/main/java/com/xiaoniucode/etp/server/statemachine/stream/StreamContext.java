package com.xiaoniucode.etp.server.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import com.xiaoniucode.etp.core.netty.NettyBatchWriteQueue;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import com.xiaoniucode.etp.server.transport.bridge.TunnelBridge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class StreamContext extends ProcessContextImpl {
    private final Logger logger = LoggerFactory.getLogger(StreamContext.class);
    private int streamId;
    private AgentContext agentContext;
    private Channel tunnel;
    private Channel visitor;
    private ProxyConfig proxyConfig;
    private String sourceAddress;
    private Target currentTarget;
    private StreamState state = StreamState.IDLE;
    private ProtocolType currentProtocol = ProtocolType.TCP;
    private boolean compress;
    private boolean encrypt;
    private boolean mux;
    private BandwidthLimiter bandwidthLimiter;
    private NettyBatchWriteQueue writeQueue;
    private TunnelBridge tunnelBridge;
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    public StreamContext(int streamId, StateMachine<StreamState, StreamEvent, StreamContext> streamStateMachine) {
        this.streamId = streamId;
        this.stateMachine = streamStateMachine;
    }

    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

    public void relayToVisitor(ByteBuf payload) {
        tunnelBridge.relayToVisitor(payload);
    }

    public void relayToTunnel(ByteBuf payload) {
        tunnelBridge.relayToTunnel(payload);
    }
}