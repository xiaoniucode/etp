package com.xiaoniucode.etp.server.proxy.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HttpProxyConfigProcessor implements ProxyConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyConfigProcessor.class);
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    public boolean supports(ProtocolType protocol) {
        return ProtocolType.isHttp(protocol);
    }

    @Override
    public void process(ProxyConfig proxyConfig) {
        logger.debug("开始处理 HTTP 协议代理配置");
        String clientId = proxyManager.getClientId(proxyConfig.getProxyId());
        ProxyStatus status = proxyConfig.getStatus();
        Set<String> domains = proxyConfig.getFullDomains();
        if (status.isOpen()) {
            agentSessionManager.addDomainsToAgentSession(clientId, domains);
        } else if (status.isClosed() || status.isDeleted()) {
            agentSessionManager.removeDomainsToAgentSession(clientId, domains);
            visitorSessionManager.closeVisitorsByDomains(domains);
        }
    }
}