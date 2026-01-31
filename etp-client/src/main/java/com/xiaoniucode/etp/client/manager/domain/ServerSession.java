package com.xiaoniucode.etp.client.manager.domain;

import com.xiaoniucode.etp.core.LanInfo;
import io.netty.channel.Channel;

public class ServerSession {
    private String sessionId;
    private Channel tunnel;
    private Channel server;
    private LanInfo lanInfo;
    private AgentSession agentSession;

    public ServerSession(String sessionId, Channel tunnel, Channel server,LanInfo lanInfo, AgentSession agentSession) {
        this.sessionId = sessionId;
        this.tunnel = tunnel;
        this.server = server;
        this.lanInfo=lanInfo;
        this.agentSession = agentSession;
    }

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

    public LanInfo getLanInfo() {
        return lanInfo;
    }

    public void setLanInfo(LanInfo lanInfo) {
        this.lanInfo = lanInfo;
    }

    public AgentSession getAgentSession() {
        return agentSession;
    }

    public void setAgentSession(AgentSession agentSession) {
        this.agentSession = agentSession;
    }
}
