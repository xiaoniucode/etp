package com.xiaoniucode.etp.core.msg;

public class Pong implements Message{
    @Override
    public char getType() {
        return Message.TYPE_PONG;
    }
}
