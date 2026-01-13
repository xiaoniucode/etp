package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;

public class CloseProxySerializer implements MessageSerializer <CloseProxy>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_CLOSE_PROXY;
    }

    @Override
    public void serialize(CloseProxy message, ByteBuf out) {

    }

    @Override
    public CloseProxy deserialize(ByteBuf in) {
        return null;
    }
}
