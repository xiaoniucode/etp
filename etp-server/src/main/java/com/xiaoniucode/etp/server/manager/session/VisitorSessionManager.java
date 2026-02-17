package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class VisitorSessionManager {
    private final Logger logger = LoggerFactory.getLogger(VisitorSessionManager.class);
    /**
     * sessionId -> Visitor session info
     */
    private final Map<String, VisitorSession> sessionIdToVisitorSession = new ConcurrentHashMap<>();
    /**
     * remotePort（listener port）-> visitor channels
     */
    private final Map<Integer, Set<Channel>> remotePortToVisitorChannels = new ConcurrentHashMap<>();
    /**
     * domain -> visitor channels
     */
    private final Map<String, Set<Channel>> domainToVisitorChannels = new ConcurrentHashMap<>();
    @Autowired
    private SessionIdGenerator sessionIdGenerator;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AgentSessionManager agentSessionManager;


    /**
     * 注册访问者连接信息
     * TCP、HTTP
     *
     * @param visitor  访问者连接
     * @param callback 回调 visitor session info
     */
    public void registerVisitor(Channel visitor, Consumer<VisitorSession> callback) {
        AgentSession agentSession = null;
        ProxyConfig proxyConfig = null;
        if (ProtocolDetection.isTcp(visitor)) {
            int remotePort = getListenerPort(visitor);
            agentSession = agentSessionManager.getAgentSessionByPort(remotePort);
            if (agentSession == null) {
                visitor.close();
                return;
            }
            remotePortToVisitorChannels.computeIfAbsent(remotePort, k ->
                    ConcurrentHashMap.newKeySet()).add(visitor);
            proxyConfig = proxyManager.getByRemotePort(remotePort);

        }
        if (ProtocolDetection.isHttp(visitor)) {
            String domain = visitor.attr(ChannelConstants.VISIT_DOMAIN).get();
            agentSession = agentSessionManager.getAgentSessionByDomain(domain);
            if (agentSession == null) {
                visitor.close();
                return;
            }
            domainToVisitorChannels.computeIfAbsent(domain, k ->
                    ConcurrentHashMap.newKeySet()).add(visitor);
            proxyConfig = proxyManager.getByDomain(domain);
        }
        if (agentSession == null) {
            return;
        }
        String sessionId = sessionIdGenerator.nextVisitorSessionId();
        visitor.attr(ChannelConstants.SESSION_ID).set(sessionId);
        VisitorSession visitorSession = new VisitorSession();
        visitorSession.setVisitor(visitor);
        visitorSession.setControl(agentSession.getControl());
        visitorSession.setSessionId(sessionId);
        visitorSession.setProxyConfig(proxyConfig);

        sessionIdToVisitorSession.put(sessionId, visitorSession);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }

    public synchronized void disconnect(Channel visitor, Consumer<VisitorSession> callback) {
        VisitorSession visitorSession = getVisitorSession(visitor);
        if (visitorSession==null){
            return;
        }
        if (ProtocolDetection.isTcp(visitor)) {
            int remotePort = getListenerPort(visitor);
            Set<Channel> visitors = remotePortToVisitorChannels.get(remotePort);
            visitors.remove(visitor);
            visitor.close();
            if (visitors.isEmpty()) {
                remotePortToVisitorChannels.remove(remotePort);
            }
        }
        if (ProtocolDetection.isHttp(visitor)) {
            String domain = getDomain(visitor);
            Set<Channel> visitors = domainToVisitorChannels.get(domain);
            visitors.remove(visitor);
            visitor.close();
            if (visitors.isEmpty()) {
                domainToVisitorChannels.remove(domain);
            }

            ByteBuf cachedPacket = visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).get();
            if (cachedPacket != null) {
                cachedPacket.release();
            }
            clearVisitorAttributeKey(visitor);
        }

        String sessionId = visitorSession.getSessionId();
        sessionIdToVisitorSession.remove(sessionId);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }

    private void clearVisitorAttributeKey(Channel visitor) {
        visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).set(null);
        visitor.attr(ChannelConstants.VISIT_DOMAIN).set(null);
        visitor.attr(ChannelConstants.SESSION_ID).set(null);
    }

    /**
     * 断开某一个Agent 所有访问者会话信息
     */
    public void disconnectAllSessionsForAgent(Channel control, Set<Integer> remotePorts, Set<String> domains) {
        //删除与某一个代理客户端有关的所有访问者会话
        sessionIdToVisitorSession.values().removeIf(session -> session.getTunnel() == control);
        //删除访问者所有端口映射
        for (Integer remotePort : remotePorts) {
            Set<Channel> visitors = remotePortToVisitorChannels.get(remotePort);
            if (visitors != null) {
                for (Channel visitor : visitors) {
                    clearVisitorAttributeKey(visitor);
                    ChannelUtils.closeOnFlush(visitor);
                }
            }
        }
        //删除访问者所有域名映射
        for (String domain : domains) {
            Set<Channel> visitors = domainToVisitorChannels.get(domain);
            if (visitors != null) {
                for (Channel visitor : visitors) {
                    clearVisitorAttributeKey(visitor);
                    ChannelUtils.closeOnFlush(visitor);
                }
            }
        }
    }

    public void disconnect(String sessionId, Consumer<VisitorSession> callback) {
        VisitorSession session = sessionIdToVisitorSession.get(sessionId);
        if (session==null){
            return;
        }
        disconnect(session.getVisitor(), callback);
    }
    public synchronized void closeVisitorsByRemotePorts(Set<Integer> remotePorts) {
        remotePorts.forEach(this::closeVisitorsByRemotePort);
    }
    /**
     * 关闭指定端口上的所有visitor 连接
     * TCP代理
     *
     * @param remotePort 访问端口
     */
    public synchronized void closeVisitorsByRemotePort(Integer remotePort) {
        if (!remotePortToVisitorChannels.containsKey(remotePort)) {
            return;
        }
        Set<Channel> visitorChannels = remotePortToVisitorChannels.get(remotePort);
        for (Channel visitor : visitorChannels) {
            VisitorSession visitorSession = getVisitorSession(visitor);
            String sessionId = visitorSession.getSessionId();
            sessionIdToVisitorSession.remove(sessionId);
            visitorChannels.remove(visitor);
            visitor.close();
        }
        if (visitorChannels.isEmpty()) {
            remotePortToVisitorChannels.remove(remotePort);
        }
    }

    /**
     * 关闭指定域名上的所有visitor 连接
     * HTTP协议
     *
     * @param domain 域名
     */
    public synchronized void closeVisitorsByDomain(String domain) {
        if (!domainToVisitorChannels.containsKey(domain)) {
            return;
        }
        Set<Channel> visitorChannels = domainToVisitorChannels.get(domain);
        for (Channel visitor : visitorChannels) {
            VisitorSession visitorSession = getVisitorSession(visitor);
            String sessionId = visitorSession.getSessionId();
            sessionIdToVisitorSession.remove(sessionId);
            visitorChannels.remove(visitor);
            visitor.close();
        }
        if (visitorChannels.isEmpty()) {
            domainToVisitorChannels.remove(domain);
        }
    }
    public synchronized void closeVisitorsByDomains(Set<String> domains) {
        domains.forEach(this::closeVisitorsByDomain);
    }
    public VisitorSession getVisitorSession(String sessionId) {
        return sessionIdToVisitorSession.get(sessionId);
    }

    public VisitorSession getVisitorSession(Channel visitor) {
        String sessionId = visitor.attr(ChannelConstants.SESSION_ID).get();
        if (sessionId == null) {
            return null;
        }
        return getVisitorSession(sessionId);
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }

    private String getDomain(Channel visitor) {
        return visitor.attr(ChannelConstants.VISIT_DOMAIN).get();
    }

}
