package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.client.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelCreateRespAction extends AgentBaseAction {
    private static final Logger logger = LoggerFactory.getLogger(TunnelCreateRespAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext agentContext) {
        logger.debug("创建隧道成功");
        boolean encrypt = agentContext.getAndRemoveAs("encrypt", Boolean.class);
        boolean multiplex = agentContext.getAndRemoveAs("multiplex", Boolean.class);
        Message.TunnelCreateResponse resp = agentContext.getAndRemoveAs("tunnel_create_response", Message.TunnelCreateResponse.class);
        if (resp.getCode() == 1) {
            logger.info("隧道创建失败: {}", resp.getMessage());
            removeCreateFailConn(agentContext, multiplex, resp.getTunnelId());
            return;
        }
        TunnelEntry tunnelEntry;
        if (multiplex) {
            MultiplexPool multiplexPool = agentContext.getMultiplexPool();
            tunnelEntry = multiplexPool.activeTunnel(encrypt);
            logger.debug("激活共享隧道: tunnelId={} 激活状态：{} 是否加密：{}", tunnelEntry.getTunnelId(), tunnelEntry.isActive(),encrypt);
        } else {
            DirectPool directPool = agentContext.getDirectPool();
            tunnelEntry = directPool.activateTunnel(resp.getTunnelId());
            logger.debug("激活独立隧道: tunnelId={} 激活状态：{} 是否加密：{}", tunnelEntry.getTunnelId(), tunnelEntry.isActive(),encrypt);
        }
    }

    private void removeCreateFailConn(AgentContext agentContext, boolean multiplex, String tunnelId) {
        if (multiplex) {
            MultiplexPool multiplexPool = agentContext.getMultiplexPool();
            multiplexPool.clearTunnel(true);
            return;
        }
        DirectPool directPool = agentContext.getDirectPool();
        directPool.removeTunnel(tunnelId);
    }
}
