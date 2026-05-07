package com.xiaoniucode.etp.server.transport.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class HeaderInjectHandler extends ByteToMessageDecoder {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HeaderInjectHandler.class);

    private boolean injected = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() == 0 || injected) {
            return;
        }

        int readableBytes = in.readableBytes();
        in.markReaderIndex();
        byte[] bytes = new byte[readableBytes];
        in.readBytes(bytes);
        String content = new String(bytes, CharsetUtil.UTF_8);
        in.resetReaderIndex();

        if (!isHttp(content)) {
            // 直接传递原始数据，并消费
            out.add(in.readRetainedSlice(readableBytes));
            injected = true;
            return;
        }

        int headerEnd = content.indexOf("\r\n\r\n");
        if (headerEnd < 0) {
            return;
        }

        String visitorIp = getVisitorIp(ctx);

        // 构建新的 HeaderBuf（追加 X-Forwarded-For）
        String newHeaderStr = buildXForwardedForLine(content, visitorIp);
        byte[] newHeaderBytes = newHeaderStr.getBytes(CharsetUtil.UTF_8);
        ByteBuf customHeaderBuf = ctx.alloc().buffer(newHeaderBytes.length);
        customHeaderBuf.writeBytes(newHeaderBytes);

        // 切分原始 body
        ByteBuf bodyBuf = in.readRetainedSlice(readableBytes - (headerEnd + 4));
        in.skipBytes(headerEnd + 4); // readerIndex 移动到末尾

        // 拼接最终 ByteBuf
        CompositeByteBuf finalBuf = ctx.alloc().compositeBuffer();
        finalBuf.addComponents(true, customHeaderBuf, bodyBuf);

        out.add(finalBuf);
        injected = true;
        logger.debug("[HTTP] 注入 X-Forwarded-For 完成，IP={}", visitorIp);
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

    private String getVisitorIp(ChannelHandlerContext ctx) {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        if (addr == null || addr.getAddress() == null) {
            return "unknown";
        }
        return addr.getAddress().getHostAddress();
    }

    private String buildXForwardedForLine(String content, String clientIp) {
        String[] lines = content.split("\\r?\\n");
        StringBuilder newHeader = new StringBuilder();
        boolean xffFound = false;

        for (String line : lines) {
            if (line.toLowerCase().startsWith("x-forwarded-for:")) {
                xffFound = true;
                newHeader.append("X-Forwarded-For: ").append(line.substring(15).trim())
                        .append(", ").append(clientIp).append("\r\n");
            } else if (line.isEmpty()) {
                if (!xffFound) {
                    newHeader.append("X-Forwarded-For: ").append(clientIp).append("\r\n");
                    xffFound = true;
                }
                newHeader.append("\r\n");
            } else {
                newHeader.append(line).append("\r\n");
            }
        }

        if (!xffFound) {
            newHeader.append("X-Forwarded-For: ").append(clientIp).append("\r\n\r\n");
        }

        return newHeader.toString();
    }
}