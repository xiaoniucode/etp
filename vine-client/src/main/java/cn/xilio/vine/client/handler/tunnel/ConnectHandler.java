package cn.xilio.vine.client.handler.tunnel;

import cn.xilio.vine.client.Config;
import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        long sessionId = msg.getSessionId();
        ByteString data = msg.getPayload();
        String lan = data.toStringUtf8();
        String[] split = lan.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);

        Bootstrap realBootstrap = ctx.channel().attr(VineConstants.REAL_BOOTSTRAP).get();
        Bootstrap tunnelBootstrap = ctx.channel().attr(VineConstants.TUNNEL_BOOTSTRAP).get();
        realBootstrap.connect(ip, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (cf.isSuccess()) {
                    Channel realChannel = cf.channel();
                    //与本地mysql建立连接后，先不读取数据，等与远程建立连接后再读取
                    realChannel.config().setOption(ChannelOption.AUTO_READ, false);

                    tunnelBootstrap.connect(Config.getServerAddr(), Config.getServerPort()).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture tunnelChannel) throws Exception {
                            if (tunnelChannel.isSuccess()) {
                                tunnelChannel.channel().attr(VineConstants.NEXT_CHANNEL).set(realChannel);
                                realChannel.attr(VineConstants.NEXT_CHANNEL).set(tunnelChannel.channel());
                                TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                                        .setType(TunnelMessage.Message.Type.CONNECT)
                                        .setSessionId(sessionId)
                                        .setExt(Config.getSecretKey())
                                        .build();

                                tunnelChannel.channel().writeAndFlush(tunnelMessage);
                                realChannel.config().setOption(ChannelOption.AUTO_READ, true);

                            }
                        }
                    });
                }
            }
        });
    }
}
