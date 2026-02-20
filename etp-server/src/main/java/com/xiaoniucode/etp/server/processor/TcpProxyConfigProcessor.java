package com.xiaoniucode.etp.server.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.manager.PortListenerManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TcpProxyConfigProcessor implements ProxyConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(TcpProxyConfigProcessor.class);
    @Autowired
    private PortListenerManager portListenerManager;
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Autowired
    private VisitorSessionManager visitorSessionManager;
    @Autowired
    private ProxyManager proxyManager;

    public boolean supports(ProtocolType protocol) {
        return ProtocolType.isTcp(protocol);
    }

    @Override
    public void process(ProxyConfig config) {
        String clientId = proxyManager.getClientId(config.getProxyId());
        Integer remotePort = config.getRemotePort();
        ProxyStatus status = config.getStatus();
        if (status.isOpen()) {
            portListenerManager.bindPort(remotePort);
            agentSessionManager.addPortToAgentSession(clientId, remotePort);
            logger.debug("TCP隧道开启：remotePort={}", remotePort);
        } else if (status.isClosed()) {
            portListenerManager.stopPortListen(remotePort, false);
            visitorSessionManager.closeVisitorsByRemotePort(remotePort);
            agentSessionManager.removePortToAgentSession(clientId, remotePort);
            logger.debug("TCP隧道暂停：remotePort={}", remotePort);
        } else if (status.isDeleted()) {
            portListenerManager.stopPortListen(remotePort, true);
            visitorSessionManager.closeVisitorsByRemotePort(remotePort);
            agentSessionManager.removePortToAgentSession(clientId, remotePort);
            MetricsCollector.removeCollector(remotePort + "");
            logger.debug("TCP隧道删除：remotePort={}", remotePort);
        }
    }
}