package com.xiaoniucode.etp.client.manager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSessionManager {
    private static final Map<String, ServerSession> sessionIdToAgentSession = new ConcurrentHashMap<>();

    public static Optional<ServerSession> registerServerSession(ServerSession serverSession) {
        AgentSessionManager.getAgentSession().ifPresent(agentSession -> {
            serverSession.setAgentSession(agentSession);
            sessionIdToAgentSession.put(serverSession.getSessionId(), serverSession);
        });
        return Optional.of(serverSession);
    }
}
