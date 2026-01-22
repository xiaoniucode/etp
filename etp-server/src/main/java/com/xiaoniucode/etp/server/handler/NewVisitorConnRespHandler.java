package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.codec.ChannelBridge;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConnResp;
import com.xiaoniucode.etp.server.handler.visitor.HttpVisitorHandler;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 处理来自代理客户端连接成功消息
 *
 * @author liuxin
 */
public class NewVisitorConnRespHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnRespHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof NewVisitorConnResp resp) {
            long sessionId = resp.getSessionId();
            String secretKey = resp.getSecretKey();
            Channel tunnel = ctx.channel();

            ChannelManager.bindVisitorAndTunnel(tunnel, sessionId, secretKey, new Consumer<Channel>() {
                @Override
                public void accept(Channel visitor) {
                    ctx.pipeline().remove("tunnelMessageCodec");
                    ctx.pipeline().remove("controlTunnelHandler");

                    //桥接，双向透明转发
                    ChannelBridge.bridge(visitor, tunnel);
                    visitor.config().setOption(ChannelOption.AUTO_READ, true);

                    HttpVisitorHandler.sendFirstPackage(visitor);
                    logger.debug("已连接到目标服务");
                }
            });
        }
    }
}
