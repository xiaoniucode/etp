package com.xiaoniucode.etp.server.web.security;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.domain.LoginToken;
import com.xiaoniucode.etp.server.web.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 安全认证拦截器，令牌校验
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(401, "未登录");
        }
        String token = auth.substring(7);
        LoginToken loginToken = authService.validateToken(token);
        if (loginToken == null) {
            throw new BizException(401, "登录过期，请重新登录");
        }
        // 把用户信息放进请求属性，后面业务方便用
        request.setAttribute("userId", loginToken.getUsername());
        request.setAttribute("auth_token", token);
        return true;
    }
}
