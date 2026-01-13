package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂，管理所有消息类型的序列化策略
 */
public class SerializerFactory {
    private static final Map<Byte, MessageSerializer<? extends Message>> SERIALIZER_MAP = new HashMap<>();
    
    static {
        registerSerializer(new LoginSerializer());
        registerSerializer(new NewWorkConnSerializer());
        registerSerializer(new CloseProxySerializer());
        registerSerializer(new ErrorSerializer());
        registerSerializer(new NewProxyRespSerializer());
        registerSerializer(new NewProxySerializer());
        registerSerializer(new NewVisitorConnRespSerializer());
        registerSerializer(new NewVisitorConnSerializer());
        registerSerializer(new PingSerializer());
        registerSerializer(new PongSerializer());
        registerSerializer(new UnregisterProxySerializer());
    }
    
    /**
     * 注册序列化器
     */
    public static void registerSerializer(MessageSerializer<? extends Message> serializer) {
        SERIALIZER_MAP.put(serializer.getMessageType(), serializer);
    }
    
    /**
     * 根据消息类型ID获取序列化器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> MessageSerializer<T> getSerializer(byte messageType) {
        return (MessageSerializer<T>) SERIALIZER_MAP.get(messageType);
    }
    
    /**
     * 根据消息对象获取序列化器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> MessageSerializer<T> getSerializer(T message) {
        return (MessageSerializer<T>) SERIALIZER_MAP.get(message.getType());
    }
}