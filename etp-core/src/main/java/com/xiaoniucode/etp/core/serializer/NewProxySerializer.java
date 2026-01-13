package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxy;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NewProxySerializer implements MessageSerializer<NewProxy>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_PROXY;
    }

    @Override
    public void serialize(NewProxy message, ByteBuf out) {
        serializeString(out, message.getName());
        out.writeInt(message.getLocalPort());
        serializeString(out, message.getProtocol());
        out.writeInt(message.getRemotePort());
        out.writeBoolean(message.getAutoStart());
    }

    @Override
    public NewProxy deserialize(ByteBuf in) {
        String name = deserializeString(in);
        int localPort = in.readInt();
        String protocol = deserializeString(in);
        int remotePort = in.readInt();
        boolean autoStart = in.readBoolean();

        NewProxy newProxy = new NewProxy();
        newProxy.setName(name);
        newProxy.setLocalPort(localPort);
        newProxy.setProtocol(protocol);
        newProxy.setRemotePort(remotePort);
        newProxy.setAutoStart(autoStart);
        return newProxy;
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
