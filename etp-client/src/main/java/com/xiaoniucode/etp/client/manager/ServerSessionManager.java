package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.client.manager.domain.AgentSession;
import com.xiaoniucode.etp.client.manager.domain.ServerSession;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.LanInfo;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerSessionManager.class);
    private static final Map<String, ServerSession> sessionIdToAgentSession = new ConcurrentHashMap<>();

    public static Optional<ServerSession> createServerSession(String sessionId, Channel tunnel, Channel server, LanInfo lanInfo) {
        if (!StringUtils.hasText(sessionId) || tunnel == null || server == null) {
            logger.warn("会话创建失败，参数不合法");
            return Optional.empty();
        }
        Optional<AgentSession> optional = AgentSessionManager.getAgentSession();
        if (optional.isPresent()) {
            AgentSession agentSession = optional.get();
            ServerSession serverSession = new ServerSession(sessionId, tunnel, server, lanInfo, agentSession);
            server.attr(ChannelConstants.SESSION_ID).set(sessionId);
            sessionIdToAgentSession.put(serverSession.getSessionId(), serverSession);
            logger.debug("隧道会话创建成功：[会话标识={}]", serverSession.getSessionId());
            return Optional.of(serverSession);
        }
        return Optional.empty();
    }

    public static Optional<ServerSession> removeServerSession(Channel server) {
        String sessionId = server.attr(ChannelConstants.SESSION_ID).get();
        if (sessionId == null) {
            return Optional.empty();
        }
        server.attr(ChannelConstants.SESSION_ID).setIfAbsent(null);
        ServerSession remove = sessionIdToAgentSession.remove(sessionId);
        return Optional.ofNullable(remove);
    }

    public static Optional<ServerSession> getServerSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }
        ServerSession serverSession = sessionIdToAgentSession.get(sessionId);
        return Optional.ofNullable(serverSession);
    }

    public static Optional<ServerSession> getServerSession(Channel server) {
        String sessionId = server.attr(ChannelConstants.SESSION_ID).get();
        return getServerSession(sessionId);
    }

    public static void removeAllServerSession() {
        Iterator<ServerSession> iterator = sessionIdToAgentSession.values().iterator();
        while (iterator.hasNext()) {
            ServerSession session = iterator.next();
            Channel server = session.getServer();
            //断开与服务的连接
            ChannelUtils.closeOnFlush(server);
            iterator.remove();
            LanInfo lanInfo = session.getLanInfo();
            logger.debug("删除隧道 - [会话标识={}，目标地址={}，目标端口={}]", session.getSessionId(), lanInfo.getLocalIP(), lanInfo.getLocalIP());
        }
        sessionIdToAgentSession.clear();
    }
}
