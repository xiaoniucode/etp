package com.xiaoniucode.etp.server.handler.tunnel;

import com.xiaoniucode.etp.core.domain.LanInfo;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.server.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自公网访问者的请求
 */
@Component
@ChannelHandler.Sharable
public class TcpVisitorHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
        super.channelActive(ctx);
    }

    private void connectToTarget(VisitorSession session) {
        Channel control = session.getControl();
        LanInfo lanInfo = session.getLanInfo();
        Message.ControlMessage message = MessageWrapper
                .buildNewVisitorConn(session.getSessionId(), lanInfo.getLocalIP(), lanInfo.getLocalPort());
        control.writeAndFlush(message);
    }
}
