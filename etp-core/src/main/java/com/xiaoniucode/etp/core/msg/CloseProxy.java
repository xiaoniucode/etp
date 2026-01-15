package com.xiaoniucode.etp.core.msg;

public class CloseProxy implements Message{
    private Long sessionId;
    private Integer proxyId;
    public CloseProxy(Long sessionId) {
        this.sessionId = sessionId;
    }

    public CloseProxy(Long sessionId, Integer proxyId) {
        this.sessionId = sessionId;
        this.proxyId = proxyId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getProxyId() {
        return proxyId;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }

    @Override
    public byte getType() {
        return Message.TYPE_CLOSE_PROXY;
    }
}
