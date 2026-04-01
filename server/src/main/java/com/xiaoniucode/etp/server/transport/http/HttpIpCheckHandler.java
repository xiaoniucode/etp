package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.transport.IpCheckHandler;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class HttpIpCheckHandler extends IpCheckHandler {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(HttpIpCheckHandler.class);
    @Autowired
    private ProxyManager proxyManager;

    @Autowired
    public HttpIpCheckHandler(IpAccessChecker ipAccessChecker) {
        super(ipAccessChecker);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel visitor = ctx.channel();
        String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
        proxyManager.findByDomain(domain).ifPresent(config->{
            String proxyId = config.getProxyId();
            doCheckAccess(visitor, proxyId);
        });

        ctx.fireChannelRead(msg);
    }
}
