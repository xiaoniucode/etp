package com.xiaoniucode.etp.server.handler.tcp;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.handler.IpCheckHandler;
import com.xiaoniucode.etp.server.manager.AccessControlManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * IP 访问控制检查
 */
@Component
@ChannelHandler.Sharable
public class TcpIpCheckHandler extends IpCheckHandler {
    private final Logger logger = LoggerFactory.getLogger(TcpIpCheckHandler.class);
    @Autowired
    private ProxyManager proxyManager;

    @Autowired
    public TcpIpCheckHandler(AccessControlManager accessControlManager) {
        super(accessControlManager);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        int remotePort = getListenerPort(visitor);
        ProxyConfig config = proxyManager.getByRemotePort(remotePort);
        if (config == null) {
            visitor.close();
            return;
        }
        doCheckAccess(visitor, config.getProxyId());
        //检查通过，继续传播
        super.channelActive(ctx);
    }
}
