package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxy;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class NewProxySerializer implements MessageSerializer<NewProxy>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_PROXY;
    }

    @Override
    public void serialize(NewProxy message, ByteBuf out) {
        serializeString(out, message.getName());
        serializeString(out, message.getLocalIP());
        out.writeInt(message.getLocalPort());
        serializeString(out, message.getProtocol().name());
        if (message.getRemotePort() != null) {
            //用一个字节标志有remotePort字段
            out.writeBoolean(true);
            out.writeInt(message.getRemotePort());
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(message.getStatus());
        if (message.getDomains() != null) {
            out.writeBoolean(true);
            out.writeInt(message.getDomains().size());
            for (String domain : message.getDomains()) {
                serializeString(out, domain);
            }
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public NewProxy deserialize(ByteBuf in) {
        String name = deserializeString(in);
        String localIP = deserializeString(in);
        int localPort = in.readInt();
        String protocol = deserializeString(in);
        Integer remotePort = null;
        if (in.readBoolean()) {
            remotePort = in.readInt();
        }
        int status = in.readInt();
        Set<String> customDomains = null;
        if (in.readBoolean()) {
            int size = in.readInt();
            customDomains = new HashSet<>();
            for (int i = 0; i < size; i++) {
                customDomains.add(deserializeString(in));
            }
        }

        NewProxy newProxy = new NewProxy();
        newProxy.setName(name);
        newProxy.setLocalIP(localIP);
        newProxy.setLocalPort(localPort);
        newProxy.setProtocol(ProtocolType.getType(protocol));
        newProxy.setRemotePort(remotePort);
        newProxy.setStatus(status);
        newProxy.setDomains(customDomains);
        return newProxy;
    }

    private void serializeString(ByteBuf out, String value) {
        if (value == null) {
            out.writeInt(-1);
        } else {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

    private String deserializeString(ByteBuf in) {
        int length = in.readInt();
        if (length == -1) {
            return null;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
