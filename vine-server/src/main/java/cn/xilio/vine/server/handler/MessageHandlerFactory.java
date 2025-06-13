package cn.xilio.vine.server.handler;

import cn.xilio.vine.core.MessageHandler;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.handler.tunnel.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 隧道消息处理器工厂
 */
public class MessageHandlerFactory {
    private static final Map<TunnelMessage.Message.Type, MessageHandler> handlers = new HashMap<>();

    static {
        handlers.put(TunnelMessage.Message.Type.HEARTBEAT, new HeartbeatHandler());
        handlers.put(TunnelMessage.Message.Type.AUTH, new AuthHandler());
        handlers.put(TunnelMessage.Message.Type.CONNECT, new ConnectHandler());
        handlers.put(TunnelMessage.Message.Type.TRANSFER, new TransferHandler());
        handlers.put(TunnelMessage.Message.Type.DISCONNECT, new DisconnectHandler());
    }

    public static MessageHandler getHandler(TunnelMessage.Message.Type type) {
        MessageHandler handler = handlers.get(type);
        if (handler==null){
            throw new RuntimeException("not found handler for type:"+type);
        }
        return handler;
    }
}
