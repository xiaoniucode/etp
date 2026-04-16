package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.domain.HttpUser;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@ChannelHandler.Sharable
public class BasicAuthHandler extends ChannelInboundHandlerAdapter {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(BasicAuthHandler.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitor = ctx.channel();
        String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
        proxyManager.findByDomain(domain).ifPresent(config -> {
            String basicAuthHeader = visitor.attr(AttributeKeys.BASIC_AUTH_HEADER).get();
            BasicAuthConfig basicAuth = config.getBasicAuth();
            if (basicAuth != null && basicAuth.isEnabled()) {
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
                        if (!check(username, password, basicAuth)) {
                            NettyHttpUtils.sendBasicAuth(visitor);
                        }
                    } else {
                        NettyHttpUtils.sendBasicAuth(visitor);
                    }
                } catch (Exception e) {
                    logger.debug("Basic Auth 解码失败: {}", e.getMessage());
                    NettyHttpUtils.sendBasicAuth(visitor);
                }
            }
        });
        ctx.fireChannelRead(msg);
    }

    private boolean check(String username, String password, BasicAuthConfig basicAuth) {
        HttpUser user = basicAuth.getUser(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

}
