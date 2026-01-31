package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.AgentRegisteredEvent;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
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
    private ProxyManager proxyManager;
    @Autowired
    private EventBus eventBus;

    /**
     * 创建代理客户端会话
     */
    public Optional<AgentSession> createAgentSession(String clientId, String token, Channel control, String arch, String os) {
        //为客户端生成一个唯一的 sessionId
        String sessionId = sessionIdGenerator.nextAgentSessionId();
        AgentSession agentSession = new AgentSession(clientId, token, control, sessionId, arch, os);

        agentSession.getControl().attr(EtpConstants.SESSION_ID).set(sessionId);
        sessionIdToAgentSession.putIfAbsent(sessionId, agentSession);

        tokenToAgentSessions.computeIfAbsent(token,
                k -> new CopyOnWriteArraySet<>()).add(agentSession);

        Set<ProxyConfig> proxyConfigs = proxyManager.getByClientId(clientId);
        for (ProxyConfig proxy : proxyConfigs) {
            ProtocolType protocol = proxy.getProtocol();
            if (ProtocolType.isTcp(protocol)) {
                portToAgentSession.putIfAbsent(proxy.getRemotePort(), agentSession);
                continue;
            }
            if (ProtocolType.isHttp(protocol)) {
                Set<String> domains = proxy.getFullDomains();
                for (String domain : domains) {
                    domainToAgentSession.putIfAbsent(domain, agentSession);
                }
            }
        }
        //发布代理客户端注册成功事件
        eventBus.publishAsync(new AgentRegisteredEvent(agentSession));
        return Optional.of(agentSession);
    }

    public void disconnect(Channel control) {
        String sessionId = control.attr(EtpConstants.SESSION_ID).get();
        disconnect(sessionId);
    }

    /**
     * 与代理客户端断开连接
     *
     * @param sessionId sessionId
     */
    public void disconnect(String sessionId) {
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
        ChannelUtils.closeOnFlush(control);
        //清理自动注册的客户端以及数据记录
        //todo 还需要清理visitor相关资源、启动的服务资源
        //需要清空与该agent有关的所有session连接
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
