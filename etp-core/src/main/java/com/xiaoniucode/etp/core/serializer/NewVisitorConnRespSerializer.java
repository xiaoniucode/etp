package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConnResp;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NewVisitorConnRespSerializer implements MessageSerializer<NewVisitorConnResp> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_VISITOR_CONN_RESP;
    }

    @Override
    public void serialize(NewVisitorConnResp message, ByteBuf out) {
        serializeString(out, message.getSecretKey());
        out.writeLong(message.getSessionId());
    }

    @Override
    public NewVisitorConnResp deserialize(ByteBuf in) {
        String secretKey = deserializeString(in);
        long sessionId = in.readLong();
        return new NewVisitorConnResp(secretKey, sessionId);
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
