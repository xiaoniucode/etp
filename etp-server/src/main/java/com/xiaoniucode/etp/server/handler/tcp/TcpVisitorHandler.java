package com.xiaoniucode.etp.server.handler.tcp;

import com.xiaoniucode.etp.server.handler.TargetConnector;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 处理来自公网访问者的请求
 */
@Component
@ChannelHandler.Sharable
public class TcpVisitorHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;
    @Autowired
    private TargetConnector targetConnector;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        InetSocketAddress remoteAddress = (InetSocketAddress) visitor.remoteAddress();
        logger.debug("[TCP] 新的访问者连接: {}:{}",
                remoteAddress.getAddress().getHostAddress(),
                remoteAddress.getPort());
        visitorSessionManager.registerVisitor(visitor, visitorSession ->
                targetConnector.connectToTarget(visitorSession));
        super.channelActive(ctx);
    }
}
