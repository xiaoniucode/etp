package com.xiaoniucode.etp.server.handler.utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;

/**
 * HTTP 工具类
 * 提供常见的 HTTP 响应发送方法
 */
public class NettyHttpUtils {

    /**
     * 发送 HTTP 403 Forbidden 响应
     *
     * @param channel 通道
     */
    public static ChannelFuture sendHttp403(Channel channel) {
        String response = """
                HTTP/1.1 403 Forbidden\r
                Content-Type: text/plain\r
                Content-Length: 15\r
                \r
                Access Denied""";
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    public static ChannelFuture sendHttpTooManyRequests(Channel channel) {
        String response = """
                HTTP/1.1 429 Too Many Requests\r
                Content-Length: 0\r
                Retry-After: 1\r
                \r
                """;
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送认证
     */
    public static ChannelFuture sendBasicAuth(Channel channel) {
        String response = """
                HTTP/1.1 401 Unauthorized\r
                WWW-Authenticate: Basic realm="Authentication Required"\r
                Content-Type: text/html; charset=UTF-8\r
                Content-Length: 0\r
                Connection: close\r
                \r
                """;
        return channel.writeAndFlush(io.netty.buffer.Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送 HTTP 200 OK 响应
     *
     * @param channel 通道
     * @param content 响应内容
     */
    public static ChannelFuture sendHttp200(Channel channel, String content) {
        int contentLength = content.getBytes(CharsetUtil.UTF_8).length;
        String response = String.format("""
                HTTP/1.1 200 OK\r
                Content-Type: text/plain\r
                Content-Length: %d\r
                \r
                %s""", contentLength, content);
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送 HTTP 404 Not Found 响应
     *
     * @param channel 通道
     */
    public static ChannelFuture sendHttp404(Channel channel) {
        String response = """
                HTTP/1.1 404 Not Found\r
                Content-Type: text/plain\r
                Content-Length: 13\r
                \r
                Not Found""";
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送 HTTP 500 Internal Server Error 响应
     *
     * @param channel 通道
     */
    public static ChannelFuture sendHttp500(Channel channel) {
        String response = """
                HTTP/1.1 500 Internal Server Error\r
                Content-Type: text/plain\r
                Content-Length: 21\r
                \r
                Internal Server Error""";
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送 HTTP 400 Bad Request 响应
     *
     * @param channel 通道
     */
    public static ChannelFuture sendHttp400(Channel channel) {
        String response = """
                HTTP/1.1 400 Bad Request\r
                Content-Type: text/plain\r
                Content-Length: 11\r
                \r
                Bad Request""";
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    /**
     * 发送自定义 HTTP 响应
     *
     * @param channel       通道
     * @param statusCode    状态码
     * @param statusMessage 状态消息
     * @param contentType   内容类型
     * @param content       响应内容
     */
    public static ChannelFuture sendHttpResponse(Channel channel, int statusCode, String statusMessage, String contentType, String content) {
        int contentLength = content.getBytes(CharsetUtil.UTF_8).length;
        String response = String.format("""
                HTTP/1.1 %d %s\r
                Content-Type: %s\r
                Content-Length: %d\r
                \r
                %s""", statusCode, statusMessage, contentType, contentLength, content);
        return channel.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }
}
