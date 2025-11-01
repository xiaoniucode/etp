package cn.xilio.etp.server.web.framework;

import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由管理
 *
 * @author liuxin
 */
public class Router {
    private final List<Route> routes = new ArrayList<>();

    /**
     * 添加路由，路由采用精确匹配
     *
     * @param method  请求方法
     * @param path    路由，精确匹配
     * @param handler 处理器
     */
    public void addRoute(HttpMethod method, String path, RequestHandler handler) {
        routes.add(new Route(method, path, handler));
    }

    public RequestHandler match(HttpMethod method, String uri) {
        for (Route route : routes) {
            if (route.matches(method, uri)) {
                return route.getHandler();
            }
        }
        return null;
    }
}

