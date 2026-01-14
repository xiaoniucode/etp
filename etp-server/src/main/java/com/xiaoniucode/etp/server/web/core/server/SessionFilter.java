package com.xiaoniucode.etp.server.web.core.server;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import java.util.Set;

/**
 * session处理，用于为连接绑定session
 * @author liuxin
 */
public class SessionFilter implements Filter {

    private static final String COOKIE_NAME = "SESSIONID";

    @Override
    public void doFilter(RequestContext context, FilterChain chain) throws Exception {
        String sessionId = extractSessionId(context);
        Session session = SessionManager.getInstance().getSession(sessionId, true);
        // 把 session 绑定到当前请求
        context.setAttribute("session", session);
        // 设置 Cookie（首次或续期）
        context.addResponseHeader("Set-Cookie",
                ServerCookieEncoder.STRICT.encode(COOKIE_NAME, session.getId()) +
                        "; Path=/; HttpOnly; SameSite=Lax");
        try {
            chain.doFilter();
        } finally {
            // 请求结束时自动touch
            session.touch();
        }
    }

    @Override
    public int getOrder() {
        return -10000;
    }

    private String extractSessionId(RequestContext context) {
        String cookieHeader = context.getRequest().headers().get("Cookie");
        if (cookieHeader == null) {
            return null;
        }
        Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieHeader);
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.name())) {
                return cookie.value();
            }
        }
        return null;
    }
}
