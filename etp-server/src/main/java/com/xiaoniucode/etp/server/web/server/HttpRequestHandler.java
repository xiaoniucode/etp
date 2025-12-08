package com.xiaoniucode.etp.server.web.server;

import com.xiaoniucode.etp.server.web.ResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Netty HTTP请求处理器
 *
 * @author liuxin
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<Filter> filters;
    private final Router router;

    public HttpRequestHandler(List<Filter> filters, Router router) {
        this.filters = filters;
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        RequestContext context = new RequestContext(ctx, request);
        try {
            FilterChain filterChain = new FilterChain(context);
            filters.forEach(filterChain::addFilter);
            filterChain.doFilter();
            if (context.isAborted()) {
                sendResponse(context);
                return;
            }
            String path = getNormalPath(request.uri());
            if (StaticResourceHandler.isStaticResourceRequest(path)) {
                StaticResourceHandler.handleStaticResource(context);
            }
            RequestHandler handler = router.match(request.method(), path);
            if (handler == null) {
                context.abortWithResponse(HttpResponseStatus.NOT_FOUND, "{\"error\":\"资源不存在\",\"path\":\"" + path + "\"}");
            } else {
                handler.handle(context);
            }
            sendResponse(context);
        } catch (Throwable e) {
            handleError(ctx, context, e);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 设置通用的响应头
     */
    private void setCommonResponseHeaders(FullHttpResponse response, RequestContext context) {
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.headers().set(HttpHeaderNames.PRAGMA, "no-cache");
        response.headers().set(HttpHeaderNames.EXPIRES, "0");
        Map<String, String> headers = context.getResponseHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.headers().set(entry.getKey(), entry.getValue());
        }
    }

    private void sendImageResponse(RequestContext context, byte[] imageData, String contentType) {
        ByteBuf buffer = Unpooled.copiedBuffer(imageData);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                context.getResponseStatus() != null ? context.getResponseStatus() : HttpResponseStatus.OK,
                buffer
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        setCommonResponseHeaders(response, context);
        context.getCtx().writeAndFlush(response);
    }

    private void sendResponse(RequestContext context) {
        if (context.getHttpResponse() != null) {
            context.getCtx().writeAndFlush(context.getHttpResponse());
            return;
        }
        if (context.getResponseData() != null) {
            sendImageResponse(context, context.getResponseData(), context.getResponseContentType());
            return;
        }
        String content = context.getResponseContent();
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                context.getResponseStatus(),
                Unpooled.copiedBuffer(content == null ? "" : content, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        setCommonResponseHeaders(response, context);
        context.getCtx().writeAndFlush(response);
    }

    private void handleError(ChannelHandlerContext ctx, RequestContext context, Throwable e) {
        logger.error(e.getMessage(), e);
        if (e instanceof BizException biz) {
            String msg = "".equals(biz.getMessage()) ? "error" : biz.getMessage();
            ByteBuf buffer = Unpooled.copiedBuffer(ResponseEntity.error(1, msg).toJson(), CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    context.getResponseStatus(),
                    buffer);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            setCommonResponseHeaders(response, context);
            ctx.writeAndFlush(response);
        } else {
        }
    }

    private String getNormalPath(String uri) {
        int idx = uri.indexOf('?');
        return idx == -1 ? uri : uri.substring(0, idx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
