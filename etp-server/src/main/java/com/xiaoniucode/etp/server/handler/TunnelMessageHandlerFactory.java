package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 隧道消息处理器工厂
 *
 * @author liuxin
 */
public class TunnelMessageHandlerFactory {
    private static final Map<Class<? extends Message>, MessageHandler> handlers = new HashMap<>();

    static {
        handlers.put(Ping.class, new HeartbeatHandler());
        handlers.put(Login.class, new LoginHandler());
        handlers.put(NewVisitorConnResp.class, new NewVisitorConnRespHandler());
        handlers.put(CloseProxy.class, new CloseProxyHandler());
        handlers.put(NewProxy.class, new NewProxyHandler());
        handlers.put(UnregisterProxy.class, new UnregisterProxyHandler());
    }

    public static MessageHandler getHandler(Message message) {
        return handlers.get(message.getClass());
    }
}
