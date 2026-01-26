package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.AuthClientInfo;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.msg.Error;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxy;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.web.core.server.BizException;
import com.xiaoniucode.etp.server.web.serivce.ServiceFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;

/**
 * 处理来自代理客户端端口映射注册
 *
 * @author liuxin
 */
public class NewProxyRespHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewProxyRespHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, Message msg) {
        try {
            if (msg instanceof NewProxy newProxy) {
                Channel control = ctx.channel();
                AuthClientInfo authClientInfo = ChannelManager.getAuthClientInfo(control);
                String secretKey = authClientInfo.getSecretKey();
                //todo 合法性检查，端口、域名、支持自动生成一个域名（http/https）、生成一个唯一的名字
                JSONObject proxy = createProxy(newProxy, secretKey);

                ctx.channel().writeAndFlush(buildResponse(proxy, newProxy));
            }
        } catch (Exception e) {
            String errorMsg = buildErrorMessage(e);
            ctx.channel().writeAndFlush(new Error(errorMsg));
        }
    }


    private static JSONObject createProxy(NewProxy newProxy, String secretKey) {
        int localPort = newProxy.getLocalPort();
        ProtocolType protocolType = newProxy.getProtocol();
        String name = newProxy.getName();
        int remotePort = newProxy.getRemotePort();
        int status = newProxy.getStatus();
        Integer clientId = ClientManager.getClient(secretKey).getClientId();
        JSONObject body = new JSONObject();
        body.put("clientId", clientId);
        body.put("secretKey", secretKey);
        body.put("localPort", localPort);
        body.put("type", protocolType.name().toLowerCase(Locale.ROOT));
        body.put("name", name);
        body.put("status", status);
        body.put("source", 1);

        if (ProtocolType.isTcp(protocolType)) {
            body.put("remotePort", remotePort);
        }
        if (ProtocolType.isHttpOrHttps(protocolType)) {
            Set<String> domains = newProxy.getCustomDomains();
            if (domains != null && !domains.isEmpty()) {
                String customDomainsStr = String.join("\n", domains);
                body.put("customDomains", customDomainsStr);
            }

        }
        return switch (protocolType) {
            case TCP -> ServiceFactory.INSTANCE.getProxyService().addTcpProxy(body);
            case HTTP -> ServiceFactory.INSTANCE.getProxyService().addHttpProxy(body);
            case HTTPS -> ServiceFactory.INSTANCE.getProxyService().addHttpsProxy(body);
        };
    }

    private NewProxyResp buildResponse(JSONObject proxy, NewProxy newProxy) {
        ProtocolType protocol = newProxy.getProtocol();
        int proxyId = proxy.getInt("proxyId");
        String host = ConfigHelper.get().getHost();
        StringBuilder remoteAddr = new StringBuilder();
        if (newProxy.getCustomDomains() != null && ProtocolType.isHttp(protocol)) {
            int httpProxyPort = ConfigHelper.get().getHttpProxyPort();
            Set<String> customDomains = newProxy.getCustomDomains();
            for (String domain : customDomains) {
                remoteAddr.append("http://").append(domain).append(":").append(httpProxyPort).append("\n");
            }
        } else if (newProxy.getCustomDomains() != null && ProtocolType.isHttps(protocol)) {
            int httpsProxyPort = ConfigHelper.get().getHttpsProxyPort();
            Set<String> customDomains = newProxy.getCustomDomains();
            for (String domain : customDomains) {
                remoteAddr.append("https://").append(domain).append(":").append(httpsProxyPort).append("\n");
            }

        } else if (ProtocolType.isTcp(protocol)) {
            int remotePort = proxy.getInt("remotePort");
            remoteAddr.append(host).append(":").append(remotePort);
        }
        return new NewProxyResp(proxyId, remoteAddr.toString());

    }

    /**
     * 构建错误信息
     */
    private String buildErrorMessage(Exception e) {
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
        return errorMsg;
    }
}
