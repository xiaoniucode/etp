package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.constant.AttributeKeys;
import com.xiaoniucode.etp.server.configuration.AgentStateMachineHolder;
import com.xiaoniucode.etp.server.generator.ConnectionIdGenerator;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentManager {
    private final Logger logger = LoggerFactory.getLogger(AgentManager.class);
    /**
     * connectionId
     */
    private final Map<Integer, AgentContext> channelToContext = new ConcurrentHashMap<>();
    /**
     * proxyId
     */
    private final Map<String, AgentContext> proxyIdToContext = new ConcurrentHashMap<>();

    @Autowired
    private ConnectionIdGenerator connectionIdGenerator;

    public Optional<AgentContext> getAgentContext(Channel control) {
        Integer connectionId = control.attr(AttributeKeys.CONNECTION_ID).get();
        if (connectionId == null) {
            return Optional.empty();
        }
        AgentContext agentContext = channelToContext.get(connectionId);
        return Optional.ofNullable(agentContext);
    }

    public Optional<AgentContext> getAgentContextByProxyId(String proxyId) {
        return Optional.ofNullable(proxyIdToContext.get(proxyId));
    }

    public void addProxyContextIndex(String proxyId, AgentContext context) {
        proxyIdToContext.put(proxyId, context);
    }

    public AgentContext createAgent(Channel control) {
        int connectionId = connectionIdGenerator.nextConnId();

        StateMachine<AgentState, AgentEvent, AgentContext> stateMachine = AgentStateMachineHolder.get(connectionId);

        AgentContext AgentContext = new AgentContext(this, stateMachine);
        AgentContext.setControl(control);
        control.attr(AttributeKeys.CONNECTION_ID).set(connectionId);
        AgentContext.setConnectionId(connectionId);
        channelToContext.put(connectionId, AgentContext);
        return AgentContext;
    }

    public int getOnlineCount() {
        return channelToContext.size();
    }
}
