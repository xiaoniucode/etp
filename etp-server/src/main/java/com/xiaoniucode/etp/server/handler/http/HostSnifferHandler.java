package com.xiaoniucode.etp.server.handler.http;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.DomainManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String basicAuth = null;
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
                    basicAuth = parseAuthHeader(content);
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
            visitor.attr(ChannelConstants.BASIC_AUTH_HEADER).set(basicAuth);
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

}
