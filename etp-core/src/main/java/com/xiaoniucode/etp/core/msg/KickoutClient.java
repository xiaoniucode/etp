package com.xiaoniucode.etp.core.msg;

public class KickoutClient implements Message{
    @Override
    public byte getType() {
        return Message.TYPE_KICKOUT_CLIENT;
    }
}
