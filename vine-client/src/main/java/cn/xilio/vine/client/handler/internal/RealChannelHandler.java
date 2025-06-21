package cn.xilio.vine.client.handler.internal;

import cn.xilio.vine.client.ChannelManager;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

/**
 *
 */
public class RealChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(RealChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        Channel tunnelChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        byte[] dataBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(dataBytes);

        TunnelMessage.Message message = TunnelMessage.Message
                .newBuilder()
                .setType(TunnelMessage.Message.Type.TRANSFER)
                .setPayload(ByteString.copyFrom(dataBytes))
                .build();
        tunnelChannel.writeAndFlush(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();//获取与内网真实服务连接的通道
        //获取访问者会话ID,sessionId在与真实服务连接的时候被设置
        Long sessionId = realChannel.attr(VineConstants.SESSION_ID).get();
        ChannelManager.removeRealServerChannel(sessionId);//移除会话与真实服务连接的通道记录
        Channel dataTunnelChannel = realChannel.attr(VineConstants.NEXT_CHANNEL).get();//获取隧道通道
        if (!ObjectUtils.isEmpty(dataTunnelChannel)) {
            //远程代理服务器发送断开连接消息，通知服务器断开连接和清理资源
            TunnelMessage.Message message = TunnelMessage.Message
                    .newBuilder()
                    .setType(TunnelMessage.Message.Type.DISCONNECT)
                    .setSessionId(sessionId)
                    .build();
            dataTunnelChannel.writeAndFlush(message);
        }
        logger.info("与内网真实服务通道断开连接, sessionId:{}", sessionId);
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Channel dataTunnelChannel = realChannel.attr(VineConstants.NEXT_CHANNEL).get();
        if (!ObjectUtils.isEmpty(dataTunnelChannel)) {
            dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, realChannel.isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
