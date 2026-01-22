package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.ConnectionPool;
import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.codec.ChannelBridge;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import com.xiaoniucode.etp.core.msg.NewVisitorConnResp;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author liuxin
 */
public class NewVisitorConnHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message message) {
        if (message instanceof NewVisitorConn) {
            NewVisitorConn msg = (NewVisitorConn) message;
            Channel controlTunnelChannel = ctx.channel();
            String secretKey = controlTunnelChannel.attr(EtpConstants.SECRET_KEY).get();
            long sessionId = msg.getSessionId();
            String localIP = msg.getLocalIP();
            int localPort = msg.getLocalPort();
            Bootstrap realBootstrap = ChannelManager.getRealBootstrap();
            realBootstrap.connect(localIP, localPort).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.debug("成功连接到内网服务{}:{}", localIP, localPort);
                    Channel realChannel = future.channel();
                    realChannel.config().setOption(ChannelOption.AUTO_READ, false);

                    ConnectionPool.borrowDataTunnelChannel()
                            .thenAccept(tunnel -> {
                                tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(realChannel);
                                realChannel.attr(EtpConstants.DATA_CHANNEL).set(tunnel);

                                tunnel.writeAndFlush(new NewVisitorConnResp(secretKey,sessionId)).addListener(f -> {
                                    if (f.isSuccess()) {
                                        ChannelManager.addRealServerChannel(sessionId, realChannel);
                                        //控制通道转换为数据通道
                                        tunnel.pipeline().remove("tunnelMessageCodec");
                                        tunnel.pipeline().remove("controlChannelHandler");
                                        //桥接，双向透明转发
                                        ChannelBridge.bridge(realChannel, tunnel);
                                        realChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
                                        realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                                        logger.debug("成功建立数据传输通道:[sessionId:{},localPort:{}]", sessionId, localPort);
                                    }
                                });
                            })
                            .exceptionally(cause -> {
                                logger.error(cause.getMessage(), cause);
                                //如果发生错误，通知服务端断开连接
                                controlTunnelChannel.writeAndFlush(new CloseProxy(sessionId));
                                return null;
                            });
                } else {
                    logger.error("内网目标服务[{}:{}]不可用!", localIP, localPort);
                }
            });
        }
    }
}
