package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Login;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Login消息序列化器
 */
public class LoginSerializer implements MessageSerializer<Login> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_LOGIN;
    }
    
    @Override
    public void serialize(Login login, ByteBuf out) {
        serializeString(out, login.getSecretKey());
        serializeString(out, login.getOs());
        serializeString(out, login.getArch());
    }
    
    @Override
    public Login deserialize(ByteBuf in) {
        String secretKey = deserializeString(in);
        String os = deserializeString(in);
        String arch = deserializeString(in);
        return new Login(secretKey, os, arch);
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