package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxy;
import io.netty.buffer.ByteBuf;

public class NewProxySerializer implements MessageSerializer<NewProxy>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_PROXY;
    }

    @Override
    public void serialize(NewProxy message, ByteBuf out) {

    }

    @Override
    public NewProxy deserialize(ByteBuf in) {
        return null;
    }
}
