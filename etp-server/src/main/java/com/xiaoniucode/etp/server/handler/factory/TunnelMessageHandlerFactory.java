package com.xiaoniucode.etp.server.handler.factory;

import com.xiaoniucode.etp.core.handler.MessageHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xiaoniucode.etp.core.message.Message.MessageType;
import com.xiaoniucode.etp.server.handler.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 隧道消息处理器工厂
 *
 * @author liuxin
 */
@Component
public class TunnelMessageHandlerFactory {

    private final Map<MessageType, MessageHandler> handlers = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;

    @Autowired
    public TunnelMessageHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initHandlers();
    }

    private void initHandlers() {
        registerHandler(MessageType.PING, HeartbeatHandler.class);
        registerHandler(MessageType.LOGIN, LoginHandler.class);
        registerHandler(MessageType.NEW_VISITOR_RESP, NewVisitorConnRespHandler.class);
        registerHandler(MessageType.CLOSE_PROXY, CloseProxyHandler.class);
        registerHandler(MessageType.NEW_PROXY, NewProxyRespHandler.class);
    }

    private void registerHandler(MessageType type, Class<? extends MessageHandler> handlerClass) {
        MessageHandler handler = applicationContext.getBean(handlerClass);
        handlers.put(type, handler);
    }

    public MessageHandler getHandler(MessageType type) {
        return handlers.get(type);
    }

}