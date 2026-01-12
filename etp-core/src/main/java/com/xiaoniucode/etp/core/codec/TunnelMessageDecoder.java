package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Login;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TunnelMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        //标记当前读位置，失败时可重置
        in.markReaderIndex();
        int totalLength = in.readInt();
        if (in.readableBytes() < totalLength) {
            in.resetReaderIndex();
            return;
        }
        char messageType = in.readChar();
        MessageSerializer<?> serializer = SerializerFactory.getSerializer(messageType);
        out.add(deserializeLogin(in));
    }
    // Login消息反序列化实现
    private Login deserializeLogin(ByteBuf in) {
        // 反序列化secretKey
        int secretKeyLength = in.readInt();
        byte[] secretKeyBytes = new byte[secretKeyLength];
        in.readBytes(secretKeyBytes);
        String secretKey = new String(secretKeyBytes, StandardCharsets.UTF_8);

        // 反序列化os
        int osLength = in.readInt();
        byte[] osBytes = new byte[osLength];
        in.readBytes(osBytes);
        String os = new String(osBytes, StandardCharsets.UTF_8);

        // 反序列化arch
        int archLength = in.readInt();
        byte[] archBytes = new byte[archLength];
        in.readBytes(archBytes);
        String arch = new String(archBytes, StandardCharsets.UTF_8);

        // 构造Login对象
        return new Login(secretKey, os, arch);
    }
}
