package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NewVisitorConnSerializer implements MessageSerializer<NewVisitorConn> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_VISITOR_CONN;
    }

    @Override
    public void serialize(NewVisitorConn message, ByteBuf out) {
        out.writeLong(message.getSessionId());
        serializeString(out, message.getLocalIP());
        out.writeInt(message.getLocalPort());
    }

    @Override
    public NewVisitorConn deserialize(ByteBuf in) {
        long sessionId = in.readLong();
        String localIP = deserializeString(in);
        int localPort = in.readInt();
        return new NewVisitorConn(sessionId, localIP, localPort);
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
