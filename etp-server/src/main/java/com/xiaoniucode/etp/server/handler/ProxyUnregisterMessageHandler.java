package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.web.ConfigStore;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理客户端下线，清理端口映射配置信息
 *
 * @author liuxin
 */
public class ProxyUnregisterMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ProxyUnregisterMessageHandler.class);
    private final static ConfigStore configStore = ConfigStore.get();
    private final static AppConfig config = AppConfig.get();
    private final static RuntimeState state = RuntimeState.get();

    @Override
    public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String proxyId = msg.getExt();
        String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
        int id = Integer.parseInt(proxyId);
        if (config.getDashboard().getEnable()) {
            configStore.deleteProxy(id);
        }
        List<Integer> ports = state.getClientRemotePorts(secretKey);
        //停掉连接的服务并释放端口
        ports.forEach(remotePort -> {
            //删除注册的端口映射
            state.removeProxy(secretKey, remotePort);
            //删除公网端口与已认证客户端的绑定
            ChannelManager.removeRemotePortToControlChannel(remotePort);
            TcpProxyServer.get().stopRemotePort(remotePort, true);
        });

        logger.info("代理：proxyId-{}下线", proxyId);
    }
}
