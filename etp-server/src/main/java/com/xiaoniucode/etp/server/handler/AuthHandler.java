package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 客户端认证消息处理器
 *
 * @author liuxin
 */
public class AuthHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private final RuntimeState state = RuntimeState.get();

    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String body = msg.getExt();
        String[] values = body.split(":");
        String secretKey = values[0];
        String os = values[1];
        String arch = values[2];
        //检查密钥是否存在
        if (!state.hasClient(secretKey)) {
            logger.error("secretKey认证密钥未授权");
            ctx.channel().close();
            return;
        }
        Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
        if (controlChannel != null) {
            ctx.channel().close();
        }
        List<Integer> remotePorts = state.getClientRemotePorts(secretKey);
        ChannelManager.addControlChannel(remotePorts, secretKey,os,arch, ctx.channel());
        logger.debug("客户端认证成功");
    }
}
