package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class HostSnifferHandler extends ByteToMessageDecoder {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(HostSnifferHandler.class);
    private boolean sniffing = true;

    public HostSnifferHandler() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Channel visitor = ctx.channel();
        if (!sniffing) {
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
        String realClientIp = null;
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
                    basicAuth = parseAuthHeader(content);
                    realClientIp = parseRealClientIp(content);
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
            visitor.attr(AttributeKeys.PROTOCOL_TYPE).set(ProtocolType.HTTP);
            visitor.attr(AttributeKeys.VISIT_DOMAIN).set(domain);
            visitor.attr(AttributeKeys.BASIC_AUTH_HEADER).set(basicAuth);

            // 如果没有从 header 中获取到真实 IP，则使用连接来源 IP
            if (realClientIp == null || realClientIp.isEmpty()) {
                if (visitor.remoteAddress() instanceof InetSocketAddress) {
                    realClientIp = ((InetSocketAddress) visitor.remoteAddress()).getAddress().getHostAddress();
                }
            }
            visitor.attr(AttributeKeys.REAL_CLIENT_IP).set(realClientIp);
        }
        ctx.pipeline().remove(this);
    }

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
     * 解析 HTTP 请求头中的真实客户端 IP
     * 优先级: X-Forwarded-For > X-Real-IP
     *
     * @param content HTTP 请求内容
     * @return 真实客户端 IP，如果没有则返回 null
     */
    private String parseRealClientIp(String content) {
        String[] lines = content.split("\\r?\\n");
        String xForwardedFor = null;
        String xRealIp = null;

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String lowerLine = trimmedLine.toLowerCase();

            if (lowerLine.startsWith("x-forwarded-for:")) {
                int colonIndex = trimmedLine.indexOf(':');
                if (colonIndex != -1) {
                    String value = trimmedLine.substring(colonIndex + 1).trim();
                    // X-Forwarded-For 格式: client, proxy1, proxy2
                    int commaIndex = value.indexOf(',');
                    if (commaIndex != -1) {
                        xForwardedFor = value.substring(0, commaIndex).trim();
                    } else {
                        xForwardedFor = value;
                    }
                }
            } else if (lowerLine.startsWith("x-real-ip:")) {
                int colonIndex = trimmedLine.indexOf(':');
                if (colonIndex != -1) {
                    xRealIp = trimmedLine.substring(colonIndex + 1).trim();
                }
            }

            if (xForwardedFor != null && xRealIp != null) {
                break;
            }
        }
        // 优先级: X-Forwarded-For > X-Real-IP
        return xForwardedFor != null ? xForwardedFor : xRealIp;
    }
}
