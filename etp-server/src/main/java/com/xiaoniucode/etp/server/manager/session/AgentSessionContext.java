package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.server.manager.domain.AgentSession;

import java.util.Optional;

/**
 * 代理客户端上下文
 */
public class AgentSessionContext {
    private static final ThreadLocal<AgentSession> AGENT_SESSION_THREAD_LOCAL = new ThreadLocal<>();

    public static void set(AgentSession agentSession) {
        AGENT_SESSION_THREAD_LOCAL.set(agentSession);
    }

    public static Optional<AgentSession> get() {
        return Optional.ofNullable(AGENT_SESSION_THREAD_LOCAL.get());
    }

    public static void clear() {
        AGENT_SESSION_THREAD_LOCAL.remove();
    }

    public static boolean exists() {
        return AGENT_SESSION_THREAD_LOCAL.get() != null;
    }
}