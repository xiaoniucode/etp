package cn.xilio.vine.client.handler.tunnel;

import cn.xilio.vine.client.ChannelManager;
import cn.xilio.vine.client.Config;
import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.DataTunnelChannelBorrowCallback;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        Channel controllTunnelChannel = ctx.channel();
        long sessionId = msg.getSessionId();
        ByteString data = msg.getPayload();
        String lan = data.toStringUtf8();
        String[] split = lan.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);

        Bootstrap realBootstrap = ctx.channel().attr(VineConstants.REAL_BOOTSTRAP).get();
        Bootstrap tunnelBootstrap = ctx.channel().attr(VineConstants.TUNNEL_BOOTSTRAP).get();
        realBootstrap.connect(ip, port).addListener((ChannelFutureListener) cf -> {
            if (cf.isSuccess()) {
                Channel realChannel = cf.channel();
                //与本地mysql建立连接后，先不读取数据，等与远程建立连接后再读取
                realChannel.config().setOption(ChannelOption.AUTO_READ, false);
                //从数据隧道通道中获取一个与服务端连接的数据隧道通道，用于隔离不同session传送数据
                ChannelManager.borrowDataTunnelChanel(tunnelBootstrap, new DataTunnelChannelBorrowCallback() {
                    @Override
                    public void success(Channel dataTunnelChannel) {
                        dataTunnelChannel.attr(VineConstants.NEXT_CHANNEL).set(realChannel);
                        realChannel.attr(VineConstants.NEXT_CHANNEL).set(dataTunnelChannel);
                        TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                                .setType(TunnelMessage.Message.Type.CONNECT)
                                .setSessionId(sessionId)
                                .setExt(Config.getSecretKey())
                                .build();

                        dataTunnelChannel.writeAndFlush(tunnelMessage);
                        ChannelManager.addRealServerChannel(sessionId, realChannel);
                        realChannel.attr(VineConstants.SESSION_ID).set(sessionId);
                        realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                    }

                    @Override
                    public void fail(Throwable cause) {
                        //如果发生错误，通知服务端断开连接
                        TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                                .setSessionId(sessionId)
                                .setType(TunnelMessage.Message.Type.DISCONNECT)
                                .build();
                        controllTunnelChannel.writeAndFlush(message);
                    }
                });
            }
        });
    }
}
