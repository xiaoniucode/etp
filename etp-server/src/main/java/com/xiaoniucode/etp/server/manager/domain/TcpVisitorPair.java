package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.domain.LanInfo;
import io.netty.channel.Channel;

public class TcpVisitorPair {
    private Channel control;
    private LanInfo lanInfo;
    private Integer remotePort;
    private Long sessionId;

    public TcpVisitorPair(Channel control, LanInfo lanInfo, Integer remotePort, Long sessionId) {
        this.control = control;
        this.lanInfo = lanInfo;
        this.remotePort = remotePort;
        this.sessionId = sessionId;
    }

    public Channel getControl() {
        return control;
    }

    public void setControl(Channel control) {
        this.control = control;
    }

    public LanInfo getLanInfo() {
        return lanInfo;
    }

    public void setLanInfo(LanInfo lanInfo) {
        this.lanInfo = lanInfo;
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
