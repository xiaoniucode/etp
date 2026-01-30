package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.LanInfo;
import io.netty.channel.Channel;

public class HttpVisitorPair {
    private Channel control;
    private LanInfo lanInfo;
    private String domain;
    private Long sessionId;

    public HttpVisitorPair(Channel control, long sessionId, String domain,LanInfo lanInfo) {
        this.control = control;
        this.sessionId = sessionId;
        this.domain = domain;
        this.lanInfo = lanInfo;

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
