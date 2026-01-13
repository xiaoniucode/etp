package com.xiaoniucode.etp.core.msg;

public class Pong implements Message{
    @Override
    public byte getType() {
        return Message.TYPE_PONG;
    }
}
