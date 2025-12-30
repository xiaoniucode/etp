package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import com.xiaoniucode.etp.server.web.ConfigService;
import com.xiaoniucode.etp.server.web.server.BizException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端主动注册端口映射信息
 *
 * @author liuxin
 */
public class ProxyRegisterMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ProxyRegisterMessageHandler.class);

    @Override public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        try {
            TunnelMessage.ProxyRequest request = TunnelMessage.ProxyRequest.parseFrom(msg.getPayload());
            Channel controlChannel = ctx.channel();
            if (controlChannel == null || !controlChannel.isActive()) {
                logger.warn("control channel is null or not active");
                return;
            }
            String secretKey = controlChannel.attr(EtpConstants.SECRET_KEY).get();
            if (StringUtils.hasText(secretKey)) {
                JSONObject body = buildAddReq(request, secretKey);
                JSONObject proxy = ConfigService.addProxy(body);
                //返回注册结果
                TunnelMessage.ProxyResponse response = TunnelMessage.ProxyResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("端口映射注册成功")
                    .setProxyId(proxy.getInt("proxyId"))
                    .setExternalUrl(proxy.getInt("remotePort") + "").build();
                sendSuccessResponse(ctx, msg, response);
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
            sendErrorResponse(ctx, msg, errorMsg);
        }
    }

    private static JSONObject buildAddReq(TunnelMessage.ProxyRequest request, String secretKey) {
        int localPort = request.getLocalPort();
        String protocol = request.getProtocol();
        String name = request.getProxyName();
        int remotePort = request.getRemotePort();
        boolean start = request.getAutoStart();
        Integer clientId = RuntimeState.get().getClient(secretKey).getClientId();
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

    private void sendSuccessResponse(ChannelHandlerContext ctx, TunnelMessage.Message originalMsg,
        TunnelMessage.ProxyResponse proxyResponse) {
        TunnelMessage.Message responseMsg = TunnelMessage.Message.newBuilder()
            .setType(TunnelMessage.Message.Type.PROXY_REGISTER)
            .setSessionId(originalMsg.getSessionId())
            .setPayload(proxyResponse.toByteString())
            .build();
        ctx.writeAndFlush(responseMsg);
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, TunnelMessage.Message originalMsg, String errorMessage) {
        TunnelMessage.Message responseMsg = TunnelMessage.Message.newBuilder()
            .setType(TunnelMessage.Message.Type.ERROR)
            .setSessionId(originalMsg.getSessionId())
            .setExt(errorMessage)
            .build();
        ctx.writeAndFlush(responseMsg);
    }
}
