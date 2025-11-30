package cn.xilio.etp.server.web;

import cn.xilio.etp.common.JsonUtils;
import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.store.ConfigManager;
import cn.xilio.etp.server.web.dto.*;
import cn.xilio.etp.server.web.framework.*;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard 管理、认证接口
 *
 * @author liuxin
 */
public class DashboardApi {
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
        router.addRoute(HttpMethod.GET, "/clients",
                context -> {

                    context.setResponseContent(ResponseEntity.ok(ConfigManager.clients()).toJson());
                });
        router.addRoute(HttpMethod.GET, "/proxies", context -> context.setResponseContent(ResponseEntity.ok(ConfigManager.proxies()).toJson()));
        router.addRoute(HttpMethod.GET, "/metrics", context -> context.setResponseContent(ResponseEntity.ok(MetricsCollector.getAllMetrics()).toJson()));
        router.addRoute(HttpMethod.GET, "/stats", context -> {
//            int clientTotal = ConfigManager.clients();
//            long onlineClient = ConfigManager.clients().stream().filter(c -> c.status() == 1).count();
//            int mappingTotal = ConfigManager.proxies().size();
//            long startMapping = ConfigManager.proxies().stream().filter(c -> c.status() == 1).count();
//            context.setResponseContent(ResponseEntity.ok(new StatsCount(clientTotal, (int) onlineClient, mappingTotal, (int) startMapping)).toJson());
        });
        router.addRoute(HttpMethod.POST, "/add-proxy", context -> {
            String requestBody = context.getRequestBody();
            AddProxyReq bean = JsonUtils.toBean(requestBody, AddProxyReq.class);
            assert bean != null;
            ConfigManager.addProxy(bean.getSecretKey(), bean.getName(), bean.getProtocol(), bean.getLocalPort(), bean.getRemotePort(), bean.getStatus());
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/update-proxy-status", context -> {
            UpdateProxyStatusReq req = JsonUtils.toBean(context.getRequestBody(), UpdateProxyStatusReq.class);
            assert req != null;
            ConfigManager.switchProxyStatus(req.getSecretKey(), req.getRemotePort(), req.getStatus());
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/delete-proxy", context -> {
            DeleteProxyReq req = JsonUtils.toBean(context.getRequestBody(), DeleteProxyReq.class);
            assert req != null;
            ConfigManager.deleteProxy(req.getSecretKey(), req.getRemotePort());
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.POST, "/add-client", context -> {
            JSONObject req = JsonUtils.toJsonObject(context.getRequestBody());
            ConfigService.addClient(req);
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/update-client", context -> {
            Map<String, Object> req = JsonUtils.toMap(context.getRequestBody());
            assert req != null;
            ConfigManager.updateClient((String) req.get("secretKey"), (String) req.get("name"));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/delete-client", context -> {
            Map<String, Object> req = JsonUtils.toMap(context.getRequestBody());
            assert req != null;
            ConfigManager.deleteClient((String) req.get("secretKey"));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
    }
}
