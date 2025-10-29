package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.server.store.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 认证消息处理器
 * @author liuxin
 */
public class AuthHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = msg.getExt();
        if (!Config.getInstance().isClientExist(secretKey)) {
            ctx.channel().close();
            return;
        }
        Channel controlChannel = ChannelManager.getControlTunnelChannel(secretKey);
        if (controlChannel!=null) {
            ctx.channel().close();
        }
        List<Integer> internalPorts = Config.getInstance().getClientPublicNetworkPorts(secretKey);
        ChannelManager.addControlTunnelChannel(internalPorts, secretKey, ctx.channel());
    }
}
