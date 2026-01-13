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
        // 序列化secretKey
        serializeString(out, login.getSecretKey());
        // 序列化os
        serializeString(out, login.getOs());
        // 序列化arch
        serializeString(out, login.getArch());
    }
    
    @Override
    public Login deserialize(ByteBuf in) {
        String secretKey = deserializeString(in);
        String os = deserializeString(in);
        String arch = deserializeString(in);
        return new Login(secretKey, os, arch);
    }
    
    // 通用字符串序列化方法
    private void serializeString(ByteBuf out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
    
    // 通用字符串反序列化方法
    private String deserializeString(ByteBuf in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}