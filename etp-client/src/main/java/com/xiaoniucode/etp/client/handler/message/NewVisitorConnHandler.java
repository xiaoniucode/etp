package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.ConnectionPool;
import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.ChannelSwitcher;
import com.xiaoniucode.etp.core.codec.ChannelBridge;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;

/**
 *
 * @author liuxin
 */
public class NewVisitorConnHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage message) {
        Message.NewVisitorConn msg = message.getNewVisitorConn();
        Channel control = ctx.channel();
        String sessionId = msg.getSessionId();
        String localIP = msg.getLocalIp();
        int localPort = msg.getLocalPort();
        Bootstrap realBootstrap = ChannelManager.getRealBootstrap();
        realBootstrap.connect(localIP, localPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("成功连接到内网服务{}:{}", localIP, localPort);
                Channel realChannel = future.channel();
                realChannel.config().setOption(ChannelOption.AUTO_READ, false);

                ConnectionPool.borrowConnection()
                        .thenAccept(tunnel -> {
                            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(realChannel);
                            realChannel.attr(EtpConstants.DATA_CHANNEL).set(tunnel);
                            MessageHeader header = MessageHeader.newBuilder().setType(MessageType.NEW_VISITOR_RESP).build();

                            Message.NewVisitorConnResp newvisitorConnResp = Message
                                    .NewVisitorConnResp
                                    .newBuilder()
                                    .setSessionId(sessionId).build();
                            ControlMessage controlMessage = ControlMessage.newBuilder().setHeader(header).setNewVisitorConnResp(newvisitorConnResp).build();
                            tunnel.writeAndFlush(controlMessage).addListener(f -> {
                                if (f.isSuccess()) {
                                    ChannelManager.addRealServerChannel(sessionId, realChannel);
                                    //控制通道转换为数据通道
                                    ChannelSwitcher.switchToDataTunnel(tunnel.pipeline());
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
                            Message.CloseProxy closeProxy = Message.CloseProxy.newBuilder().setSessionId(sessionId).build();
                            control.writeAndFlush(closeProxy);
                            return null;
                        });
            } else {
                Message.CloseProxy closeProxy = Message.CloseProxy.newBuilder().setSessionId(sessionId).build();
                control.writeAndFlush(closeProxy);
                logger.error("内网目标服务[{}:{}]不可用!", localIP, localPort);
            }
        });
    }
}
