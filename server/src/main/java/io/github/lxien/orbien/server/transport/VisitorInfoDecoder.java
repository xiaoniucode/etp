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

package io.github.lxien.orbien.server.transport;

import io.github.lxien.orbien.core.transport.AttributeKeys;
import io.github.lxien.orbien.core.enums.ProtocolType;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.github.lxien.orbien.core.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

/**
 * 首包嗅探 HTTP Host / BasicAuth。不得在 decode 内 remove handler，见 {@link io.github.lxien.orbien.server.transport.http.HeaderInjectDecoder}。
 */
public class VisitorInfoDecoder extends ByteToMessageDecoder {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(VisitorInfoDecoder.class);
    private boolean finished;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive() || !in.isReadable()) {
            return;
        }
        if (finished) {
            out.add(in.readRetainedSlice(in.readableBytes()));
            return;
        }
        Channel visitor = ctx.channel();
        if (in.readableBytes() < 8) {
            return;
        }
        in.markReaderIndex();
        boolean isHttp = false;
        boolean headersComplete = false;
        String domain = null;
        String basicAuth = null;
        try {
            int len = Math.min(in.readableBytes(), 8192);
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            String content = new String(bytes, CharsetUtil.UTF_8);

            if (isHttp(content)) {
                isHttp = true;
                headersComplete = content.contains("\r\n\r\n");
                String host = parseHost(content);
                if (host != null) {
                    if (host.contains(":")) {
                        domain = host.split(":")[0];
                    } else {
                        domain = host;
                    }
                    basicAuth = parseAuthHeader(content);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            in.resetReaderIndex();
        }
        if (isHttp) {
            // Host 可能还在后续 TCP 分片中：头未收齐前继续攒包，避免误关连接
            if (!StringUtils.hasText(domain) && !headersComplete && in.readableBytes() < 8192) {
                return;
            }
            visitor.attr(AttributeKeys.PROTOCOL_TYPE).set(ProtocolType.HTTP);
            visitor.attr(AttributeKeys.BASIC_AUTH_HEADER).set(basicAuth);
            if (!StringUtils.hasText(domain)) {
                // 头已收齐仍无 Host（扫描/HTTP1.0/畸形请求），尽早关闭避免后续改写分配 ByteBuf
                logger.debug("HTTP 请求缺少 Host，关闭连接");
                in.skipBytes(in.readableBytes());
                finished = true;
                ChannelUtils.closeOnFlush(visitor);
                return;
            }
            visitor.attr(AttributeKeys.VISIT_DOMAIN).set(domain);
        }
        out.add(in.readRetainedSlice(in.readableBytes()));
        finished = true;
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
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
}
