package cn.xilio.vine.server.handler.tunnel;

import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.core.AbstractMessageHandler;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 传输消息处理器
 */
public class TransferHandler extends AbstractMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(TransferHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        Channel visitorChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        if (visitorChannel != null && visitorChannel.isWritable()) {
            ByteString bytes = msg.getPayload();
            ByteBuf byteBuf = ctx.alloc().buffer(bytes.size()).writeBytes(bytes.asReadOnlyByteBuffer());
            visitorChannel.writeAndFlush(byteBuf);
        }
    }
}
