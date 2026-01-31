package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.ProxyRegisterEvent;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import com.xiaoniucode.etp.core.msg.Message.ControlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自代理客户端端口映射注册
 *
 * @author liuxin
 */
@Component
public class NewProxyRespHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewProxyRespHandler.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyManager proxyManager;

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        Message.NewProxy newProxy = msg.getNewProxy();
        ProxyConfig config = buildProxyConfig(newProxy);
        //保存到代理到配置管理器
        proxyManager.createProxy("1",config, proxyConfig -> {
            //发布事件，可订阅事件对其进行持久化或其他操作
            ProxyRegisterEvent event = new ProxyRegisterEvent();
            event.setProxyConfig(proxyConfig);
            eventBus.publishAsync(event);
            control.writeAndFlush(buildResponse(proxyConfig));
            logger.debug("代理: {} 注册成功", proxyConfig.getName());
        });
    }

    private ProxyConfig buildProxyConfig(Message.NewProxy newProxy) {
        return new ProxyConfig();
    }

    private Message.NewProxyResp buildResponse(ProxyConfig ext) {
        ProtocolType protocol = ext.getProtocol();
        Set<String> domains = ext.getFullDomains();
        Message.NewProxyResp.Builder builder = Message.NewProxyResp.newBuilder();
        if (domains == null || domains.isEmpty()) {
            return builder.build();
        }
        String host = ConfigHelper.get().getHost();
        StringBuilder remoteAddr = new StringBuilder();
        if (ProtocolType.isHttp(protocol)) {
            int httpProxyPort = ConfigHelper.get().getHttpProxyPort();
            for (String domain : domains) {
                remoteAddr.append("http://").append(domain);
                if (httpProxyPort != 80) {
                    remoteAddr.append(":").append(httpProxyPort);
                }
                remoteAddr.append("\n");
            }

        } else if (ProtocolType.isTcp(protocol)) {
            Integer remotePort = ext.getRemotePort();
            remoteAddr.append(host).append(":").append(remotePort);
        }
        builder.setProxyName(ext.getName());
        builder.setRemoteAddr(remoteAddr.toString());
        return builder.build();
    }
}
