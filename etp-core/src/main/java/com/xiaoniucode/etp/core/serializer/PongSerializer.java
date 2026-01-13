package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.Pong;
import io.netty.buffer.ByteBuf;

public class PongSerializer implements MessageSerializer<Pong>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_PONG;
    }

    @Override
    public void serialize(Pong message, ByteBuf out) {
    }

    @Override
    public Pong deserialize(ByteBuf in) {
        return new Pong();
    }
}
