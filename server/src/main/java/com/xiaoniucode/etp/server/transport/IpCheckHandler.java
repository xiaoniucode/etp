package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.server.security.IpAccessChecker;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;

/**
 * IP 访问控制检查
 */
public abstract class IpCheckHandler extends ChannelInboundHandlerAdapter {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(IpCheckHandler.class);
    private final IpAccessChecker ipAccessChecker;

    public IpCheckHandler(IpAccessChecker ipAccessChecker) {
        this.ipAccessChecker = ipAccessChecker;
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
        boolean checkAccess = ipAccessChecker.checkAccess(proxyId, visitorIp);
        if (!checkAccess) {
            logger.debug("来源IP {} 无访问权限",visitorIp);
            NettyHttpUtils.sendHttp403(visitor);
        }
    }
}
