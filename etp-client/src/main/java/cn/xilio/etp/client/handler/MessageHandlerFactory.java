package cn.xilio.etp.client.handler;

import cn.xilio.etp.core.MessageHandler;
import cn.xilio.etp.core.protocol.TunnelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;

/**
 * 隧道消息处理器工厂
 *
 * @author liuxin
 */
public class MessageHandlerFactory {
    private final static Logger logger = LoggerFactory.getLogger(MessageHandlerFactory.class);
    private static final Map<TunnelMessage.Message.Type, MessageHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(TunnelMessage.Message.Type.DISCONNECT, new DisconnectHandler());
        HANDLERS.put(TunnelMessage.Message.Type.CONNECT, new ConnectHandler());
        HANDLERS.put(TunnelMessage.Message.Type.TRANSFER, new TransferHandler());
    }

    public static MessageHandler getHandler(TunnelMessage.Message.Type type) {
        MessageHandler handler = HANDLERS.get(type);
        if (handler == null) {
            logger.error("没有该类型的处理器: {}", type.name());
            throw new IllegalArgumentException("没有该类型的处理器: " + type.name());
        }
        return handler;
    }
}
