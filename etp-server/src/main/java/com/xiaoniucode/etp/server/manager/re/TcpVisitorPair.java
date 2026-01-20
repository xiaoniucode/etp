package com.xiaoniucode.etp.server.manager.re;

import io.netty.channel.Channel;

public class TcpVisitorPair {
    private Channel control;
    private Integer localPort;
    private Integer remotePort;
    private Long sessionId;

    public TcpVisitorPair() {
    }

    public TcpVisitorPair(Channel control, Integer localPort, Integer remotePort, Long sessionId) {
        this.control = control;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.sessionId = sessionId;
    }

    public Channel getControl() {
        return control;
    }

    public void setControl(Channel control) {
        this.control = control;
    }
    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
