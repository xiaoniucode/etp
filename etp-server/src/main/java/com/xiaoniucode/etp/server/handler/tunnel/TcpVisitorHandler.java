package com.xiaoniucode.etp.server.handler.tunnel;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.manager.AccessControlManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
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
    private AccessControlManager accessControlManager;
    @Autowired
    private ProxyManager proxyManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        //检查 IP 访问控制权限
        int remotePort = getListenerPort(visitor);
        ProxyConfig proxyConfig = proxyManager.getByRemotePort(remotePort);
        if (proxyConfig == null) {
            logger.warn("访问目标隧道不存在");
            visitor.close();
            return;
        }
        String visitorIp = getVisitorIp(visitor);
        boolean checkAccess = accessControlManager.checkAccess(proxyConfig.getProxyId(), visitorIp);
        if (!checkAccess) {
            logger.debug("无访问权限");
            visitor.close();
            return;
        }

        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
        super.channelActive(ctx);
    }

    private void connectToTarget(VisitorSession session) {
        Channel control = session.getControl();
        ProxyConfig config = session.getProxyConfig();
        Message.ControlMessage message = MessageUtils
                .buildNewVisitorConn(session.getSessionId(),
                        config.getLocalIp(),
                        config.getLocalPort(),
                        config.getCompress(),
                        config.getEncrypt());
        control.writeAndFlush(message);
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }

    private String getVisitorIp(Channel visitor) {
        if (visitor.remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) visitor.remoteAddress())
                    .getAddress().getHostAddress();
        }
        return visitor.remoteAddress().toString();
    }
}
