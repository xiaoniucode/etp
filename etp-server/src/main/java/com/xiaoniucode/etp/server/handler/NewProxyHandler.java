package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Error;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxy;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.web.core.server.BizException;
import com.xiaoniucode.etp.server.web.serivce.ServiceFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理来自代理客户端端口映射注册
 *
 * @author liuxin
 */
public class NewProxyHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewProxyHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, Message msg) {
        try {
            if (msg instanceof NewProxy newProxy) {
                Channel control = ctx.channel();
                if (control == null || !control.isActive()) {
                    logger.warn("控制隧道不存在或隧道未激活！");
                    return;
                }
                String secretKey = control.attr(EtpConstants.SECRET_KEY).get();
                if (StringUtils.hasText(secretKey)) {
                    JSONObject body = buildAddReq(newProxy, secretKey);
                    JSONObject proxy = ServiceFactory.INSTANCE.getProxyService().addTcpProxy(body);
                    NewProxyResp newProxyResp = new NewProxyResp(proxy.getInt("proxyId"),
                            proxy.getInt("remotePort") + "");
                    ctx.channel().writeAndFlush(newProxyResp);
                }
            }
        } catch (Exception e) {
            String errorMsg = "";
            Throwable current = e;
            while (current != null) {
                if (current instanceof BizException biz) {
                    errorMsg = biz.getMessage();
                    break;
                }
                current = current.getCause();
            }
            if (errorMsg.isEmpty() && e.getMessage() != null) {
                errorMsg = e.getMessage();
            }
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            logger.error(root.toString(), root);
            ctx.channel().writeAndFlush(new Error(errorMsg));
        }
    }

    private static JSONObject buildAddReq(NewProxy newProxy, String secretKey) {
        int localPort = newProxy.getLocalPort();
        String protocol = newProxy.getProtocol();
        String name = newProxy.getName();
        int remotePort = newProxy.getRemotePort();
        boolean start = newProxy.getAutoStart();
        Integer clientId = ClientManager.getClient(secretKey).getClientId();
        JSONObject body = new JSONObject();
        body.put("clientId", clientId);
        body.put("secretKey", secretKey);
        body.put("localPort", localPort);
        body.put("remotePort", remotePort);
        body.put("type", protocol);
        body.put("name", name);
        body.put("status", start ? 1 : 0);
        body.put("autoRegistered", 1);
        return body;
    }
}
