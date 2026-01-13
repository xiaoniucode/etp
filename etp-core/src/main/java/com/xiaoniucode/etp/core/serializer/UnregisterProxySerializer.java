package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.UnregisterProxy;
import io.netty.buffer.ByteBuf;

public class UnregisterProxySerializer implements MessageSerializer<UnregisterProxy>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_UNREGISTER_PROXY;
    }

    @Override
    public void serialize(UnregisterProxy message, ByteBuf out) {
        out.writeInt(message.getProxyId());
    }

    @Override
    public UnregisterProxy deserialize(ByteBuf in) {
        int proxyId = in.readInt();
        return new UnregisterProxy(proxyId);
    }
}
