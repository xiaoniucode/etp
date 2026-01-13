package com.xiaoniucode.etp.core.msg;

public class Error implements Message{
    private String error;
    public Error(String error) {
        this.error = error;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {}

    @Override
    public byte getType() {
        return Message.TYPE_ERROR;
    }
}
