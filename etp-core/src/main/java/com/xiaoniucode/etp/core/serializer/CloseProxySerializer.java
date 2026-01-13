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
        out.writeLong(message.getSessionId());
        if (message.getProxyId() != null) {
            out.writeBoolean(true);
            out.writeInt(message.getProxyId());
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public CloseProxy deserialize(ByteBuf in) {
        long sessionId = in.readLong();
        boolean hasProxyId = in.readBoolean();
        Integer proxyId = null;
        if (hasProxyId) {
            proxyId = in.readInt();
        }

        CloseProxy closeProxy = new CloseProxy(sessionId);
        closeProxy.setProxyId(proxyId);
        return closeProxy;
    }
}
