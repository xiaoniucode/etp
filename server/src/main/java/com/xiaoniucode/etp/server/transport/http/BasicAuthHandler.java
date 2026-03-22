package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@ChannelHandler.Sharable
public class BasicAuthHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(BasicAuthHandler.class);
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private ProxyManager proxyManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitor = ctx.channel();
        String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
        String proxyId = domainManager.getProxyId(domain);
        ProxyConfig config = proxyManager.findById(proxyId).get();
        String basicAuthHeader = visitor.attr(AttributeKeys.BASIC_AUTH_HEADER).get();

        BasicAuthConfig basicAuth = config.getBasicAuth();
        if (basicAuth != null && basicAuth.isEnable()) {
            if (basicAuthHeader == null || !basicAuthHeader.toLowerCase().startsWith("basic ")) {
                NettyHttpUtils.sendBasicAuth(visitor);
                return;
            }
            try {
                String base64Credentials = basicAuthHeader.substring(6).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Credentials), CharsetUtil.UTF_8);
                String[] parts = credentials.split(":", 2);

                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    if (!basicAuth.check(username, password)) {
                        NettyHttpUtils.sendBasicAuth(visitor);
                        return;
                    }
                } else {
                    NettyHttpUtils.sendBasicAuth(visitor);
                    return;
                }
            } catch (Exception e) {
                logger.debug("Basic Auth 解码失败: {}", e.getMessage());
                NettyHttpUtils.sendBasicAuth(visitor);
                return;
            }
        }
        //传递给下一个处理器
        super.channelRead(ctx, msg);
    }

}
