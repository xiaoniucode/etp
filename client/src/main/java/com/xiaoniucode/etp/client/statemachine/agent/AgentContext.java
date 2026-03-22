package com.xiaoniucode.etp.client.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.core.transport.AbstractAgentContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentContext extends AbstractAgentContext {
    private AgentState state = AgentState.IDLE;
    private AppConfig config;
    private SslContext tlsContext;
    private Bootstrap controlBootstrap;
    private Bootstrap serverBootstrap;
    private EventLoopGroup controlWorkerGroup;
    private EventLoopGroup serverWorkerGroup;
    private int retryCount;
    private boolean authenticated;
    private TunnelClient tunnelClient;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;

    public AgentContext(AppConfig config) {
        this.config = config;
    }

    public void fireEvent(AgentEvent clientEvent) {
        stateMachine.fireEvent(state, clientEvent, this);
    }
}
