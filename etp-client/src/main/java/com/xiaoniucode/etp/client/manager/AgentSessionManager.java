package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.client.manager.domain.AgentSession;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import io.netty.channel.Channel;

import java.util.Optional;
import java.util.function.Consumer;

public class AgentSessionManager {
    private static AgentSession agentSession;

    private AgentSessionManager() {
    }

    public static Optional<AgentSession> createAgentSession(String clientId, String sessionId, Channel control) {
        if (clientId == null || sessionId == null || control == null) {
            return Optional.empty();
        }
        AgentSession agentSession = new AgentSession(clientId, sessionId, control);
        String token = control.attr(ChannelConstants.TOKEN).get();
        String serverAddr = control.attr(ChannelConstants.SERVER_DDR).get();
        Integer serverPort = control.attr(ChannelConstants.SERVER_PORT).get();

        agentSession.setToken(token);
        agentSession.setServerAddr(serverAddr);
        agentSession.setServerPort(serverPort);

        AgentSessionManager.agentSession = agentSession;
        return Optional.of(agentSession);
    }

    public static Optional<AgentSession> getAgentSession() {
        return Optional.ofNullable(agentSession);
    }

    public static Optional<Channel> getControl() {
        Optional<AgentSession> agent = getAgentSession();
        if (agent.isPresent()) {
            AgentSession session = agent.get();
            return Optional.ofNullable(session.getControl());
        }
        return Optional.empty();
    }

    public static void removeAgentSession() {
        removeAgentSession(null);
    }

    public static void removeAgentSession(Consumer<AgentSession> callback) {
        if (callback != null) {
            callback.accept(agentSession);
        }
        AgentSessionManager.agentSession = null;
    }
}
