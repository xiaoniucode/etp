package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.service.his.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyInitAction extends AgentBaseAction{
    private final InternalLogger logger= InternalLoggerFactory.getInstance(ProxyInitAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AgentManager agentManager;
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        logger.debug("初始化客户端配置信息");
        //todo 如果有代理配置信息则添加索引
        List<String> proxyIds = proxyManager.findProxyIdsByAgentId(context.getAgentInfo().getAgentId());
        proxyIds.forEach(proxyId-> {
            agentManager.addProxyContextIndex(proxyId,context);
        });
    }
}
