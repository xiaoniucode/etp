package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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
        int port = -1;
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
                    }else {
                        domain = host;
                    }
                    int targetPort = ProxyManager.getLocalPortByDomain(domain);
                    if (targetPort == -1) {
                        logger.warn("没有该域名的代理服务");
                        ctx.close();
                        return;
                    }
                    port = targetPort;
                }
                isHttp = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            in.resetReaderIndex();
            sniffing = false;
        }
        visitor.attr(EtpConstants.TARGET_PORT).set(port);
        visitor.attr(EtpConstants.VISITOR_DOMAIN).set(domain);
        ctx.pipeline().remove(this);
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
            if (line.toLowerCase().startsWith("host:")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }
}
