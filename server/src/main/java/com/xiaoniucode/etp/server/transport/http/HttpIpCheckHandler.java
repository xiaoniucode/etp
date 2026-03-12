package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.server.transport.IpCheckHandler;
import com.xiaoniucode.etp.server.security.AccessControlManager;
import com.xiaoniucode.etp.server.vhost.DomainManager;
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
    private DomainManager domainManager;

    @Autowired
    public HttpIpCheckHandler(AccessControlManager accessControlManager) {
        super(accessControlManager);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitor = ctx.channel();
        String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
        String proxyId = domainManager.getProxyId(domain);
        doCheckAccess(visitor, proxyId);
        //继续传递给下一个处理器
        super.channelRead(ctx, msg);
    }
}
