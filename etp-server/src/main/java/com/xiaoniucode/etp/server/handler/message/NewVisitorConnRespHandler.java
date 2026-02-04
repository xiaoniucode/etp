package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.handler.ChannelSwitcher;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.handler.bridge.ChannelBridge;
import com.xiaoniucode.etp.core.handler.bridge.ChannelBridgeCallback;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.handler.tunnel.HttpVisitorHandler;
import com.xiaoniucode.etp.server.handler.utils.MessageWrapper;
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

import java.util.concurrent.atomic.AtomicBoolean;

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
        ChannelBridgeCallback callback = new ChannelBridgeCallback() {
            /**
             * 由于是双向通道，所以会出现两次调用回调接口，需要做防重处理
             */
            private final AtomicBoolean executed = new AtomicBoolean(false);

            @Override
            public void onChannelInactive(Channel channel, Channel peer) {
                if (executed.compareAndSet(false, true)) {
                    logger.debug("会话连接断开，释放资源");
                    visitorSessionManager.disconnect(visitor, session -> {
                        Channel tunnel = session.getTunnel();
                        Message.ControlMessage message = MessageWrapper
                                .buildCloseProxy(session.getSessionId());
                        tunnel.writeAndFlush(message);
                    });
                    ChannelUtils.closeOnFlush(visitor);
                }

            }

            @Override
            public void onExceptionCaught(Channel channel, Channel peer, Throwable cause) {
                if (executed.compareAndSet(false, true)) {
                    logger.error(cause.getMessage(), cause);
                    visitorSessionManager.disconnect(visitor, session -> {
                        Channel tunnel = session.getTunnel();
                        Message.ControlMessage message = MessageWrapper.buildCloseProxy(session.getSessionId());
                        tunnel.writeAndFlush(message);

                    });
                    ChannelUtils.closeOnFlush(visitor);
                }

            }

            @Override
            public void onChannelWritabilityChanged(Channel channel, Channel peer, boolean isWritable) {
                VisitorSession visitorSession = visitorSessionManager.getVisitorSession(visitor);
                Channel tunnel = visitorSession.getTunnel();
                if (tunnel != null) {
                    tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
                }
            }
        };
        //[visitor <-> tunnel]桥接，双向透明转发，无需序列化和拷贝
        ChannelBridge.bridge(visitor, tunnel, callback);
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        if (ProtocolDetection.isHttp(visitor)) {
            visitor.attr(ChannelConstants.CONNECTED).set(true);
            httpVisitorHandler.sendFirstPackage(visitorSession);
        }
        logger.debug("已连接到目标服务");
    }
}
