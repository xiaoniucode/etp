package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import com.xiaoniucode.etp.server.web.ConfigService;
import com.xiaoniucode.etp.server.web.server.BizException;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理信息注册，由客户端发起
 *
 * @author liuxin
 */
public class ProxyRegisterMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ProxyRegisterMessageHandler.class);

    @Override public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        try {
            TunnelMessage.ProxyRequest request = TunnelMessage.ProxyRequest.parseFrom(msg.getPayload());
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
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
            logger.error(e.getMessage(), e);
            String errorMsg;
            if (e instanceof BizException biz) {
                errorMsg = biz.getMessage();
            } else {
                errorMsg = e.getMessage();
            }
            sendErrorResponse(ctx, msg, errorMsg);
        }
    }

    private static JSONObject buildAddReq(TunnelMessage.ProxyRequest request, String secretKey) {
        int localPort = request.getLocalPort();
        String protocol = request.getProtocol();
        String name = request.getProxyName();
        int remotePort = request.getRemotePort();
        boolean start = request.getAutoStart();
        JSONObject body = new JSONObject();
        body.put("clientId", RuntimeState.get().getClient(secretKey).getClientId());
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
        TunnelMessage.ProxyResponse errorResponse = TunnelMessage.ProxyResponse.newBuilder()
            .setSuccess(false)
            .setMessage(errorMessage)
            .build();

        TunnelMessage.Message responseMsg = TunnelMessage.Message.newBuilder()
            .setType(TunnelMessage.Message.Type.ERROR)
            .setSessionId(originalMsg.getSessionId())
            .setPayload(errorResponse.toByteString())
            .build();

        ctx.writeAndFlush(responseMsg);
    }
}
