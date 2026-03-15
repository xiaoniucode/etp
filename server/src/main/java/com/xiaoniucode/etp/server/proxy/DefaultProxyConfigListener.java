package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.domain.DomainConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultProxyConfigListener implements ProxyConfigListener {
    @Autowired
    private PortAcceptor portAcceptor;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private PortManager portManager;

    @Override
    public void onAdded(ProxyConfig config) {
        agentManager.getAgentContext(config.getClientId()).ifPresent(agentContext -> {
            agentManager.addProxyContextIndex(config.getProxyId(), agentContext);
        });
        if (config.isTcp()) {
            Integer remotePort = config.getRemotePort();
            portAcceptor.bindPort(remotePort);
        } else if (config.isHttp()) {
            // agentSessionManager.addDomainsToAgentSession(clientId, domains);
        }
    }

    @Override
    public void onUpdated(ProxyConfig oldConfig, ProxyConfig newConfig) {
        if (newConfig.isTcp()) {

        } else if (newConfig.isHttp()) {
            // agentSessionManager.removeDomainsToAgentSession(clientId, domains);
            //visitorSessionManager.closeStreamsByDomains(domains);
        }
    }

    @Override
    public void onDeleted(ProxyConfig config) {
        if (config.isTcp()) {
            Integer remotePort = config.getRemotePort();
            MetricsCollector.removeCollector(remotePort + "");
            portAcceptor.stopPortListen(remotePort);
            streamManager.closeStreams(remotePort);
        } else if (config.isHttp()) {
            //MetricsCollector.removeCollectors(domains);
            DomainConfig domainInfo = config.getDomainInfo();
            Set<String> fullDomains = domainInfo.getFullDomains();
            streamManager.closeStreams(fullDomains);
        }

        agentManager.getAgentContext(config.getClientId()).ifPresent(agentContext -> {
            agentManager.removeProxyContextIndex(config.getProxyId());
        });

    }

    @Override
    public void onStatusChanged(ProxyConfig config, boolean newStatus) {
        if (config.isTcp()) {
            Integer remotePort = config.getRemotePort();
            portAcceptor.stopPortListen(remotePort);

        } else if (config.isHttp()) {

        }
    }
}