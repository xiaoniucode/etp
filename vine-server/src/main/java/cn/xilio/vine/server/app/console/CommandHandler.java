package cn.xilio.vine.server.app.console;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class CommandHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String command = frame.text();
        System.out.println("收到命令: " + command);

        // 处理命令（示例：add操作）
        if (command.startsWith("add")) {
            String response = "服务端执行成功: " + command.toUpperCase();
            ctx.writeAndFlush(new TextWebSocketFrame(response));
        }else if (command.startsWith("list")) {
            String response = "服务端执行成功: " + command.toUpperCase();
            ctx.writeAndFlush(new TextWebSocketFrame(response));
        }
        else {
            ctx.writeAndFlush(new TextWebSocketFrame("未知命令"));
        }
    }
}
