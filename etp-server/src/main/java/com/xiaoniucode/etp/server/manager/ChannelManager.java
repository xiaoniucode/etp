package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Login;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.config.domain.AuthInfo;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChannelManager {
    private static final Map<String, Channel> clientToControlChannel = new ConcurrentHashMap<>();
    /**
     * 远程端口 → 对应控制隧道的映射
     * key: (远程端口号)remotePort
     * value: （已认证并活跃的控制通道）control channel
     */
    private static final Map<Integer, Channel> portToControlChannel = new ConcurrentHashMap<>();
    private static final Map<String, Channel> domainToControlChannel = new ConcurrentHashMap<>();
    /**
     * key: (远程端口号)remotePort
     * value: （访问者连接）visitor channel
     */
    private static final Map<Integer, Set<Channel>> portToVisitorChannels = new ConcurrentHashMap<>();
    /**
     * key: (域名)domain
     * value: （访问者连接）visitor channel
     */
    private static final Map<String, Set<Channel>> domainToVisitorChannels = new ConcurrentHashMap<>();

    public static void registerClient(Channel control, Login login) {
        String secretKey = login.getSecretKey();
        Set<Integer> remotePorts = ProxyManager.getClientRemotePorts(secretKey);
        Set<String> domains = ProxyManager.getClientDomains(secretKey);
        for (Integer remotePort : remotePorts) {
            portToControlChannel.put(remotePort, control);
        }
        for (String domain : domains) {
            domainToControlChannel.put(domain, control);
        }
        control.attr(EtpConstants.CHANNEL_REMOTE_PORT).set(remotePorts);
        control.attr(EtpConstants.SECRET_KEY).set(secretKey);
        control.attr(EtpConstants.OS).set(login.getOs());
        control.attr(EtpConstants.ARCH).set(login.getArch());
        control.attr(EtpConstants.VISITOR_CHANNELS).set(new ConcurrentHashMap<>());
        clientToControlChannel.put(secretKey, control);
    }

    public static void removePort(Integer remotePort) {
        portToControlChannel.remove(remotePort);
    }

    /**
     * 注册公网访问者
     *
     * @param visitor channel
     */
    public static void registerTcpVisitor(Channel visitor, Consumer<TcpVisitorPair> callback) {
        int remotePort = getListenerPort(visitor);
        Channel control = getControlByDomain(remotePort);
        if (control == null) {
            //没有可用控制隧道
            visitor.close();
            return;
        }
        long sessionId = GlobalIdGenerator.nextId();
        int localPort = ProxyManager.getLocalPort(remotePort);
        //
        control.attr(EtpConstants.VISITOR_CHANNELS).get().put(sessionId, visitor);
        visitor.attr(EtpConstants.SESSION_ID).set(sessionId);
        //记录公网端口上的访问者连接
        portToVisitorChannels.computeIfAbsent(remotePort, k -> ConcurrentHashMap.newKeySet()).add(visitor);
        //回调
        callback.accept(new TcpVisitorPair(control, localPort, remotePort, sessionId));
    }


    public static void unregisterTcpVisitor(Channel visitor, BiConsumer<Long, Channel> callback) {
        int remotePort = getListenerPort(visitor);
        Channel control = getControlByDomain(remotePort);
        if (control == null) {
            //没有可用控制隧道
            visitor.close();
            return;
        }
        Long sessionId = visitor.attr(EtpConstants.SESSION_ID).get();
        if (control.attr(EtpConstants.VISITOR_CHANNELS).get() != null) {
            control.attr(EtpConstants.VISITOR_CHANNELS).get().remove(sessionId);
        }

        Set<Channel> visitorChannels = portToVisitorChannels.get(remotePort);
        if (visitorChannels != null) {
            visitorChannels.remove(visitor);
            if (visitorChannels.isEmpty()) {
                portToVisitorChannels.remove(remotePort);
            }
        }

        Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null && tunnel.isActive()) {
            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).getAndSet(null);
            tunnel.attr(EtpConstants.SECRET_KEY).getAndSet(null);
            tunnel.attr(EtpConstants.SESSION_ID).getAndSet(null);
            tunnel.attr(EtpConstants.CONNECTED).getAndSet(null);
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            callback.accept(sessionId, tunnel);
        }
    }

    public static void registerHttpVisitor(Channel visitor, Consumer<HttpVisitorPair> callback) {
        String domain = visitor.attr(EtpConstants.VISITOR_DOMAIN).get();
        Channel control = getControlByDomain(domain);
        if (control == null) {
            visitor.close();
            return;
        }
        long sessionId = GlobalIdGenerator.nextId();
        Integer localPort = visitor.attr(EtpConstants.TARGET_PORT).get();

        control.attr(EtpConstants.VISITOR_CHANNELS).get().put(sessionId, visitor);
        visitor.attr(EtpConstants.SESSION_ID).set(sessionId);

        domainToVisitorChannels.computeIfAbsent(domain, k -> ConcurrentHashMap.newKeySet()).add(visitor);
        //回调
        callback.accept(new HttpVisitorPair(control, sessionId, domain, localPort));

    }

    public static void unregisterHttpVisitor(Channel visitor, BiConsumer<Long, Channel> callback) {
        String domain = visitor.attr(EtpConstants.VISITOR_DOMAIN).get();
        Channel control = getControlByDomain(domain);
        if (control == null) {
            visitor.close();
            return;
        }
        Long sessionId = visitor.attr(EtpConstants.SESSION_ID).get();
        if (control.attr(EtpConstants.VISITOR_CHANNELS).get() != null) {
            control.attr(EtpConstants.VISITOR_CHANNELS).get().remove(sessionId);
        }
        Set<Channel> visitorChannels = domainToVisitorChannels.get(domain);
        if (visitorChannels != null) {
            visitorChannels.remove(visitor);
            if (visitorChannels.isEmpty()) {
                domainToVisitorChannels.remove(domain);
            }
        }

        Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null && tunnel.isActive()) {
            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).getAndSet(null);
            tunnel.attr(EtpConstants.SECRET_KEY).getAndSet(null);
            tunnel.attr(EtpConstants.SESSION_ID).getAndSet(null);
            tunnel.attr(EtpConstants.CONNECTED).getAndSet(null);
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            callback.accept(sessionId, tunnel);
        }
    }

    /**
     * 双向绑定 数据传输隧道 < - > 访问者连接
     *
     * @param tunnel    数据传输隧道
     * @param sessionId sessionId
     * @param secretKey 客户端密钥
     * @param callback  回调接口
     */
    public static void bindVisitorAndTunnel(Channel tunnel, long sessionId, String secretKey, Consumer<Channel> callback) {
        Channel control = clientToControlChannel.get(secretKey);
        if (control == null) {
            return;
        }
        Channel visitor = control.attr(EtpConstants.VISITOR_CHANNELS).get().get(sessionId);
        if (visitor == null) {
            return;
        }

        tunnel.attr(EtpConstants.SECRET_KEY).set(secretKey);
        tunnel.attr(EtpConstants.SESSION_ID).set(sessionId);
        visitor.attr(EtpConstants.CONNECTED).set(true);

        tunnel.attr(EtpConstants.VISITOR_CHANNEL).set(visitor);
        visitor.attr(EtpConstants.DATA_CHANNEL).set(tunnel);

        callback.accept(visitor);
    }

    public static void closeVisitor(int remotePort) {
        Set<Channel> visitors = portToVisitorChannels.get(remotePort);
        if (visitors != null) {
            for (Channel ch : visitors) {
                ch.close();
            }
            portToVisitorChannels.remove(remotePort);
        }
    }

    public static Channel getControlByDomain(int remotePort) {
        return portToControlChannel.get(remotePort);
    }

    public static Channel getControlByDomain(String domain) {
        return domainToControlChannel.get(domain);
    }

    public static Channel getControlByVisitor(Channel visitor) {
        int remotePort = getListenerPort(visitor);
        return portToControlChannel.get(remotePort);
    }

    public static Channel getControl(String secretKey) {
        return clientToControlChannel.get(secretKey);
    }

    public static int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }

    public static AuthInfo getAuthInfo(String secretKey) {
        Channel control = clientToControlChannel.get(secretKey);
        if (control != null) {
            String os = control.attr(EtpConstants.OS).get();
            String arch = control.attr(EtpConstants.ARCH).get();
            return new AuthInfo(os, arch);
        }
        return null;
    }

    public static void addPortToControlChannelIfOnline(String secretKey, Integer remotePort) {
        Channel control = clientToControlChannel.get(secretKey);
        if (null == control) {
            return;
        }
        portToControlChannel.put(remotePort, control);
        control.attr(EtpConstants.CHANNEL_REMOTE_PORT).get().add(remotePort);
    }

    public static boolean clientIsOnline(String secretKey) {
        return clientToControlChannel.get(secretKey) != null;
    }

    public static int onlineClientCount() {
        return clientToControlChannel.size();
    }

    public static void closeVisitor(String secretKey, Long sessionId) {
        Channel control = getControl(secretKey);
        if (control.attr(EtpConstants.VISITOR_CHANNELS).get() != null) {
            Channel visitor = control.attr(EtpConstants.VISITOR_CHANNELS).get().remove(sessionId);
            if (visitor != null) {
                visitor.attr(EtpConstants.DATA_CHANNEL).getAndSet(null);
                visitor.attr(EtpConstants.SECRET_KEY).getAndSet(null);
                visitor.attr(EtpConstants.SESSION_ID).getAndSet(null);
                ChannelUtils.closeOnFlush(visitor);
            }
        }
    }

    public static void closeControl(String secretKey) {
        Channel control = getControl(secretKey);
        if (control != null) {
            closeControl(control);
        }
    }

    public static void closeControl(Channel control) {
        if (control.attr(EtpConstants.CHANNEL_REMOTE_PORT).get() == null) {
            return;
        }
        String secretKey = control.attr(EtpConstants.SECRET_KEY).get();
        Channel existingChannel = clientToControlChannel.get(secretKey);
        if (control == existingChannel) {
            clientToControlChannel.remove(secretKey);
        }
        Set<Integer> remotePorts = control.attr(EtpConstants.CHANNEL_REMOTE_PORT).get();
        if (remotePorts != null) {
            for (int port : remotePorts) {
                portToControlChannel.remove(port);
            }
        }
        if (control.isActive()) {
            control.close();
        }
        //将该隧道上所有用户连接都给关闭掉
        Map<Long, Channel> clientChannels = control.attr(EtpConstants.VISITOR_CHANNELS).get();
        if (clientChannels != null) {
            clientChannels.keySet().forEach(sessionId -> {
                Channel clientChannel = clientChannels.get(sessionId);
                if (clientChannel.isActive()) {
                    clientChannel.close();
                }
            });
        }
    }
}
