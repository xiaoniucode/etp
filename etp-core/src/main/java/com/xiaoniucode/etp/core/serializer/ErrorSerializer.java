package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Error;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ErrorSerializer implements MessageSerializer <Error>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_ERROR;
    }

    @Override
    public void serialize(Error message, ByteBuf out) {
        serializeString(out, message.getError());
    }

    @Override
    public Error deserialize(ByteBuf in) {
        String error = deserializeString(in);
        return new Error(error);
    }

    private void serializeString(ByteBuf out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    private String deserializeString(ByteBuf in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
