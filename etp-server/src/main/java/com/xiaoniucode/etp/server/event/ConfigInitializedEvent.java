package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.event.EventObject;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;

public class ConfigInitializedEvent extends EventObject {
    private final TcpProxyServer tcpProxyServer;

    public ConfigInitializedEvent(TcpProxyServer tcpProxyServer) {
        this.tcpProxyServer = tcpProxyServer;
    }

    public TcpProxyServer getTcpProxyServer() {
        return tcpProxyServer;
    }
}
