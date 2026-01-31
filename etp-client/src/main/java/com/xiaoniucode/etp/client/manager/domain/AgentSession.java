package com.xiaoniucode.etp.client.manager.domain;

import io.netty.channel.Channel;

public class AgentSession {
    private String sessionId;
    private String clientId;
    private String token;
    private Channel control;
    private String serverAddr;
    private Integer serverPort;

    public AgentSession(String clientId, String sessionId, Channel control) {
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.control = control;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Channel getControl() {
        return control;
    }

    public void setControl(Channel control) {
        this.control = control;
    }
    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
