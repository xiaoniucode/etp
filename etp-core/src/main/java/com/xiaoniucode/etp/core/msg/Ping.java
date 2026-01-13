package com.xiaoniucode.etp.core.msg;

public class Ping implements Message{


    @Override
    public byte getType() {
        return Message.TYPE_PING;
    }
}
