package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
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
    @Autowired
    private StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine;

    public Optional<AgentContext> getAgentContext(Channel control) {
        Integer connectionId = control.attr(ChannelConstants.CONNECTION_ID).get();
        if (connectionId==null){
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
        AgentContext AgentContext = new AgentContext(this,agentStateMachine);
        //创建一个唯一 ID
        int connectionId = connectionIdGenerator.nextConnId();
        AgentContext.setControl(control);
        control.attr(ChannelConstants.CONNECTION_ID).set(connectionId);
        AgentContext.setConnectionId(connectionId);
        channelToContext.put(connectionId, AgentContext);
        return AgentContext;
    }


    public int getOnlineCount() {
        return channelToContext.size();
    }
}
