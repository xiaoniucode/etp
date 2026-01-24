package com.xiaoniucode.etp.server.web;

import com.xiaoniucode.etp.common.utils.JsonUtils;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.web.common.ResponseEntity;
import com.xiaoniucode.etp.server.web.core.server.Filter;
import com.xiaoniucode.etp.server.web.core.server.Router;
import com.xiaoniucode.etp.server.web.core.server.Session;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import com.xiaoniucode.etp.server.web.security.AuthFilter;
import com.xiaoniucode.etp.server.web.serivce.ServiceFactory;
import com.xiaoniucode.etp.server.web.common.CaptchaGenerator;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;

import java.util.List;

/**
 * Dashboard 管理接口
 *
 * @author liuxin
 */
public class DashboardApi {

    public static void initFilters(List<Filter> filters) {
        filters.add(new AuthFilter());
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
        router.route(HttpMethod.GET, "/metrics/summery", context ->
                context.setResponseJson(ResponseEntity.ok(MetricsCollector.summaryMetrics()).toJson()));
        router.route(HttpMethod.GET, "/monitor", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getStatsService().monitorInfo()).toJson()));
        router.route(HttpMethod.GET, "/monitor/server", context ->
                context.setResponseJson(ResponseEntity.ok(ServiceFactory.INSTANCE.getStatsService().getServerInfo()).toJson()));

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
        router.route(HttpMethod.POST, "/proxy/add-http", context -> {
            ServiceFactory.INSTANCE.getProxyService().addHttpProxy(JsonUtils.toJsonObject(context.getRequestBody()));
            context.setResponseJson(ResponseEntity.ok("ok").toJson());
        });
        router.route(HttpMethod.POST, "/proxy/add-https", context -> {
            ServiceFactory.INSTANCE.getProxyService().addHttpsProxy(JsonUtils.toJsonObject(context.getRequestBody()));
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
