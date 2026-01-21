package com.xiaoniucode.etp.core.msg;

public class NewVisitorConn implements Message {
    private Long sessionId;
    private String localIP;
    private Integer localPort;

    public NewVisitorConn(Long sessionId, String localIP, Integer localPort) {
        this.sessionId = sessionId;
        this.localIP = localIP;
        this.localPort = localPort;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
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
