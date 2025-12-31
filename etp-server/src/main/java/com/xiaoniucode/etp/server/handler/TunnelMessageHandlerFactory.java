package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 隧道消息处理器工厂
 *
 * @author liuxin
 */
public class TunnelMessageHandlerFactory {
    private static final Map<TunnelMessage.Message.Type, MessageHandler> handlers = new HashMap<>();

    static {
        handlers.put(TunnelMessage.Message.Type.HEARTBEAT, new HeartbeatHandler());
        handlers.put(TunnelMessage.Message.Type.AUTH, new AuthHandler());
        handlers.put(TunnelMessage.Message.Type.CONNECT, new ConnectHandler());
        handlers.put(TunnelMessage.Message.Type.TRANSFER, new TransferHandler());
        handlers.put(TunnelMessage.Message.Type.DISCONNECT, new DisconnectHandler());
        handlers.put(TunnelMessage.Message.Type.PROXY_REGISTER, new ProxyRegisterMessageHandler());
        handlers.put(TunnelMessage.Message.Type.PROXY_UNREGISTER, new ProxyUnregisterMessageHandler());
    }

    public static MessageHandler getHandler(TunnelMessage.Message.Type type) {
        return handlers.get(type);
    }
}
