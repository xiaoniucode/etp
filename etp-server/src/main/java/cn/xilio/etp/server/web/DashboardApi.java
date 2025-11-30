package cn.xilio.etp.server.web;

import cn.xilio.etp.common.JsonUtils;
import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.web.framework.*;
import io.netty.handler.codec.http.HttpMethod;

import java.util.List;

/**
 * Dashboard 管理、认证接口
 *
 * @author liuxin
 */
public class DashboardApi {
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
        router.addRoute(HttpMethod.GET, "/clients", context -> context.setResponseContent(ResponseEntity.ok(ConfigService.clients()).toJson()));
        router.addRoute(HttpMethod.GET, "/proxies", context -> context.setResponseContent(ResponseEntity.ok(ConfigService.proxies()).toJson()));
        router.addRoute(HttpMethod.GET, "/metrics", context -> context.setResponseContent(ResponseEntity.ok(MetricsCollector.getAllMetrics()).toJson()));
        router.addRoute(HttpMethod.GET, "/stats", context -> context.setResponseContent(ResponseEntity.ok(ConfigService.countStats()).toJson()));
        router.addRoute(HttpMethod.POST, "/add-proxy", context -> {
            ConfigService.saveProxy(JsonUtils.toJsonObject(context.getRequestBody()), false);
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.POST, "/update-proxy", context -> {
            ConfigService.saveProxy(JsonUtils.toJsonObject(context.getRequestBody()), true);
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/switch-proxy-status", context -> {
            ConfigService.switchProxyStatus(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/delete-proxy", context -> {
            ConfigService.deleteProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.POST, "/add-client", context -> {
            ConfigService.addClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/update-client", context -> {
            ConfigService.updateClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/delete-client", context -> {
            ConfigService.deleteClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/kickout-client", context -> {
            ConfigService.kickoutClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });


    }
}
