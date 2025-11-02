package cn.xilio.etp.server.web.framework;

import cn.xilio.etp.server.web.ResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
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

            String path = getNormalPath(request.uri());

            // 新增：检查是否为静态资源请求
            if (StaticResourceHandler.isStaticResourceRequest(path)) {
                StaticResourceHandler.handleStaticResource(context);
            } else {
                // 原有的API路由逻辑
                RequestHandler handler = router.match(request.method(), path);
                if (handler == null) {
                    context.abortWithResponse(HttpResponseStatus.NOT_FOUND,
                            "{\"error\":\"接口不存在\",\"path\":\"" + path + "\"}");
                } else {
                    handler.handle(context);
                }
            }

            sendResponse(context);
        } catch (Exception e) {
            handleError(ctx, context, e);
            logger.error(e.getMessage(), e);
        }
    }

    private void sendResponse(RequestContext context) {
        if (context.getHttpResponse() != null) {
            context.getCtx().writeAndFlush(context.getHttpResponse());
            return;
        }

        // 原有的API响应逻辑
        String content = context.getResponseContent();
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                context.getResponseStatus(),
                Unpooled.copiedBuffer(content == null ? "" : content, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        context.getCtx().writeAndFlush(response);
    }

    private void handleError(ChannelHandlerContext ctx, RequestContext context, Exception e) {
        logger.error(e.getMessage(), e);
        if (e instanceof BizException biz) {
            String msg = "".equals(biz.getMessage()) ? "error" : biz.getMessage();
            ByteBuf buffer = Unpooled.copiedBuffer(ResponseEntity.of(1, msg).toJsonString(), CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    context.getResponseStatus(),
                    buffer);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            ctx.writeAndFlush(response);
        } else {
            //todo other
        }
    }

    private String getNormalPath(String uri) {
        int idx = uri.indexOf('?');
        return idx == -1 ? uri : uri.substring(0, idx);
    }
}
