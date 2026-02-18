package com.xiaoniucode.etp.server.proxy.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
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
        String clientId = proxyManager.getClientId(proxyConfig.getProxyId());
        ProxyStatus status = proxyConfig.getStatus();
        Set<String> domains = proxyConfig.getFullDomains();
        if (status.isOpen()) {
            agentSessionManager.addDomainsToAgentSession(clientId, domains);
            logger.debug("HTTP隧道开启：{}", domains);
        } else if (status.isClosed()) {
            agentSessionManager.removeDomainsToAgentSession(clientId, domains);
            visitorSessionManager.closeVisitorsByDomains(domains);
            logger.debug("HTTP隧道关闭：{}", domains);
        } else if (status.isDeleted()) {
            agentSessionManager.removeDomainsToAgentSession(clientId, domains);
            visitorSessionManager.closeVisitorsByDomains(domains);
            MetricsCollector.removeCollectors(domains);
            logger.debug("HTTP隧道删除：{}", domains);
        }
    }
}