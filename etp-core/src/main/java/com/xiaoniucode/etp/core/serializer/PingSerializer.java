package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.Ping;
import io.netty.buffer.ByteBuf;

public class PingSerializer implements MessageSerializer <Ping> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_PING;
    }

    @Override
    public void serialize(Ping message, ByteBuf out) {

    }

    @Override
    public Ping deserialize(ByteBuf in) {
        return new Ping();
    }
}
