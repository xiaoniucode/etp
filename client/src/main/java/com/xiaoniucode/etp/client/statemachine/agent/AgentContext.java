package com.xiaoniucode.etp.client.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentContext extends ProcessContextImpl {
    private Integer connectionId;
    private AgentState state;
    private AppConfig config;
    private boolean configValid;
    private boolean sslInitialized;
    private SslContext tlsContext;
    private Bootstrap controlBootstrap;
    private Bootstrap serverBootstrap;
    private EventLoopGroup controlWorkerGroup;
    private EventLoopGroup serverWorkerGroup;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;
    private int retryCount;
    private boolean stopped;
    private boolean authenticated;
    private Channel control;
    public AgentContext(AppConfig config) {
        this.config = config;
    }

    public void fireEvent(AgentEvent clientEvent) {
        stateMachine.fireEvent(state, clientEvent, this);
    }

}
