package com.xiaoniucode.etp.client.manager;

import java.util.Optional;

public class AgentSessionManager {
    private static AgentSession agentSession;

    private AgentSessionManager() {
    }

    public static Optional<AgentSession> setAgentSession(AgentSession agentSession) {
        if (agentSession == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(agentSession);
    }

    public static Optional<AgentSession> getAgentSession() {
        return Optional.of(agentSession);
    }

    public static void removeAgentSession() {
        agentSession = null;
    }
}
