package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建并缓存隧道消息处理器
 *
 * @author liuxin
 */
public class MessageHandlerFactory {
    private static final Map<TunnelMessage.Message.Type, MessageHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(TunnelMessage.Message.Type.DISCONNECT, new DisconnectHandler());
        HANDLERS.put(TunnelMessage.Message.Type.CONNECT, new ConnectHandler());
        HANDLERS.put(TunnelMessage.Message.Type.TRANSFER, new TransferHandler());
        HANDLERS.put(TunnelMessage.Message.Type.ERROR, new ErrorChannelHandler());
        HANDLERS.put(TunnelMessage.Message.Type.PROXY_REGISTER, new ProxyRegisterMessageHandler());
    }

    public static MessageHandler getHandler(TunnelMessage.Message.Type type) {
        return HANDLERS.get(type);
    }
}
