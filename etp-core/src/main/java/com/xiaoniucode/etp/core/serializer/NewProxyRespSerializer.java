package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
import io.netty.buffer.ByteBuf;

public class NewProxyRespSerializer implements MessageSerializer <NewProxyResp>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_PROXY_RESP;
    }

    @Override
    public void serialize(NewProxyResp message, ByteBuf out) {

    }

    @Override
    public NewProxyResp deserialize(ByteBuf in) {
        return null;
    }
}
