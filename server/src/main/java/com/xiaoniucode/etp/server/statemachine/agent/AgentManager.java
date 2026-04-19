package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.server.generator.ConnectionIdGenerator;
import com.xiaoniucode.etp.server.store.AgentStore;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private final Map<String, AgentContext> agentToContextIndex = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();
    @Autowired
    private AgentStore agentStore;
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

    public Optional<AgentContext> getAgentContext(String agentId) {
        return Optional.ofNullable(agentToContextIndex.get(agentId));
    }

    public Optional<AgentContext> getAgentContext(Integer connectionId) {
        return Optional.ofNullable(connToContext.get(connectionId));
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

    public void addClientContextIndex(String agentId, AgentContext context) {
        agentToContextIndex.put(agentId, context);
    }

    public int getOnlineCount() {
        return connToContext.size();
    }

    public void removeProxyContextIndex(String proxyId) {
        proxyToContextIndex.remove(proxyId);
    }

    public void removeAgentContext(String agentId) {
        writeLock.lock();
        try {
            AgentContext agentContext = agentToContextIndex.remove(agentId);
            if (agentContext != null) {
                Integer connectionId = agentContext.getConnectionId();
                connToContext.remove(connectionId);
                List<String> proxyIdsToRemove = new ArrayList<>();
                for (Map.Entry<String, AgentContext> entry : proxyToContextIndex.entrySet()) {
                    String proxyId = entry.getKey();
                    AgentContext value = entry.getValue();
                    if (value.getAgentInfo() != null && value.getAgentInfo().getAgentId().equals(agentId)) {
                        proxyIdsToRemove.add(proxyId);
                    }
                }
                for (String proxyId : proxyIdsToRemove) {
                    proxyToContextIndex.remove(proxyId);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isOnline(String agentId) {
        AgentContext agentContext = agentToContextIndex.get(agentId);
        if (agentContext == null) {
            return false;
        }
        AgentState state = agentContext.getState();
        return state == AgentState.CONNECTED;
    }

    public Optional<AgentInfo> getAgentInfo(String agentId) {
        return Optional.ofNullable(agentStore.findById(agentId));
    }

    public void kickout(String agentId) {
        AgentContext agentContext = agentToContextIndex.get(agentId);
        if (agentContext != null) {
            agentContext.fireEvent(AgentEvent.LOCAL_GOAWAY);
        }
    }

    public Collection<AgentContext> getAllAgentContext() {
        return agentToContextIndex.values();
    }

    public void save(AgentInfo agentInfo) {
        agentStore.save(agentInfo);
    }

    public Optional<AgentInfo> findById(String agentId) {
        return Optional.ofNullable(agentStore.findById(agentId));
    }
}
