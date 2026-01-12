package com.xiaoniucode.etp.core.msg;

public class NewProxyResp implements Message{
    private Integer proxyId;
    private String remoteAddr;
    private Long sessionId;

    public NewProxyResp(Integer proxyId, String remoteAddr) {
        this.proxyId = proxyId;
        this.remoteAddr = remoteAddr;
    }

    public NewProxyResp(Integer proxyId, String remoteAddr, Long sessionId) {
        this.proxyId = proxyId;
        this.remoteAddr = remoteAddr;
        this.sessionId = sessionId;
    }

    public Integer getProxyId() {
        return proxyId;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
