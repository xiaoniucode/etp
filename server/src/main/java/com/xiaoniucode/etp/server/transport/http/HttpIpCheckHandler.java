package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.transport.IpCheckHandler;
import com.xiaoniucode.etp.server.security.AccessControlManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class HttpIpCheckHandler extends IpCheckHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpIpCheckHandler.class);
    @Autowired
    private ProxyManager proxyManager;

    @Autowired
    public HttpIpCheckHandler(AccessControlManager accessControlManager) {
        super(accessControlManager);
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
