package com.xiaoniucode.etp.core.msg;

public class NewVisitorConnResp implements Message{
    private String secretKey;
    private Long sessionId;

    public NewVisitorConnResp(String secretKey, Long sessionId) {
        this.secretKey = secretKey;
        this.sessionId = sessionId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
