package com.xiaoniucode.etp.server.transport.tcp;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.service.his.ProxyManager;
import com.xiaoniucode.etp.server.transport.IpCheckHandler;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
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
    public TcpIpCheckHandler(IpAccessChecker ipAccessChecker) {
        super(ipAccessChecker);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("IP访问控制检查");
        Channel visitor = ctx.channel();
        int remotePort = getListenerPort(visitor);
        Optional<ProxyConfig> opt = proxyManager.findByRemotePort(remotePort);
        if (opt.isPresent()) {
            if (doCheckAccess(visitor, opt.get())) {
                logger.debug("访问权限检查通过，放行");
            }
        }
        ctx.fireChannelActive();
    }
}
