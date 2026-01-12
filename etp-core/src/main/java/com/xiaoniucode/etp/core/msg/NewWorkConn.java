package com.xiaoniucode.etp.core.msg;

import io.netty.buffer.ByteBuf;

public class NewWorkConn implements Message{
    private ByteBuf payload;
    private Long sessionId;
    public NewWorkConn(ByteBuf payload) {
        this.payload = payload;
    }
    public NewWorkConn(ByteBuf payload, Long sessionId) {
        this.payload = payload;
        this.sessionId = sessionId;
    }
    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public char getType() {
        return Message.TYPE_NEW_WORK_CONN;
    }
}
