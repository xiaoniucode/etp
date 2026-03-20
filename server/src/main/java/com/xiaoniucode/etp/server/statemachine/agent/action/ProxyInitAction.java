package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyInitAction extends AgentBaseAction{
    private final Logger logger= LoggerFactory.getLogger(ProxyInitAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AgentManager agentManager;
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        logger.debug("初始化客户端配置信息");
        //如果有代理配置信息则添加索引
        List<ProxyConfig> configs = proxyManager.findByClientId(context.getClientId());
        configs.forEach(config -> {
            agentManager.addProxyContextIndex(config.getProxyId(),context);
        });
    }
}
