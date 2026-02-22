package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.domain.BandwidthConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.handler.ChannelSwitcher;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.handler.BandwidthLimiter;
import com.xiaoniucode.etp.server.handler.factory.ServerBridgeFactory;
import com.xiaoniucode.etp.server.handler.http.HttpVisitorHandler;
import com.xiaoniucode.etp.server.manager.LeastConnManager;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.ControlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自代理客户端连接成功消息
 *
 * @author liuxin
 */
@Component
public class NewVisitorConnRespHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnRespHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;
    @Autowired
    private HttpVisitorHandler httpVisitorHandler;

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        Message.NewVisitorConnResp resp = msg.getNewVisitorConnResp();
        String sessionId = resp.getSessionId();

        VisitorSession visitorSession = visitorSessionManager.getVisitorSession(sessionId);
        if (visitorSession == null) {
            logger.warn("visitor visitorSession is not exist.");
            return;
        }
        ProxyConfig config = visitorSession.getProxyConfig();
        Channel visitor = visitorSession.getVisitor();
        Channel tunnel = ctx.channel();
        //将数据连接保存到visitor session会话中
        visitorSession.setTunnel(tunnel);
        //将控制隧道切换为数据隧道
        ChannelSwitcher.switchToDataTunnel(tunnel.pipeline(), config.getCompress(), config.getEncrypt());
        if (config.hasBandwidthLimit()) {
            BandwidthConfig bandwidth = config.getBandwidth();
            BandwidthLimiter bandwidthLimiter = new BandwidthLimiter(bandwidth);
            ServerBridgeFactory.bridge(visitorSessionManager, visitor, tunnel, bandwidthLimiter, config.getProxyId(), config.getProtocol());
        } else {
            ServerBridgeFactory.bridge(visitorSessionManager, visitor, tunnel, config.getProxyId(), config.getProtocol());
        }
        LeastConnManager.incrementConnection(visitorSession);
        if (ProtocolDetection.isHttp(visitor)) {
            visitor.attr(ChannelConstants.CONNECTED).set(true);
            httpVisitorHandler.sendFirstPackage(visitorSession);
        }
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        logger.debug("已连接到目标服务: proxyName-{}", config.getName());
    }
}
