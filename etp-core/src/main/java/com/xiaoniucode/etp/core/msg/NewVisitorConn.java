package com.xiaoniucode.etp.core.msg;

public class NewVisitorConn implements Message{
    private Long sessionId;
    private Integer localPort;

    public NewVisitorConn(Long sessionId, Integer localPort) {
        this.sessionId = sessionId;
        this.localPort = localPort;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    @Override
    public byte getType() {
        return Message.TYPE_NEW_VISITOR_CONN;
    }
}
