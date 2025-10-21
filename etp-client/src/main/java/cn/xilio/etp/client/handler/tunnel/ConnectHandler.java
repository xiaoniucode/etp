package cn.xilio.etp.client.handler.tunnel;

import cn.xilio.etp.client.ChannelManager;
import cn.xilio.etp.client.Config;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.DataTunnelChannelBorrowCallback;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        Channel controlTunnelChannel = ctx.channel();
        long sessionId = msg.getSessionId();
        int port = msg.getPort();
        Bootstrap realBootstrap = controlTunnelChannel.attr(EtpConstants.REAL_BOOTSTRAP).get();
        Bootstrap tunnelBootstrap = ctx.channel().attr(EtpConstants.TUNNEL_BOOTSTRAP).get();
        realBootstrap.connect("127.0.0.1", port).addListener((ChannelFutureListener) cf -> {
            if (cf.isSuccess()) {
                Channel realChannel = cf.channel();
                //与本地mysql建立连接后，先不读取数据，等与远程建立连接后再读取
                realChannel.config().setOption(ChannelOption.AUTO_READ, false);
                //从数据隧道通道中获取一个与服务端连接的数据隧道通道，用于隔离不同session传送数据
                ChannelManager.borrowDataTunnelChanel(tunnelBootstrap, new DataTunnelChannelBorrowCallback() {
                    @Override
                    public void success(Channel dataTunnelChannel) {
                        dataTunnelChannel.attr(EtpConstants.NEXT_CHANNEL).set(realChannel);
                        realChannel.attr(EtpConstants.NEXT_CHANNEL).set(dataTunnelChannel);
                        TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                                .setType(TunnelMessage.Message.Type.CONNECT)
                                .setSessionId(sessionId)
                                .setExt(Config.getInstance().getSecretKey())
                                .build();

                        dataTunnelChannel.writeAndFlush(tunnelMessage);
                        ChannelManager.addRealServerChannel(sessionId, realChannel);
                        realChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
                        realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                    }

                    @Override
                    public void fail(Throwable cause) {
                        //如果发生错误，通知服务端断开连接
                        TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                                .setSessionId(sessionId)
                                .setType(TunnelMessage.Message.Type.DISCONNECT)
                                .build();
                        controlTunnelChannel.writeAndFlush(message);
                    }
                });
            }
        });
    }
}
