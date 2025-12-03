package cn.xilio.etp.server.web.server;

import io.netty.handler.codec.http.HttpMethod;

/**
 * 路由类
 *
 * @author liuxin
 */
public class Route {
    private final String path;
    private final HttpMethod method;
    private final RequestHandler handler;

    public Route(HttpMethod method, String path, RequestHandler handler) {
        this.method = method;
        this.handler = handler;
        this.path = path;
    }

    /**
     * 检查请求是否匹配当前路由，精确匹配，必须保证请求方法和路由都一致
     */
    public boolean matches(HttpMethod requestMethod, String uri) {
        if (!this.method.equals(requestMethod)) {
            return false;
        }
        return path.equals(uri);
    }


    public HttpMethod getMethod() {
        return method;
    }

    public RequestHandler getHandler() {
        return handler;
    }
}
