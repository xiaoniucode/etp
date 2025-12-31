package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理客户端收到来自服务端的连接请求后，代理客户端通过代理服务端提供的内网端口号与内网真实服务建立连接
 * 连接建立成功后，代理客户端需要和代理服务端建立一条数据传输隧道。
 *
 * @author liuxin
 */
public class ConnectHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectHandler.class);
    private final String LOCALHOST = "127.0.0.1";

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Channel controlTunnelChannel = ctx.channel();
        String secretKey = controlTunnelChannel.attr(EtpConstants.SECRET_KEY).get();
        long sessionId = msg.getSessionId();
        int port = msg.getPort();
        Bootstrap realBootstrap = ChannelManager.getRealBootstrap();
        Bootstrap controlBootstrap = ChannelManager.getControlBootstrap();
        //代理客户端与内网真实服务建立连接
        realBootstrap.connect(LOCALHOST, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("成功连接到内网服务{}:{}", LOCALHOST, port);
                Channel realChannel = future.channel();
                realChannel.config().setOption(ChannelOption.AUTO_READ, false);

                ChannelManager.borrowDataTunnelChannel(controlBootstrap)
                    .thenAccept(dataChannel -> {
                        dataChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(realChannel);
                        realChannel.attr(EtpConstants.DATA_CHANNEL).set(dataChannel);
                        Message tunnelMessage = Message.newBuilder()
                            .setType(Message.Type.CONNECT)
                            .setSessionId(sessionId)
                            .setExt(secretKey)
                            .build();

                        dataChannel.writeAndFlush(tunnelMessage).addListener(f -> {
                            if (f.isSuccess()) {
                                ChannelManager.addRealServerChannel(sessionId, realChannel);
                                realChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
                                realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                                logger.debug("成功建立数据传输通道:[sessionId:{},port:{}]", sessionId, port);
                            }
                        });
                    })
                    .exceptionally(cause -> {
                        logger.error(cause.getMessage(), cause);
                        //如果发生错误，通知服务端断开连接
                        Message message = Message.newBuilder()
                            .setSessionId(sessionId)
                            .setType(Message.Type.DISCONNECT)
                            .build();
                        controlTunnelChannel.writeAndFlush(message);
                        return null;
                    });
            } else {
                logger.error("内网服务[{}:{}]不可用!", LOCALHOST, port);
            }
        });
    }
}
