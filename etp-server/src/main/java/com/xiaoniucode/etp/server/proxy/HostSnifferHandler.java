package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.EtpConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析出域名
 */
public class HostSnifferHandler extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(HostSnifferHandler.class);
    private static final Map<String, Integer> DOMAIN_PORT_MAP = new HashMap<>();

    static {
        // todo 测试数据
        DOMAIN_PORT_MAP.put("a.local.cc", 8081);
        DOMAIN_PORT_MAP.put("b.local.cc", 8082);
        DOMAIN_PORT_MAP.put("localhost", 8081);
    }

    private static final int DEFAULT_PORT = 8081;
    private boolean sniffing = true;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!sniffing) {
            // 嗅探结束，直接透传
            out.add(in.retain());
            return;
        }
        if (in.readableBytes() < 8) {
            return;
        }
        in.markReaderIndex();
        int port = DEFAULT_PORT;
        boolean isHttp = false;
        try {
            int len = Math.min(in.readableBytes(), 4096);
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            String content = new String(bytes, CharsetUtil.UTF_8);

            if (isHttp(content)) {
                String host = parseHost(content);
                if (host != null) {
                    if (host.contains(":")) {
                        host = host.split(":")[0];
                    }
                    port = DOMAIN_PORT_MAP.getOrDefault(host, DEFAULT_PORT);
                }
                isHttp = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            in.resetReaderIndex();
            sniffing = false;
        }
        ctx.channel().attr(EtpConstants.TARGET_PORT).set(port);
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
