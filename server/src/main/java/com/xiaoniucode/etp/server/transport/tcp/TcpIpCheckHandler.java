package com.xiaoniucode.etp.server.transport.tcp;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.transport.IpCheckHandler;
import com.xiaoniucode.etp.server.security.AccessControlManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * IP 访问控制检查
 */
@Component
@ChannelHandler.Sharable
public class TcpIpCheckHandler extends IpCheckHandler {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(TcpIpCheckHandler.class);
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
        Optional<ProxyConfig> opt = proxyManager.findByRemotePort(remotePort);
        if (opt.isEmpty()) {
            visitor.close();
            return;
        }
        ProxyConfig config = opt.get();
        doCheckAccess(visitor, config.getProxyId());
        //检查通过，继续传播
        super.channelActive(ctx);
    }
}
