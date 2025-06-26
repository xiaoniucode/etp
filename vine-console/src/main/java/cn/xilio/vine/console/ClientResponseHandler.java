package cn.xilio.vine.console;

import cn.xilio.vine.console.handler.ClientListHandler;
import cn.xilio.vine.console.handler.ProxyListHandler;
import cn.xilio.vine.core.MessageHandler;
import cn.xilio.vine.core.command.CommandMessageHandler;
import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.protocol.CommandMessage;
import cn.xilio.vine.core.command.protocol.MethodType;
import cn.xilio.vine.core.command.view.ClientTableView;
import cn.xilio.vine.core.command.view.ProxyTableView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.taobao.text.ui.Element.label;

public class ClientResponseHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Gson GSON = new Gson();
    private final Map<String, CommandMessageHandler> handlers = new HashMap<>();

    public ClientResponseHandler() {
        // 注册处理器
        handlers.put("client_list", new ClientListHandler());
        handlers.put("proxy_list", new ProxyListHandler());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        System.out.print("\033[2K\r"); // ANSI清除整行
        String text = frame.text();
        CommandMessage message = CommandMessage.fromJson(text);
        String method = message.getMethod().name().toLowerCase();
        JsonElement data = message.getData();
        CommandMessageHandler handler = handlers.get(method);
        if (handler != null) {
            handler.handle(data);
        } else {
            System.err.println("No handler for method: " + method);
        }
        System.out.print("admin $ ");
        System.out.flush(); // 强制刷新缓冲区
    }
}
