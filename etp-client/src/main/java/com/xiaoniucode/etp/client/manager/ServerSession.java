package com.xiaoniucode.etp.client.manager;

import io.netty.channel.Channel;

public class ServerSession {
    private String sessionId;
    private Channel tunnel;
    private Channel server;
    private AgentSession agentSession;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Channel getTunnel() {
        return tunnel;
    }

    public void setTunnel(Channel tunnel) {
        this.tunnel = tunnel;
    }

    public Channel getServer() {
        return server;
    }

    public void setServer(Channel server) {
        this.server = server;
    }

    public AgentSession getAgentSession() {
        return agentSession;
    }

    public void setAgentSession(AgentSession agentSession) {
        this.agentSession = agentSession;
    }
}
