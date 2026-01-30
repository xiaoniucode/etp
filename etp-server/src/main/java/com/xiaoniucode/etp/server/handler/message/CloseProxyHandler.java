package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.msg.Message.*;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Agent主动要求关闭visitor Session连接
 *
 * @author liuxin
 */
@Component
public class CloseProxyHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(CloseProxyHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        CloseProxy closeProxy = msg.getCloseProxy();
        visitorSessionManager.disconnect(closeProxy.getSessionId(), session -> {
                    ChannelUtils.closeOnFlush(session.getVisitor());
                    logger.debug("visitor: {} 断开连接", session.getSessionId());
                }
        );

    }
}
