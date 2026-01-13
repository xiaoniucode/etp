package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;
import com.xiaoniucode.etp.core.msg.Error;
public class ErrorSerializer implements MessageSerializer <Error>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_ERROR;
    }

    @Override
    public void serialize(Error message, ByteBuf out) {

    }

    @Override
    public Error deserialize(ByteBuf in) {
        return null;
    }
}
