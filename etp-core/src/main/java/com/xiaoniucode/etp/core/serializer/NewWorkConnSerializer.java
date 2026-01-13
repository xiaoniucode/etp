package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.buffer.ByteBuf;

public class NewWorkConnSerializer implements MessageSerializer<NewWorkConn> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_WORK_CONN;
    }

    @Override
    public void serialize(NewWorkConn message, ByteBuf out) {
        ByteBuf payload = message.getPayload();
        int payloadLength = payload.readableBytes();
        out.writeInt(payloadLength);
        out.writeBytes(payload.duplicate());

        if (message.getSessionId() != null) {
            out.writeBoolean(true);
            out.writeLong(message.getSessionId());
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public NewWorkConn deserialize(ByteBuf in) {
        int payloadLength = in.readInt();
        ByteBuf payload = in.readRetainedSlice(payloadLength);
        
        boolean hasSessionId = in.readBoolean();
        Long sessionId = null;
        if (hasSessionId) {
            sessionId = in.readLong();
        }
        if (sessionId != null) {
            return new NewWorkConn(payload, sessionId);
        } else {
            return new NewWorkConn(payload);
        }
    }
}
