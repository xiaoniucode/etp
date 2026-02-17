package com.xiaoniucode.etp.server.proxy.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.manager.PortListenerManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TcpProxyConfigProcessor implements ProxyConfigProcessor {
    @Autowired
    private PortListenerManager portListenerManager;
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Override
    public boolean supports(ProtocolType protocol) {
        return ProtocolType.isTcp(protocol);
    }

    @Override
    public void process(ProxyConfig proxyConfig) {
        Integer remotePort = proxyConfig.getRemotePort();
        portListenerManager.bindPort(remotePort);
        agentSessionManager.addPortToAgentSession(remotePort);
    }
}