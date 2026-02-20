package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.server.manager.AccessControlManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * IP 访问控制检查
 */
public abstract class IpCheckHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(IpCheckHandler.class);
    private final AccessControlManager accessControlManager;

    public IpCheckHandler(AccessControlManager accessControlManager) {
        this.accessControlManager = accessControlManager;
    }

    /**
     * 获取访问来源 IP 地址
     *
     * @param visitor 管道
     * @return IP 地址
     */
    protected String getVisitorIp(Channel visitor) {
        if (visitor.remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) visitor.remoteAddress())
                    .getAddress().getHostAddress();
        }
        return visitor.remoteAddress().toString();
    }

    /**
     * 获取服务器监听端口
     *
     * @param visitor 管道
     * @return 监听端口
     */
    protected int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }

    protected void doCheckAccess(Channel visitor, String proxyId) {
        String visitorIp = getVisitorIp(visitor);
        boolean checkAccess = accessControlManager.checkAccess(proxyId, visitorIp);
        if (!checkAccess) {
            logger.debug("无访问权限");
            visitor.close();
        }
    }
}
