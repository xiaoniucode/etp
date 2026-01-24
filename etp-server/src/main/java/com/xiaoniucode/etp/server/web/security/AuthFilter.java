package com.xiaoniucode.etp.server.web.security;

import com.xiaoniucode.etp.server.web.common.ResponseEntity;
import com.xiaoniucode.etp.server.web.core.server.Filter;
import com.xiaoniucode.etp.server.web.core.server.FilterChain;
import com.xiaoniucode.etp.server.web.core.server.RequestContext;
import com.xiaoniucode.etp.server.web.serivce.ServiceFactory;
import org.json.JSONObject;

import java.util.Set;

/**
 * 安全认证过滤器，令牌校验
 */
public class AuthFilter implements Filter {
    private static final Set<String> WHITE_LIST = Set.of("/api/user/login", "/login.html", "/api/captcha", "/layui/");

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
            context.abortWithResponse(ResponseEntity.error(401, "未登录").toJson());
            return;
        }
        String token = auth.substring(7);
        JSONObject authToken = ServiceFactory.INSTANCE.getAuthTokenService().validateToken(token);
        if (authToken == null) {
            context.abortWithResponse(ResponseEntity.error(401, "登录过期，请重新登录").toJson());
            return;
        }
        // 把用户信息放进上下文，后面业务方便用
        context.setAttribute("userId", authToken.getInt("uid"));
        context.setAttribute("username", authToken.getString("username"));
        context.setAttribute("auth_token", token);
        chain.doFilter();
    }

    @Override
    public int getOrder() {
        return Filter.super.getOrder();
    }
}
