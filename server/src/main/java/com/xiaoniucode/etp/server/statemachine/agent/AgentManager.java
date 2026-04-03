package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.server.generator.ConnectionIdGenerator;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentManager {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AgentManager.class);
    /**
     * connectionId
     */
    private final Map<Integer, AgentContext> connToContext = new ConcurrentHashMap<>();
    /**
     * proxyId --> agent context index
     */
    private final Map<String, AgentContext> proxyToContextIndex = new ConcurrentHashMap<>();
    /**
     * agentId --> context
     */
    private final Map<String, AgentContext> clientToContextIndex = new ConcurrentHashMap<>();

    @Autowired
    private ConnectionIdGenerator connectionIdGenerator;

    public Optional<AgentContext> getAgentContext(Channel control) {
        Integer connectionId = control.attr(AttributeKeys.CONNECTION_ID).get();
        if (connectionId == null) {
            return Optional.empty();
        }
        AgentContext agentContext = connToContext.get(connectionId);
        return Optional.ofNullable(agentContext);
    }

    public Optional<AgentContext> getAgentContext(int connectionId) {
        return Optional.ofNullable(connToContext.get(connectionId));
    }

    public Optional<AgentContext> getAgentContext(String clientId) {
        return Optional.ofNullable(clientToContextIndex.get(clientId));
    }

    public Optional<AgentContext> getAgentContextByProxyId(String proxyId) {
        if (!StringUtils.hasText(proxyId)) {
            throw new IllegalArgumentException("proxyId can not null");
        }
        return Optional.ofNullable(proxyToContextIndex.get(proxyId));
    }

    public void addProxyContextIndex(String proxyId, AgentContext context) {
        proxyToContextIndex.put(proxyId, context);
    }

    public AgentContext createAgent(Channel control, StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine) {
        int connectionId = connectionIdGenerator.nextConnId();
        AgentContext agentContext = new AgentContext(agentStateMachine);
        agentContext.setControl(control);
        control.attr(AttributeKeys.CONNECTION_ID).set(connectionId);
        agentContext.setConnectionId(connectionId);
        connToContext.put(connectionId, agentContext);
        return agentContext;
    }

    public void addClientContextIndex(String clientId, AgentContext context) {
        clientToContextIndex.put(clientId, context);
    }

    public int getOnlineCount() {
        return connToContext.size();
    }

    public void removeProxyContextIndex(String proxyId) {
        clientToContextIndex.remove(proxyId);
    }
   public void removeAgentContext(String agentId){

   }
    public Collection<AgentContext> getAllAgentContext() {
        return clientToContextIndex.values();
    }
}
