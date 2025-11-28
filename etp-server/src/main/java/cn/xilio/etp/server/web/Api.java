package cn.xilio.etp.server.web;

import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.store.ConfigManager;
import cn.xilio.etp.server.web.framework.*;
import io.netty.handler.codec.http.HttpMethod;

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
        router.addRoute(HttpMethod.GET, "/stats", context -> {
            int clientTotal = ConfigManager.clients().size();
            long onlineClient = ConfigManager.clients().stream().filter(c -> c.status() == 1).count();
            int mappingTotal = ConfigManager.proxies().size();
            long startMapping = ConfigManager.proxies().stream().filter(c -> c.status() == 1).count();
            context.setResponseContent(ResponseEntity.of(new StatsCount(clientTotal, (int) onlineClient, mappingTotal, (int) startMapping)).toJsonString());
        });
    }
}
