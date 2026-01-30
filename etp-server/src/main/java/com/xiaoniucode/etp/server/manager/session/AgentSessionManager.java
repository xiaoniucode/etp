package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.ClientDisconnectEvent;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import com.xiaoniucode.etp.server.manager.domain.ProxyConfigExt;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class AgentSessionManager {
    private final Logger logger = LoggerFactory.getLogger(AgentSessionManager.class);
    /**
     * 连接超时时间 5分钟
     */
    private static final long HEARTBEAT_TIMEOUT = 300000L;
    /**
     * 定时任务运行间隔时间1分钟
     */
    private static final long CLEANUP_INTERVAL = 60000L;

    /**
     * Token -> AgentSessions
     */
    private final Map<String, Set<AgentSession>> tokenToAgentSessions = new ConcurrentHashMap<>();
    /**
     * sessionId ->AgentSession
     */
    private final Map<String, AgentSession> sessionIdToAgentSession = new ConcurrentHashMap<>();
    /**
     * remotePort -> Agent连接信息
     */
    private static final Map<Integer, AgentSession> portToAgentSession = new ConcurrentHashMap<>();
    /**
     * domain -> Agent连接信息
     */
    private static final Map<String, AgentSession> domainToAgentSession = new ConcurrentHashMap<>();
    @Autowired
    private SessionIdGenerator sessionIdGenerator;
    @Autowired
    private EventBus eventBus;

    public AgentSession registerAgent(AgentSession agentSession) {
        String sessionId = sessionIdGenerator.nextAgentSessionId();
        agentSession.setSessionId(sessionId);
        agentSession.getControl().attr(EtpConstants.SESSION_ID).set(sessionId);

        sessionIdToAgentSession.putIfAbsent(sessionId, agentSession);

        String token = agentSession.getToken();

        tokenToAgentSessions.computeIfAbsent(token, k -> new CopyOnWriteArraySet<>())
                .add(agentSession);

        ProxyManager proxyManager = BeanHelper.getBean(ProxyManager.class);
        List<ProxyConfigExt> proxyConfigs = proxyManager.getProxyConfigsBySessionId(sessionId);
        for (ProxyConfigExt proxy : proxyConfigs) {
            ProtocolType protocol = proxy.getProtocol();
            if (ProtocolType.isTcp(protocol)) {
                portToAgentSession.putIfAbsent(proxy.getRemotePort(), agentSession);
            }
            if (ProtocolType.isHttp(protocol)) {
                Set<String> domains = proxy.getDomains();
                for (String domain : domains) {
                    domainToAgentSession.putIfAbsent(domain, agentSession);
                }
            }
        }
        return agentSession;
    }

    public synchronized void disconnect(Channel control) {
        String sessionId = control.attr(EtpConstants.SESSION_ID).get();
        disconnect(sessionId);
    }

    /**
     * 与代理客户端断开连接
     *
     * @param sessionId sessionId
     */
    public synchronized void disconnect(String sessionId) {
        AgentSession agentSession = sessionIdToAgentSession.remove(sessionId);
        if (agentSession == null) {
            return;
        }
        Channel control = agentSession.getControl();
        control.attr(EtpConstants.SESSION_ID).set(null);

        String token = agentSession.getToken();
        Set<AgentSession> agentSessions = tokenToAgentSessions.get(token);
        agentSessions.remove(agentSession);
        if (agentSessions.isEmpty()) {
            tokenToAgentSessions.remove(token);
        }
        //清理自动注册的客户端以及数据记录
        //todo 还需要清理visitor相关资源、启动的服务资源
        //需要清空与该agent有关的所有session连接
        eventBus.publishAsync(new ClientDisconnectEvent());
    }

    /**
     * 心跳更新，用于如果连接者长时间没有心跳，释放连接资源
     */
    public void updateHeartbeat(Channel control) {
        String sessionId = control.attr(EtpConstants.SESSION_ID).get();
        if (sessionId == null) {
            return;
        }
        AgentSession agentSession = sessionIdToAgentSession.get(sessionId);
        if (agentSession != null) {
            agentSession.setLastHeartbeat(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("Heartbeat updated for session: {}", sessionId);
            }
        }
    }

    /**
     * 自动清理超时 session 会话
     *
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessionIdToAgentSession.values().stream()
                .filter(conn -> (now - conn.getLastHeartbeat()) > HEARTBEAT_TIMEOUT)
                .forEach(conn -> {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Cleaning up expired agent session connection: {}", conn.getSessionId());
                    }
                    disconnect(conn.getSessionId());
                });
    }

    public AgentSession getAgentSessionByPort(Integer remotePort) {
        return portToAgentSession.get(remotePort);
    }

    public AgentSession getAgentSessionByDomain(String domain) {
        return domainToAgentSession.get(domain);
    }
}
