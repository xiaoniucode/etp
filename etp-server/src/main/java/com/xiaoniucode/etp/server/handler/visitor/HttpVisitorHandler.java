package com.xiaoniucode.etp.server.handler.visitor;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import com.xiaoniucode.etp.server.GlobalIdGenerator;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http visitor handler
 *
 * @author xiaoniucode
 */
public class HttpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(HttpVisitorHandler.class);
    public static final AttributeKey<ByteBuf> CACHED_FIRST_PACKET = AttributeKey.newInstance("cachedFirstPacket");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel visitorChannel = ctx.channel();
        int port = visitorChannel.attr(EtpConstants.TARGET_PORT).get();

        Boolean connected = visitorChannel.attr(EtpConstants.CONNECTED).get();
        if (connected == null || !connected) {
            visitorChannel.attr(EtpConstants.CONNECTED).set(false);
            buf.retain();
            visitorChannel.attr(CACHED_FIRST_PACKET).set(buf);
            connectToTarget(visitorChannel, port);
            return;
        }
        Channel dataChannel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        if (dataChannel == null || !dataChannel.isActive()) {
            logger.warn("data channel is null");
            return;
        }
        if (dataChannel.isWritable()) {
            dataChannel.writeAndFlush(new NewWorkConn(buf.retain()));
        }
    }

    private void connectToTarget(Channel visitorChannel, int localPort) {
        long sessionId = GlobalIdGenerator.nextId();
        visitorChannel.config().setOption(ChannelOption.AUTO_READ, false);
        Channel controllChannel = ChannelManager.getControlChannelBySecretKey("your-secret-key");
        if (controllChannel == null) {
            logger.warn("channel is null");
            return;
        }
        ChannelManager.addClientChannelToControlChannel(visitorChannel, sessionId, controllChannel);
        ChannelManager.registerActiveConnection(localPort, visitorChannel);
        controllChannel.writeAndFlush(new NewVisitorConn(sessionId, localPort));
    }

    public static void connectToTarget(ChannelHandlerContext ctx,Channel visitorChannel) {
        Channel dataChannel = visitorChannel.attr(EtpConstants.DATA_CHANNEL).get();
        ByteBuf cached = visitorChannel.attr(CACHED_FIRST_PACKET).get();
        if (cached != null && dataChannel.isWritable()) {
            ctx.pipeline().addAfter(ctx.name(), "chunkedWriteHandler", new ChunkedWriteHandler());
            ctx.pipeline().addAfter(ctx.name(), "httpAggregator", new HttpObjectAggregator(64 * 1024));
            dataChannel.writeAndFlush(new NewWorkConn(cached.retain()));
            cached.release();
            visitorChannel.attr(CACHED_FIRST_PACKET).set(null);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 清理缓存的数据包
        ByteBuf cachedPacket = ctx.channel().attr(CACHED_FIRST_PACKET).get();
        if (cachedPacket != null) {
            cachedPacket.release();
            ctx.channel().attr(CACHED_FIRST_PACKET).set(null);
        }
        super.channelInactive(ctx);
    }
}
