package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoawayAction extends AgentBaseAction{
    private final Logger logger= LoggerFactory.getLogger(GoawayAction.class);
    @Autowired
    private AgentManager agentManager;
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        logger.debug("客户端连接断开");
        //代理客户端连接断开，清理资源

    }
}
