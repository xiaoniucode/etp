package cn.xilio.etp.server.web;

import cn.xilio.etp.common.JsonUtils;
import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.server.metrics.MetricsCollector;
import cn.xilio.etp.server.web.framework.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dashboard 管理、认证接口
 *
 * @author liuxin
 */
public class DashboardApi {
    private static final Set<String> WHITE_LIST = Set.of("/api/user/login", "/login.html", "/api/captcha", "/layui/");

    public static void initFilters(List<Filter> filters) {
        filters.add(new Filter() {
            @Override
            public void doFilter(RequestContext context, FilterChain chain) throws Exception {
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
            }

            @Override
            public int getOrder() {
                return -999;
            }
        });
    }

    public static void initRoutes(Router router) {
        router.setRoutePrefix("/api");
        router.addRoute(HttpMethod.POST, "/user/login", context -> {
            JSONObject req = JsonUtils.toJsonObject(context.getRequestBody());
            Map<String, String> headers = context.getHeaders();
            req.put("captchaId", headers.get("captchaId"));
            context.setResponseContent(ResponseEntity.ok(ConfigService.login(req)).toJson());
        });
        router.addRoute(HttpMethod.PUT, "/user/flush-token", context -> {
            String auth = context.getHeader("Authorization");
            String oldToken = (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
            JSONObject newToken = TokenAuthService.refreshToken(oldToken);
            if (newToken != null) {
                context.setResponseContent(ResponseEntity.ok(newToken).toJson());
            } else {
                context.setResponseContent(ResponseEntity.error(401, "无效的token").toJson());
            }
        });
        router.addRoute(HttpMethod.DELETE, "/user/logout", context -> {
            String auth = context.getHeader("Authorization");
            if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
                TokenAuthService.invalidateToken(auth.substring(7));
            }
            context.setResponseContent(ResponseEntity.ok().toJson());
        });
        router.addRoute(HttpMethod.GET, "/user/info", context -> {

        });
        router.addRoute(HttpMethod.GET, "/captcha", context -> {
            try {
                CaptchaGenerator generator = new CaptchaGenerator();
                CaptchaGenerator.CaptchaResult result = generator.generateCaptcha();
                String captchaId = CaptchaHolder.put(result.code(), 300);  // 5分钟有效
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(result.image(), "JPEG", baos);
                byte[] imageData = baos.toByteArray();
                context.addResponseHeader("captchaId", captchaId);
                context.setResponseData(imageData, "image/jpeg");
            } catch (Exception e) {
                throw new RuntimeException("生成验证码失败", e);
            }
        });
        router.addRoute(HttpMethod.PUT, "/user/update", context -> {
            ConfigService.updateUserPassword((Integer) context.getAttribute("userId"), JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok().toJson());
        });
        router.addRoute(HttpMethod.GET, "/client/list", context ->
                context.setResponseContent(ResponseEntity.ok(ConfigService.clients()).toJson()));
        router.addRoute(HttpMethod.GET, "/client/get", context ->
                context.setResponseContent(ResponseEntity.ok(ConfigService.getClient(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
        router.addRoute(HttpMethod.GET, "/proxy/get", context ->
                context.setResponseContent(ResponseEntity.ok(ConfigService.getProxy(JsonUtils.toJsonObject(context.getQueryParams()))).toJson()));
        router.addRoute(HttpMethod.GET, "/proxy/list", context ->
                context.setResponseContent(ResponseEntity.ok(ConfigService.proxies()).toJson()));
        router.addRoute(HttpMethod.GET, "/metrics", context ->
                context.setResponseContent(ResponseEntity.ok(MetricsCollector.getAllMetrics()).toJson()));
        router.addRoute(HttpMethod.GET, "/stats", context ->
                context.setResponseContent(ResponseEntity.ok(ConfigService.countStats()).toJson()));
        router.addRoute(HttpMethod.POST, "/proxy/add", context -> {
            ConfigService.addProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/proxy/update", context -> {
            ConfigService.updateProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/proxy/switch-proxy-status", context -> {
            ConfigService.switchProxyStatus(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/proxy/del", context -> {
            ConfigService.deleteProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.POST, "/client/add", context -> {
            ConfigService.addClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.PUT, "/client/update", context -> {
            ConfigService.updateClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/client/del", context -> {
            ConfigService.deleteClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
        router.addRoute(HttpMethod.DELETE, "/client/kickout", context -> {
            ConfigService.kickoutClient(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseContent(ResponseEntity.ok("ok").toJson());
        });
    }
}
