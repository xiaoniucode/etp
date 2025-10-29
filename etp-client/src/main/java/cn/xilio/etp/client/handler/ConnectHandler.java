package cn.xilio.etp.client.handler;

import cn.xilio.etp.client.ChannelManager;
import cn.xilio.etp.client.Config;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.DataTunnelChannelBorrowCallback;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage.Message;
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
public class ConnectHandler extends AbstractMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Channel controlTunnelChannel = ctx.channel();
        long sessionId = msg.getSessionId();
        int port = msg.getPort();
        Bootstrap realBootstrap = ChannelManager.getRealBootstrap();
        Bootstrap controlBootstrap = ChannelManager.getControlBootstrap();
        //代理客户端与内网真实服务建立连接
        realBootstrap.connect("127.0.0.1", port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("成功连接到内网服务：127.0.0.1:{}", port);
                Channel realChannel = future.channel();
                realChannel.config().setOption(ChannelOption.AUTO_READ, false);
                ChannelManager.borrowDataTunnelChanel(controlBootstrap, new DataTunnelChannelBorrowCallback() {
                    @Override
                    public void success(Channel dataChannel) {
                        dataChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(realChannel);
                        realChannel.attr(EtpConstants.DATA_CHANNEL).set(dataChannel);
                        Message tunnelMessage = Message.newBuilder()
                                .setType(Message.Type.CONNECT)
                                .setSessionId(sessionId)
                                .setExt(Config.getInstance().getSecretKey())
                                .build();

                        dataChannel.writeAndFlush(tunnelMessage).addListener(future -> {
                            if (future.isSuccess()) {
                                ChannelManager.addRealServerChannel(sessionId, realChannel);
                                realChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
                                realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                                logger.debug("代理客户端向代理服务端成功建立数据传输通道:[sessionId:{},port:{}]", sessionId, port);
                            }
                        });
                    }

                    @Override
                    public void fail(Throwable cause) {
                        logger.error(cause.getMessage(), cause);
                        //如果发生错误，通知服务端断开连接
                        Message message = Message.newBuilder()
                                .setSessionId(sessionId)
                                .setType(Message.Type.DISCONNECT)
                                .build();
                        controlTunnelChannel.writeAndFlush(message);
                    }
                });
            } else {
                logger.error("内网服务[123.0.0.1:{}]不可用!", port);
            }
        });
    }
}
