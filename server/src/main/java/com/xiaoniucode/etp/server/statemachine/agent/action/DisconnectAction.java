package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 连接断开，清理与该客户端有关的所有资源、会话、连接池等
 */
@Component
public class DisconnectAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(DisconnectAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private TunnelManager tunnelManager;
    @Autowired
    private StreamManager streamManager;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        String clientId = context.getClientId();
        logger.debug("{} 客户端断开，开始清理资源", clientId);

        try {
            // 清理流资源
            // cleanupStreams(clientId);

            // 清理隧道资源
            //cleanupTunnels(clientId);

            // 清理代理资源
            //cleanupAgent(clientId);

            // 清理关联的 Channel
            //cleanupChannels(context);

            // 清理Context 中的临时数据
            //cleanupContextData(context);
            logger.info("{} 客户端资源清理完成", clientId);
        } catch (Exception e) {
            logger.error("{} 资源清理过程中发生异常", clientId, e);
        } finally {

        }

    }


}
