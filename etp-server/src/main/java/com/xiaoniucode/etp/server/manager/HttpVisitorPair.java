package com.xiaoniucode.etp.server.manager;

import io.netty.channel.Channel;

public class HttpVisitorPair {
    private Channel control;
    private String localIP;
    private Integer localPort;
    private String domain;
    private Long sessionId;

    public HttpVisitorPair(Channel control, long sessionId, String domain,String localIP, Integer localPort) {
        this.control = control;
        this.sessionId = sessionId;
        this.domain = domain;
        this.localIP = localIP;
        this.localPort = localPort;
    }

    public Channel getControl() {
        return control;
    }

    public void setControl(Channel control) {
        this.control = control;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
