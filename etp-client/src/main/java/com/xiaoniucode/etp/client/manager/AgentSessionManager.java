package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.EtpConstants;
import io.netty.channel.Channel;

import java.util.Optional;

public class AgentSessionManager {
    private static AgentSession agentSession;

    private AgentSessionManager() {
    }

    public static Optional<AgentSession> createAgentSession(String clientId, String sessionId, Channel control) {
        if (clientId == null || sessionId == null || control == null) {
            return Optional.empty();
        }
        AgentSession agentSession = new AgentSession(clientId, sessionId, control);
        String token = control.attr(EtpConstants.TOKEN).get();
        String serverAddr = control.attr(EtpConstants.SERVER_DDR).get();
        Integer serverPort = control.attr(EtpConstants.SERVER_PORT).get();

        agentSession.setToken(token);
        agentSession.setServerAddr(serverAddr);
        agentSession.setServerPort(serverPort);
        return Optional.of(agentSession);
    }

    public static Optional<AgentSession> getAgentSession() {
        return Optional.of(agentSession);
    }

    public static void removeAgentSession() {
        agentSession = null;
    }
}
