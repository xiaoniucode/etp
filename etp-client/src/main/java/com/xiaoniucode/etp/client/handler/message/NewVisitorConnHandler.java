package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.handler.tunnel.ClientBridgeFactory;
import com.xiaoniucode.etp.client.manager.ConnectionPool;
import com.xiaoniucode.etp.client.handler.utils.MessageUtils;
import com.xiaoniucode.etp.client.manager.BootstrapManager;
import com.xiaoniucode.etp.client.manager.ServerSessionManager;
import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.handler.ChannelSwitcher;
import com.xiaoniucode.etp.core.domain.LanInfo;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.*;

/**
 *
 * @author liuxin
 */
public class NewVisitorConnHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage res) {
        Message.NewVisitorConn msg = res.getNewVisitorConn();
        Channel control = ctx.channel();
        String sessionId = msg.getSessionId();
        String localIP = msg.getLocalIp();
        int localPort = msg.getLocalPort();

        Bootstrap serverBootstrap = BootstrapManager.getRealServerBootstrap();
        serverBootstrap.connect(localIP, localPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("连接到目标服务 - [地址={}，端口={}]", localIP, localPort);
                Channel server = future.channel();
                server.config().setOption(ChannelOption.AUTO_READ, false);
                ConnectionPool.acquire().thenAccept(tunnel -> tunnel.writeAndFlush(MessageUtils.buildVisitorConn(sessionId)).addListener(f -> {
                    if (f.isSuccess()) {
                        boolean compress = msg.getCompress();
                        boolean encrypt = msg.getEncrypt();
                        //控制通道转换为数据通道
                        ChannelSwitcher.switchToDataTunnel(tunnel.pipeline(), compress, encrypt);
                        //隧道双向桥接
                        ClientBridgeFactory.bridge(tunnel,server);
                        //创建连接会话
                        ServerSessionManager.createServerSession(sessionId, tunnel, server, new LanInfo(localIP, localPort)).ifPresent(serverSession -> {
                            //设置通道可读
                            server.config().setOption(ChannelOption.AUTO_READ, true);
                            logger.debug("隧道创建成功 - [目标地址={}，目标端口={}]", localIP, localPort);
                        });
                    }
                })).exceptionally(cause -> {
                    logger.error(cause.getMessage(), cause);
                    control.writeAndFlush(MessageUtils.buildCloseProxy(sessionId));
                    return null;
                });
            } else {
                control.writeAndFlush(MessageUtils.buildCloseProxy(sessionId));
                logger.error("隧道创建失败 - [服务地址={}:服务端口={}] 不可用!", localIP, localPort);
            }
        });
    }
}
