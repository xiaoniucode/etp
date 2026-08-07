/*
 *    Copyright 2026 lxien
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.github.lxien.orbien.server.transport.http;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.lxien.orbien.core.domain.BasicAuthConfig;
import io.github.lxien.orbien.core.domain.HttpUser;
import io.github.lxien.orbien.core.domain.ProxyConfig;
import io.github.lxien.orbien.core.domain.ProxyConfigExt;
import io.github.lxien.orbien.core.transport.AttributeKeys;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.github.lxien.orbien.core.utils.StringUtils;
import io.github.lxien.orbien.server.service.ProxyConfigService;
import io.github.lxien.orbien.server.statemachine.stream.StreamManager;
import io.github.lxien.orbien.server.utils.NettyHttpUtils;
import io.github.lxien.orbien.server.vhost.DomainRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class BasicAuthHandler extends ChannelInboundHandlerAdapter {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(BasicAuthHandler.class);

    /**
     * key = proxyId + '\\0' + Authorization
     */
    private final Cache<String, Boolean> verifiedAuthCache = Caffeine.newBuilder()
            .maximumSize(4096)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    /**
     * 已通过鉴权的 visitor 连接索引
     */
    private final ConcurrentHashMap<String/*proxyId*/, Set<Channel>> authedVisitors = new ConcurrentHashMap<>();

    @Autowired
    private ProxyConfigService proxyConfigService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DomainRegistry domainRegistry;
    @Autowired
    private StreamManager streamManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel visitor = ctx.channel();
        if (Boolean.TRUE.equals(visitor.attr(AttributeKeys.BASIC_AUTH_PASSED).get())) {
            ctx.fireChannelRead(msg);
            return;
        }

        String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
        if (!StringUtils.hasText(domain)) {
            ReferenceCountUtil.release(msg);
            ctx.close();
            return;
        }
        String proxyId = domainRegistry.getProxyIdByDomain(domain);
        if (!StringUtils.hasText(proxyId)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ProxyConfigExt ext = proxyConfigService.findById(proxyId);
        if (ext == null || ext.getProxyConfig().isFile()) {
            markPassedAndDetach(ctx, proxyId);
            ctx.fireChannelRead(msg);
            return;
        }

        ProxyConfig config = ext.getProxyConfig();
        BasicAuthConfig basicAuth = config.getBasicAuth();
        if (basicAuth == null || !basicAuth.isEnabled()) {
            markPassedAndDetach(ctx, proxyId);
            ctx.fireChannelRead(msg);
            return;
        }

        String basicAuthHeader = visitor.attr(AttributeKeys.BASIC_AUTH_HEADER).get();
        if (basicAuthHeader == null || !isBasicScheme(basicAuthHeader)) {
            ReferenceCountUtil.release(msg);
            sendBasicAuth(visitor);
            return;
        }

        String cacheKey = cacheKey(proxyId, basicAuthHeader);
        if (Boolean.TRUE.equals(verifiedAuthCache.getIfPresent(cacheKey))) {
            markPassedAndDetach(ctx, proxyId);
            ctx.fireChannelRead(msg);
            return;
        }

        try {
            String base64Credentials = basicAuthHeader.substring(6).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), CharsetUtil.UTF_8);
            String[] parts = credentials.split(":", 2);
            if (parts.length != 2 || !check(parts[0], parts[1], basicAuth)) {
                ReferenceCountUtil.release(msg);
                sendBasicAuth(visitor);
                return;
            }
            verifiedAuthCache.put(cacheKey, Boolean.TRUE);
            markPassedAndDetach(ctx, proxyId);
            ctx.fireChannelRead(msg);
        } catch (Exception e) {
            logger.debug("Basic Auth 解码失败: {}", e.getMessage());
            ReferenceCountUtil.release(msg);
            sendBasicAuth(visitor);
        }
    }

    /**
     * 标记通过并从 pipeline 移除，后续读写不再经过本 Handler
     */
    private void markPassedAndDetach(ChannelHandlerContext ctx, String proxyId) {
        Channel visitor = ctx.channel();
        visitor.attr(AttributeKeys.BASIC_AUTH_PASSED).set(Boolean.TRUE);
        trackAuthedVisitor(proxyId, visitor);
        if (ctx.pipeline().context(this) != null) {
            ctx.pipeline().remove(this);
        }
    }

    private void trackAuthedVisitor(String proxyId, Channel visitor) {
        if (!StringUtils.hasText(proxyId) || visitor == null) {
            return;
        }
        Set<Channel> channels = authedVisitors.computeIfAbsent(proxyId, id -> ConcurrentHashMap.newKeySet());
        if (channels.add(visitor)) {
            visitor.closeFuture().addListener(future -> {
                Set<Channel> set = authedVisitors.get(proxyId);
                if (set != null) {
                    set.remove(visitor);
                    if (set.isEmpty()) {
                        authedVisitors.remove(proxyId, set);
                    }
                }
            });
        }
    }

    private static boolean isBasicScheme(String header) {
        return header.length() >= 6 && header.regionMatches(true, 0, "Basic ", 0, 6);
    }

    private void sendBasicAuth(Channel visitor) {
        NettyHttpUtils.sendBasicAuth(visitor).addListener(future -> ChannelUtils.closeOnFlush(visitor));
    }

    private boolean check(String username, String password, BasicAuthConfig basicAuth) {
        HttpUser user = basicAuth.getUser(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    private static String cacheKey(String proxyId, String authorizationHeader) {
        return proxyId + '\0' + authorizationHeader;
    }

    public void invalidate(String proxyId) {
        if (!StringUtils.hasText(proxyId)) {
            return;
        }
        String prefix = proxyId + '\0';
        verifiedAuthCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 清缓存并主动断开该代理已鉴权连接，保证立即生效
     */
    public void invalidateAndDisconnect(String proxyId) {
        invalidate(proxyId);
        if (!StringUtils.hasText(proxyId)) {
            return;
        }

        Set<Channel> channels = authedVisitors.remove(proxyId);
        if (channels != null && !channels.isEmpty()) {
            List<Channel> snapshot = new ArrayList<>(channels);
            channels.clear();
            for (Channel channel : snapshot) {
                ChannelUtils.closeOnFlush(channel);
            }
        }

        Set<String> domains = domainRegistry.getDomainsByProxyId(proxyId);
        for (String domain : domains) {
            streamManager.fireCloseByDomain(domain);
        }
        logger.debug("Basic Auth 策略变更，已断开代理 {} 的现有会话", proxyId);
    }
}
