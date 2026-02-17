package com.xiaoniucode.etp.server.proxy.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HttpProxyConfigProcessor implements ProxyConfigProcessor {
    @Autowired
    private  AgentSessionManager agentSessionManager;
    
    @Override
    public boolean supports(ProtocolType protocol) {
        return ProtocolType.isHttp(protocol);
    }
    
    @Override
    public void process(ProxyConfig proxyConfig) {
        Set<String> domains = proxyConfig.getFullDomains();
        agentSessionManager.addDomainsToAgentSession(domains);
    }
}