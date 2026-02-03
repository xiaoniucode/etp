package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.event.AgentRegisteredEvent;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class AgentSessionManager {
    private final Logger logger = LoggerFactory.getLogger(AgentSessionManager.class);
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
    /**
     * control channel -> remotePorts
     */
    private static final Map<Channel, Set<Integer>> controlToPorts = new ConcurrentHashMap<>();
    /**
     * control channel -> domains
     */
    private static final Map<Channel, Set<String>> controlToDomains = new ConcurrentHashMap<>();
    @Autowired
    private SessionIdGenerator sessionIdGenerator;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private EventBus eventBus;


    /**
     * 创建代理客户端会话
     * 1.注册系统中已经存在代理配置
     */
    public Optional<AgentSession> createAgentSession(String clientId, String token, Channel control, String arch, String os, String version) {
        //为客户端生成一个唯一的 sessionId
        String sessionId = sessionIdGenerator.nextAgentSessionId();
        AgentSession agentSession = new AgentSession(clientId, token, control, sessionId, arch, os, version);

        agentSession.getControl().attr(ChannelConstants.SESSION_ID).set(sessionId);
        sessionIdToAgentSession.putIfAbsent(sessionId, agentSession);

        tokenToAgentSessions.computeIfAbsent(token,
                k -> new CopyOnWriteArraySet<>()).add(agentSession);

        Set<ProxyConfig> proxyConfigs = proxyManager.getByClientId(clientId);
        for (ProxyConfig proxy : proxyConfigs) {
            ProtocolType protocol = proxy.getProtocol();
            if (ProtocolType.isTcp(protocol)) {
                Integer remotePort = proxy.getRemotePort();
                portToAgentSession.putIfAbsent(remotePort, agentSession);
                Set<Integer> ports = controlToPorts.computeIfAbsent(control,
                        k -> ConcurrentHashMap.newKeySet()
                );
                ports.add(remotePort);
                continue;
            }
            Set<String> domainMapping = controlToDomains.computeIfAbsent(control,
                    k -> ConcurrentHashMap.newKeySet()
            );

            if (ProtocolType.isHttp(protocol)) {
                Set<String> domains = proxy.getFullDomains();
                for (String domain : domains) {
                    domainToAgentSession.putIfAbsent(domain, agentSession);
                    domainMapping.add(domain.trim());
                }
            }
        }
        //发布代理客户端注册成功事件
        eventBus.publishAsync(new AgentRegisteredEvent(agentSession));
        return Optional.of(agentSession);
    }

    public void addPortToAgentSession(Integer remotePort) {
        AgentSessionContext.get().ifPresent(agentSession -> {
            portToAgentSession.putIfAbsent(remotePort, agentSession);
            controlToPorts.computeIfAbsent(agentSession.getControl(),
                    k -> ConcurrentHashMap.newKeySet()
            ).add(remotePort);
        });
    }

    public void addDomainsToAgentSession(Set<String> domains) {
        AgentSessionContext.get().ifPresent(agentSession -> {
            for (String domain : domains) {
                domainToAgentSession.putIfAbsent(domain, agentSession);
            }
            controlToDomains.computeIfAbsent(agentSession.getControl(),
                    k -> ConcurrentHashMap.newKeySet()
            ).addAll(domains);
        });
    }

    /**
     * 与代理客户端断开连接
     * 1.清理掉所有远程端口到代理客户端会话信息
     *
     */
    public void disconnect(Channel control) {
        getAgentSession(control).ifPresent(agentSession -> {
            String sessionId = agentSession.getSessionId();
            sessionIdToAgentSession.remove(sessionId);
            control.attr(ChannelConstants.SESSION_ID).set(null);

            String token = agentSession.getToken();
            //清理登陆令牌代理客户端映射
            Set<AgentSession> agentSessions = tokenToAgentSessions.get(token);
            if (agentSessions != null) {
                agentSessions.remove(agentSession);
                if (agentSessions.isEmpty()) {
                    tokenToAgentSessions.remove(token);
                }
            }
            Set<Integer> ports = controlToPorts.remove(control);
            if (ports != null) {
                //清理掉代理客户端所有端口映射信息
                for (Integer remotePort : ports) {
                    portToAgentSession.remove(remotePort);
                }
            }
            Set<String> domains = controlToDomains.remove(control);
            if (domains != null) {
                //清理掉代理客户端所有域名映射关系
                for (String domain : domains) {
                    domainToAgentSession.remove(domain);
                }
            }
        });
    }

    /**
     * 心跳更新，用于如果连接者长时间没有心跳，释放连接资源
     */
    public void updateHeartbeat(Channel control) {
        String sessionId = control.attr(ChannelConstants.SESSION_ID).get();
        if (sessionId == null) {
            return;
        }
        AgentSession agentSession = sessionIdToAgentSession.get(sessionId);
        if (agentSession != null) {
            agentSession.setLastHeartbeat(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("更新代理客户端最后心跳时间 - 会话标识={}", sessionId);
            }
        }
    }

    /**
     * 获取所有客户端会话信息
     * @return 客户端会话列表
     */
    public Collection<AgentSession> getAllAgentSessions() {
        return sessionIdToAgentSession.values();
    }

    public AgentSession getAgentSessionByPort(Integer remotePort) {
        return portToAgentSession.get(remotePort);
    }

    public AgentSession getAgentSessionByDomain(String domain) {
        return domainToAgentSession.get(domain);
    }


    public Optional<AgentSession> getAgentSession(Channel control) {
        if (control == null) {
            return Optional.empty();
        }
        String sessionId = control.attr(ChannelConstants.SESSION_ID).get();
        if (StringUtils.hasText(sessionId)) {
            return Optional.ofNullable(sessionIdToAgentSession.get(sessionId));
        }
        return Optional.empty();
    }

    public Set<Integer> getAgentRemotePorts(Channel control) {
        Set<Integer> remotePorts = controlToPorts.get(control);
        if (remotePorts == null) {
            return new HashSet<>();
        }
        return remotePorts;
    }

    public Set<String> getAgentDomains(Channel control) {
        Set<String> domains = controlToDomains.get(control);
        if (domains == null) {
            return new HashSet<>();
        }
        return domains;
    }

    /**
     * 获取令牌的会话数量
     *
     * @param token 登陆认证令牌
     * @return 会话数量
     */
    public Integer getOnlineAgents(String token) {
        Set<AgentSession> agentSessions = tokenToAgentSessions.get(token);
        if (agentSessions != null) {
            return agentSessions.size();
        }
        return 0;
    }
}
