package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.manager.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.server.manager.RuntimeState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 客户端认证消息处理器
 *
 * @author liuxin
 */
public class AuthHandler extends AbstractMessageHandler {
    private final RuntimeState state = RuntimeState.get();

    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = msg.getExt();
        //检查密钥是否存在
        if (!state.hasClient(secretKey)) {
            ctx.channel().close();
            return;
        }
        Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
        if (controlChannel != null) {
            ctx.channel().close();
        }
        List<Integer> remotePorts = state.getClientRemotePorts(secretKey);
        ChannelManager.addControlChannel(remotePorts, secretKey, ctx.channel());
    }
}
