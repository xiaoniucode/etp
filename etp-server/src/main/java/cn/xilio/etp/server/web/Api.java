package cn.xilio.etp.server.web;

import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.store.ConfigManager;
import cn.xilio.etp.server.store.dto.ClientDTO;
import cn.xilio.etp.server.store.dto.ProxyDTO;
import cn.xilio.etp.server.web.framework.*;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author liuxin
 */
public class Api {
    private static String authToken;

    public static void initFilters(List<Filter> filters) {
        filters.add(new Filter() {
            @Override
            public void doFilter(RequestContext context, FilterChain chain) throws Exception {
                chain.doFilter();
            }

            @Override
            public int getOrder() {
                return 0;
            }
        });
    }

    public static void initRoutes(Router router) {
        router.addRoute(HttpMethod.GET, "/clients", context -> context.setResponseContent(ResponseEntity.of(ConfigManager.clients()).toJsonString()));
        router.addRoute(HttpMethod.GET, "/proxies", context -> context.setResponseContent(ResponseEntity.of(ConfigManager.proxies()).toJsonString()));
        router.addRoute(HttpMethod.GET, "/metrics", context -> context.setResponseContent(ResponseEntity.of(MetricsCollector.getAllMetrics()).toJsonString()));
    }
}
