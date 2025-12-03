package cn.xilio.etp.server.web;

import cn.xilio.etp.common.JsonUtils;
import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.web.manager.CaptchaHolder;
import cn.xilio.etp.server.web.manager.TokenAuthService;
import cn.xilio.etp.server.web.server.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
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
                context.abortWithResponse(HttpResponseStatus.UNAUTHORIZED, ResponseEntity.error(401, "未登录").toJson());
                return;
            }
            String token = auth.substring(7);
            JSONObject authtoken = TokenAuthService.validateToken(token);
            if (authtoken == null) {
                context.abortWithResponse(HttpResponseStatus.UNAUTHORIZED, ResponseEntity.error(401, "登录过期").toJson());
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
        router.route(HttpMethod.POST, "/user/login", context -> {
            JSONObject req = JsonUtils.toJsonObject(context.getRequestBody());
            Session session = (Session) context.getAttribute("session");
            String captchaId = session.getAttribute("captchaId");
            req.put("captchaId", captchaId);
            context.setResponseJson(ResponseEntity.ok(ConfigService.login(req)).toJson());
        });
        router.route(HttpMethod.PUT, "/user/flush-token", context -> {
            String auth = context.getHeader("Authorization");
            String oldToken = (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
            JSONObject newToken = TokenAuthService.refreshToken(oldToken);
            if (newToken != null) {
                context.setResponseJson(ResponseEntity.ok(newToken).toJson());
            } else {
                context.setResponseJson(ResponseEntity.error(401, "无效的token").toJson());
            }
        });
        router.route(HttpMethod.DELETE, "/user/logout", context -> {
            String auth = context.getHeader("Authorization");
            if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
                TokenAuthService.invalidateToken(auth.substring(7));
            }
            context.setResponseJson(ResponseEntity.ok().toJson());
        });
        router.route(HttpMethod.GET, "/user/info", context -> {

        });
        router.route(HttpMethod.GET, "/captcha", context -> {
            try {
                CaptchaGenerator generator = new CaptchaGenerator();
                String code = generator.generateCaptcha();
                String captchaId = CaptchaHolder.put(code, 120);
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
            ConfigService.updateUserPassword((Integer) context.getAttribute("userId"), JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok().toJson());
        });
        router.route(HttpMethod.GET, "/client/list", context ->
                context.setResponseJson(ResponseEntity.ok(ConfigService.clients()).toJson()));
        router.route(HttpMethod.GET, "/client/get", context ->
                context.setResponseJson(ResponseEntity.ok(ConfigService.getClient(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
        router.route(HttpMethod.GET, "/proxy/get", context ->
                context.setResponseJson(ResponseEntity.ok(ConfigService.getProxy(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
        router.route(HttpMethod.GET, "/proxy/list", context ->
                context.setResponseJson(ResponseEntity.ok(ConfigService.proxies()).toJson()));
        router.route(HttpMethod.GET, "/metrics", context ->
                context.setResponseJson(ResponseEntity.ok(MetricsCollector.getAllMetrics()).toJson()));
        router.route(HttpMethod.GET, "/stats", context ->
                context.setResponseJson(ResponseEntity.ok(ConfigService.countStats()).toJson()));
        router.route(HttpMethod.POST, "/proxy/add", context -> {
            ConfigService.addProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/proxy/update", context -> {
            ConfigService.updateProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/proxy/switch-proxy-status", context -> {
            ConfigService.switchProxyStatus(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/proxy/del", context -> {
            ConfigService.deleteProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.POST, "/client/add", context -> {
            ConfigService.addClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.PUT, "/client/update", context -> {
            ConfigService.updateClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/client/del", context -> {
            ConfigService.deleteClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.DELETE, "/client/kickout", context -> {
            ConfigService.kickoutClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
    }
}
