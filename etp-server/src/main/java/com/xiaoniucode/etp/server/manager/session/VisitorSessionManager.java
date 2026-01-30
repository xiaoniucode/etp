package com.xiaoniucode.etp.server.manager.session;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class VisitorSessionManager {
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

    /**
     * 注册访问者连接信息
     * TCP、HTTP
     *
     * @param visitor  访问者连接
     * @param callback 回调 visitor session info
     */
    public void registerVisitor(Channel visitor, Consumer<VisitorSession> callback) {
        ProxyManager proxyManager = BeanHelper.getBean(ProxyManager.class);
        AgentSessionManager agentSessionManager = BeanHelper.getBean(AgentSessionManager.class);
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
            String domain = visitor.attr(EtpConstants.VISIT_DOMAIN).get();
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
        visitor.attr(EtpConstants.SESSION_ID).set(sessionId);
        VisitorSession visitorSession = new VisitorSession();
        visitorSession.setVisitor(visitor);
        visitorSession.setControl(agentSession.getControl());
        visitorSession.setSessionId(sessionId);
        visitorSession.setLanInfo(new LanInfo(proxyConfig.getLocalIp(), proxyConfig.getLocalPort()));

        sessionIdToVisitorSession.put(sessionId, visitorSession);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }

    public synchronized void disconnect(Channel visitor, Consumer<VisitorSession> callback) {
        VisitorSession visitorSession = getVisitorSession(visitor);

        if (ProtocolDetection.isTcp(visitor)) {
            int remotePort = getListenerPort(visitor);
            Set<Channel> visitorChannels = remotePortToVisitorChannels.get(remotePort);
            visitorChannels.remove(visitor);
            visitor.close();
            if (visitorChannels.isEmpty()) {
                remotePortToVisitorChannels.remove(remotePort);
            }
        }
        if (ProtocolDetection.isHttp(visitor)) {
            String domain = getDomain(visitor);
            Set<Channel> visitorChannels = domainToVisitorChannels.get(domain);
            visitorChannels.remove(visitor);
            visitor.close();
            if (visitorChannels.isEmpty()) {
                domainToVisitorChannels.remove(domain);
            }

            ByteBuf cachedPacket = visitor.attr(EtpConstants.HTTP_FIRST_PACKET).get();
            if (cachedPacket != null) {
                cachedPacket.release();
                visitor.attr(EtpConstants.HTTP_FIRST_PACKET).set(null);
            }
        }

        String sessionId = visitorSession.getSessionId();
        sessionIdToVisitorSession.remove(sessionId);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }


    public void disconnect(String sessionId, Consumer<VisitorSession> callback) {
        VisitorSession session = sessionIdToVisitorSession.get(sessionId);
        disconnect(session.getVisitor(), callback);
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

    public VisitorSession getVisitorSession(String sessionId) {
        return sessionIdToVisitorSession.get(sessionId);
    }

    public VisitorSession getVisitorSession(Channel visitor) {
        String sessionId = visitor.attr(EtpConstants.SESSION_ID).get();
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
        return visitor.attr(EtpConstants.VISIT_DOMAIN).get();
    }
}
