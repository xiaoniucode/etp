package com.xiaoniucode.etp.server.handler.visitor;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

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
        Channel visitor = ctx.channel();

        Boolean connected = visitor.attr(EtpConstants.CONNECTED).get();
        if (connected == null || !connected) {
            visitor.attr(EtpConstants.CONNECTED).set(false);
            buf.retain();
            visitor.attr(CACHED_FIRST_PACKET).set(buf);
            connectToTarget(visitor);
            return;
        }
        Channel tunnel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel == null || !tunnel.isActive()) {
            logger.warn("data channel is null");
            return;
        }
        if (tunnel.isWritable()) {
            tunnel.writeAndFlush(buf.retain());
        }
    }

    private void connectToTarget(Channel visitor) {
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        ChannelManager.registerHttpVisitor(visitor, pair -> {
            Channel control = pair.getControl();
            LanInfo lanInfo = pair.getLanInfo();
            control.writeAndFlush(new NewVisitorConn(pair.getSessionId(), lanInfo.getLocalIP(), lanInfo.getLocalPort()));
        });
    }

    public static void sendFirstPackage(Channel visitor) {
        Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
        ByteBuf cached = visitor.attr(CACHED_FIRST_PACKET).get();
        if (cached != null && tunnel.isWritable()) {
            tunnel.writeAndFlush(cached.retain());
            cached.release();
            visitor.attr(CACHED_FIRST_PACKET).set(null);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        // 清理缓存的数据包
        ByteBuf cachedPacket = ctx.channel().attr(CACHED_FIRST_PACKET).get();
        if (cachedPacket != null) {
            cachedPacket.release();
            ctx.channel().attr(CACHED_FIRST_PACKET).set(null);
        }
        ChannelManager.unregisterHttpVisitor(visitor, new BiConsumer<Long, Channel>() {
            @Override
            public void accept(Long sessionId, Channel control) {
                control.writeAndFlush(new CloseProxy(sessionId));
            }
        });
        super.channelInactive(ctx);
    }
}
