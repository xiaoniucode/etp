package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.manager.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.server.manager.RuntimeState;
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
public class AuthHandler extends AbstractMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private final RuntimeState state = RuntimeState.get();

    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = msg.getExt();
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
        ChannelManager.addControlChannel(remotePorts, secretKey, ctx.channel());
        logger.debug("客户端认证成功");
    }
}
