package com.xiaoniucode.etp.server.web;

import com.xiaoniucode.etp.common.JsonUtils;
import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.core.server.Filter;
import com.xiaoniucode.etp.server.web.core.server.Router;
import com.xiaoniucode.etp.server.web.core.server.Session;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import com.xiaoniucode.etp.server.web.serivce.ServiceFactory;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Dashboard 管理、认证接口
 *
 * @author liuxin
 */
public class DashboardApi {
    private static final Set<String> WHITE_LIST = Set.of("/api/user/login", "/login.html", "/api/captcha", "/layui/");

    public static void initFilters(List<Filter> filters) {
        filters.add((context, chain) -> {
            String path = context.getRequest().uri();
            // 白名单直接放行
            if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
                chain.doFilter();
                return;
            }
            //只有接口才检查登录
            if (!path.startsWith("/api")) {
                chain.doFilter();
                return;
            }
            String auth = context.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                context.abortWithResponse(ResponseEntity.error(401, "未登录").toJson());
                return;
            }
            String token = auth.substring(7);
            JSONObject authtoken = ServiceFactory.INSTANCE.getAuthTokenService().validateToken(token);
            if (authtoken == null) {
                context.abortWithResponse(ResponseEntity.error(401, "登录过期，请重新登录").toJson());
                return;
            }
            // 把用户信息放进上下文，后面业务方便用
            context.setAttribute("userId", authtoken.getInt("uid"));
            context.setAttribute("username", authtoken.getString("username"));
            context.setAttribute("auth_token", token);
            chain.doFilter();
        });
    }

    public static void initRoutes(Router router) {
        router.setRoutePrefix("/api");
        //用户接口
        router.route(HttpMethod.POST, "/user/login", context -> {
            JSONObject req = JsonUtils.toJsonObject(context.getRequestBody());
            Session session = (Session) context.getAttribute("session");
            String captchaId = session.getAttribute("captchaId");
            req.put("captchaId", captchaId);
            context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getUserService().login(req)).toJson());
        });
        router.route(HttpMethod.DELETE, "/user/logout", context -> {
            String auth = context.getHeader("Authorization");
            if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
                ServiceFactory.INSTANCE.getAuthTokenService().invalidateToken(auth.substring(7));
            }
            context.setResponseJson(ResponseEntity.ok().toJson());
        });
        router.route(HttpMethod.GET, "/user/info", context -> {

        });
        router.route(HttpMethod.GET, "/captcha", context -> {
            try {
                CaptchaGenerator generator = new CaptchaGenerator();
                String code = generator.generateCaptcha();
                String captchaId = CaptchaManager.put(code, 120);
                Session session = (Session) context.getAttribute("session");
                session.setAttribute("captchaId", captchaId);
                context.addResponseHeader("captchaId", captchaId);
                JSONObject captcha = new JSONObject();
                captcha.put("captchaId", captchaId);
                captcha.put("code", code);
                context.setResponseJson(ResponseEntity.ok(captcha).toJson());
            } catch (Exception e) {
                throw new RuntimeException("生成验证码失败", e);
            }
        });
        router.route(HttpMethod.PUT, "/user/update", context -> {
            ServiceFactory.INSTANCE.getUserService().updatePassword((Integer) context.getAttribute("userId"), JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok().toJson());
        });
        //数据统计接口
        router.route(HttpMethod.GET, "/metrics", context ->
                context.setResponseJson(ResponseEntity.ok(MetricsCollector.getAllMetrics()).toJson()));
        router.route(HttpMethod.GET, "/monitor", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getStatsService().monitorInfo()).toJson()));
        //代理配置接口
        router.route(HttpMethod.GET, "/proxy/get", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getProxyService().getProxy(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
        router.route(HttpMethod.GET, "/proxy/list", context -> {
                    String type = (String) context.getQueryParam("type");
                    context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getProxyService().proxies(type)).toJson());
                }
        );
        router.route(HttpMethod.POST, "/proxy/add-tcp", context -> {
            ServiceFactory.INSTANCE.getProxyService().addTcpProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/proxy/update-tcp", context -> {
            ServiceFactory.INSTANCE.getProxyService().updateTcpProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/proxy/switch-proxy-status", context -> {
            ServiceFactory.INSTANCE.getProxyService().switchProxyStatus(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/proxy/del", context -> {
            ServiceFactory.INSTANCE.getProxyService().deleteProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        //客户端接口
        router.route(HttpMethod.POST, "/client/add", context -> {
            ServiceFactory.INSTANCE.getClientService().addClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/client/update", context -> {
            ServiceFactory.INSTANCE.getClientService().updateClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/client/del", context -> {
            ServiceFactory.INSTANCE.getClientService().deleteClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/client/kickout", context -> {
            ServiceFactory.INSTANCE.getClientService().kickoutClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.GET, "/client/list", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getClientService().clients()).toJson()));
        router.route(HttpMethod.GET, "/client/get", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getClientService().getClient(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
    }
}
