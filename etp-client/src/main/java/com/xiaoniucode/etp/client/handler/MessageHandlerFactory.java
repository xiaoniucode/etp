package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.*;
import com.xiaoniucode.etp.core.msg.Error;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建并缓存隧道消息处理器
 *
 * @author liuxin
 */
public class MessageHandlerFactory {
    private static final Map<Class<? extends Message>, MessageHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(CloseProxy.class, new CloseProxyHandler());
        HANDLERS.put(NewWorkConn.class, new NewVisitorConnHandler());
        HANDLERS.put(Transfer.class, new NewWorkConnHandler());
        HANDLERS.put(Error.class, new ErrorChannelHandler());
        HANDLERS.put(NewProxyResp.class, new NewProxyRespHandler());
    }

    public static MessageHandler getHandler(Message type) {
        return HANDLERS.get(type.getClass());
    }
}
