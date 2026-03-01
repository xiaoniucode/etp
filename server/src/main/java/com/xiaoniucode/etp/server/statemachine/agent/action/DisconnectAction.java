package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 连接断开，清理与该客户端有关的所有资源、会话、连接池等
 */
@Component
public class DisconnectAction extends AgentBaseAction {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private TunnelManager tunnelManager;
    @Autowired
    private StreamManager streamManager;
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

    }
}
