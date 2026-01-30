package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.ChannelSwitcher;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.codec.ChannelBridge;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.handler.tunnel.HttpVisitorHandler;
import com.xiaoniucode.etp.server.handler.tunnel.ResourceReleaseHandler;
import com.xiaoniucode.etp.server.handler.tunnel.TcpVisitorHandler;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.ControlMessage;
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
        Channel visitor = visitorSession.getVisitor();
        Channel tunnel = ctx.channel();
        //将数据连接保存到visitor session会话中
        visitorSession.setTunnel(tunnel);
        //将控制隧道切换为数据隧道
        ChannelSwitcher.switchToDataTunnel(ctx.pipeline());
        //[visitor <-> tunnel]桥接，双向透明转发，无需序列化和拷贝
        ChannelBridge.bridge(visitor, tunnel);
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        if (ProtocolDetection.isHttp(visitor)) {
            visitor.attr(EtpConstants.CONNECTED).set(true);
            httpVisitorHandler.sendFirstPackage(visitorSession);
        }
        logger.debug("已连接到目标服务");
    }
}
