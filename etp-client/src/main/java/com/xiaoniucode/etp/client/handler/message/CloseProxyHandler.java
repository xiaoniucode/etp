package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.manager.ConnectionPool;
import com.xiaoniucode.etp.client.manager.ServerSessionManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;

/**
 *
 * @author liuxin
 */
public class CloseProxyHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(CloseProxyHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        CloseProxy closeProxy = msg.getCloseProxy();
        String sessionId = closeProxy.getSessionId();
        ServerSessionManager.getServerSession(sessionId).ifPresent(serverSession -> {
            Channel server = serverSession.getServer();
            Channel tunnel = serverSession.getTunnel();
            //归还连接到连接池
            ConnectionPool.release(tunnel);
            ChannelUtils.closeOnFlush(server);
            logger.debug("隧道关闭 - [会话标识={}]", serverSession.getSessionId());
        });
    }
}
