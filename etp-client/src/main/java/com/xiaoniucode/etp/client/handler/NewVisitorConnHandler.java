package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
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
 * 与内网真实目标服务建立连接，同时建立数据传输隧道
 *
 * @author liuxin
 */
public class NewVisitorConnHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnHandler.class);
    private final String LOCALHOST = "127.0.0.1";

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message message) {
        if (message instanceof NewVisitorConn) {
            NewVisitorConn msg = (NewVisitorConn) message;
            Channel controlTunnelChannel = ctx.channel();
            String secretKey = controlTunnelChannel.attr(EtpConstants.SECRET_KEY).get();
            long sessionId = msg.getSessionId();
            int localPort = msg.getLocalPort();
            Bootstrap realBootstrap = ChannelManager.getRealBootstrap();
            Bootstrap controlBootstrap = ChannelManager.getControlBootstrap();
            //与内网真实服务建立连接
            realBootstrap.connect(LOCALHOST, localPort).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.debug("成功连接到内网服务{}:{}", LOCALHOST, localPort);
                    Channel realChannel = future.channel();
                    realChannel.config().setOption(ChannelOption.AUTO_READ, false);

                    ChannelManager.borrowDataTunnelChannel(controlBootstrap)
                            .thenAccept(dataChannel -> {
                                dataChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(realChannel);
                                realChannel.attr(EtpConstants.DATA_CHANNEL).set(dataChannel);

                                dataChannel.writeAndFlush(new NewVisitorConnResp(secretKey,sessionId)).addListener(f -> {
                                    if (f.isSuccess()) {
                                        ChannelManager.addRealServerChannel(sessionId, realChannel);
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
                    logger.error("内网目标服务[{}:{}]不可用!", LOCALHOST, localPort);
                }
            });
        }
    }
}
