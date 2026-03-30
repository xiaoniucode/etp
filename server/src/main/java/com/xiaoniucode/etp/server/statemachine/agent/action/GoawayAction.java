package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.transport.connection.DirectPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GoawayAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(GoawayAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private DirectPool directPool;
    @Autowired
    private MultiplexPool multiplexPool;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        String clientId = context.getAgentInfo().getAgentId();
        logger.debug("{} 客户端断开，开始清理资源", clientId);
        try {
            // 清理流资源
            // cleanupStreams(clientId);
            logger.debug("清理客户端 {} 所有连接", clientId);
            // 清理隧道资源
            directPool.offline(clientId);
            multiplexPool.offline(clientId);

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
