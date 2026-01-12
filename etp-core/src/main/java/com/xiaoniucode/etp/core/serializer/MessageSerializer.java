package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;

/**
 * 消息序列化器接口，每种消息类型对应一个实现类
 */
public interface MessageSerializer<T extends Message> {
    /**
     * 获取消息类型ID
     */
    char getMessageType();
    
    /**
     * 序列化消息到ByteBuf
     */
    void serialize(T message, ByteBuf out);
    
    /**
     * 从ByteBuf反序列化消息
     */
    T deserialize(ByteBuf in);
}