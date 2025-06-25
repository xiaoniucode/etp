package cn.xilio.vine.server.app.adapter.console;

import cn.xilio.vine.core.command.CommandMessage;
import cn.xilio.vine.core.command.MethodType;
import cn.xilio.vine.server.app.Api;
import cn.xilio.vine.server.store.FileProxyRuleStore;
import cn.xilio.vine.server.store.ProxyRuleStore;
import cn.xilio.vine.server.store.dto.ClientInfoDTO;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;
import java.util.Map;

public class CommandHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
  private ProxyRuleStore store=new FileProxyRuleStore();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        CommandMessage<Object> message = CommandMessage.fromJson(text, Object.class);
        MethodType method = message.getMethod();

        // 处理命令（示例：add操作）
        if ("client_list".equalsIgnoreCase(method.name())) {
            List<ClientInfoDTO> clients = store.getClients();
            CommandMessage<Object> res = new CommandMessage<>(MethodType.CLIENT_LIST);
            res.setData(clients);


            ctx.writeAndFlush(new TextWebSocketFrame(res.toJson()));
        }
    }
}
