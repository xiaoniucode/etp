package cn.xilio.vine.server.app.adapter.console;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ProxyModel;
import cn.xilio.vine.core.command.protocol.CommandMessage;
import cn.xilio.vine.core.command.protocol.MethodType;
import cn.xilio.vine.server.store.FileProxyRuleStore;
import cn.xilio.vine.server.store.ProxyRuleStore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class CommandHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ProxyRuleStore store = new FileProxyRuleStore();
    private final Gson gson = new Gson();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        CommandMessage message = CommandMessage.fromJson(text);
        MethodType method = message.getMethod();
        if ("client_list".equalsIgnoreCase(method.name())) {
            List<ClientModel> clients = store.getClients();
            CommandMessage res = new CommandMessage(MethodType.CLIENT_LIST);
            res.setData(gson.toJsonTree(clients));
            ctx.writeAndFlush(new TextWebSocketFrame(res.toJson()));
        } else if ("proxy_list".equalsIgnoreCase(method.name())) {
            List<ProxyModel> proxies = store.getProxies();
            CommandMessage res = new CommandMessage(MethodType.PROXY_LIST);
            res.setData(gson.toJsonTree(proxies));
            ctx.writeAndFlush(new TextWebSocketFrame(res.toJson()));
        }else if ("client_add".equalsIgnoreCase(method.name())) {
            JsonElement json = message.getData();
            JsonElement name = json.getAsJsonObject().get("name");
            store.addClient(name.getAsString());
        }
    }
}
