package com.xiaoniucode.etp.client.handler.factory;

import com.xiaoniucode.etp.client.handler.message.*;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.message.Message.MessageType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author liuxin
 */
public class MessageHandlerFactory {
    private static final Map<MessageType, MessageHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(MessageType.CLOSE_PROXY, new CloseProxyHandler());
        HANDLERS.put(MessageType.LOGIN_RESP, new LoginRespHandler());
        HANDLERS.put(MessageType.NEW_VISITOR, new NewVisitorConnHandler());
        HANDLERS.put(MessageType.ERROR, new ErrorChannelHandler());
        HANDLERS.put(MessageType.NEW_PROXY_RESP, new NewProxyRespHandler());
        HANDLERS.put(MessageType.KICKOUT, new KickoutClientHandler());
    }

    public static MessageHandler getHandler(MessageType type) {
        return HANDLERS.get(type);
    }
}
