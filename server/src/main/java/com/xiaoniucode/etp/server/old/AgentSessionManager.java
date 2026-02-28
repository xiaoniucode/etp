package com.xiaoniucode.etp.server.old;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.constant.AttributeKeys;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.generator.ConnectionIdGenerator;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@Component
public class AgentSessionManager {
    private final Logger logger = LoggerFactory.getLogger(AgentSessionManager.class);
    /**
     * Token -> AgentSessions
     */
    private final Map<String, Set<AgentSession>> tokenToAgentSessions = new ConcurrentHashMap<>();
    /**
     * clientId --> AgentSession
     */
    private final Map<String, AgentSession> clientIdToAgentSession = new ConcurrentHashMap<>();
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
    private ConnectionIdGenerator sessionIdGenerator;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private ClientManager clientManager;
    @Autowired
    private HashedWheelTimer wheelTimer;
    private static final long HEARTBEAT_TIMEOUT = 60;

    /**
     * 创建代理客户端会话
     * 1.注册系统中已经存在代理配置
     */
    public Optional<AgentSession> createAgentSession(AgentSession.AgentSessionBuilder builder) {
        //为客户端生成一个唯一的 sessionId
        String sessionId = "sessionIdGenerator.nextSessionId()";
        AgentSession tempSession = builder
                .sessionId(sessionId)
                .build();
        String clientId = tempSession.getClientId();
        boolean hasClient = clientManager.hasClient(clientId);
        //如果不存在客户端则将其加入到管理器
        if (!hasClient) {
            ClientInfo clientInfo = ClientInfo.builder()
                    .clientId(clientId)
                    .name(tempSession.getName())
                    .build();
            clientManager.addClient(clientInfo);
        }
        AgentSession agentSession = builder.isNew(!hasClient).build();
        agentSession.getControl().attr(AttributeKeys.SESSION_ID).set(sessionId);
        String token = agentSession.getToken();
        Channel control = agentSession.getControl();
        sessionIdToAgentSession.putIfAbsent(sessionId, agentSession);

        tokenToAgentSessions.computeIfAbsent(token,
                k -> new CopyOnWriteArraySet<>()).add(agentSession);
        //clientId --> AgentSession
        clientIdToAgentSession.put(clientId, agentSession);
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
                Set<String> domains = proxy.getDomainInfo().getFullDomains();
                for (String domain : domains) {
                    domainToAgentSession.putIfAbsent(domain, agentSession);
                    domainMapping.add(domain.trim());
                }
            }
        }
        scheduleTimeout(agentSession);
        return Optional.of(agentSession);
    }

    public Optional<AgentSession> getById(String clientId) {
        return Optional.ofNullable(clientIdToAgentSession.get(clientId));
    }

    public void addPortToAgentSession(String clientId, Integer remotePort) {
        getById(clientId).ifPresent(agentSession -> {
            portToAgentSession.putIfAbsent(remotePort, agentSession);
            controlToPorts.computeIfAbsent(agentSession.getControl(),
                    k -> ConcurrentHashMap.newKeySet()
            ).add(remotePort);
        });
    }

    public void removePortToAgentSession(String clientId, Integer remotePort) {
        getById(clientId).ifPresent(agentSession -> {
            portToAgentSession.remove(remotePort);
            Set<Integer> ports = controlToPorts.get(agentSession.getControl());
            ports.remove(remotePort);
        });
    }

    public void addDomainsToAgentSession(String clientId, Set<String> domains) {
        getById(clientId).ifPresent(agentSession -> {
            for (String domain : domains) {
                domainToAgentSession.putIfAbsent(domain, agentSession);
            }
            controlToDomains.computeIfAbsent(agentSession.getControl(),
                    k -> ConcurrentHashMap.newKeySet()
            ).addAll(domains);
        });
    }

    public void removeDomainsToAgentSession(String clientId, Set<String> domains) {
        getById(clientId).ifPresent(agentSession -> {
            for (String domain : domains) {
                domainToAgentSession.remove(domain);
            }
            controlToDomains.get(agentSession.getControl()).removeAll(domains);
        });
    }

    /**
     * 服务端与客户端断开连接
     * 清理掉所有远程端口到代理客户端会话信息
     */
    public void disconnect(Channel control) {
        getAgentSession(control).ifPresent(agentSession -> {
            String sessionId = agentSession.getSessionId();
            sessionIdToAgentSession.remove(sessionId);
            control.attr(AttributeKeys.SESSION_ID).set(null);

            String token = agentSession.getToken();
            //清理登陆令牌代理客户端映射
            Set<AgentSession> agentSessions = tokenToAgentSessions.get(token);
            if (agentSessions != null) {
                agentSessions.remove(agentSession);
                if (agentSessions.isEmpty()) {
                    tokenToAgentSessions.remove(token);
                }
            }
            //清理客户端ID 连接会话
            clientIdToAgentSession.remove(agentSession.getClientId());

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
     * 添加心跳处理
     *
     * @param session 连接会话
     */
    private void scheduleTimeout(AgentSession session) {
        Channel control = session.getControl();
        Timeout timer = wheelTimer.newTimeout(timeout -> {
            if (timeout.isCancelled()) {
                // 任务已被取消，不执行
                return;
            }
            logger.debug("心跳超时，向客户端发送探测包: {}", session.getSessionId());

            control.writeAndFlush(new TMSPFrame(0,TMSP.MSG_PING));
            control.eventLoop().schedule(() -> {
                // 再次检查连接是否还在，以及是否被其他逻辑处理了
                if (control.isActive() && !timeout.isCancelled()) {
                    logger.warn("心跳无响应，断开连接: {}", session.getSessionId());
                    disconnect(control);
                }
            }, 5, TimeUnit.SECONDS);
        }, HEARTBEAT_TIMEOUT, TimeUnit.SECONDS);
        session.setWheelTimer(timer);
    }

    /**
     * 心跳更新，用于如果连接者长时间没有心跳，释放连接资源
     */
    public void updateHeartbeat(Channel control) {
        String sessionId = control.attr(AttributeKeys.SESSION_ID).get();
        if (sessionId == null) {
            return;
        }
        AgentSession agentSession = sessionIdToAgentSession.get(sessionId);
        if (agentSession != null) {
            long current = System.currentTimeMillis();
            agentSession.setLastHeartbeat(current);
            Timeout oldHandle = agentSession.getWheelTimer();
            if (agentSession.hasWheelTimer() && !oldHandle.isCancelled()) {
                logger.debug("心跳更新，取消旧的时间轮: sessionId={}", sessionId);
                oldHandle.cancel();
            }
            logger.debug("心跳更新，重建时间轮: sessionId={}", sessionId);
            scheduleTimeout(agentSession);
        }
    }

    /**
     * 获取所有客户端会话信息
     *
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
        String sessionId = control.attr(AttributeKeys.SESSION_ID).get();
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

    /**
     * 给客户端发送断开消息，如果断开会自动清理资源
     *
     * @param clientId 客户端唯一标识
     * @return 会话
     */
    public AgentSession kickoutAgent(String clientId) {
        AgentSession agentSession = clientIdToAgentSession.get(clientId);
        if (agentSession == null) {
            logger.warn("没有找到客户端会话: {}", clientId);
            return null;
        }
        Channel control = agentSession.getControl();
        if (control == null) {
            logger.warn("客户端 - {} 控制隧道不存在", clientId);
            return null;
        }
        control.writeAndFlush(new TMSPFrame(0, TMSP.MSG_GOAWAY)).addListener(future -> {
            ChannelUtils.closeOnFlush(control);
        });
        return agentSession;
    }

    /**
     * 如果存在说明客户端是在线状态
     *
     * @param clientId 客户端唯一标识
     * @return 是否在线
     */
    public boolean isOnline(String clientId) {
        return clientIdToAgentSession.get(clientId) != null;
    }
}
