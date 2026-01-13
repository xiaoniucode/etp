package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NewProxyRespSerializer implements MessageSerializer <NewProxyResp>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_PROXY_RESP;
    }

    @Override
    public void serialize(NewProxyResp message, ByteBuf out) {
        out.writeInt(message.getProxyId());
        serializeString(out, message.getRemoteAddr());
        if (message.getSessionId() != null) {
            out.writeBoolean(true);
            out.writeLong(message.getSessionId());
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public NewProxyResp deserialize(ByteBuf in) {
        int proxyId = in.readInt();
        String remoteAddr = deserializeString(in);
        boolean hasSessionId = in.readBoolean();
        Long sessionId = null;
        if (hasSessionId) {
            sessionId = in.readLong();
        }

        if (sessionId != null) {
            return new NewProxyResp(proxyId, remoteAddr, sessionId);
        } else {
            return new NewProxyResp(proxyId, remoteAddr);
        }
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
