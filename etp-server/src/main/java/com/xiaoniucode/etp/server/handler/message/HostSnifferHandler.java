package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.handler.utils.NettyHttpUtils;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.AccessControlManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.DomainManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.List;

/**
 * 解析出域名
 */
public class HostSnifferHandler extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(HostSnifferHandler.class);
    private boolean sniffing = true;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Channel visitor = ctx.channel();
        if (!sniffing) {
            // 嗅探结束，直接透传
            out.add(in.retain());
            return;
        }
        if (in.readableBytes() < 8) {
            return;
        }
        in.markReaderIndex();
        boolean isHttp = false;
        String domain = null;
        try {
            int len = Math.min(in.readableBytes(), 4096);
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            String content = new String(bytes, CharsetUtil.UTF_8);

            if (isHttp(content)) {
                String host = parseHost(content);
                if (host != null) {
                    if (host.contains(":")) {
                        domain = host.split(":")[0];
                    } else {
                        domain = host;
                    }
                    String proxyId = BeanHelper.getBean(DomainManager.class).getProxyId(domain);

                    if (proxyId == null) {
                        visitor.close();
                        logger.debug("隧道不存在");
                        return;
                    }
                    String visitorIp = getVisitorIp(visitor);
                    boolean checkAccess = BeanHelper.getBean(AccessControlManager.class).checkAccess(proxyId, visitorIp);
                    if (!checkAccess) {
                        logger.debug("访问来源 IP 没有访问权限：{}", visitorIp);
                        NettyHttpUtils.sendHttp403(visitor).addListener((ChannelFutureListener) future -> visitor.close());
                        return;
                    }
                    ProxyConfig config = BeanHelper.getBean(ProxyManager.class).getById(proxyId);
                    if (!config.isOpen()) {
                        visitor.close();
                        logger.debug("隧道为关闭状态");
                        return;
                    }
                    if (!BeanHelper.getBean(DomainManager.class).exists(domain)) {
                        logger.warn("没有该域名的代理服务");
                        visitor.close();
                        return;
                    }
                    //Basic Auth 认证
                    BasicAuthConfig basicAuth = config.getBasicAuth();
                    if (basicAuth != null && basicAuth.isEnable()) {
                        String authHeader = parseAuthHeader(content);
                        if (authHeader == null || !authHeader.toLowerCase().startsWith("basic ")) {
                            NettyHttpUtils.sendBasicAuth(visitor);
                            return;
                        }

                        try {
                            String base64Credentials = authHeader.substring(6).trim();
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

                }
                isHttp = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            in.resetReaderIndex();
            sniffing = false;
        }
        if (isHttp) {
            visitor.attr(ChannelConstants.PROTOCOL_TYPE).set(ProtocolType.HTTP);
            visitor.attr(ChannelConstants.VISIT_DOMAIN).set(domain);
        }
        ctx.pipeline().remove(this);
    }
    /**
     * 解析 Authorization 头
     */
    private String parseAuthHeader(String content) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.toLowerCase().startsWith("authorization:")) {
                int colonIndex = trimmedLine.indexOf(':');
                if (colonIndex != -1) {
                    return trimmedLine.substring(colonIndex + 1).trim();
                }
            }
        }
        return null;
    }

    private boolean isHttp(String content) {
        return content.startsWith("GET ") ||
                content.startsWith("POST ") ||
                content.startsWith("PUT ") ||
                content.startsWith("DELETE ") ||
                content.startsWith("HEAD ") ||
                content.startsWith("OPTIONS ") ||
                content.startsWith("PATCH ") ||
                content.startsWith("CONNECT ");
    }

    private String parseHost(String content) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.toLowerCase().startsWith("host:")) {
                int colonIndex = trimmedLine.indexOf(':');
                if (colonIndex != -1) {
                    return trimmedLine.substring(colonIndex + 1).trim();
                }
            }
        }
        return null;
    }

    /**
     * 获取访问用户的IP 地址
     *
     * @param visitor 访问者
     * @return IP地址
     */
    private String getVisitorIp(Channel visitor) {
        if (visitor.remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) visitor.remoteAddress())
                    .getAddress().getHostAddress();
        }
        return visitor.remoteAddress().toString();
    }
}
