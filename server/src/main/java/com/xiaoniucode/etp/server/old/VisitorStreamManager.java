package com.xiaoniucode.etp.server.old;

import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.generator.StreamIdGenerator;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import com.xiaoniucode.etp.server.manager.domain.VisitorStream;
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
public class VisitorStreamManager {
    private final Logger logger = LoggerFactory.getLogger(VisitorStreamManager.class);
    /**
     * streamId -> Visitor stream info
     */
    private final Map<Integer, VisitorStream> streamIdToVisitorStream = new ConcurrentHashMap<>();
    /**
     * remotePort（listener port）-> visitor channels
     */
    private final Map<Integer, Set<Channel>> remotePortToVisitorChannels = new ConcurrentHashMap<>();
    /**
     * domain -> visitor channels
     */
    private final Map<String, Set<Channel>> domainToVisitorChannels = new ConcurrentHashMap<>();
    @Autowired
    private StreamIdGenerator streamIdGenerator;
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
    public void createStream(Channel visitor, Consumer<VisitorStream> callback) {
        AgentSession agentSession = null;
        ProxyConfig proxyConfig = null;
        if (ProtocolDetection.isTcp(visitor)) {
            int remotePort = getListenerPort(visitor);
            proxyConfig = proxyManager.getByRemotePort(remotePort);

            agentSession = agentSessionManager.getAgentSessionByPort(remotePort);
            if (agentSession == null) {
                visitor.close();
                return;
            }
            remotePortToVisitorChannels.computeIfAbsent(remotePort, k ->
                    ConcurrentHashMap.newKeySet()).add(visitor);
        }
        if (ProtocolDetection.isHttp(visitor)) {
            String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
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
        int streamId = streamIdGenerator.nextStreamId();
        visitor.attr(AttributeKeys.STREAM_ID).set(streamId);
        VisitorStream visitorSession = new VisitorStream();
        visitorSession.setVisitor(visitor);
        visitorSession.setControl(agentSession.getControl());
        visitorSession.setStreamId(streamId);
        visitorSession.setProxyConfig(proxyConfig);

        streamIdToVisitorStream.put(streamId, visitorSession);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }

    public synchronized void closeStream(Channel visitor, Consumer<VisitorStream> callback) {
        VisitorStream visitorSession = getStream(visitor);
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

            ByteBuf cachedPacket = visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).get();
            if (cachedPacket != null) {
                cachedPacket.release();
            }
            clearVisitorAttributeKey(visitor);
        }

        int streamId = visitorSession.getStreamId();
        streamIdToVisitorStream.remove(streamId);
        if (callback != null) {
            callback.accept(visitorSession);
        }
    }

    private void clearVisitorAttributeKey(Channel visitor) {
        visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(null);
        visitor.attr(AttributeKeys.VISIT_DOMAIN).set(null);
        visitor.attr(AttributeKeys.STREAM_ID).set(null);
    }

    /**
     * 断开某一个Agent 所有访问者会话信息
     */
    public void closeAllStreamsForAgent(Channel control, Set<Integer> remotePorts, Set<String> domains) {
        //删除与某一个代理客户端有关的所有访问者会话
        streamIdToVisitorStream.values().removeIf(session -> session.getTunnel() == control);
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

    public void closeStream(int streamId, Consumer<VisitorStream> callback) {
        VisitorStream stream = streamIdToVisitorStream.get(streamId);
        if (stream==null){
            return;
        }
        closeStream(stream.getVisitor(), callback);
    }
    public synchronized void closeStreamsByRemotePorts(Set<Integer> remotePorts) {
        remotePorts.forEach(this::closeStreamsByRemotePort);
    }
    /**
     * 关闭指定端口上的所有visitor 连接
     * TCP代理
     *
     * @param remotePort 访问端口
     */
    public synchronized void closeStreamsByRemotePort(Integer remotePort) {
        if (!remotePortToVisitorChannels.containsKey(remotePort)) {
            return;
        }
        Set<Channel> visitorChannels = remotePortToVisitorChannels.get(remotePort);
        for (Channel visitor : visitorChannels) {
            VisitorStream visitorStream = getStream(visitor);
            int sessionId = visitorStream.getStreamId();
            streamIdToVisitorStream.remove(sessionId);
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
    public synchronized void closeStreamsByDomain(String domain) {
        if (!domainToVisitorChannels.containsKey(domain)) {
            return;
        }
        Set<Channel> visitorChannels = domainToVisitorChannels.get(domain);
        for (Channel visitor : visitorChannels) {
            VisitorStream visitorSession = getStream(visitor);
            int streamId = visitorSession.getStreamId();
            streamIdToVisitorStream.remove(streamId);
            visitorChannels.remove(visitor);
            visitor.close();
        }
        if (visitorChannels.isEmpty()) {
            domainToVisitorChannels.remove(domain);
        }
    }
    public synchronized void closeStreamsByDomains(Set<String> domains) {
        domains.forEach(this::closeStreamsByDomain);
    }
    public VisitorStream getStream(Integer streamId) {
        return streamIdToVisitorStream.get(streamId);
    }

    public VisitorStream getStream(Channel visitor) {
        Integer streamId = visitor.attr(AttributeKeys.STREAM_ID).get();
        if (streamId == null) {
            return null;
        }
        return getStream(streamId);
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }

    private String getDomain(Channel visitor) {
        return visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
    }

}
