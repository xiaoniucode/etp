package cn.xilio.vine.server.app.adapter.console;

import cn.xilio.vine.server.app.Api;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;
import java.util.Map;

public class CommandHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final Api api = new CommandApi();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String command = frame.text();
        QueryStringDecoder queryDecoder = new QueryStringDecoder(command);
        System.out.println("收到命令: " + command);
        Map<String, List<String>> parameters = queryDecoder.parameters();

        // 处理命令（示例：add操作）
        if (command.startsWith("add")) {
            String response = "服务端执行成功: " + command.toUpperCase();
            ctx.writeAndFlush(new TextWebSocketFrame(response));
        } else if (command.startsWith("list")) {
            String response = "服务端执行成功: " + command.toUpperCase();
            ctx.writeAndFlush(new TextWebSocketFrame(response));
        } else {
            ctx.writeAndFlush(new TextWebSocketFrame("未知命令"));
        }
    }
}
