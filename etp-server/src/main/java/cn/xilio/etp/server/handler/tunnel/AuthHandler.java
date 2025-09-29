package cn.xilio.etp.server.handler.tunnel;

import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.server.store.ProxyManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 认证消息处理器
 */
public class AuthHandler extends AbstractMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        //获取客户端传过来的认证密钥
        String secretKey = msg.getExt();
        //如果系统中不存在客户端的密钥，说明该客户端没有注册或者密钥错误
        if (!ProxyManager.getInstance().isClientExist(secretKey)) {
            logger.info("认证失败，客户端不存在！");
            ctx.channel().close();//关闭当前通道
            return;//返回方法，不执行后续的逻辑
        }
        //获取与客户端绑定的安全隧道channel
        Channel controlTunnelChannel = ChannelManager.getControlTunnelChannel(secretKey);
        //如果已经存在安全通道，表示已经认证过了，不能够重复认证，一个客户端只绑定一条安全通道-隧道
        if (!ObjectUtils.isEmpty(controlTunnelChannel)) {
            ctx.channel().close();//关闭当前客户端的通道
        }
        //获取客户端的所有内网服务对应的公网端口
        List<Integer> internalPorts = ProxyManager.getInstance().getClientPublicNetworkPorts(secretKey);
        //将客户端所有内网服务对应的公网端口绑定到控制隧道通道上
        ChannelManager.addControlTunnelChannel(internalPorts, secretKey, ctx.channel());
    }
}
